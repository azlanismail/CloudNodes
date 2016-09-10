/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.roles;

import java.util.ArrayList;
import java.util.Collection;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.strategy.gossip.GossipHelenaBasedStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.REnsembleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleCreationException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAcknowledgeExecutionMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAskForExecutionMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RDeclineExecutionMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RExecuteAppMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RExecutionResultMessage;
import eu.ascens_ist.scp.node.info.AppExecutionStatus;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.info.Requirements;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class PotentialExecutorRole extends HelenaRole {

	private Logger log;

	private AppInfo appInfo;

	public PotentialExecutorRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		log = LogFactory.get(nodeId + " " + ensembleId + " HELENA.POTENTIALEXECUTOR");
	}

	@Override
	public void run() {
		log.info("Booting potential executor role %s", getRoleId());

		try {
			log.info("Waiting for incoming message RAskForExecutionMessage...");
			RAskForExecutionMessage askMsg;
			try {
				askMsg = waitForIncomingMessage(INFINITY, RAskForExecutionMessage.class);
			} catch (RTimeoutException e) {
				log.error("Got at timeout while waiting for RAskForExecutionMessage. Shutting down potential executor role");
				return;
			}
			appInfo = askMsg.getAppInfo();
			RoleId initiatorRole = askMsg.getFromRole();

			log.info("Got RAskForExecutionMessage. Evaluating whether we want to participate...");
			getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_POTENTIAL_EXECUTER, "Evaluating participation");

			if (canExecute(appInfo.getRequirements())) {
				// yes
				getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_POTENTIAL_EXECUTER, "Creating executor");

				// create executor on myself
				log.info("We want to participate.");
				log.info("Creating executor role on myself...");
				RoleId executorRole = createLocalRoleInstance(ExecutorRole.class);
				log.info("Got the executor role %s.", executorRole);

				log.info("Sending RExecuteAppMessage to executor role %s", executorRole);
				try {
					sendMessage(new RExecuteAppMessage(getRoleId(), executorRole, appInfo));
				} catch (RTimeoutException e) {
					log.error("Got at timeout while waiting for answer from RExecuteAppMessage.");
					return;
				}

				while (true) {
					Collection<Class<? extends RR2RMessage>> messages = new ArrayList<>();
					messages.add(RAcknowledgeExecutionMessage.class);
					messages.add(RExecutionResultMessage.class);
					log.info("Waiting for incoming message RExecutionResultMessage or RAcknowledgeExecutionMessage from executor...");
					RR2RMessage message;
					try {
						message = waitForIncomingMessages(INFINITY, messages);
						if (message instanceof RAcknowledgeExecutionMessage) {
							log.info("Got RAcknowledgeExecutionMessage from executor");
							log.info("Sending RAcknowledgeExecutionMessage to initiator role %s", initiatorRole);
							sendMessage(new RAcknowledgeExecutionMessage(getRoleId(), initiatorRole, executorRole));
							break;

						} else if (message instanceof RExecutionResultMessage) {
							RExecutionResultMessage resultMessage = (RExecutionResultMessage) message;
							if (resultMessage.getStatus() == AppExecutionStatus.PROBLEM_COULD_NOT_START) {
								log.info("Got RExecutionResultMessage from executor. Executor could not start bundle.");
								log.info("Sending RExecutionResultMessage to initiator role %s", initiatorRole);
								sendMessage(new RExecutionResultMessage(getRoleId(), initiatorRole, resultMessage.getStatus()));
							}
						}
					} catch (RTimeoutException e) {
						log.error("Got at timeout while waiting for RExecutionResultMessage or RAcknowledgeExecutionMessage.");
					}
				}

			} else {
				getStrategy().addAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_POTENTIAL_EXECUTER, "");
				// no
				log.info("We don't want to participate.");
				log.info("Sending RDeclineExecutionMessage to initiator role %s", initiatorRole);
				try {
					sendMessage(new RDeclineExecutionMessage(getRoleId(), initiatorRole));
				} catch (RTimeoutException e) {
					log.error("Got at timeout while waiting for answer from RDeclineExecutionMessage.");
					return;
				}
			}

		} catch (RRoleNotFoundException | RRoleCreationException | REnsembleNotFoundException e) {
			log.error("Exception occurred: %s", e.getMessage());
		} finally {
			getStrategy().roleShutdown(this);
			if (appInfo != null)
				getStrategy().removeAppRole(appInfo, GossipHelenaBasedStrategy.ROLE_POTENTIAL_EXECUTER);
			log.info("Shutting down potential executor role %s", getRoleId());
		}

	}

	private boolean canExecute(Requirements appReqs) {
		return getStrategy().getNodeInfo().canExecute(appReqs);
	}

}
