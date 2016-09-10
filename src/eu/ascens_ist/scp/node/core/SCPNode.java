/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import rice.Continuation;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdSet;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastObserver;
import rice.p2p.past.PastPolicy;
import rice.p2p.past.gc.GCPast;
import rice.p2p.past.gc.GCPastImpl;
import rice.pastry.NodeSet;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.leafset.LeafSet;
import rice.persistence.LRUCache;
import rice.persistence.MemoryStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManager;
import rice.persistence.StorageManagerImpl;
import eu.ascens_ist.scp.node.Configuration;
import eu.ascens_ist.scp.node.NodeBundleActivator;
import eu.ascens_ist.scp.node.NodeEnvironment;
import eu.ascens_ist.scp.node.core.exceptions.AppRequestException;
import eu.ascens_ist.scp.node.core.exceptions.BundleStartException;
import eu.ascens_ist.scp.node.core.strategy.IStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.GossipHelenaBasedStrategy;
import eu.ascens_ist.scp.node.info.AppExecutionStatus;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;
import eu.ascens_ist.scp.node.storage.LookupContinuation;
import eu.ascens_ist.scp.node.storage.PastAppAbstractContent;
import eu.ascens_ist.scp.node.storage.PastAppContent;
import eu.ascens_ist.scp.node.storage.PastObserverImpl;
import eu.ascens_ist.scp.node.webservice.SCPWebService;

/**
 * 
 * This class implements one Science Cloud Platform (SCP) network node. There
 * may be one such node per VM or multiple ones. If there are multiple ones,
 * they interact via network the same as if they were on different machines
 * (however: They reside in one OSGi container!).
 * 
 * @author A. Zeblin, P. Mayer, A. Dittrich
 * 
 */
public class SCPNode implements Application, ISCPNode {
	protected Logger nodeLog;

	protected IStrategy strategy;
	/**
	 * The pastry node we are basing this node on.
	 */
	protected PastryNode pastryNode;
	/**
	 * The local Pastry endpoint
	 */
	protected Endpoint endpoint;
	// Past
	protected GCPast gcpast;
	protected StorageManager storageManager;
	protected MonitorClient monitorClient;
	protected NodeEnvironment nodeEnvironment;
	protected InetAddress baseAddress;
	protected int basePort;
	/**
	 * Used during the shutdown procedure.
	 */
	protected boolean isAlive;
	protected Map<String, IAppService> cachedService;
	protected NodeInfo nodeInfo;
	protected SCPWebService webService;

	/**
	 * @param pastryNode
	 * @param address
	 * @param port
	 * @throws Exception
	 */
	public SCPNode(NodeEnvironment env, PastryNode pastryNode, InetAddress address, int port) throws Exception {
		this.pastryNode = pastryNode;
		nodeLog = LogFactory.get(this.getId() + " NODE");
		nodeLog.info("Starting up new SCP node at address %s and port %d.", address, port);

		this.isAlive = true;
		this.nodeEnvironment = env;
		this.strategy = new GossipHelenaBasedStrategy();
		this.baseAddress = address;
		this.basePort = port;

		this.nodeInfo = new NodeInfo(this.getId(), this.getBaseAddress(), this.getBasePort(), this.strategy);

		this.cachedService = new HashMap<String, IAppService>();

		this.monitorClient = new MonitorClient(this);

		this.endpoint = pastryNode.buildEndpoint(this, "CoreApp");

		// gcPast
		PastryIdFactory idf = new PastryIdFactory(nodeEnvironment.getPastryEnvironment());

		Storage stor = new MemoryStorage(idf);
		this.storageManager = new StorageManagerImpl(idf, stor, new LRUCache(new MemoryStorage(idf), 512 * 1024, pastryNode.getEnvironment()));
		this.gcpast = new GCPastImpl(pastryNode, storageManager, Configuration.GCPAST_REPLICATION_FACTOR, "GCPast", new PastPolicy.DefaultPastPolicy(), GCPast.INFINITY_EXPIRATION);

		// add PastObserver to be notified about data inserts
		PastObserver pastObserver = new PastObserverImpl(this);
		this.gcpast.setPastObserver(pastObserver);

		this.endpoint.register();

		this.strategy.initialize(this);

		this.webService = new SCPWebService(this);
		this.webService.activate();

		// Start a thread for refreshing system infos
		Runnable systemInfoRunnable = new UpdateRunnable();
		Thread systemInfoThread = new Thread(systemInfoRunnable);
		systemInfoThread.start();

		nodeLog.info("SCP node started: %s.", this.getId());
	}

