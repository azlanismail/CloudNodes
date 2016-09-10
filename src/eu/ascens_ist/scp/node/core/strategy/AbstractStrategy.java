/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy;

import static eu.ascens_ist.scp.node.Configuration.ZIMORY_DEFAULT_CPU_COUNT;
import static eu.ascens_ist.scp.node.Configuration.ZIMORY_DEFAULT_RAM_MB;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Id.Distance;
import rice.p2p.commonapi.Message;
import eu.ascens_ist.scp.iaas.zimory.ZimoryConnection;
import eu.ascens_ist.scp.iaas.zimory.ZimoryDeployment;
import eu.ascens_ist.scp.iaas.zimory.ZimoryDeploymentState;
import eu.ascens_ist.scp.iaas.zimory.ZimoryException;
import eu.ascens_ist.scp.node.Configuration;
import eu.ascens_ist.scp.node.NodeEnvironment;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.info.NodeLocation;
import eu.ascens_ist.scp.node.info.Requirements;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;
import eu.ascens_ist.scp.node.messages.ShutdownNodeRequest;
import eu.ascens_ist.scp.node.webservice.SCPAPICaller;

/**
 * 
 * 
 * @author A. Dittrich
 * 
 */
public abstract class AbstractStrategy implements IStrategy {
	public static final String ROLE_INITIATOR = "Initiator";
	public static final String ROLE_EXECUTOR = "Executor";

	private Logger abstractStrategyLog;

	protected SCPNode node;

	@Override
	public void initialize(SCPNode scpNode) {
		abstractStrategyLog = LogFactory.get(scpNode.getId() + " STRATEGY");

		this.node = scpNode;
	}

	@Override
	public void updateNodeInformation() {
	}

	@Override
	public void handleShutdown() {
	}

	@Override
	public NodeInfo getNodeInfo() {
		return node.getNodeInfo();
	}

	@Override
	public SCPNode getSCPNode() {
		return node;
	}

	@Override
	public void handleDirectIncomingMessage(Id id, Message message) {
		if (message instanceof ShutdownNodeRequest) {
			this.node.shutdown();
		}
	}

