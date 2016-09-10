/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.time.DateUtils;
import org.hyperic.sigar.NetConnection;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.core.exceptions.NodeEnvironmentException;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * 
 * This class represents the environment in which node(s) may be run. Normally,
 * only one node is started in one VM; however we can also start multiple ones;
 * this is the environment they live in.
 * 
 * There is only one NodeEnvironment per VM.
 * 
 * @author A. Zeblin, P. Mayer, A. Dittrich
 * 
 */
public class NodeEnvironment {
	private static Logger log = LogFactory.get("NODEENVIRONMENT");
	private static NodeEnvironment instance;
	private Environment env;
	private PastryIdFactory pastryIdFactory;
	private NodeIdFactory nodeIdFactory;
	private InetAddress socketAddress;
	/**
	 * Nodes managed by this environment (==VM)
	 */
	private List<SCPNode> nodes;
	private int nodeCount = Configuration.START_NODE_COUNT;
	private HashMap<Integer, Date> portCache;

	/**
	 * Returns the instance, if any. The instance must be initialized by calling
	 * .start().
	 * 
	 * @return
	 */
	public static NodeEnvironment get() {
		if (instance == null)
			instance = new NodeEnvironment();
		return instance;
	}

	public static void start() throws NodeEnvironmentException {
		instance = NodeEnvironment.get();
		instance.startInternal();
	}

	public void stop() {
		log.info("Shutting down node environment...");

		for (SCPNode node : getNodes()) {
			log.info("Shutting down node %s.", node);
			node.shutdown();
		}

		log.info("Node Environment was shut down.");
		instance = null;
	}

	public Environment getPastryEnvironment() {
		return this.env;
	}

