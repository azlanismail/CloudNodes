/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleException;

import rice.Continuation;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdSet;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastContent;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.core.exceptions.AppRequestException;
import eu.ascens_ist.scp.node.core.exceptions.BundleStartException;
import eu.ascens_ist.scp.node.core.exceptions.IncorrectUICommandException;
import eu.ascens_ist.scp.node.core.strategy.AbstractStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.REnsembleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleCreationException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.GossipInfoMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.HelenaWrapperAnswerMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.HelenaWrapperFrameworkResultMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.HelenaWrapperMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.HelenaWrapperR2RAnswerMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RFrameworkMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RCreateRoleInstanceMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RCreateRoleInstanceResultMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RFrameworkRequestMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RGetRoleInstanceMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RGetRoleInstanceResultMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RDeployAppMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RRequestorRequestMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RRouteRoleMessageResult;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RUndeployAppMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.DeployerRole;
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.ExecutorRole;
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.HelenaRole;
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.InitiatorRole;
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.MainStorageRole;
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.RequesterRole;
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.UnDeployerRole;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.info.Requirements;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;
import eu.ascens_ist.scp.node.messages.AbstractSCPDirectMessage;
import eu.ascens_ist.scp.node.storage.PastAppBinary;
import eu.ascens_ist.scp.node.storage.PastAppTombstone;

/**
 * This class implements the Gossipping/Helena-based strategy for the SCP. This
 * strategy is different in three ways from the (original) Contract.NET
 * strategy:
 * 
 * <ul>
 * <li>it implements a gossipping mode of exchanging information (instead of
 * broadcasting);</li>
 * <li>it implements a role-based communication layer based on the Helena ideas</li>
 * <li>it takes the role of the "external party" for the Helena roles (to
 * overcome limitations of the approach itself as it is now)</li>
 * </ul>
 * 
 * There is one instance of the HelenaStrategy per SCP node (component).
 * 
 * @author P. Mayer
 * 
 */
public class GossipHelenaBasedStrategy extends AbstractStrategy {
	private Logger strategyLog;
	/**
	 * Nodes known through gossipping (WITHOUT OURSELVES!!!)
	 */
	private Map<Id, NodeInfo> knownNodes;
	private Map<Id, Ensemble> ensembles;
	private Set<RMessage> incomingMessageCache;
	private Map<Id, HelenaWrapperAnswerMessage> incomingWrapperAnswerMessageCache;
	private Map<Id, String> uiResponseCache;

	public static final String ROLE_DEPLOYER = "Deployer";
	public static final String ROLE_REQUESTER = "requester";
	public static final String ROLE_POTENTIAL_EXECUTER = "Potential Executor";
	public static final String ROLE_MAINSTORAGE = "Main Storage";
	public static final String ROLE_UNDEPLOYER = "Undeployer";
	public static final String ROLE_DEPLOYMENT_CREATOR = "Deployment Creator";
	public static final String ROLE_DEPLOYMENT_STOPPER = "Deployment Stopper";

	public static boolean TESTMODE = false;

	protected boolean doNotDeployYet = false;

	public synchronized void setDoNotDeployYet(boolean doNotDeployYet) {
		this.doNotDeployYet = doNotDeployYet;
	}

	@Override
	public void initialize(SCPNode scpNode) {
		super.initialize(scpNode);

		strategyLog = LogFactory.get(scpNode.getId() + " HELENASTRATEGY");

		this.knownNodes = new HashMap<>();
		this.ensembles = new HashMap<Id, Ensemble>();
		this.incomingMessageCache = new CopyOnWriteArraySet<>();
		this.incomingWrapperAnswerMessageCache = new ConcurrentHashMap<>();
		this.uiResponseCache = new HashMap<>();
	}

	@Override
	public void updateNodeInformation() {
		List<NodeHandle> leafSet = node.getRandomLeafSetNodeHandles(2);

		Set<NodeInfo> knownNodesWithOurselves = new HashSet<>();
		knownNodesWithOurselves.addAll(knownNodes.values());
		knownNodesWithOurselves.add(getNodeInfo());

		for (NodeHandle thisHandle : leafSet) {
			GossipInfoMessage m = new GossipInfoMessage(getNodeInfo().getId(), thisHandle.getId(), knownNodesWithOurselves);
			getSCPNode().sendMessage(m, thisHandle.getId());
		}

		removeOldKnownNodes();
	}