	@Override
	public ZimoryDeployment createZimoryDeploymentForApp(AppInfo appInfo, Logger log, String roleName) {
		if (!Configuration.IS_LOCAL_TEST && Configuration.validIaasConnection()) {
			Requirements req = appInfo.getRequirements();
			int cpuRequested = (int) req.getCPUCores();
			double cpuLoadRequested = req.getCPULoad();
			int memRequested = (int) req.getTotalMemory();
			int cpu = Math.max(cpuRequested, ZIMORY_DEFAULT_CPU_COUNT);
			int mem = Math.max(memRequested, ZIMORY_DEFAULT_RAM_MB);
			NodeLocation locationRequested = req.getLocations().get(0);

			getNodeInfo().addRoleForApp(appInfo, roleName, "Creating deployment");

			try {
				String host = Configuration.ZIMORY_API_IP;
				String apiPath = Configuration.ZIMORY_BASE_API_STRING;
				URL certFileUrl = Configuration.ZIMORY_CERT_PATH;
				String passwd = Configuration.ZIMORY_CERT_PASS;
				int applianceId = Configuration.ZIMORY_APPLIANCE_ID;
				int networkId = Configuration.ZIMORY_NETWORK_ID;
				ZimoryConnection conn = new ZimoryConnection(host, apiPath, certFileUrl, passwd, applianceId, networkId);
				ZimoryDeployment deployment = conn.createDeployment(cpu, mem);

				if (deployment != null) {
					log.info("Deployment \"" + deployment.getName() + "\" created");
					getNodeInfo().addRoleForApp(appInfo, roleName, "Waiting for deployment to start");

					while ((deployment.getState() != ZimoryDeploymentState.RUNNING) || deployment.getInternalIP().equals("")) {
						log.info("Deployment is not running or has no internal IP");
						try {
							Thread.sleep(10000);
							deployment.update();
						} catch (ZimoryException | InterruptedException e) {
						}
					}

					log.info("Deployment is running, waiting for SCPi to start");
					getNodeInfo().addRoleForApp(appInfo, roleName, "Waiting for SCPi to start");

					try {
						InetAddress bootstrapAddress = this.node.getBaseAddress();
						if (bootstrapAddress.isLoopbackAddress()) {
							bootstrapAddress = NodeEnvironment.getNonLoopbackAddress();
						}

						int scpPort = 9000;
						boolean deploymentSCPIsClosestToApp = true;
						boolean firstTry = true;

						do {
							if (!firstTry) {
								if (deploymentSCPIsClosestToApp)
									log.info("SCP is running but would be closest to App and therefore new initiator. Starting new instance on next port.");
								int oldPort = scpPort;
								scpPort++;
								String response = new SCPAPICaller(deployment.getInternalIP(), oldPort).startLocalNode(scpPort, false);
								if (response.equals("false"))
									continue;

								getSCPNode().getEnvironment().sleep(1000);

								try {
									new SCPAPICaller(deployment.getInternalIP(), oldPort).shutdown();
								} catch (IOException e) {
								}
							}
							boolean alive = false;
							try {
								alive = new SCPAPICaller(deployment.getInternalIP(), scpPort).getAlive();
							} catch (IOException e) {
							}
							Date maxDate = DateUtils.addSeconds(new Date(), 90); // wait max 1.5 min for SCP
							while (!alive && new Date().before(maxDate)) {
								try {
									log.info("SCP is not running");
									getSCPNode().getEnvironment().sleep(1000);

									alive = new SCPAPICaller(deployment.getInternalIP(), scpPort).getAlive();
								} catch (IOException e) {
								}
							}

							Id deploymentSCPId = new SCPAPICaller(deployment.getInternalIP(), scpPort).getId();

							Id appId = getSCPNode().getEnvironment().createIdHashFromArbitraryString(appInfo.getName());
							Id nodeId = this.node.getId();

							Distance appToDeployment = ((rice.pastry.Id) appId).distanceFromId(deploymentSCPId);
							Distance appToInitiator = ((rice.pastry.Id) appId).distanceFromId(nodeId);
							deploymentSCPIsClosestToApp = appToDeployment.compareTo(appToInitiator) < 0;

							firstTry = false;
						} while (deploymentSCPIsClosestToApp);

						log.info("SCP is running, injecting credentials");
						getNodeInfo().addRoleForApp(appInfo, roleName, "Injecting credentials");

						SCPAPICaller caller = new SCPAPICaller(deployment.getInternalIP(), scpPort);

						caller.setDeploymentId(deployment.getId());
						caller.setApplianceId(Configuration.ZIMORY_APPLIANCE_ID);
						caller.setNetworkId(Configuration.ZIMORY_NETWORK_ID);
						caller.setLocation(locationRequested);
						if (cpuLoadRequested > 0.0)
							caller.setCPULoad((100 - cpuLoadRequested) * 0.9);
						caller.connect(bootstrapAddress.getHostAddress(), this.node.getBasePort());
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}

						return deployment;
					} catch (IOException e) {
					}
				} else
					log.error("Deployment could not be created");
			} catch (UnknownHostException | ZimoryException e1) {
				log.error("Connection to Zimory Server could not be established");
			}
		}
		return null;
	}

	@Override
	public NodeInfo nodeInfoSuitableForExecution(AppInfo appInfo, NodeInfo nodeInfoToCheck, Id executorId) {
		if (!getNodeInfo().getId().equals(nodeInfoToCheck.getId()) && !executorId.equals(nodeInfoToCheck.getId())) {

			// if node can execute and is not virtualized, check if executor is virtualized.
			if (nodeInfoToCheck != null && !nodeInfoToCheck.isVirtualized() && nodeInfoToCheck.canExecute(appInfo.getRequirements())) {
				NodeInfo execInfo = getNeighbourInformation(executorId);
				if (execInfo != null) {

					// if executor is still virtualized (why should this change? you never know...), return info
					if (execInfo.isVirtualized()) {
						return execInfo;
					}
				}
			}
		}
		return null;
	}

	/**
	 * should be called in a new thread since it is waiting until deployment is
	 * deleted
	 * 
	 * @throws UnknownHostException
	 * @throws ZimoryException
	 */
	@Override
	public void stopZimoryDeployment(int deploymentId, Logger log, String roleName) {
		if (Configuration.validIaasConnection() && deploymentId > 0) {
			try {
				String host = Configuration.ZIMORY_API_IP;
				String apiPath = Configuration.ZIMORY_BASE_API_STRING;
				URL certFileUrl = Configuration.ZIMORY_CERT_PATH;
				String passwd = Configuration.ZIMORY_CERT_PASS;
				int applianceId = Configuration.ZIMORY_APPLIANCE_ID;
				int networkId = Configuration.ZIMORY_NETWORK_ID;
				ZimoryConnection connection = new ZimoryConnection(host, apiPath, certFileUrl, passwd, applianceId, networkId);
				boolean stopped = connection.stopDeployment(deploymentId);
				if (stopped)
					log.info("Deployment %s stopped.", deploymentId);
				else
					log.info("Deployment %s could not be stopped.", deploymentId);
				if (stopped) {
					log.info("Wait until deployment %s is stopped.", deploymentId);
					try {
						while ((connection.getDeploymentState(deploymentId) != ZimoryDeploymentState.STOPPED)) {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
							}
							log.info("Deployment %s is still not stopped.", deploymentId);
						}
						if (connection.deleteDeployment(deploymentId))
							log.info("Deployment %s was deleted.", deploymentId);
						else
							log.info("Deployment %s could not be deleted.", deploymentId);

					} catch (ZimoryException e) {
					}
				}
			} catch (UnknownHostException | ZimoryException e) {
				log.error("Connection to Zimory Server could not be established");
			}
		}
	}

	public void sendShutdownRequest(Id id) {
		node.sendMessage(new ShutdownNodeRequest(node.getId(), id), id);
	}

	@Override
	public void bootedIntoRing() {
		abstractStrategyLog.info("%s booted into pastry ring", this.node.getId());
	}
}