	public List<SCPNode> getNodes() {
		return this.nodes;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

	private synchronized void startInternal() throws NodeEnvironmentException {
		log.info("New NodeEnvironment is starting %d nodes...", this.nodeCount);

		this.env = new Environment();
		// disable the UPnP setting (in case you are testing this on a NATted
		// LAN)
		this.env.getParameters().setString("nat_search_policy", "never");

		this.pastryIdFactory = new PastryIdFactory(this.env);
		this.nodeIdFactory = new RandomNodeIdFactory(this.env);

		try {
			this.socketAddress = getNonLoopbackAddress();
		} catch (SocketException e2) {
			try {
				this.socketAddress = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				throw new NodeEnvironmentException("Could not get InetAddress for Nodes", e);
			}
		}

		this.nodes = new ArrayList<>();
		this.portCache = new HashMap<Integer, Date>();

		SCPNode currentNode = null;
		for (int i = 0; i < this.nodeCount; i++) {
			currentNode = createNode(currentNode);
			this.nodes.add(currentNode);
		}
	}

	public SCPNode createNode(SCPNode bootNode) throws NodeEnvironmentException {
		return this.createNode(bootNode, null);
	}

	public SCPNode createNode(SCPNode bootNode, Id nodeId) throws NodeEnvironmentException {
		// if no port is specified, the next free port is selected for node creation

		int port = Configuration.START_NODE_PORT;
		boolean portAvailable = false;
		do {
			String logEntry = "Looking up port " + port + " in cache... ";
			Date cachedDateForPort = this.portCache.get(port);
			Date expirationLimit = DateUtils.addMilliseconds(new Date(), -Configuration.PORT_CACHE_INTERVAL);
			boolean testing = true;
			if (cachedDateForPort != null) {
				if (cachedDateForPort.after(expirationLimit)) {
					logEntry += "found. ";
					portAvailable = false;
					testing = false;
				} else if (cachedDateForPort.before(expirationLimit)) {
					logEntry += "found, but expired. Testing...  ";
					this.portCache.remove(port);
				}
			} else {
				logEntry += "not found. Testing... ";
			}

			if (testing) {
				portAvailable = portAvailable(port);
				this.portCache.put(port, new Date());
			}
			if (!portAvailable) {
				logEntry += "not available.";
				port++;
			}
			log.info(logEntry);
		} while (!portAvailable);
		log.info("Port %d available", port);

		return this.createNode(bootNode, port, nodeId);
	}

	public SCPNode createNode(SCPNode bootNode, int port) throws NodeEnvironmentException {
		return this.createNode(bootNode, port, null);
	}

	public SCPNode createNode(SCPNode bootNode, int port, Id nodeId) throws NodeEnvironmentException {
		if (!portAvailable(port)) {
			log.info("Could not create instance on port %d. Port already in use.", port);
			throw new NodeEnvironmentException("Port already in use.");
		}
		log.info("Creating instance on port %d.", port);

		// construct the PastryNodeFactory
		PastryNodeFactory factory = null;
		try {
			factory = new SocketPastryNodeFactory(this.nodeIdFactory, this.socketAddress, port, this.env);
		} catch (IOException e) {
			throw new NodeEnvironmentException("Could not create SocketPastryFactory.", e);
		}

		PastryNode pastryNode = null;
		try {
			if (nodeId != null)
				pastryNode = factory.newNode((rice.pastry.Id) nodeId);
			else
				pastryNode = factory.newNode();
		} catch (IOException e) {
			throw new NodeEnvironmentException("Could not create a new PastryNode.", e);
		}

		// construct a new Node
		SCPNode scpNode = null;
		try {
			scpNode = new SCPNode(this, pastryNode, this.socketAddress, port);
		} catch (Exception e1) {
			throw new NodeEnvironmentException("Could not create a new SCPNode.", e1);
		}

		// is there a bootstrapping address?
		InetSocketAddress bootStrappingAddress = null;
		if (bootNode != null) {
			bootStrappingAddress = new InetSocketAddress(bootNode.getBaseAddress(), bootNode.getBasePort());
		} else if (Configuration.BOOTSTRAP_INET_ADRESS != null && Configuration.BOOTSTRAP_PORT > 0) {
			InetAddress address = Configuration.BOOTSTRAP_INET_ADRESS;
			int bootPort = Configuration.BOOTSTRAP_PORT;
			bootStrappingAddress = new InetSocketAddress(address, bootPort);
		}

		scpNode.boot(bootStrappingAddress);

		log.info("Started node at port %d.", port);
		return scpNode;
	}

	public Id rebuildIdObjectFromGivenIdString(String idString) {
		return this.pastryIdFactory.buildIdFromToString(idString);
	}

	public Id createIdHashFromArbitraryString(String text) {
		return this.pastryIdFactory.buildId(text);
	}

	public Id createArbitraryId() {
		return this.pastryIdFactory.buildRandomId(new Random());
	}

	public long getCurrentTimeMillis() {
		return this.env.getTimeSource().currentTimeMillis();
	}

	public void sleep(long time) {
		try {
			this.env.getTimeSource().sleep(time);
		} catch (InterruptedException e) {
			log.error("Environment was told to sleep, but got interrupted during sleep with message %s.", e.getMessage());
		}
	}

	/**
	 * Checks to see if a specific port is available.
	 * 
	 * @param port
	 *            the port to check for availability
	 */
	private static boolean portAvailable(int port) {
		Sigar sigar = new Sigar();
		int flags = NetFlags.CONN_TCP | NetFlags.CONN_SERVER | NetFlags.CONN_CLIENT;
		NetConnection[] netConnectionList;
		try {
			netConnectionList = sigar.getNetConnectionList(flags);

			for (NetConnection netConnection : netConnectionList) {
				if (netConnection.getLocalPort() == port)
					return false;
			}
			return true;
		} catch (SigarException e) {
			return false;
		}
	}

	public static InetAddress getNonLoopbackAddress() throws SocketException {
		Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
		for (; n.hasMoreElements();) {
			NetworkInterface e = n.nextElement();
			if (!e.isLoopback()) {
				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements();) {
					InetAddress addr = a.nextElement();
					if (addr instanceof Inet4Address) {
						return addr;
					}
				}
			}
		}
		return null;
	}
}