	// From lower level

	@Override
	public void handleNodeLeft(NodeHandle handle) {
		strategyLog.debug("Node left was called in the strategy. Checking if we are new initiator to any app...");

		this.checkNodeIsInitiator();
	}

	public void checkNodeIsInitiator() {

		strategyLog.debug("Check if node is initiator");

		// TODO for some reason, sometimes, the new initiator node sometimes
		// takes a LONG time to get new PAST data (up to two minutes). It will
		// get it eventually, though.

		IdSet locallyKnownPASTData = getSCPNode().getLocallyKnownPASTData();
		for (Iterator<Id> i = locallyKnownPASTData.getIterator(); i.hasNext();) {
			Id current = i.next();

			strategyLog.debug("Current past data is %s", current);

			if (node.getPastryNode().isClosest((rice.pastry.Id) current)) {
				// we are closest to this node
				strategyLog.debug("We are closest to id %s, evaluating whether this is an app... ", current);
				getSCPNode().getLocallyKnownPASTData(current, new Continuation<Object, Exception>() {

					@Override
					public void receiveResult(Object result) {
						if (result instanceof PastAppBinary) {

							PastAppBinary binary = (PastAppBinary) result;
							AppInfo appInfo = binary.getAppInfo();
							byte[] code = binary.getData();

							strategyLog.debug("Yes, found app %s...", appInfo.getName());

							// We might already be the initiator, or on the way
							// to becoming one.
							Ensemble ensemble = ensembles.get(getSCPNode().getEnvironment().createIdHashFromArbitraryString(
									appInfo.getName()));
							if (ensemble != null) {
								List<HelenaRole> deployer = ensemble.getRoles(DeployerRole.class);
								List<HelenaRole> mainstorage = ensemble.getRoles(MainStorageRole.class);
								List<HelenaRole> initor = ensemble.getRoles(InitiatorRole.class);
								if (deployer.size() == 1 || mainstorage.size() == 1 || initor.size() == 1) {
									strategyLog.debug("...but we are already initiator. Everything is fine.");
									return;
								}
							}

							strategyLog.debug("...and we are NOT initiator! Deploying!");

							// we MAY also be the executor for this app which is
							// NOT cared for currently
							if (ensemble != null) {
								strategyLog.debug("Checking if we already are EXECUTOR...");
								Class<? extends HelenaRole> ex = ExecutorRole.class;
								List<HelenaRole> rolesForApp = ensemble.getRoles(ex);
								if (rolesForApp.size() == 1) {
									strategyLog.debug("...but we are already EXECUTOR. Shutting this down...");
									ExecutorRole r = ((ExecutorRole) rolesForApp.get(0));
									r.stop();
								} else {
									strategyLog.debug("We are not EXECUTOR. Everything is fine.");
								}
							}
							// We re-use the deploy mechanism here!

							synchronized (GossipHelenaBasedStrategy.this) {
								if (!TESTMODE || !doNotDeployYet) {
									handleDeployApplication(appInfo.getName(), appInfo.getRequirements(), code);
								}
							}

						} else {
							strategyLog.debug("Not an app: " + result);
						}
					}

					@Override
					public void receiveException(Exception exception) {
						// do nothing.
					}

				});
			} else {
				strategyLog
						.debug("We are not closest to id %s, evaluating whether this is an app and we are initiator for it... ", current);
				getSCPNode().getLocallyKnownPASTData(current, new Continuation<Object, Exception>() {

					@Override
					public void receiveResult(Object result) {
						if (result instanceof PastAppBinary) {

							PastAppBinary binary = (PastAppBinary) result;
							AppInfo appInfo = binary.getAppInfo();
							byte[] code = binary.getData();

							strategyLog.debug("Yes, found app %s...", appInfo.getName());

							// We might be the initiator
							Ensemble ensemble = ensembles.get(getSCPNode().getEnvironment().createIdHashFromArbitraryString(
									appInfo.getName()));
							if (ensemble != null) {
								Class<? extends HelenaRole> c = InitiatorRole.class;
								List<HelenaRole> initRoles = ensemble.getRoles(c);
								if (initRoles.size() == 1) {
									strategyLog.debug("...and we are initiator! Shutting down initiator and main storage!");

									// WHY IS THIS HERE?
									// handleDeployApplication(appInfo.getName(),
									// appInfo.getRequirements(), code);

									HelenaRole initiatorRole = initRoles.get(0);

									initiatorRole.stop();
									c = MainStorageRole.class;
									List<HelenaRole> storageRoles = ensemble.getRoles(c);
									HelenaRole mainStorageRole = null;
									if (storageRoles.size() == 1) {
										mainStorageRole = storageRoles.get(0);
										mainStorageRole.stop();
									}

									return;
								}
							}

							strategyLog.debug("...and we are not initiator. Everything is fine.");

						} else {
							strategyLog.debug("Not an app.");
						}
					}

					@Override
					public void receiveException(Exception exception) {
						// do nothing.
					}

				});
			}
		}
	}

