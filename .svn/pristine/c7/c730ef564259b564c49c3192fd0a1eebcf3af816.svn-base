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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.NodeBundleActivator;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

public class NodeHandler extends AbstractHandler {
	private static Logger log;

	private SCPNode node;

	public NodeHandler(SCPNode node) {
		super();

		log = LogFactory.get(node.getId() + " NODEHANDLER");

		this.node = node;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		log.info(target);

		this.nodeInfo(target, request, response);

	}

	private void nodeInfo(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String nodeId = target.substring(1);

		Id id = this.node.getEnvironment().rebuildIdObjectFromGivenIdString(nodeId);
		if (id == null) {
			renderError(request, response, "No id provided.");
			return;
		}

		URL resource = NodeBundleActivator.getContext().getBundle().getResource("res/html/nodeInfo.html");

		InputStream input = resource.openStream();
		ServletOutputStream output = response.getOutputStream();

		VelocityContext context = new VelocityContext();

		NodeInfo info = this.node.getStrategy().getNeighbourInformation(id);
		if (info == null) {
			renderError(request, response, "The required information is not available at the moment.");
			return;
		}

		context.put("localNode", this.node.getNodeInfo());
		context.put("addressString", info.getAddressString());
		context.put("port", info.getPort());
		context.put("FullIdString", info.getFullIdString());
		context.put("location", "Munich");
		context.put("CpuModel", info.getCpuModel());
		context.put("CpuCores", info.getCpuCores());
		context.put("CpuSpeed", info.getCpuSpeed());
		context.put("CpuLoad", info.getCpuLoad());
		context.put("MemTotal", info.getMemTotal());
		context.put("MemUsed", info.getMemUsed());
		context.put("MemFree", info.getMemFree());
		context.put("node", info);
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		OutputStreamWriter os = new OutputStreamWriter(output, "UTF-8");
		Velocity.evaluate(context, os, "scp.core", new InputStreamReader(input, "UTF-8"));
		os.close();
	}

	private void renderError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		log.warn("%s %s: %s", request.getMethod(), request.getRequestURI(), message);

		URL resource = NodeBundleActivator.getContext().getBundle().getResource("res/html/error.html");

		InputStream input = resource.openStream();
		ServletOutputStream output = response.getOutputStream();

		VelocityContext context = new VelocityContext();
		context.put("errorMessage", message);

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		OutputStreamWriter outputStream = new OutputStreamWriter(output, "UTF-8");
		Velocity.evaluate(context, outputStream, "scp.core", new InputStreamReader(input, "UTF-8"));
		outputStream.close();
	}

}
