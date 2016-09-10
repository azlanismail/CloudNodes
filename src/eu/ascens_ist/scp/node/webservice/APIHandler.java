/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.webservice;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.Configuration;
import eu.ascens_ist.scp.node.NodeEnvironment;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.core.exceptions.NodeEnvironmentException;
import eu.ascens_ist.scp.node.info.NodeLocation;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * Handler for API requests on SCPi.
 * 
 * @author A. Dittrich
 *
 */
public class APIHandler extends AbstractHandler {
	private static Logger log;

	private SCPNode node;

	public APIHandler(SCPNode node) {
		super();

		log = LogFactory.get(node.getId() + " APIHANDLER");

		this.node = node;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		log.info(request.getMethod() + ": " + target);

		if (request.getMethod().equals("POST")) {
			if (target.equals("/connect")) { // connect local instance to remote node
				this.connectToNode(request);

			} else if (target.equals("/startLocalNode")) { // start new local node at port given in request
				Id newNodeId = this.startLocalNode(request);

				String message = "false";
				if (newNodeId != null)
					message = newNodeId.toStringFull();

				this.writeInResponse(response, message);

			} else if (target.equals("/stopDeployment")) {
				this.stopDeploymen();

			} else if (target.equals("/shutdown")) {
				this.shutdownNode(request);

			} else if (target.equals("/deploymentId")) {
				if (request.getParameter("deploymentId") != null) {
					int deploymentId = Integer.parseInt(request.getParameter("deploymentId"));
					this.setDeploymentId(deploymentId);
				}

			} else if (target.equals("/location")) {
				if (request.getParameter("location") != null) {
					String location = request.getParameter("location");
					this.setLocation(location);
				}

			} else if (target.equals("/cpuLoad")) {
				if (request.getParameter("cpuLoad") != null) {
					double load = Double.parseDouble(request.getParameter("cpuLoad"));
					this.setCpuLoad(load);
				}

			} else if (target.equals("/freeMemory")) {
				if (request.getParameter("freeMemory") != null) {
					long memFree = Long.parseLong(request.getParameter("freeMemory"));
					this.setFreeMemory(memFree);
				}

			} else if (target.equals("/freeDisk")) {
				if (request.getParameter("freeDisk") != null) {
					long diskFree = Long.parseLong(request.getParameter("freeDisk"));
					this.setFreeDisk(diskFree);
				}

			} else if (target.equals("/virtualized")) {
				if (request.getParameter("virtualized") != null) {
					String param = request.getParameter("virtualized");
					boolean virtualized = param != null ? param.equals("true") || param.equals("1") : false;
					this.setVirtualized(virtualized);
				}

			} else if (target.equals("/applianceId")) {
				if (request.getParameter("applianceId") != null) {
					int applianceId = Integer.parseInt(request.getParameter("applianceId"));
					this.setApplianceId(applianceId);
				}

			} else if (target.equals("/networkId")) {
				if (request.getParameter("networkId") != null) {
					int networkId = Integer.parseInt(request.getParameter("networkId"));
					this.setNetworkId(networkId);
				}

			} else if (target.equals("/resetNode")) {
				this.resetNode();

			}
		} else if (request.getMethod().equals("GET")) {

			if (target.equals("/alive")) {
				String message = new Boolean(this.node.isAlive()).toString();
				this.writeInResponse(response, message);

			} else if (target.equals("/id")) {
				String message = this.node.getId().toStringFull();
				this.writeInResponse(response, message);

			} else if (target.equals("/neighborCount")) {
				String message = "" + this.node.getStrategy().getNeighbourInformations().size();
				this.writeInResponse(response, message);

			} else if (target.equals("/deploymentId")) {
				String message = "" + this.node.getNodeInfo().getDeploymentId();
				this.writeInResponse(response, message);

			} else if (target.equals("/location")) {
				String message = "" + this.node.getNodeInfo().getLocation();
				this.writeInResponse(response, message);

			} else if (target.equals("/cpuCores")) {
				String message = "" + this.node.getNodeInfo().getCpuCores();
				this.writeInResponse(response, message);

			} else if (target.equals("/cpuSpeed")) {
				String message = "" + this.node.getNodeInfo().getCpuSpeed();
				this.writeInResponse(response, message);

			} else if (target.equals("/cpuLoad")) {
				String message = "" + this.node.getNodeInfo().getCpuLoad();
				this.writeInResponse(response, message);

			} else if (target.equals("/freeMemory")) {
				String message = "" + this.node.getNodeInfo().getMemFree();
				this.writeInResponse(response, message);

			} else if (target.equals("/totalMemory")) {
				String message = "" + this.node.getNodeInfo().getMemTotal();
				this.writeInResponse(response, message);

			} else if (target.equals("/freeDisk")) {
				String message = "" + this.node.getNodeInfo().getDiskFree();
				this.writeInResponse(response, message);

			} else if (target.equals("/totalDisk")) {
				String message = "" + this.node.getNodeInfo().getDiskTotal();
				this.writeInResponse(response, message);

			} else if (target.equals("/virtualized")) {
				String message = "" + this.node.getNodeInfo().isVirtualized();
				this.writeInResponse(response, message);

			}
		}

		response.setStatus(HttpServletResponse.SC_OK);
	}