	@Override
	public void handleNodeJoined(NodeHandle handle) {
		// we might stop being initiator--- currently handled by direct
		// messaging. Might also use this here.
	}

	@Override
	public void handleDirectIncomingMessage(Id id, Message msg) {
		// route to appropriate role

		if (!(msg instanceof AbstractSCPDirectMessage)) {
			strategyLog.error("Received unknown message type %s.", msg.getClass());
			return;
		}

		AbstractSCPDirectMessage message = (AbstractSCPDirectMessage) msg;

		if (message instanceof GossipInfoMessage) {
			GossipInfoMessage gi = (GossipInfoMessage) message;
			Set<NodeInfo> knownNodes = gi.getKnownNodes();
			mergeInfo(knownNodes);
			checkNodeIsInitiator();
			return;
		}

		if (message instanceof HelenaWrapperMessage) {
			HelenaWrapperMessage wrapperMsg = (HelenaWrapperMessage) message;

			// Depends on what kind of a message we have
			RMessage wrapped = wrapperMsg.getWrapped();

			strategyLog.debug("Received wrapped message %s with content %s ID is %s", message.getClass().getSimpleName(), wrapped,
					wrapperMsg.getMessageId().toStringFull());

			if (wrapped instanceof RFrameworkRequestMessage)
				handleFrameworkMessage(wrapperMsg.getMessageId(), (RFrameworkRequestMessage) wrapped);
			if (wrapped instanceof RR2RMessage)
				handleRoleMessage(wrapperMsg.getMessageId(), (RR2RMessage) wrapped);
		}

		if ((message instanceof HelenaWrapperAnswerMessage)) {

			HelenaWrapperAnswerMessage answerMsg = (HelenaWrapperAnswerMessage) message;

			// Just drop it into the appropriate bin
			incomingWrapperAnswerMessageCache.put(answerMsg.getMessageId(), answerMsg);
			return;
		}

		super.handleDirectIncomingMessage(id, message);
	}

	@Override
	public void handleBroadcastIncomingMessage(Topic topic, ScribeContent content) {

		// not used.

	}

	@Override
	public void handleNewLocalPASTContent(Id msgid, NodeHandle nodeHandle, PastContent content) {

		// Do nothing: The content is not really handled by past anyway; it is
		// being sent here by an explicit message and then stored.
		// Initiator switch is done by the methods for node add/removal

	}

	@Override
	public void handleShutdown() {

		// Stop all roles!
		Collection<Ensemble> ensembles = this.ensembles.values();
		for (Ensemble ensemble : ensembles) {
			ensemble.stopAll();
		}

		super.handleShutdown();
	}

	@Override
	public void handleDeployApplication(String appName, Requirements requirements, byte[] bytes) {
		strategyLog.info("New application \"%s\" deployed.", appName);

		Id ensembleId = getSCPNode().getEnvironment().createIdHashFromArbitraryString(appName);

		Ensemble ensemble = ensembles.get(ensembleId);
		if (ensemble == null) {
			// create it
			ensemble = new Ensemble(ensembleId, this);
			ensembles.put(ensembleId, ensemble);
		}

		strategyLog.info("Created ensemble with ID %s", ensemble.getEnsembleId().toStringFull());

		try {
			strategyLog.info("Creating deployer role...");
			RoleId startNewRole = ensemble.startNewRole(DeployerRole.class);

			AppInfo appInfo = new AppInfo(appName, requirements);

			strategyLog.info("Sending RDeployAppMessage to deployer role %s", startNewRole);
			RDeployAppMessage rdp = new RDeployAppMessage(null, startNewRole, appInfo, bytes);
			incomingMessageCache.add(rdp);

		} catch (RRoleCreationException e) {
			strategyLog.info("Could not create deployer role");
		}
	}