	private final class UpdateRunnable implements Runnable {
		@Override
		public void run() {
			nodeInfo.initialize();
			while (updateInformation()) {
				NodeEnvironment.get().sleep(2500);
			}
		}
	};

	protected boolean updateInformation() {
		if (!isAlive()) {
			return false;
		}

		if (nodeInfo != null)
			nodeInfo.updateSystemInformation();
		if (monitorClient != null)
			monitorClient.sendNodeInfo(nodeInfo);

		strategy.updateNodeInformation();

		return true;
	}

	public PastryNode getPastryNode() {
		return pastryNode;
	}

	/**
	 * Called when you hear about a new neighbor.
	 */
	@Override
	public void update(NodeHandle handle, boolean joined) {

		if (joined)
			nodeJoined(handle);
		else
			nodeLeft(handle);

	}

	/**
	 * A node has left the leaf set. We have to check if maybe we are the new
	 * initiator for an app which we already store.
	 * 
	 * @param handle
	 */
	private void nodeLeft(NodeHandle handle) {

		nodeLog.info("Notification that a node has left the leaf set: %s.", handle.getId());
		strategy.handleNodeLeft(handle);
	}

	/**
	 * A node has joined the leaf set. It may be the new initiator for an app
	 * for which we were the initiator before. If so, we have to remove
	 * ourselves as initiator.
	 * 
	 */
	private void nodeJoined(NodeHandle handle) {

		nodeLog.info("Notified that a node has joined the leaf set: %s.", handle.getId());
		strategy.handleNodeJoined(handle);
	}

	/**
	 * 
	 * Connects to a Pastry network based on an existing node given address and
	 * port
	 * 
	 * @param address
	 * @param port
	 * @throws UnknownHostException
	 */
	public void boot(String address, int port) throws UnknownHostException {
		InetAddress addr = InetAddress.getByName(address);
		InetSocketAddress sockAddr = new InetSocketAddress(addr, port);
		boot(sockAddr);
	}

	/**
	 * Connects to a Pastry network based on an existing node given an address
	 * 
	 * @param address
	 */
	public void boot(InetSocketAddress address) {
		pastryNode.boot(address);

		/*
		 * The node may need to send multiple messages to boot into the ring
		 */
		synchronized (pastryNode) {
			while (!pastryNode.isReady() && !pastryNode.joinFailed()) {
				try {
					pastryNode.wait(500);
				} catch (InterruptedException e) {
					nodeLog.error("Error while waiting (%s).", e);
				}

				if (pastryNode.joinFailed()) {
					nodeLog.error("Could not join the FreePastry ring: %s.", pastryNode.joinFailedReason());
				}
			}
		}
		strategy.bootedIntoRing();
		nodeLog.info("Just started %s.", this.getId());
	}

	/**
	 * 
	 * For testing: shutdown this node
	 */
	public void shutdown() {

		/**
		 * Already shut down? Don't do it again (leads to nullpointers)
		 */
		if (!isAlive)
			return;

		nodeLog.info("Shutting down SCP node %s.", this.getId());

		this.webService.deactivate();

		strategy.handleShutdown();

		nodeInfo = null;

		pastryNode.destroy();

		gcpast = null;

		isAlive = false;

		nodeLog.info("Node %s was shut down.", this.getId());

	}

	public NodeSet getLeafSetNodeHandles(int maxNeighbours) {
		LeafSet leafs = pastryNode.getLeafSet();

		// MaxNeighbors + 1 to prevent counting the own node
		int neighbourCount = Math.min(leafs.getUniqueCount(), maxNeighbours);
		NodeSet set = leafs.neighborSet(neighbourCount);
		return set;
	}

