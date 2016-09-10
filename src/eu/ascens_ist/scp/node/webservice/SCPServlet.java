/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import eu.ascens_ist.scp.node.NodeBundleActivator;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.info.NodeLocation;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * Servlet for SCPi web interface.
 * 
 * @author A. Zeblin, A. Dittrich
 *
 */
public class SCPServlet extends HttpServlet {
	private static Logger log;

	private static final long serialVersionUID = 1L;

	private SCPNode node;

	private static String shutdownButtonText = "Shutdown this node";
	private static String setCPULoadText = "Set CPU Load";
	private static String setFullLoadText = "Set full CPU Load";
	private static String setLocationText = "Set Location";
	private static String setVirtualizedText = "Set Virtualized";
	private static String setDeploymentIdText = "Set Deployment ID";
	private static String stopDeploymentText = "Stop Deployment";
	private static String resetNodeInfoText = "Reset Node Information";

	public SCPServlet(SCPNode scpNode) {
		super();

		log = LogFactory.get(scpNode.getId() + " SCPSERVLET");

		this.node = scpNode;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGetPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGetPost(req, resp);
	}

	private void doGetPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String target = request.getServletPath();
		log.info(target);

		if (target.equals("/") || target.equals("/index.html")) {
			URL resource = NodeBundleActivator.getContext().getBundle().getResource("res/html/index.html");

			InputStream input = resource.openStream();
			ServletOutputStream output = response.getOutputStream();

			VelocityContext context = new VelocityContext();

			context.put("localNode", this.node.getNodeInfo());
			context.put("knownApps", this.node.getStrategy().getAllKnownApps());
			context.put("locations", NodeLocation.values());
			context.put("shutdownButtonText", shutdownButtonText);
			context.put("setCPULoadText", setCPULoadText);
			context.put("setFullLoadText", setFullLoadText);
			context.put("setLocationText", setLocationText);
			context.put("setVirtualizedText", setVirtualizedText);
			context.put("setDeploymentIdText", setDeploymentIdText);
			context.put("stopDeploymentText", stopDeploymentText);
			context.put("resetNodeInfoText", resetNodeInfoText);

			List<NodeInfo> neighbors = this.node.getStrategy().getNeighbourInformations();
			Collections.sort(neighbors);
			context.put("knownNodes", neighbors);

			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);

			OutputStreamWriter outputStream = new OutputStreamWriter(output, "UTF-8");
			Velocity.evaluate(context, outputStream, "scp.core", new InputStreamReader(input, "UTF-8"));
			outputStream.close();
		}

	}
}
