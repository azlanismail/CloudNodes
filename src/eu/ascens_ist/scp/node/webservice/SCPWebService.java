/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.webservice;

import org.apache.velocity.app.Velocity;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import eu.ascens_ist.scp.node.NodeBundleActivator;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * 
 * Class for web services offered by each SCPi
 * 
 * @author A. Dittrich
 *
 */
public class SCPWebService {
	private static Logger log;

	private SCPNode node;

	private Server server;

	public SCPWebService(SCPNode node) {
		super();
		log = LogFactory.get(node.getId() + " WEBSERVICE");
		this.node = node;
	}

	public void activate() {
		int port = this.node.getBasePort() + 1000;

		this.server = new Server(port);

		/*
		 * RessourceHandler
		 */
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setResourceBase(NodeBundleActivator.getContext().getBundle().getResource("res").toString());
		ContextHandler resContext = new ContextHandler();
		resContext.setContextPath("/res");
		resContext.setHandler(resourceHandler);

		ContextHandler resContextNode = new ContextHandler();
		resContextNode.setContextPath("/node/res");
		resContextNode.setHandler(resourceHandler);

		ContextHandler resContextApp = new ContextHandler();
		resContextApp.setContextPath("/app/res");
		resContextApp.setHandler(resourceHandler);

		ContextHandler resContextAppInfo = new ContextHandler();
		resContextAppInfo.setContextPath("/app/info/res");
		resContextAppInfo.setHandler(resourceHandler);

		/*
		 * AppHandler
		 */
		ContextHandler appHandler = new ContextHandler();
		appHandler.setContextPath("/app");
		appHandler.setHandler(new AppHandler(this.node));

		/*
		 * NodeHandler
		 */
		ContextHandler nodeHandler = new ContextHandler();
		nodeHandler.setContextPath("/node");
		nodeHandler.setHandler(new NodeHandler(this.node));

		/*
		 * APIHandler
		 */
		ContextHandler apiHandler = new ContextHandler();
		apiHandler.setContextPath("/api");
		apiHandler.setHandler(new APIHandler(this.node));

		/*
		 * SCPServlet
		 */
		ServletContextHandler interfaceHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		interfaceHandler.setContextPath("/");
		interfaceHandler.addServlet(new ServletHolder(new SCPServlet(this.node)), "/");

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { resContext, resContextNode, resContextApp, resContextAppInfo, appHandler, apiHandler, nodeHandler, interfaceHandler, new DefaultHandler() });
		this.server.setHandler(contexts);

		Velocity.init();

		try {
			this.server.start();
			log.info("Web Service started");
			for (Connector connector : server.getConnectors()) {
				log.info("Web Service for node " + this.node.getId() + " is accessible at http://" + this.node.getBaseAddress().getHostAddress() + ":" + connector.getLocalPort());
			}
		} catch (Exception e) {
			log.error("Web Service could not be started");
		}
	}

	public void deactivate() {

		if (this.server != null) {
			try {
				try {
					this.server.stop();
				} catch (InterruptedException e) {
				}

				this.server.destroy();

			} catch (Exception e) {
				log.error("Server deactivation failed: %s.", e);
			}
		}

		this.server = null;
	}
}
