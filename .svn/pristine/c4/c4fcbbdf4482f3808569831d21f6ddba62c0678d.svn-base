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
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RUndeployAppMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RUnstoreApplicationMessage;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class UnDeployerRole extends HelenaRole {

	private Logger undeployerlog;

	private AppInfo appInfo;

	private RoleId mainStorage;

	public UnDeployerRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		undeployerlog = LogFactory.get(nodeId + " " + ensembleId + " HELENA.UNDEPLOYER");
	}

	@Override
	public void run() {

		undeployerlog.info("Booting undeployer role %s", getRoleId());

		try {
			undeployerlog.info("Waiting for incoming message RUnDeployAppMessage... ");

			RUndeployAppMessage undeployMsg = waitForIncomingMessage(INFINITY, RUndeployAppMessage.class);
			undeployerlog.info("Got RUndeployMessage");

			appInfo = undeployMsg.getAppInfo();
			getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_UNDEPLOYER, "Undeploying");

			Id targetNode = createIdfromString(appInfo.getName());
			mainStorage = getRoleInstance(targetNode, MainStorageRole.class);

			undeployerlog.info("Undeploying from main storage...");
			sendMessage(new RUnstoreApplicationMessage(getRoleId(), mainStorage, appInfo));

		} catch (RTimeoutException | RRoleNotFoundException | REnsembleNotFoundException e) {
			undeployerlog.error("Exception occurred: %s", e.getMessage());
		} finally {
			getStrategy().roleShutdown(this);
			if (appInfo != null)
				getStrategy().removeAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_UNDEPLOYER);
			undeployerlog.info("Shutting down undeployer role %s", getRoleId());
		}

	}

}
