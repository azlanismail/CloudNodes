/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.roles;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.iaas.zimory.ZimoryDeployment;
import eu.ascens_ist.scp.node.core.strategy.gossip.GossipHelenaBasedStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.REnsembleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RCreateDeploymentMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RDeploymentCreatedMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RDeploymentCreationFailedMessage;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class DeploymentCreatorRole extends HelenaRole {

	private Logger deploymentCreatorLog;

	private AppInfo appInfo;

	public DeploymentCreatorRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		deploymentCreatorLog = LogFactory.get(nodeId + " " + ensembleId + " HELENA.DEPLOYMENT-CREATOR");
	}

	@Override
	public void run() {

		deploymentCreatorLog.info("Booting deployment creator role %s", getRoleId());

		try {
			deploymentCreatorLog.info("Now waiting for RCreateDeploymentMessage...");

			RCreateDeploymentMessage startMessage = waitForIncomingMessage(INFINITY, RCreateDeploymentMessage.class);
			RoleId sourceRole = startMessage.getFromRole();
			appInfo = startMessage.getAppInfo();

			getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_DEPLOYMENT_CREATOR, "Creating deployment"); // TODO add role status
			deploymentCreatorLog.info("Got an RCreateDeploymentMessage. Creating deployment...");

			// create deployment with requested requirements on Zimory platform
			// if app requirements allow virtualization
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ZimoryDeployment deployment = getStrategy().createZimoryDeploymentForApp(appInfo, deploymentCreatorLog, GossipHelenaBasedStrategy.ROLE_DEPLOYMENT_CREATOR);
			if (deployment != null) {
				deploymentCreatorLog.error("Deployment was successfully created");
				sendMessage(new RDeploymentCreatedMessage(getRoleId(), sourceRole));
			} else {
				deploymentCreatorLog.error("Deployment could not be created");
				sendMessage(new RDeploymentCreationFailedMessage(getRoleId(), sourceRole));
			}

		} catch (RTimeoutException | RRoleNotFoundException | REnsembleNotFoundException e) {
			deploymentCreatorLog.error("Exception occurred: %s", e.getMessage());
		} finally {
			getStrategy().roleShutdown(this);
			if (appInfo != null)
				getStrategy().removeAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_DEPLOYMENT_CREATOR);
			deploymentCreatorLog.info("Shutting down deployment creator role %s", getRoleId());
		}
	}

}
