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
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RStopDeploymentMessage;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class DeploymentStopperRole extends HelenaRole {

	private Logger deploymentStopperLog;

	private NodeInfo executorInfo;
	private AppInfo appInfo;

	public DeploymentStopperRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		deploymentStopperLog = LogFactory.get(nodeId + " " + ensembleId + " HELENA.DEPLOYMENT-STOPPER");
	}

	@Override
	public void run() {

		deploymentStopperLog.info("Booting deployment stopper role %s", getRoleId());

		try {
			deploymentStopperLog.info("Now waiting for RStopDeploymentMessage...");

			RStopDeploymentMessage stopMessage = waitForIncomingMessage(INFINITY, RStopDeploymentMessage.class);
			appInfo = stopMessage.getAppInfo();
			executorInfo = stopMessage.getExecutorInfo();

			getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_DEPLOYMENT_STOPPER, "Stopping deployment");
			deploymentStopperLog.info("Executor for app %s runs on virtualized machine. Stopping deployment %s.", appInfo.getName(), executorInfo.getDeploymentId());

			getStrategy().sendShutdownRequest(executorInfo.getId());
			getStrategy().stopZimoryDeployment(executorInfo.getDeploymentId(), deploymentStopperLog, GossipHelenaBasedStrategy.ROLE_DEPLOYMENT_STOPPER);

		} catch (RTimeoutException e) {
			deploymentStopperLog.error("Exception occurred: %s", e.getMessage());
		} finally {
			getStrategy().roleShutdown(this);
			if (appInfo != null)
				getStrategy().removeAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_DEPLOYMENT_STOPPER);
			deploymentStopperLog.info("Shutting down deployment stopper role %s", getRoleId());
		}
	}

}
