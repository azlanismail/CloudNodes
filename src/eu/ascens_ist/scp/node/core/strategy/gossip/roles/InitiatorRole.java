/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.roles;

import static eu.ascens_ist.scp.node.Configuration.EXECUTOR_REQUIREMENTS_EXCEEDING_INTERVAL;
import static eu.ascens_ist.scp.node.Configuration.LIVENESS_CHECK_INTERVAL;
import static eu.ascens_ist.scp.node.Configuration.ZIMORY_CHECK_VIRTUALIZED_NODE_NEEDED_INTERVAL;
import static eu.ascens_ist.scp.node.Configuration.ZIMORY_UNSUCCESSFUL_ATTEMPTS_BEFORE_VM_CREATION;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.Configuration;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.core.strategy.AbstractStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.GossipHelenaBasedStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.REnsembleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleCreationException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAcknowledgeExecutionMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAskForExecutionMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RAskForExecutorMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RCreateDeploymentMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RDeclineExecutionMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RDeploymentCreatedMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RDeploymentCreationFailedMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RExecutionResultMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RExecutorFailedMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RExecutorFailedRequirementsMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RInitApplicationMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RInternalInitiatorShutdownMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RPingExecutorMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RPongExecutorMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RReportOnExecutorMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RStopAppHandlingMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RStopDeploymentMessage;
import eu.ascens_ist.scp.node.info.AppExecutionStatus;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.info.Requirements;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

//importing from the planner package
import planner.CompositionalMultiPlanner;
import planner.ConfigurationPlanner;

public class InitiatorRole extends HelenaRole {

	public static boolean TESTMODE = false;

	private Logger log;

	private AppInfo appInfo;

	private boolean pinging;

	private Date firstTimeAppRequirementsNotMet;
	private Date lastNodeCheck;

	private RoleId executorRole;

	private boolean isUp = false;

	private boolean shouldLookForExecutor = false;
	
	
	//attributes related to the games-based planning
	private Requirements appReq;
	private ConfigurationPlanner conf;
	private CompositionalMultiPlanner plan;
	

