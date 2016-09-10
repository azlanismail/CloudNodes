/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.roles;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.strategy.gossip.GossipHelenaBasedStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.REnsembleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAskForExecutorMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RReportOnExecutorMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RRequestorRequestMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RUIRequestMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RUIResponseMessage;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class RequesterRole extends HelenaRole {

	private Logger requesterLog;

	private AppInfo appInfo;

	public RequesterRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		requesterLog = LogFactory.get(nodeId + " " + ensembleId + " HELENA.REQUESTER");
	}

	@Override
	public void run() {

		requesterLog.info("Booting requester role %s", getRoleId());

		try {
			requesterLog.info("Waiting for RRequestorRequestMessage...");

			RRequestorRequestMessage reqReq;
			try {
				reqReq = waitForIncomingMessage(INFINITY, RRequestorRequestMessage.class);
			} catch (RTimeoutException e1) {
				requesterLog.info("Timeout waiting for RRequestorRequestMessage...");
				return;
			}

			appInfo = reqReq.getAppInfo();
			Id routingId = reqReq.getRoutingId();

			getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_REQUESTER, ""); // TODO add role status

			Id initatorId = createIdfromString(appInfo.getName());

			// Find initiator role
			requesterLog.info("Grabbing initiator role...");
			RoleId initiator;
			try {
				initiator = getRoleInstance(initatorId, InitiatorRole.class);
			} catch (RTimeoutException e) {
				requesterLog.info("Timeout waiting for initator role...");
				return;
			}

			requesterLog.info("Asking initiator for executor...");
			try {
				sendMessage(new RAskForExecutorMessage(getRoleId(), initiator, appInfo));
			} catch (RTimeoutException e) {
				requesterLog.info("Timeout sending RAskForExecutorMessage");
				return;
			}

			requesterLog.info("Wait for answer from initiator on executor...");
			RReportOnExecutorMessage answer;
			try {
				answer = waitForIncomingMessage(5000, RReportOnExecutorMessage.class);
			} catch (RTimeoutException e) {
				requesterLog.info("Timeout waiting for answer from initiator on executor...");
				return;
			}

			RoleId executor = answer.getExecutorRole();

			// forward request to executor, wait for answer
			requesterLog.info("Forward UI request to executor....");
			try {
				sendMessage(new RUIRequestMessage(getRoleId(), executor, routingId, appInfo, reqReq.getTarget(), reqReq.getProperties()));
			} catch (RTimeoutException e) {
				requesterLog.info("Timeout sending UI request to executor...");
			}

			requesterLog.info("Waiting for UI response from  executor....");
			RUIResponseMessage uiResponse;
			try {
				uiResponse = waitForIncomingMessage(20000, RUIResponseMessage.class);
			} catch (RTimeoutException e) {
				requesterLog.info("Timeout waiting for UI response from executor....");
				return;
			}

			requesterLog.info("Got info from executor, sending back to UI.");

			// send back to the UI
			getStrategy().setUIResponseFor(routingId, appInfo, reqReq.getTarget(), uiResponse.getResult());

		} catch (RRoleNotFoundException | REnsembleNotFoundException e) {
			requesterLog.error("Exception occurred: %s", e.getMessage());
		} finally {
			getStrategy().roleShutdown(this);
			if (appInfo != null)
				getStrategy().removeAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_REQUESTER);
			requesterLog.info("Shutting down requester role %s.", getRoleId());
		}
	}

}
