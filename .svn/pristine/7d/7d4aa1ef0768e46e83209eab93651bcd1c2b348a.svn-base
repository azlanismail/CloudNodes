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
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAppCodeMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RInitApplicationMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RInitiatingStartedMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RInternalMainStorageShutdownMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RMainStorageFailedMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RRequestCodeMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RStopAppHandlingMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RStoreAppMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RUnstoreApplicationMessage;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class MainStorageRole extends HelenaRole {

	private Logger log;
	private AppInfo appInfo;

	public MainStorageRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		log = LogFactory.get(nodeId + " " + ensembleId + " HELENA.MAINSTORAGE");
	}

	@Override
	public void run() {
		log.info("Booting main storage role %s", getRoleId());
		appInfo = null;

		try {
			log.info("Waiting for incoming message RStoreAppMessage...");
			RStoreAppMessage storeAppMsg;
			try {
				storeAppMsg = waitForIncomingMessage(INFINITY, RStoreAppMessage.class);
			} catch (RTimeoutException e) {
				log.error("Got at timeout while waiting for RStoreAppMessage. Shutting down main storage role");
				return;
			}
			RoleId deployerRole = storeAppMsg.getFromRole();
			appInfo = storeAppMsg.getAppInfo();
			byte[] appCode = storeAppMsg.getAppCode();

			log.info("Got RStoreAppMessage. Storing app %s in past locally", appInfo.getName());
			getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_MAINSTORAGE, "");

			// This is required for "real", not for the model
			getStrategy().storeInPAST(appInfo, appCode);

			Id initatorNode = createIdfromString(appInfo.getName());
			log.info("Creating initator role on %s", initatorNode);
			RoleId initRole;
			try {
				initRole = createRoleInstance(initatorNode, InitiatorRole.class);
			} catch (RTimeoutException e) {
				log.error("Got at timeout while waiting for InitatorRole. Shutting down main storage role");
				sendMessage(new RMainStorageFailedMessage(getRoleId(), deployerRole));
				return;
			}
			log.info("Got the initiator role %s", initRole);

			log.info("Sending RInitApplicationMessage to initiator role %s", initRole);
			try {
				sendMessage(new RInitApplicationMessage(getRoleId(), initRole, appInfo));
			} catch (RTimeoutException e) {
				log.error("Got at timeout while waiting for answer from RInitApplicationMessage. Shutting down main storage role.");
				sendMessage(new RMainStorageFailedMessage(getRoleId(), deployerRole));
				return;
			}

			sendMessage(new RInitiatingStartedMessage(getRoleId(), deployerRole));

			while (true) {

				ArrayList<Class<? extends RR2RMessage>> classList = new ArrayList<Class<? extends RR2RMessage>>();
				classList.add(RRequestCodeMessage.class);
				classList.add(RUnstoreApplicationMessage.class);
				classList.add(RInternalMainStorageShutdownMessage.class);

				log.info("Waiting for incoming message of type RRequestCodeMessage, RUnstoreApplicationMessage or RInternalMainStorageShutdownMessage...");
				try {
					RR2RMessage answer = waitForIncomingMessages(INFINITY, classList);

					if (answer instanceof RInternalMainStorageShutdownMessage) {
						// this is a workaround for stopping the main storage from the outside.
						// TODO we might want to replace this with dedicated thread stopping at some point.

						// Do NOT shut down executor.
						log.info("Got internal shutdown message, shutting down now.");
						break;
					}

					if (answer instanceof RRequestCodeMessage) {
						RRequestCodeMessage rrc = (RRequestCodeMessage) answer;
						log.info("Got message RRequestCodeMessage.");
						RoleId srcRole = rrc.getFromRole();
						try {
							sendMessage(new RAppCodeMessage(getRoleId(), srcRole, appCode));
						} catch (RTimeoutException e1) {
							log.error("Timeout sending RAppCodeMessage to (presumably) the executor. Shutting down main storage role.");
							break;
						}
					}

					if (answer instanceof RUnstoreApplicationMessage) {
						RUnstoreApplicationMessage unstore = (RUnstoreApplicationMessage) answer;

						log.info("Got RUnstoreApplication. Removing app from past.");
						getStrategy().removeFromPAST(unstore.getAppInfo());

						log.info("Sending stop message to initiator");
						try {
							sendMessage(new RStopAppHandlingMessage(getRoleId(), initRole, appInfo));
						} catch (RTimeoutException e) {
							log.error("Got at timeout sending RStopAppHandlingMessage to initiator. Shutting down main storage role.");
						}

						break; // end.
					}
				} catch (RTimeoutException e) {
					log.error("Got at timeout while waiting for RRequestCodeMessage, RUnstoreApplicationMessage or RInternalMainStorageShutdownMessage. Shutting down deployer role.");
				}
			}

			// that's it.

		} catch (RRoleNotFoundException | RRoleCreationException | REnsembleNotFoundException | RTimeoutException e) {
			log.error("Exception occurred: %s", e.getMessage());
		} finally {
			getStrategy().roleShutdown(this);
			if (appInfo != null)
				getStrategy().removeAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_MAINSTORAGE);
			log.info("Shutting down main storage role %s", getRoleId());
		}
	}

	@Override
	public void stop() {
		try {
			sendMessage(new RInternalMainStorageShutdownMessage(getRoleId(), getRoleId()));
		} catch (RRoleNotFoundException | RTimeoutException | REnsembleNotFoundException e) {
			log.error("Could not properly shut down main storage node.");
		}
	}

}