	private void writeInResponse(HttpServletResponse response, String string) throws IOException {
		ServletOutputStream output = response.getOutputStream();

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html;charset=utf-8");
		output.write(string.getBytes(Charset.forName("UTF-8")));
		output.close();
	}

	private void connectToNode(HttpServletRequest request) throws IOException {
		String ip = request.getParameter("ip");

		int port = -1;

		try {
			port = Integer.parseInt(request.getParameter("port"));
		} catch (Exception e) {

		}
		log.info("Connect to %s:%d.", ip, port);
		node.boot(ip, port);
	}

	private Id startLocalNode(HttpServletRequest request) throws IOException {
		String portParam = request.getParameter("port");
		String connectToLocalParam = request.getParameter("connectToLocal");
		String nodeIdParam = request.getParameter("nodeId");

		SCPNode bootNode = null;
		boolean connectToLocal = true;
		if (connectToLocalParam != null) {
			connectToLocal = Boolean.parseBoolean(connectToLocalParam);
		}
		if (connectToLocal)
			bootNode = this.node;

		Id nodeId = null;
		if (nodeIdParam != null)
			nodeId = this.node.getEnvironment().rebuildIdObjectFromGivenIdString(nodeIdParam);

		log.info("Start new local node at port %s.", portParam);
		try {
			NodeEnvironment env = this.node.getEnvironment();
			SCPNode node = null;
			if (portParam != null) {
				int port = Integer.parseInt(portParam);
				node = env.createNode(bootNode, port, nodeId);
			} else {
				node = env.createNode(bootNode, nodeId);
			}

			return node.getId();

		} catch (NodeEnvironmentException e) {
			return null;
		}
	}

	public void setCpuLoad(double load) {
		log.info("Set CPU load to %.1f%%.", load);
		node.getNodeInfo().setCpuLoad(load);
	}

	public void setFreeMemory(long memFree) {
		log.info("Set free memory to %.1f%%.", memFree);
		node.getNodeInfo().setMemFree(memFree);
	}

	public void setFreeDisk(long diskFree) {
		log.info("Set free disk space to %.1f%%.", diskFree);
		node.getNodeInfo().setDiskFree(diskFree);
	}

	public void setLocation(String location) {
		log.info("Set location to " + location + ".");
		node.getNodeInfo().setLocation(NodeLocation.getLocationFromString(location));
	}

	public void setVirtualized(boolean virtualized) {
		log.info("Set virtualized state to " + virtualized + ".");
		node.getNodeInfo().setVirtualized(virtualized);
	}

	public void setDeploymentId(int deploymentId) {
		log.info("Set deployment ID to " + deploymentId + ".");
		node.getNodeInfo().setDeploymentId(deploymentId);
	}

	private void setApplianceId(int applianceId) {
		log.info("Set Zimory appliance ID to " + applianceId + ".");
		Configuration.ZIMORY_APPLIANCE_ID = applianceId;
	}

	private void setNetworkId(int networkId) {
		log.info("Set Zimory network ID to " + networkId + ".");
		Configuration.ZIMORY_NETWORK_ID = networkId;
	}

	public void stopDeploymen() {
		log.info("Stop deployment.");
		node.getStrategy().stopZimoryDeployment(this.node.getNodeInfo().getDeploymentId(), log, "");
	}

	public void resetNode() {
		log.info("Reset node information");
		node.getNodeInfo().reset();
	}

	private void shutdownNode(HttpServletRequest request) {
		log.info("Node shutdown.");

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					node.shutdown();
				} catch (Exception e) {
					log.error("Deactivation failed: %s.", e);
				}

			}
		}).start();
	}

}
