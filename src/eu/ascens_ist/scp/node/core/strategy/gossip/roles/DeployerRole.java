/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.roles;

import java.util.ArrayList;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.strategy.gossip.GossipHelenaBasedStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.REnsembleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleCreationException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RDeployAppMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RInitiatingStartedMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RMainStorageFailedMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RStoreAppMessage;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class DeployerRole extends HelenaRole {
	private Logger log;

	public DeployerRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		log = LogFactory.get(nodeId + " " + ensembleId + " HELENA.DEPLOYER");
	}

	@Override
	public void run() {
		log.info("Booting deployer role %s", getRoleId());
		AppInfo appInfo = null;

		while (true) {
			try {
				log.info("Waiting for incoming message RDeployAppMessage... ");
				RDeployAppMessage deployAppMessage;
				try {
					deployAppMessage = waitForIncomingMessage(INFINITY, RDeployAppMessage.class);
				} catch (RTimeoutException e) {
					log.error("Got at timeout while waiting for RDeployAppMessage. Shutting down deployer role");
					break;
				}
				appInfo = deployAppMessage.getAppInfo();
				byte[] appCode = deployAppMessage.getAppCode();

				log.info("Got RDeployAppMessage. Deploying App %s", appInfo.getName());
				getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_DEPLOYER, "Deploying");

				Id targetNode = createIdfromString(appInfo.getName());
				log.info("Crating main storage role on  %s", targetNode.toStringFull());
				RoleId mainStorageRole;
				try {
					mainStorageRole = createRoleInstance(targetNode, MainStorageRole.class);
				} catch (RTimeoutException e) {
					log.error("Got at timeout while waiting for MainStorageRole. Restart deploying");
					continue;
				}
				log.info("Got the main storage role %s", mainStorageRole);

				log.info("Sending RStoreAppMessage to main storage role %s", mainStorageRole);
				try {
					sendMessage(new RStoreAppMessage(getRoleId(), mainStorageRole, appInfo, appCode));
				} catch (RTimeoutException e) {
					log.error("Got at timeout while waiting for answer from RStoreAppMessage. Restart deploying.");
					continue;
				}

				ArrayList<Class<? extends RR2RMessage>> classList = new ArrayList<Class<? extends RR2RMessage>>();
				classList.add(RMainStorageFailedMessage.class);
				classList.add(RInitiatingStartedMessage.class);
				log.info("Waiting for incoming message RMainStorageFailedMessage or RAcknowledgeExecutionMessage ...");
				try {
					RR2RMessage answer = waitForIncomingMessages(INFINITY, classList);
					if (answer instanceof RMainStorageFailedMessage) {
						log.info("Got RMainStorageFailedMessage. Restart deploying.");
						continue;
					}
					if (answer instanceof RInitiatingStartedMessage) {
						log.info("Got RInitiatingStartedMessage.");
						break;
					}
				} catch (RTimeoutException e) {
					log.error("Got at timeout while waiting for RMainStorageFailedMessage or RAcknowledgeExecutionMessage. Shutting down deployer role.");
				}

			} catch (RRoleCreationException e) {
				log.info("Could not create main storage role");
			} catch (RRoleNotFoundException e) {
				log.info("Could not find main storage role");
			} catch (REnsembleNotFoundException e) {
				log.error("Exception occurred: %s", e.getMessage());
			}
		}

		getStrategy().roleShutdown(this);
		if (appInfo != null)
			getStrategy().removeAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_DEPLOYER);
		log.info("Shutting down deployer role %s", getRoleId());

	}
}
