/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastContent;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;
import eu.ascens_ist.scp.iaas.zimory.ZimoryDeployment;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.core.exceptions.AppRequestException;
import eu.ascens_ist.scp.node.core.exceptions.IncorrectUICommandException;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.info.Requirements;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * A strategy is the middle layer in the SCP between the UI and the apps on top
 * and the SCPNode at the bottom. It is responsible for the complete
 * <b>distributed application execution process</b>, i.e. deploying apps,
 * finding executing nodes, reacting if the nodes fail, undeploying apps,
 * handling UI, etc.
 * 
 * Regarding generic app handling, what needs to be done is the following:
 * <ul>
 * <li>App deployment: An app is deployed via UI. It needs to be stored (in PAST
 * probably) and an executor node needs to be found which matches the
 * requirements of the app.</li>
 * <li>App undeployment: If an app is undeployed via the UI, it needs to be
 * stopped executing (if running) and unstored from PAST.</li>
 * <li>App UI: If a user targets an HTTP request at an app, the app must receive
 * this request and the answer must be sent to the user, regardless of where the
 * app is actually runnning in the system.</li>
 * </ul>
 * 
 * Some things might go wrong:
 * <ul>
 * <li>A node may leave the system. This may be the node executing an app, or
 * storing an app, or having another kind of role in the app lifecycle. For all
 * of these roles, some way of coping with the loss of the node must be found</li>.
 * <li>A node may join the system. Based on how things are organized, this might
 * mean that the new node needs some information, or must take up some role for
 * app execution.</li>
 * </ul>
 * 
 * 
 * @author P. Mayer
 * 
 */
public interface IStrategy {

	// from the lower levels -----------------------------------------------

	/**
	 * Notification from SCPNode that a node has left the system.
	 * 
	 * @param handle
	 */
	public void handleNodeLeft(NodeHandle handle);

	/**
	 * Notification from SCPNode that a node has joined the system.
	 * 
	 * @param handle
	 */
	public void handleNodeJoined(NodeHandle handle);

	/**
	 * Notification from SCPNode that a message has arrived directly for this
	 * node (i.e., not a broadcast but a message routed towards a specific hash,
	 * which need not be the identical hash of this node)
	 * 
	 * @param id
	 * @param message
	 */
	public void handleDirectIncomingMessage(Id id, Message message);

	/**
	 * Notification from SCPNode that a broadcast message has arrived via
	 * SCRIBE.
	 * 
	 * @param topic
	 * @param content
	 */
	public void handleBroadcastIncomingMessage(Topic topic, ScribeContent content);

	/**
	 * Notification from SCPNode that the local node has been asked to store a
	 * new PAST content at this very node (note however that the PAST content
	 * might not be CLOSEST to this node in case of replication). Also see
	 * {@link SCPNode#getLocallyKnownPASTData()} for retrieving, in general,
	 * PAST data from the local storage manager.
	 * 
	 * @param msgid
	 * @param nodeHandle
	 * @param content
	 */
	public void handleNewLocalPASTContent(Id msgid, NodeHandle nodeHandle, PastContent content);

	/**
	 * Clean up: Stop all threads, clear all caches, etc.
	 */
	public void handleShutdown();

	// from the UI -----------------------------------------------

	/**
	 * User has uploaded an app JAR file in the UI on this very node which needs
	 * to be deployed.
	 * 
	 * @param appName
	 * @param requirements
	 * @param bytes
	 */
	public void handleDeployApplication(String appName, Requirements requirements, byte[] bytes);

	/**
	 * User has selected UNDEPLOY for an app; the app need not be executed or
	 * initiated on this node
	 * 
	 * @param appName
	 * @throws IncorrectUICommandException
	 */
	public void handleUndeployApplication(String appName) throws IncorrectUICommandException;

	/**
	 * User has made an HTTP request to a URL which belongs to a given app, and
	 * expects a routed response (even if the app does not reside on the same
	 * node).
	 * 
	 * @param appInfo
	 * @param appTarget
	 * @param request
	 * @param response
	 * @return
	 * @throws AppRequestException
	 */
	public String appRequest(AppInfo appInfo, String appTarget, HttpServletRequest request, HttpServletResponse response) throws AppRequestException;

	// Infos -----------------------------------------------

	/**
	 * Returns information on an app (if known).
	 * 
	 * @param name
	 * @return
	 */
	public AppInfo getAppInfo(String name);

	/**
	 * Returns the info from the current node.
	 * 
	 * @return
	 */
	public NodeInfo getNodeInfo();

	/**
	 * Returns a list of neighbor information of this node. Which nodes are in
	 * this neighbor set is unspecified; this depends on the implementation, but
	 * it will usually include a notion of nodes "around" the current node. It
	 * may be empty.
	 * 
	 * The result does not contain the information about the node itself; for
	 * that, use {@link #getNodeInfo()}.
	 * 
	 * @param count
	 * @return
	 */
	public List<NodeInfo> getNeighbourInformations();

	/**
	 * 
	 * Asks the current node for information about the node with the given Id.
	 * The current node may or may not have this information. It also may or may
	 * not retrieve the information on the fly if necessary (all
	 * implementation-dependent).
	 * 
	 * @param id
	 * @return
	 */
	public NodeInfo getNeighbourInformation(Id id);

	/**
	 * Returns the list of known apps. Which apps are listed here is
	 * implementation-specific, but it includes at least those which the current
	 * node plays a role in, and possible more apps whose information is taken
	 * from other nodes.
	 * 
	 * @return
	 */
	public Set<AppInfo> getAllKnownApps();

	// Technical stuff -----------------------------------------------

	/**
	 * Startup: Perform initialization.
	 * 
	 * @param scpNode
	 */
	public void initialize(SCPNode scpNode);

	/**
	 * Returns the underlying SCP node.
	 * 
	 * @return
	 */
	public SCPNode getSCPNode();

	/**
	 * Method is every 5 seconds called. Can be used to update Information
	 * 
	 * @return
	 */
	public void updateNodeInformation();

	/**
	 * Creates ZimoryDeployment with requirements from App
	 */
	public ZimoryDeployment createZimoryDeploymentForApp(AppInfo appInfo, Logger log, String roleName);

	/**
	 * Checks whether virtualized executor is still needed returns executor's
	 * nodeInfo if it is no more needed, otherwise null
	 */
	public NodeInfo nodeInfoSuitableForExecution(AppInfo appInfo, NodeInfo nodeInfoToCheck, Id executorId);

	/**
	 * Stops deployment with Id {@code deploymentId}. Logger {@code log} for
	 * logging.
	 * 
	 * @param deploymentId
	 * @param log
	 * @param roleName
	 * @return
	 */

	public void stopZimoryDeployment(final int deploymentId, final Logger log, String roleName);

	/**
	 * Called when node bootet into PastryRing
	 */

	public void bootedIntoRing();
}