	@Override
	public void handleUndeployApplication(String appName) throws IncorrectUICommandException {

		// Create UndeployerRole and send message based on the name.

		strategyLog.info("Application to undeploy! Creating undeploy role.");

		// Create the first role
		Id ensembleId = getSCPNode().getEnvironment().createIdHashFromArbitraryString(appName);

		Ensemble ensemble = ensembles.get(ensembleId);
		if (ensemble == null) {
			// create it
			ensemble = new Ensemble(ensembleId, this);
			ensembles.put(ensembleId, ensemble);
		}

		strategyLog.info("Created ensemble with ID %s", ensemble.getEnsembleId().toStringFull());

		try {
			strategyLog.info("Starting new undeployer role...");
			RoleId undeployer = ensemble.startNewRole(UnDeployerRole.class);

			// TODO problematic: An appInfo without requirement bytes.
			AppInfo appInfo = new AppInfo(appName, new Requirements());

			// Shortcut: Just deploy the message :-)
			RUndeployAppMessage rdp = new RUndeployAppMessage(null, undeployer, appInfo);
			strategyLog.info("Deploying message into cache...");
			incomingMessageCache.add(rdp);

		} catch (RRoleCreationException e) {
			// cannot happen.
		}
	}

	@Override
	public String appRequest(AppInfo appInfo, String appTarget, HttpServletRequest request, HttpServletResponse response)
			throws AppRequestException {

		// We use a local Requester role for this; create a new one each time

		Id ensembleId = getSCPNode().getEnvironment().createIdHashFromArbitraryString(appInfo.getName());

		Ensemble ensemble = ensembles.get(ensembleId);
		if (ensemble == null) {
			// create it locally
			ensemble = new Ensemble(ensembleId, this);
			ensembles.put(ensembleId, ensemble);
		}

		try {
			RoleId reqRole = ensemble.startNewRole(RequesterRole.class);

			Id routingId = getSCPNode().getEnvironment().createArbitraryId();

			RRequestorRequestMessage rdp = new RRequestorRequestMessage(null, reqRole, routingId, appInfo, appTarget,
					request.getParameterMap());
			strategyLog.info("Deploying message into cache...");
			incomingMessageCache.add(rdp);

			String result = uiResponseCache.get(routingId);
			int mstowait = 10000;
			int current = 0;
			while (current < mstowait) {

				try {
					Thread.sleep(200);
					current += 200;
				} catch (InterruptedException e) {
					throw new AppRequestException("Interrupted while waiting for answer from App.");
				}

				result = uiResponseCache.get(routingId);
				if (result != null)
					return result;
			}

			strategyLog.error("Timeout waiting for uiResponseCache.");
			throw new AppRequestException("Waited in vain for an answer from the app.");

		} catch (RRoleCreationException e) {
			throw new AppRequestException("Could not start requester role", e);
		}

	}

	// Infos

	@Override
	public AppInfo getAppInfo(String name) {
		return getAppMap().get(name);
	}

	@Override
	public List<NodeInfo> getNeighbourInformations() {
		List<NodeInfo> listNodeInfo = new ArrayList<>();
		listNodeInfo.addAll(knownNodes.values());
		return listNodeInfo;
	}

	@Override
	public NodeInfo getNeighbourInformation(Id id) {
		return knownNodes.get(id);
	}

	@Override
	public Set<AppInfo> getAllKnownApps() {

		Map<String, AppInfo> allKnownApps = getAppMap();

		Set<AppInfo> set = new HashSet<>();
		set.addAll(allKnownApps.values());

		return set;
	}

