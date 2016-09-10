/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.roles;

import java.util.ArrayList;

import org.osgi.framework.BundleException;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.exceptions.AppRequestException;
import eu.ascens_ist.scp.node.core.exceptions.BundleStartException;
import eu.ascens_ist.scp.node.core.strategy.AbstractStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.GossipHelenaBasedStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.REnsembleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAcknowledgeExecutionMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAppCodeMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RExecuteAppMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RExecutionResultMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RPingExecutorMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RPongExecutorMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RRequestCodeMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RStopAppHandlingMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RUIRequestMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RUIResponseMessage;
import eu.ascens_ist.scp.node.info.AppExecutionStatus;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class ExecutorRole extends HelenaRole {
	private Logger log;

	private RoleId initiatorRole;

	private AppInfo appInfo;

	private boolean fullBooted= false;

	private boolean roleIsDown= false;
	
	public ExecutorRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		log = LogFactory.get(nodeId + " " + ensembleId + " HELENA.EXECUTOR");
	}

	@Override
	public void run() {

		log.info("Booting executor role %s", getRoleId());

		try {
			log.info("Waiting for incoming message RExecuteAppMessage...");
			RExecuteAppMessage executeAppMessage;
			try {
				executeAppMessage = waitForIncomingMessage(INFINITY, RExecuteAppMessage.class);
			} catch (RTimeoutException e) {
				log.error("Got at timeout while waiting for RAskForExecutionMessage. Shutting down executor role");
				return;
			}
			appInfo = executeAppMessage.getAppInfo();
			RoleId potentialExecutorRole = executeAppMessage.getFromRole();

			log.info("Got RExecuteAppMessage.");
			getStrategy().addAppRole(appInfo, AbstractStrategy.ROLE_EXECUTOR, "");

			Id targetNode = createIdfromString(appInfo.getName());
			log.info("Retrieving main storage role from the target node %s.", targetNode);
			RoleId mainStorage;
			try {
				mainStorage = getRoleInstance(targetNode, MainStorageRole.class);
			} catch (RTimeoutException | REnsembleNotFoundException | RRoleNotFoundException e1) {
				log.error("Could not get main storage role. Shutting down executor role.");
				return;
			}

			log.info("Requesting code for app %s from main storage role %s", appInfo, mainStorage);
			getStrategy().addAppRole(appInfo, AbstractStrategy.ROLE_EXECUTOR, "Grabbing bytecode");

			log.info("Sending RRequestCodeMessage to main storage role %s", mainStorage);
			try {
				sendMessage(new RRequestCodeMessage(getRoleId(), mainStorage, appInfo));
			} catch (RTimeoutException | RRoleNotFoundException | REnsembleNotFoundException e1) {
				log.error("Could not send message RRequestCodeMessage to main storage role %s...", mainStorage);
				return;
			}

			log.info("Waiting for incoming message RAppCodeMessage...");
			RAppCodeMessage appCodeMessage;
			try {
				appCodeMessage = waitForIncomingMessage(5000, RAppCodeMessage.class);
			} catch (RTimeoutException e1) {
				log.error("Got at timeout while waiting for RAppCodeMessage. Shutting down executor role");
				return;
			}
			byte[] appCode = appCodeMessage.getAppCode();

			log.info("Got RAppCodeMessage. Trying to execute the application");
			getStrategy().addAppRole(appInfo, AbstractStrategy.ROLE_EXECUTOR, "Trying to execute");

			try {
				initiatorRole = getRoleInstance(targetNode, InitiatorRole.class);
			} catch (RTimeoutException | RRoleNotFoundException | REnsembleNotFoundException e1) {
				log.error("Could not get initiator role");
				return;
			}

			try {
				getStrategy().executeApp(appInfo, appCode);
				getStrategy().bootLocalApp(appInfo);
				log.info("WE DID IT! APP WAS SUCCESSFULLY STARTED!!");

				log.info("Sending RExecutionResultMessage to potential executor role %s", potentialExecutorRole);
				sendMessage(new RExecutionResultMessage(getRoleId(), potentialExecutorRole, AppExecutionStatus.SUCCESS));

			} catch (BundleStartException e) {
				e.printStackTrace();
				log.error(e.getMessage());

				log.info("Sending RExecutionResultMessage to potential executor role %s", potentialExecutorRole);
				sendMessage(new RExecutionResultMessage(getRoleId(), potentialExecutorRole, e.getStatus()));
				return;
			} catch (AppRequestException e) {
				e.printStackTrace();
				log.error(e.getMessage());
				log.info("Sending RExecutionResultMessage to potential executor role %s", potentialExecutorRole);
				sendMessage(new RExecutionResultMessage(getRoleId(), potentialExecutorRole, AppExecutionStatus.PROBLEM_COULD_NOT_START));
			}

			// send back gossipping info to INITIATOR to ensure INITITATOR knows about us in case of failures.
			getStrategy().gossipInformationTo(initiatorRole.getNodeId());

			getStrategy().addAppRole(appInfo, AbstractStrategy.ROLE_EXECUTOR, "Executing");
			log.info("Execution successful, app is running.");

			log.info("Sending RAcknowledgeExecutionMessage to potential executor role %s", potentialExecutorRole);
			sendMessage(new RAcknowledgeExecutionMessage(getRoleId(), potentialExecutorRole, getRoleId()));

			while (true) {
				
				fullBooted= true;

				RR2RMessage m;
				try {
					ArrayList<Class<? extends RR2RMessage>> classList = new ArrayList<Class<? extends RR2RMessage>>();
					classList.add(RPingExecutorMessage.class);
					classList.add(RUIRequestMessage.class);
					classList.add(RStopAppHandlingMessage.class);
					m = waitForIncomingMessages(INFINITY, classList);
				} catch (RTimeoutException e) {
					log.error("Timeout occurred waiting for incoming RPing, RUIRequest, RStopApp.");
					return;
				}
				
				if (m instanceof RUIRequestMessage) {

					log.info("Got a RUIRequestMessage; handling it locally...");

					RUIRequestMessage uiRequest = (RUIRequestMessage) m;
					RoleId sourceRole = uiRequest.getFromRole();

					String result = getStrategy().handleLocalUI(appInfo.getName(), uiRequest.getTarget(), uiRequest.getProperties());

					log.info("Handled; sending back result...");
					try {
						sendMessage(new RUIResponseMessage(getRoleId(), sourceRole, result));
					} catch (RTimeoutException e) {
						log.error("Timeout occurred sending back UI response message.");
						return;
					}
					log.info("Result sent back.");
					continue;
				}

				// PingPong
				if (m instanceof RPingExecutorMessage) {
					log.info("Ping received. Sending pong to initiator...");
					try {

						// !!! Important: The init role MAY HAVE CHANGED HERE!! So, we update it each time

						initiatorRole = ((RPingExecutorMessage) m).getFromRole();
						sendMessage(new RPongExecutorMessage(getRoleId(), initiatorRole, getStrategy().getNodeInfo()));
					} catch (RTimeoutException e) {
						log.error("Timeout occurred sending back pong message to initiator... ignoring.");
					} catch (RRoleNotFoundException e) {
						log.error("Role not found in the initiator despite ping and updating...");
					}
					continue;
				}

				if (m instanceof RStopAppHandlingMessage) {
					log.info("Got RStopAppHandlingMessage. Shutting down app.");
					fullBooted= false;
					stopApp();
					break;
				}
			}

		} catch (RRoleNotFoundException | REnsembleNotFoundException | RTimeoutException e) {
			log.error("Exception occurred: %s", e.getMessage());
			e.printStackTrace();
		} finally {
			getStrategy().roleShutdown(this);
			if (appInfo != null)
				getStrategy().removeAppRole(appInfo, AbstractStrategy.ROLE_EXECUTOR);
			log.info("Shutting down executor role %s", getRoleId());
			synchronized(this) {this.roleIsDown= true; }
		}

	}
	
	public boolean isFullBooted() {
		return fullBooted;
	}
	
	public synchronized boolean isRoleIsDown() {
		return roleIsDown;
	}

	private void stopApp() {
		try {
			getStrategy().stopApp(appInfo.getName());
		} catch (BundleException e) {
			e.printStackTrace();
			log.error(e, "Executor could not stop app %s.", appInfo);
		}
	}

	@Override
	public void stop() {
		try {
			sendMessage(new RStopAppHandlingMessage(getRoleId(), getRoleId(), appInfo));
		} catch (RRoleNotFoundException | RTimeoutException | REnsembleNotFoundException e) {
			log.error(e, "Could not properly stop executor due to exception.");
		}
	}

}