	public InitiatorRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		super(ensembleId, nodeId, strategy);
		log = LogFactory.get(nodeId + " " + ensembleId + " HELENA.INITIATOR");
	}

	@Override
	public void run() {
		log.info("Booting initiator role %s", getRoleId());

		try {
			log.info("Waiting for incoming message RInitApplicationMessage...");
			RInitApplicationMessage initMsg;
			try {
				initMsg = waitForIncomingMessage(5000, RInitApplicationMessage.class);
			} catch (RTimeoutException e) {
				log.error("Got at timeout while waiting for RInitApplicationMessage. Shutting down initiator role");
				return;
			}
			appInfo = initMsg.getAppInfo();

			log.info("Got RInitApplicationMessage. Initiating App...");
			getStrategy().addAppRole(appInfo, AbstractStrategy.ROLE_INITIATOR, "Initiating App");

			synchronized (this) {
				isUp = true;
			}

			if (TESTMODE) {
				waitForFlag();
			}

			int requestCycleCounter = 1;

			while (true) {

				executorRole = null;

				// First, lookup all nodes to see if there is already an
				// executor

				Set<NodeInfo> allNodes = getAllNodeInfosAvailableIncludingOurselves();
				log.info("Going through all matching nodes; size is %d, checking for existing executor...", allNodes.size());
				getStrategy().addAppRole(appInfo, AbstractStrategy.ROLE_INITIATOR, "Searching for Executor");

				// First check
				for (NodeInfo potentialExistingExecutor : allNodes) {

					Id potentialExecutorIdentifierNode = potentialExistingExecutor.getId();

					// This node might actually already execute the app ---
					// since we might be a next-iteration initiator.
					Map<String, String> rolesOfPotentialExecutor = potentialExistingExecutor.getAppRoles().get(appInfo);
					if (rolesOfPotentialExecutor != null && rolesOfPotentialExecutor.containsKey(AbstractStrategy.ROLE_EXECUTOR)) {
						// might be, check...
						try {
							executorRole = getRoleInstance(potentialExecutorIdentifierNode, ExecutorRole.class);
							if (executorRole.getNodeId().equals(this.getNodeId())) {
								log.info("Executor is running on initiator node. Shutting down executor.");
								sendMessage(new RStopAppHandlingMessage(this.getRoleId(), executorRole, appInfo));
								while (executorRole != null) {
									executorRole = getRoleInstance(potentialExecutorIdentifierNode, ExecutorRole.class);
								}
							}
							break;

						} catch (RTimeoutException | RRoleNotFoundException | REnsembleNotFoundException e) {
							executorRole = null;
						}
					}

				}

				if (executorRole == null) {
					log.info("No existing executor found.");

				    
                    
					//=============================
					
					//passing requirements information to the games-based planner
					appReq = appInfo.getRequirements();
					
					//call planner
					plan = new CompositionalMultiPlanner();
					plan.setApplicationRequirements(0, (int)appReq.getCPUCores(), 
													(int)appReq.getCPUSpeed(), appReq.getCPULoad(),
													(int)appReq.getTotalMemory(), (int)appReq.getFreeMemory());
					plan.setApplicationRequirements(1, (int)appReq.getCPUCores(), 
													(int)appReq.getCPUSpeed(), appReq.getCPULoad(),
													(int)appReq.getTotalMemory(), (int)appReq.getFreeMemory());
					log.info("Requirements are: cpu core-%d, cpu speed-%d, cpu loads-%f, total memory-%d, free memory- %d",
							(int)appReq.getCPUCores(), (int)appReq.getCPUSpeed(), appReq.getCPULoad(),
							(int)appReq.getTotalMemory(), (int)appReq.getFreeMemory());
							
					log.info("Number of all node is %d ",allNodes.size());
					if(allNodes.size() > plan.getMaxResource())
						log.error("Number of nodes is greater, cannot synthesize");
					else {
						int i=0;
						
						for (NodeInfo potentialExistingExecutor : allNodes) {
							Id potentialExecutorIdentifierNode = potentialExistingExecutor.getId();
							log.info("Got the potential executor role %s", potentialExecutorIdentifierNode.toString());
							plan.setNodeCapabilities(i, potentialExistingExecutor.getIdString(), potentialExistingExecutor.getCpuCores(), 
													potentialExistingExecutor.getCpuSpeed(), potentialExistingExecutor.getCpuLoad(),
													(int)potentialExistingExecutor.getMemTotal(), (int)potentialExistingExecutor.getMemFree(), 
													potentialExistingExecutor.getLocationString());
							i++;
						}	
												
						plan.generate();
						String nodeName = plan.getDecision(0);
						Id potentialExecutorIdentifierNode = null;
						for (NodeInfo potentialExecutor : allNodes) {
							if (nodeName.equalsIgnoreCase(potentialExecutor.getIdString())) {
								potentialExecutorIdentifierNode = potentialExecutor.getId();
								log.info("SELECTED NODE IS : %s", nodeName);
								log.info("SELECTED NODE ID IS : %s", potentialExecutorIdentifierNode);
							}//end of if
						}//end of for
						
						log.info("Creating potential executor role on  %s", potentialExecutorIdentifierNode);
						RoleId potExRole;
						try {
							potExRole = createRoleInstance(potentialExecutorIdentifierNode, PotentialExecutorRole.class);
						} catch (RTimeoutException e) {
							log.error("Got at timeout while waiting for PotentialExecutorRole.");
							continue;
						}
						log.info("Got the potential executor role %s", potExRole);

						log.info("Sending RAskForExecutionMessag to potential executor role %s", potExRole);
						try {
							sendMessage(new RAskForExecutionMessage(getRoleId(), potExRole, appInfo));
						} catch (RTimeoutException e) {
							log.error("Got at timeout while waiting for answer from RAskForExecutionMessage.");
							continue;
						}

						ArrayList<Class<? extends RR2RMessage>> classList = new ArrayList<>();
						classList.add(RAcknowledgeExecutionMessage.class);
						classList.add(RDeclineExecutionMessage.class);
						classList.add(RExecutionResultMessage.class);
						log.info(
								"Waiting for incoming message RAcknowledgeExecutionMessage, RDeclineExecutionMessage or RExecutionResultMessage from %s",
								potExRole);
						try {
							RR2RMessage answer = waitForIncomingMessages(INFINITY, classList);

							if (answer instanceof RAcknowledgeExecutionMessage) {
								log.info("Got RAcknowledgeExecutionMessage.");
								executorRole = ((RAcknowledgeExecutionMessage) answer).getExecutorRole();
								break;
							} else if (answer instanceof RDeclineExecutionMessage) {
								log.info("Got RDeclineExecutionMessage. Continue search for executor.");
								continue;
							} else if (answer instanceof RExecutionResultMessage) {
								RExecutionResultMessage resultMessage = (RExecutionResultMessage) answer;
								if (resultMessage.getStatus() == AppExecutionStatus.PROBLEM_COULD_NOT_START) {
									log.info("Got RExecutionResultMessage and app could not start. Continue search for executor.");
								}
								continue;
							}

						} catch (RTimeoutException e) {
							log.error("Got at timeout while waiting for RAcknowledgeExecutionMessage, RDeclineExecutionMessage or RExecutionResultMessage.");
							continue;
						}						
						
					}//end of else for possible synthesis
					
			 		
					//=============================
//					Set<NodeInfo> allMatchingNodes = getMatchingNodesFromGossipStorage(appInfo.getRequirements());
//					log.info("Going through all matching nodes; size is %d, finding potential executor...", allMatchingNodes.size());
//					for (NodeInfo potentialExecutor : allMatchingNodes) {
//						Id potentialExecutorIdentifierNode = potentialExecutor.getId();
//
//						log.info("Creating potential executor role on  %s", potentialExecutorIdentifierNode);
//						RoleId potExRole;
//						try {
//							potExRole = createRoleInstance(potentialExecutorIdentifierNode, PotentialExecutorRole.class);
//						} catch (RTimeoutException e) {
//							log.error("Got at timeout while waiting for PotentialExecutorRole.");
//							continue;
//						}
//						log.info("Got the potential executor role %s", potExRole);
//
//						log.info("Sending RAskForExecutionMessag to potential executor role %s", potExRole);
//						try {
//							sendMessage(new RAskForExecutionMessage(getRoleId(), potExRole, appInfo));
//						} catch (RTimeoutException e) {
//							log.error("Got at timeout while waiting for answer from RAskForExecutionMessage.");
//							continue;
//						}
//
//						ArrayList<Class<? extends RR2RMessage>> classList = new ArrayList<>();
//						classList.add(RAcknowledgeExecutionMessage.class);
//						classList.add(RDeclineExecutionMessage.class);
//						classList.add(RExecutionResultMessage.class);
//						log.info(
//								"Waiting for incoming message RAcknowledgeExecutionMessage, RDeclineExecutionMessage or RExecutionResultMessage from %s",
//								potExRole);
//						try {
//							RR2RMessage answer = waitForIncomingMessages(INFINITY, classList);
//
//							if (answer instanceof RAcknowledgeExecutionMessage) {
//								log.info("Got RAcknowledgeExecutionMessage.");
//								executorRole = ((RAcknowledgeExecutionMessage) answer).getExecutorRole();
//								break;
//							} else if (answer instanceof RDeclineExecutionMessage) {
//								log.info("Got RDeclineExecutionMessage. Continue search for executor.");
//								continue;
//							} else if (answer instanceof RExecutionResultMessage) {
//								RExecutionResultMessage resultMessage = (RExecutionResultMessage) answer;
//								if (resultMessage.getStatus() == AppExecutionStatus.PROBLEM_COULD_NOT_START) {
//									log.info("Got RExecutionResultMessage and app could not start. Continue search for executor.");
//								}
//								continue;
//							}
//
//						} catch (RTimeoutException e) {
//							log.error("Got at timeout while waiting for RAcknowledgeExecutionMessage, RDeclineExecutionMessage or RExecutionResultMessage.");
//							continue;
//						}
//					}//end of for
				}//end of if

				if (executorRole == null) {
					if (requestCycleCounter % ZIMORY_UNSUCCESSFUL_ATTEMPTS_BEFORE_VM_CREATION == 0) {
						if (!appInfo.getRequirements().getNoVirtualization()) {
							if (Configuration.validIaasConnection()) {
								log.info("Could not find an executor after %d attempts. Creating deployment creator", requestCycleCounter);
								// after specified attempts,
								// start deployment creator role

								RoleId creatorRole = createLocalRoleInstance(DeploymentCreatorRole.class);

								sendMessage(new RCreateDeploymentMessage(getRoleId(), creatorRole, appInfo));
								ArrayList<Class<? extends RR2RMessage>> classList = new ArrayList<Class<? extends RR2RMessage>>();
								classList.add(RDeploymentCreatedMessage.class);
								classList.add(RDeploymentCreationFailedMessage.class);

								RR2RMessage answer = waitForIncomingMessages(INFINITY, classList);

								if (answer instanceof RDeploymentCreationFailedMessage) {
									log.info("Deployment could not be created. Repeat the initiating process");
									requestCycleCounter = 1;
								}
							} else {
								log.info(
										"Could not find an executor after %d attempts and can not create deployment since there is no valid IaaS information.",
										requestCycleCounter);
							}
						} else {
							log.info(
									"Could not find an executor after %d attempts and can not create deployment since virtualization is not allowed for this app.",
									requestCycleCounter);
						}
					} else {
						log.error("We could not find an executor among the existing matching nodes. Waiting and retrying.");
					}
					sleep(2000);

					requestCycleCounter++;
					continue;
				}
				requestCycleCounter = 1;

				log.info("We assume that the executor is now properly running and simply wait for any other incoming message.");

				boolean stop = false;
				startPinging(executorRole);

				while (true) {
					ArrayList<Class<? extends RR2RMessage>> classList = new ArrayList<Class<? extends RR2RMessage>>();
					classList.add(RExecutorFailedMessage.class);
					classList.add(RExecutorFailedRequirementsMessage.class);
					classList.add(RStopAppHandlingMessage.class);
					classList.add(RAskForExecutorMessage.class);
					classList.add(RInternalInitiatorShutdownMessage.class);

					RR2RMessage initiatorNotifyMessage = waitForIncomingMessages(INFINITY, classList);

					if (initiatorNotifyMessage instanceof RInternalInitiatorShutdownMessage) {
						// this is a workaround for stopping the initiator from
						// the outside.
						// TODO we might want to replace this with dedicated
						// thread stopping at some point.

						// Do NOT shut down executor.
						stop = true;
						executorRole = null;
						log.info("Got internal shutdown message,  shutting down now.");
						break;
					}

					if (initiatorNotifyMessage instanceof RAskForExecutorMessage) {
						RAskForExecutorMessage msg = (RAskForExecutorMessage) initiatorNotifyMessage;

						RoleId originRole = msg.getFromRole();
						if (msg.getAppInfo().equals(appInfo))
							sendMessage(new RReportOnExecutorMessage(getRoleId(), originRole, executorRole));
						continue;
					}

					if (initiatorNotifyMessage instanceof RExecutorFailedMessage) {
						// our initiator thread notified us that the executor is
						// down. We need to find a new one.
						
						if (TESTMODE) {
							waitForFlag();
						}

						// we are not stopping but finding a new executor =>
						// loop
						executorRole = null;
						stop = false;
						break;
					}

					if (initiatorNotifyMessage instanceof RExecutorFailedRequirementsMessage) {
						// our initiator thread notified us that the executor is
						// failed meeting the requirements. We need to find a
						// new one.

						sendMessage(new RStopAppHandlingMessage(getRoleId(), executorRole, appInfo));
						executorRole = null;
						stop = false;
						break;
					}

					if (initiatorNotifyMessage instanceof RStopAppHandlingMessage) {
						// We are told to stop execution of this app.

						sendMessage(new RStopAppHandlingMessage(getRoleId(), executorRole, appInfo));

						// BEGIN ZIMORY INTEGRATION if executor is virtualized,
						// shutdown deployment
						NodeInfo executorInfo = getStrategy().getNeighbourInformation(executorRole.getNodeId());
						if (executorInfo.isVirtualized()) {
							log.info("Executor for app %s runs on virtualized machine. Stopping deployment %s.", appInfo.getName(),
									executorInfo.getDeploymentId());
							try {
								RoleId stopperRole = createLocalRoleInstance(DeploymentStopperRole.class);

								sendMessage(new RStopDeploymentMessage(getRoleId(), stopperRole, appInfo, executorInfo));
							} catch (RRoleCreationException e) {
							}
						}
						// END ZIMORY INTEGRATION
						executorRole = null;
						stop = true;
						break;
					}
				}

				stopPinging(executorRole);

				if (stop)
					break;
			}

		} catch (RTimeoutException | RRoleCreationException | RRoleNotFoundException | REnsembleNotFoundException e) {
			log.error("Exception occurred: %s", e.getMessage());
		} finally {
			getStrategy().roleShutdown(this);
			if (appInfo != null)
				getStrategy().removeAppRole(appInfo, AbstractStrategy.ROLE_INITIATOR);
			log.info("Shutting down initiator role %s", getRoleId());
			synchronized (this) {
				isUp = false;
			}
		}
	}

	private void waitForFlag() {
		// WAIT HERE BEFORE SEARCHING FOR AN EXECUTOR TO ENABLE
		// MAKING SURE THAT THERE IS NO EXECUTOR.
		while (true) {
			synchronized (InitiatorRole.this) {
				if (shouldLookForExecutor)
					break;
			}
			try {
				log.info("WAITING FOR FLAG...");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void startPinging(final RoleId executorRole) {

		pinging = true;

		Runnable r = new Runnable() {

			@Override
			public void run() {

				// Before we start, wait a good long while for the executor to
				// properly boot.
				log.info("Starting Pinging Thread... waiting 15 seconds before actually pinging.");
				getStrategy().addAppRole(appInfo, AbstractStrategy.ROLE_INITIATOR, "Observing Executor");
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e3) {
				}

				int sleepTime = LIVENESS_CHECK_INTERVAL;

				while (pinging) {

					log.info("Sending Ping to executor...");

					try {
						sendMessage(new RPingExecutorMessage(getRoleId(), executorRole));
						RPongExecutorMessage pong = waitForIncomingMessage(5000, RPongExecutorMessage.class);

						log.info("Pong received. Executor still there.");

						if (!pong.getNodeInfo().canExecute(appInfo.getRequirements())) {
							Date time = new Date();
							if (firstTimeAppRequirementsNotMet == null)
								firstTimeAppRequirementsNotMet = time;

							// now sleep time 1 second
							sleepTime = 1000;

							// when virtualized, it can happen that executor
							// dows not meet CPU requirements. Do not look for
							// another executor in this case
							if (pong.getNodeInfo().isVirtualized()) {
								log.info("Executor for app %s does not meet requirements for %d seconds but is virtualized", appInfo,
										(time.getTime() - firstTimeAppRequirementsNotMet.getTime()) / 1000);
							} else {
								log.info("Executor for app %s does not meet requirements for %d seconds", appInfo,
										(time.getTime() - firstTimeAppRequirementsNotMet.getTime()) / 1000);

								if (firstTimeAppRequirementsNotMet.before(DateUtils.addMilliseconds(time,
										-EXECUTOR_REQUIREMENTS_EXCEEDING_INTERVAL))) {

									try {
										sendMessage(new RExecutorFailedRequirementsMessage(getRoleId(), getRoleId()));
									} catch (RRoleNotFoundException | RTimeoutException | REnsembleNotFoundException e2) {
										log.error("Could not send message to myself: %s", e2.getMessage());
									}
									pinging = false;
								}
							}

						} else {
							firstTimeAppRequirementsNotMet = null;
							sleepTime = LIVENESS_CHECK_INTERVAL;
							// BEGIN ZIMORY INTEGRATION
							if (pong.getNodeInfo().isVirtualized()) {
								Date time = new Date();
								if (lastNodeCheck == null)
									lastNodeCheck = time;

								if (lastNodeCheck.before(DateUtils.addMilliseconds(new Date(),
										-ZIMORY_CHECK_VIRTUALIZED_NODE_NEEDED_INTERVAL))) {
									SCPNode self = getStrategy().getSCPNode();

									if (self.getPastryNode().isClosest(
											(rice.pastry.Id) self.getEnvironment().createIdHashFromArbitraryString(appInfo.getName()))) {
										// We are closest and clearly initiator
										// hence we are in the initiator role
										Id executorId = executorRole.getNodeId();

										if (executorId != null) {
											for (NodeInfo nodeInfoToCheck : getStrategy().getGossiplyKnownNodes()) {
												NodeInfo executorInfo = getStrategy().nodeInfoSuitableForExecution(appInfo,
														nodeInfoToCheck, executorId);
												if (executorInfo != null) {
													try {
														RoleId stopperRole = createLocalRoleInstance(DeploymentStopperRole.class);

														sendMessage(new RStopDeploymentMessage(getRoleId(), stopperRole, appInfo,
																executorInfo));
													} catch (RRoleCreationException e) {
													}
												}
											}
										}
									}
									lastNodeCheck = time;
								}
							}
							// END ZIMORY INTEGRATION
						}

					} catch (RRoleNotFoundException | RTimeoutException | REnsembleNotFoundException e1) {

						log.info("Got an exception while pinging. Aborting: %s", e1.getMessage());
						// send message to myself about this
						try {
							sendMessage(new RExecutorFailedMessage(getRoleId(), getRoleId()));
						} catch (RRoleNotFoundException | RTimeoutException | REnsembleNotFoundException e2) {
							log.error("Could not send message to myself: %s", e2.getMessage());
						}
						pinging = false;
						return;
					}

					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		};

		new Thread(r, "Initiator Pinger Thread").start();

	}

	private void stopPinging(RoleId executorRole) {
		pinging = false;
	}

	private Set<NodeInfo> getAllNodeInfosAvailableIncludingOurselves() {

		Set<NodeInfo> nodeInfos = getStrategy().getGossiplyKnownNodes();
		Set<NodeInfo> nodesInHere = new HashSet<>();
		nodesInHere.addAll(nodeInfos);
		nodesInHere.add(getStrategy().getNodeInfo());
		return nodesInHere;
	}

	private Set<NodeInfo> getMatchingNodesFromGossipStorage(Requirements appReqs) {

		Set<NodeInfo> nodeInfos = getStrategy().getGossiplyKnownNodes();
		Set<NodeInfo> matchingNodes = new HashSet<>();

		for (NodeInfo rNodeInfo : nodeInfos) {
			if (rNodeInfo.canExecute(appReqs))
				matchingNodes.add(rNodeInfo);
		}
		return matchingNodes;
	}

	@Override
	public void stop() {

		try {
			log.info("STOP called on initiator.");
			sendMessage(new RInternalInitiatorShutdownMessage(getRoleId(), getRoleId()));
			log.info("Sent internal shutdown message.");
		} catch (RRoleNotFoundException | RTimeoutException | REnsembleNotFoundException e) {
			log.error("Could not properly shut down initiator: %s", e.getMessage());
		}
	}

	public synchronized boolean isUp() {
		return isUp;
	}

	public synchronized void setShouldLookForExecutor(boolean shouldLookForExecutor) {
		this.shouldLookForExecutor = shouldLookForExecutor;
	}
}