	private void handleRoleMessage(Id exchangeMessageId, RR2RMessage wrapped) {

		Id sourceNodeId = wrapped.getFromRole().getNodeId();

		Id ensembleId = wrapped.getFromRole().getEnsembleId();
		Ensemble ensemble = ensembles.get(ensembleId);
		if (ensemble == null) {
			RRouteRoleMessageResult roleBasedResult = new RRouteRoleMessageResult(new REnsembleNotFoundException());
			HelenaWrapperR2RAnswerMessage answer = new HelenaWrapperR2RAnswerMessage(getSCPNode().getId(), sourceNodeId, roleBasedResult,
					exchangeMessageId);
			getSCPNode().sendMessage(answer, sourceNodeId);
			return;
		}

		HelenaRole role = ensemble.getSpecificRole(wrapped.getToRole());
		if (role == null) {
			RRouteRoleMessageResult roleBasedResult = new RRouteRoleMessageResult(new RRoleNotFoundException("Role not found."));
			HelenaWrapperR2RAnswerMessage answer = new HelenaWrapperR2RAnswerMessage(getSCPNode().getId(), sourceNodeId, roleBasedResult,
					exchangeMessageId);
			getSCPNode().sendMessage(answer, sourceNodeId);
			return;
		}

		// else: it is OK! We add it to the queue
		incomingMessageCache.add(wrapped);

		HelenaWrapperR2RAnswerMessage msg = new HelenaWrapperR2RAnswerMessage(getSCPNode().getId(), sourceNodeId,
				new RRouteRoleMessageResult(), exchangeMessageId);
		getSCPNode().sendMessage(msg, sourceNodeId);

	}

	public RoleId createLocalRoleInstance(Id ensembleId, Class<? extends HelenaRole> roleType) throws RRoleCreationException {

		Ensemble thisEnsemble = ensembles.get(ensembleId);
		if (thisEnsemble == null) {
			thisEnsemble = new Ensemble(ensembleId, this);
			ensembles.put(ensembleId, thisEnsemble);
		}

		return thisEnsemble.startNewRole(roleType);
	}

	private void handleFrameworkMessage(Id exchangeMessageId, RFrameworkRequestMessage wrapped) {

		if (wrapped instanceof RCreateRoleInstanceMessage) {
			RCreateRoleInstanceMessage msg = (RCreateRoleInstanceMessage) wrapped;
			Id ensembleId = msg.getFromRole().getEnsembleId();
			Class<?> roleType = msg.getRoleType();

			Ensemble thisEnsemble = ensembles.get(ensembleId);
			if (thisEnsemble == null) {
				thisEnsemble = new Ensemble(ensembleId, this);
				ensembles.put(ensembleId, thisEnsemble);
			}

			RFrameworkMessage answer = null;
			try {

				RoleId newRole = thisEnsemble.startNewRole(roleType);
				answer = new RCreateRoleInstanceResultMessage(getSCPNode().getId(), wrapped.getFromRole(), newRole);

			} catch (RRoleCreationException e) {
				answer = new RCreateRoleInstanceResultMessage(getSCPNode().getId(), wrapped.getFromRole(), e);

			}

			HelenaWrapperFrameworkResultMessage result = new HelenaWrapperFrameworkResultMessage(getSCPNode().getId(), wrapped
					.getFromRole().getNodeId(), answer, exchangeMessageId);
			getSCPNode().sendMessage(result, wrapped.getFromRole().getNodeId());
		}

		if (wrapped instanceof RGetRoleInstanceMessage) {

			RGetRoleInstanceMessage msg = (RGetRoleInstanceMessage) wrapped;
			Id ensembleId = msg.getFromRole().getEnsembleId();
			Class<? extends HelenaRole> roleType = msg.getRoleType();

			Ensemble thisEnsemble = ensembles.get(ensembleId);
			if (thisEnsemble == null) {

				// No role if no ensemble
				RGetRoleInstanceResultMessage result = new RGetRoleInstanceResultMessage(getSCPNode().getId(), msg.getFromRole(),
						new REnsembleNotFoundException());
				HelenaWrapperFrameworkResultMessage answer = new HelenaWrapperFrameworkResultMessage(getSCPNode().getId(), msg
						.getFromRole().getNodeId(), result, exchangeMessageId);
				getSCPNode().sendMessage(answer, msg.getFromRole().getNodeId());

				return;
			} else {

				List<HelenaRole> roles = thisEnsemble.getRoles(roleType);
				if (roles.size() != 1) {

					RGetRoleInstanceResultMessage result = new RGetRoleInstanceResultMessage(getSCPNode().getId(), msg.getFromRole(),
							new RRoleNotFoundException("Ensemble with ID " + ensembleId.toStringFull()
									+ " has not 1 role of this type, but " + roles.size()));
					HelenaWrapperFrameworkResultMessage answer = new HelenaWrapperFrameworkResultMessage(getSCPNode().getId(), msg
							.getFromRole().getNodeId(), result, exchangeMessageId);
					getSCPNode().sendMessage(answer, msg.getFromRole().getNodeId());
					return;
				}

				HelenaRole helenaRole = roles.get(0);
				RGetRoleInstanceResultMessage result = new RGetRoleInstanceResultMessage(getSCPNode().getId(), msg.getFromRole(),
						helenaRole.getRoleId());

				HelenaWrapperFrameworkResultMessage answer = new HelenaWrapperFrameworkResultMessage(getSCPNode().getId(), msg
						.getFromRole().getNodeId(), result, exchangeMessageId);
				getSCPNode().sendMessage(answer, msg.getFromRole().getNodeId());
			}
		}
	}