	public List<NodeHandle> getRandomLeafSetNodeHandles(int maxNeighbours) {

		LeafSet leafSet = pastryNode.getLeafSet();
		NodeHandle[] uniqueSet = leafSet.getUniqueSet().toArray(new NodeHandle[0]);

		List<NodeHandle> returner = new ArrayList<>();
		int max = Math.min(maxNeighbours, uniqueSet.length);
		int count = 0;

		// Get random nodes
		while (count < max) {
			int currentNo = (int) (Math.random() * uniqueSet.length);
			NodeHandle nodeHandle = uniqueSet[currentNo];
			if (!returner.contains(nodeHandle)) {
				returner.add(nodeHandle);
				count++;
			}
		}

		return returner;
	}

	/*
	 * Messages (simple Pastry-P2P-Messages)
	 */

	/**
	 * Send the message to the node with Id to.
	 * 
	 * @param message
	 * @param to
	 */
	public void sendMessage(Message message, Id to) {

		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new ByteArrayOutputStream());
			outputStream.writeObject(message);
		} catch (Throwable e) {
			String msg = "We've got non-serializable message content in message class " + message.getClass().getCanonicalName() + ".";
			nodeLog.error(msg);
			throw new RuntimeException(msg);
		}

		this.endpoint.route(to, message, null);

		//Inform MonitorServer about Message
		if (monitorClient != null)
			monitorClient.sendDirectMessageInfo(message);

	}

	/**
	 * Called if a message was received directly (not via Scribe)
	 */
	@Override
	public void deliver(Id id, Message message) {

		strategy.handleDirectIncomingMessage(id, message);
	}

	/**
	 * Called a message travels along your path. Don't worry about this method
	 * for now.
	 */
	@Override
	public boolean forward(RouteMessage message) {
		return true;
	}

	/*
	 * GCPast
	 */
	public int getGCPastReplicationFactor() {
		return gcpast.getReplicationFactor();
	}

	public void insertGCPastData(PastAppAbstractContent pastContent) {
		gcpast.insert(pastContent, new Continuation.ExternalContinuation<>());
	}

	public PastAppAbstractContent lookupAndWaitForPastContent(String name, int maxwaittimeinms) {

		nodeLog.debug("Looking up past content with name %s", name);
		LookupContinuation con = lookupPastContent(name);

		long time = 0; // waiting time
		long waitingTime = 200; // lapse time

		while (!con.hasResult() && time < maxwaittimeinms) {
			nodeEnvironment.sleep(waitingTime);
			time = time + waitingTime;
		}

		nodeLog.debug("Lookup complete, result of con is " + con.hasResult() + ".");

		if (!con.hasResult()) {
			return null;
		}

		return (PastAppAbstractContent) con.getResult();
	}

	public LookupContinuation lookupPastContent(String name) {
		Id idForApp = nodeEnvironment.createIdHashFromArbitraryString(name);
		LookupContinuation con = new LookupContinuation();
		gcpast.lookup(idForApp, con);
		return con;
	}

	@Override
	public void saveContent(String key, Object content) {
		PastAppContent c = new PastAppContent(key, content);
		insertGCPastData(c);
	}

	public IdSet getLocallyKnownPASTData() {
		return storageManager.scan();
	}

	public void getLocallyKnownPASTData(Id id, Continuation<Object, Exception> c) {
		storageManager.getObject(id, c);
	}

	@Override
	public Object getContent(String key, int waitingTimeInMs) {

		if (!isAlive)
			return null;

		PastAppAbstractContent c = lookupAndWaitForPastContent(key, waitingTimeInMs);
		if (c instanceof PastAppContent) {
			return ((PastAppContent) c).getContent();
		} else {
			return null;
		}
	}

	@Override
	public Id getId() {
		return pastryNode.getId();
	}

	/**
	 * This method is called by the SCPPastObserver as soon as new PastContent
	 * has been stored on this node.
	 * 
	 * @param msgid
	 * @param nodeHandle
	 * @param content
	 */
	public void newLocalPastContent(Id msgid, NodeHandle nodeHandle, PastContent content) {
		strategy.handleNewLocalPASTContent(msgid, nodeHandle, content);
	}

	@Override
	public String toString() {
		String address = "";
		int port = -1;
		try {
			address = baseAddress.getHostName();
			port = basePort;
		} catch (NullPointerException e) {
			address = e.getMessage();
		}
		return "Node [" + address + ":" + port + "] id [" + getId().toStringFull() + "]";
	}

	public NodeEnvironment getEnvironment() {
		return nodeEnvironment;
	}

	public InetAddress getBaseAddress() {
		return baseAddress;
	}

	public int getBasePort() {
		return basePort;
	}

	public MonitorClient getMonitorClient() {
		return monitorClient;
	}

	public IStrategy getStrategy() {
		return strategy;
	}

	@Override
	public boolean isAlive() {
		return isAlive;
	}

	public void installAndRunBundle(final String appName, InputStream bundleStream) throws BundleStartException {

		// install the bundle.
		Bundle bundle = null;
		try {
			bundle = NodeBundleActivator.getContext().installBundle(appName, bundleStream);
		} catch (BundleException e) {
			throw new BundleStartException(String.format("Failed to install %s's bundle on %s.", appName, this), AppExecutionStatus.PROBLEM_COULD_NOT_INSTALL, e);
		}

		// start the bundle
		try {
			bundle.start();
		} catch (BundleException e) {
			throw new BundleStartException(String.format("Failed to start bundle %s (%s) on %s: %s", bundle.getSymbolicName(), appName, this, e.getMessage()),
					AppExecutionStatus.PROBLEM_COULD_NOT_START, e);
		}
	}

	public void stopApp(String appName) throws BundleException {
		
		IAppService appService;
		try {
			appService = getOrRetrieveAppService(appName);
			appService.stop();
		} catch (AppRequestException e) {
			nodeLog.error("Could not stop app.");
		}
		
		// Still uninstalling in any case.
		
		BundleContext context = NodeBundleActivator.getContext();
		if (context == null) {
			nodeLog.warn("Attempt to uninstall app; but main bundle is already stopped.");
			return;
		}
		
		Bundle bundle = NodeBundleActivator.getContext().getBundle(appName);
		if (bundle == null) {
			nodeLog.warn("Attempt to uninstall app; but app bundle is already stopped.");
			return;
		}
		
		bundle.uninstall();
	}

	public void bootLocalApp(String appName) throws AppRequestException {
		IAppService appService = getOrRetrieveAppService(appName);
		appService.start(this);
	}

	public String getLocalAppResult(String appName, String target, Map<String, String[]> properties) {

		IAppService appService;
		try {
			appService = getOrRetrieveAppService(appName);
			return appService.handleUI(this, target, properties);

		} catch (AppRequestException e1) {
			nodeLog.error(e1.getMessage());
			return e1.getMessage();
		} catch (Exception e) {
			String error = String.format("Application %s cannot not be found on %s (request failed: %s).", appName, this, e.getMessage());
			nodeLog.error(e, "%s", error);
			return error;
		}
	}

	private IAppService getOrRetrieveAppService(String appName) throws AppRequestException {
		IAppService appService = cachedService.get(appName);
		if (appService == null) {

			// create a service name (by convention)
			String serviceName = appName.toLowerCase() + "." + appName + "Service";

			try {
				appService = (IAppService) NodeBundleActivator.getService(serviceName);
			} catch (Exception e) {
				throw new AppRequestException(String.format("Application %s cannot be found on %s: %s.", appName, this, e.getMessage()));
			}

			if (appService == null) {
				throw new AppRequestException(String.format("Application %s cannot be found on %s (%s: no such service).", appName, this, serviceName));
			}

			cachedService.put(appName, appService);
		}
		return appService;
	}

	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}
}