	private void mergeInfo(Set<NodeInfo> nodesFromOutside) {

		for (NodeInfo nodeFromOutside : nodesFromOutside) {

			// We know best about ourselves.
			if (nodeFromOutside.equals(getNodeInfo()))
				continue;

			NodeInfo alreadyKnown = knownNodes.get(nodeFromOutside.getId());
			if (alreadyKnown == null || (alreadyKnown.getTimestamp() < nodeFromOutside.getTimestamp()))
				knownNodes.put(nodeFromOutside.getId(), nodeFromOutside);
		}
	}

	public void removeOldKnownNodes() {

		List<NodeInfo> copy = new ArrayList<>();
		copy.addAll(knownNodes.values());

		for (NodeInfo nodeInfo : copy) {

			if (nodeInfo.isTooOld())
				knownNodes.remove(nodeInfo.getId());
		}

	}

	public void storeInPAST(AppInfo appInfo, byte[] appCode) {
		PastAppBinary appData = new PastAppBinary(appInfo, appCode);
		node.insertGCPastData(appData);
	}

	public void removeFromPAST(AppInfo appInfo) {
		PastAppTombstone tombstone = new PastAppTombstone(appInfo);
		getSCPNode().insertGCPastData(tombstone);

	}

	// ******************************************* from the roles
	// *******************************************

	public void executeApp(AppInfo appInfo, byte[] appCode) throws BundleStartException {
		getSCPNode().installAndRunBundle(appInfo.getName(), new ByteArrayInputStream(appCode));
	}

	public void stopApp(String appName) throws BundleException {
		getSCPNode().stopApp(appName);
	}

	public RFrameworkMessage routeFrameworkMessage(RFrameworkRequestMessage message) throws RTimeoutException {

		// wrap in an SCP message, send, wait for result.
		Id targetNode = message.getTargetNode();

		Id randomWaitId = getSCPNode().getEnvironment().createArbitraryId();

		HelenaWrapperMessage msg = new HelenaWrapperMessage(getSCPNode().getId(), message.getTargetNode(), message, randomWaitId);
		getSCPNode().sendMessage(msg, targetNode);

		HelenaWrapperFrameworkResultMessage rMsg = (HelenaWrapperFrameworkResultMessage) waitForReturn(randomWaitId);
		RFrameworkMessage result = rMsg.getFrameworkBasedResult();
		return result;
	}

	/**
	 * Locally called to route a message to a remote target.
	 * 
	 * @param message
	 * @return
	 * @throws RTimeoutException
	 */
	public RRouteRoleMessageResult routeRoleMessage(RR2RMessage message) throws RTimeoutException {

		Id sourceNodeId = getSCPNode().getId();
		Id randomWaitId = getSCPNode().getEnvironment().createArbitraryId();

		HelenaWrapperMessage msg = new HelenaWrapperMessage(sourceNodeId, message.getToRole().getNodeId(), message, randomWaitId);

		getSCPNode().sendMessage(msg, message.getToRole().getNodeId());

		HelenaWrapperR2RAnswerMessage rMsg = (HelenaWrapperR2RAnswerMessage) waitForReturn(randomWaitId);
		RRouteRoleMessageResult result = rMsg.getRoleBasedResult();

		return result;
	}

	public <E extends RR2RMessage> E waitForIncomingMessage(int timeoutinms, Class<E> clazz) throws RTimeoutException {
		strategyLog.debug("Been asked to wait for incoming message %s", clazz.getSimpleName());
		ArrayList<Class<? extends RR2RMessage>> classList = new ArrayList<Class<? extends RR2RMessage>>();

		Class<? extends RR2RMessage> clazz2 = clazz;
		classList.add(clazz2);

		RR2RMessage msg = waitForIncomingMessages(timeoutinms, classList);
		return clazz.cast(msg);
	}

	// TODO include looking for the target role id and whether this fits.
	public RR2RMessage waitForIncomingMessages(int timeoutinms, Collection<Class<? extends RR2RMessage>> msgs) throws RTimeoutException {

		int currentwaitingtime = 0;
		RMessage found = null;
		while (currentwaitingtime < timeoutinms) {
			for (RMessage rMessage : incomingMessageCache) {

				for (Class<? extends RR2RMessage> clazz : msgs) {
					if (clazz.isInstance(rMessage)) {
						found = rMessage;
						break;
					}
				}
			}

			if (found != null) {
				incomingMessageCache.remove(found);
				break;
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				break;
			}
			currentwaitingtime += 200;
		}

		if (found == null)
			throw new RTimeoutException();

		return (RR2RMessage) found;
	}

	public String handleLocalUI(String appName, String target, Map<String, String[]> properties) {
		return getSCPNode().getLocalAppResult(appName, target, properties);
	}

	public Set<NodeInfo> getGossiplyKnownNodes() {
		Set<NodeInfo> setOfKnownNodes = new HashSet<>();
		setOfKnownNodes.addAll(this.knownNodes.values());
		return setOfKnownNodes;
	}

	public Id createIdFromString(String string) {
		return getSCPNode().getEnvironment().createIdHashFromArbitraryString(string);
	}

	public void setUIResponseFor(Id routingId, AppInfo appInfo, String target, String result) {
		uiResponseCache.put(routingId, result);
	}

	private AbstractSCPDirectMessage waitForReturn(Id randomWaitId) throws RTimeoutException {

		int timeoutinms = 5000;
		int currentwaitingtime = 0;
		HelenaWrapperAnswerMessage msg = null;
		while (currentwaitingtime < timeoutinms) {

			msg = incomingWrapperAnswerMessageCache.remove(randomWaitId);
			if (msg != null)
				break;

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				break;
			}
			currentwaitingtime += 200;
		}

		if (msg == null)
			throw new RTimeoutException();

		return msg;
	}

	private Map<String, AppInfo> getAppMap() {
		Map<String, AppInfo> allKnownApps = new HashMap<>();

		Collection<NodeInfo> values = knownNodes.values();
		for (NodeInfo nodeInfo : values) {

			Set<AppInfo> keySet = nodeInfo.getAppRoles().keySet();
			for (AppInfo appInfo : keySet) {
				allKnownApps.put(appInfo.getName(), appInfo);
			}
		}
		return allKnownApps;
	}

	public void addAppRole(AppInfo appName, String appRole, String roleStatus) {
		if (getNodeInfo() != null) {
			getNodeInfo().addRoleForApp(appName, appRole, roleStatus);
			strategyLog.debug("Added role " + appRole + " for app " + appName);
		}
	}

	public void removeAppRole(AppInfo appName, String appRole) {
		if (getNodeInfo() != null) {
			getNodeInfo().removeRoleForApp(appName, appRole);
			strategyLog.debug("Removed role " + appRole + " for app " + appName);
		}
	}

	public void bootLocalApp(AppInfo appInfo) throws AppRequestException {
		getSCPNode().bootLocalApp(appInfo.getName());
	}

	public void gossipInformationTo(Id nodeId) {

		Set<NodeInfo> knownNodesWithOurselves = new HashSet<>();
		knownNodesWithOurselves.addAll(knownNodes.values());
		knownNodesWithOurselves.add(getNodeInfo());

		GossipInfoMessage m = new GossipInfoMessage(getNodeInfo().getId(), nodeId, knownNodesWithOurselves);
		getSCPNode().sendMessage(m, nodeId);
	}

	public void roleShutdown(HelenaRole helenaRole) {

		if (ensembles != null) {
			Ensemble ensemble = ensembles.get(helenaRole.getRoleId().getEnsembleId());
			if (ensemble != null)
				ensemble.removeRole(helenaRole);
		}

	}

	public List<HelenaRole> getRolesForApp(String appName) {

		Id appId = createIdFromString(appName);

		Ensemble ens = this.ensembles.get(appId);
		if (ens == null)
			return new ArrayList<HelenaRole>();
		else
			return ens.getRoles(HelenaRole.class);
	}

	public List<HelenaRole> getRolesForApp(AppInfo appInfo) {

		Id appId = createIdFromString(appInfo.getName());

		Ensemble ens = this.ensembles.get(appId);
		if (ens == null)
			return new ArrayList<HelenaRole>();
		else
			return ens.getRoles(HelenaRole.class);
	}

}
