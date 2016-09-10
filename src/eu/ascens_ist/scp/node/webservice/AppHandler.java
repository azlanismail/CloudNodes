/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.webservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import eu.ascens_ist.scp.node.NodeBundleActivator;
import eu.ascens_ist.scp.node.core.SCPNode;
import eu.ascens_ist.scp.node.core.exceptions.IncorrectUICommandException;
import eu.ascens_ist.scp.node.core.exceptions.SCPException;
import eu.ascens_ist.scp.node.core.strategy.IStrategy;
import eu.ascens_ist.scp.node.info.AppInfo;
import eu.ascens_ist.scp.node.info.Requirements;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * 
 * Handler for web services concerning applications
 * 
 * @author A. Dittrich
 *
 */
public class AppHandler extends AbstractHandler {
	private static Logger log;

	private SCPNode node;

	public AppHandler(SCPNode node) {
		super();

		log = LogFactory.get(node.getId() + " APPHANDLER");

		this.node = node;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);

		log.info(target);
		if (target.equals("/uploadApp.html")) {
			this.uploadApp(request, response);
		} else if (target.substring(0, 7).contains("/delete")) {
			this.deleteApp(target, request, response);
		} else if (target.substring(0, 5).contains("/info")) {
			this.appInfo(target, request, response);
		} else {
			this.appRequest(target, request, response);
		}
	}

	private void uploadApp(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/*
		 * An app must be uploaded as a JAR. The requirements must be in this jar with a file ending of .req.
		 */
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			renderError(request, response, "Do select a file please. (!isMultipart)");
			return;
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<?> fileItems = upload.parseRequest(request);
			if (fileItems == null || fileItems.size() != 1) {
				renderError(request, response, "Do select a file please. " + fileItems.size());
				return;
			}

			FileItem firstFile = (FileItem) fileItems.get(0);
			if (firstFile.getName().trim().equals("")) {
				renderError(request, response, "Filename is empty. " + fileItems.size());
				return;
			}

			if (!firstFile.getName().endsWith(".jar")) {
				renderError(request, response, "Please choose a jar-file. ");
				return;
			}

			byte[] bytes = firstFile.get();

			JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(bytes));
			JarEntry entry = null;

			// Get name of the app.
			// TODO apparently the file name must match the app name inside the JAR (right?)
			String appName = firstFile.getName().replace(".jar", "");
			if (appName.indexOf("_") != -1)
				appName = appName.substring(0, appName.indexOf("_"));

			byte[] reqBytes = null;

			while ((entry = (JarEntry) jarStream.getNextEntry()) != null) {
				String entryName = entry.getName();

				if (entryName.endsWith(".req")) {

					ByteArrayOutputStream out = new ByteArrayOutputStream();

					byte[] byteBuff = new byte[4096];
					int bytesRead = 0;
					while ((bytesRead = jarStream.read(byteBuff)) != -1) {
						out.write(byteBuff, 0, bytesRead);
					}

					reqBytes = out.toByteArray();
					out.close();

				}

				jarStream.closeEntry();
			}

			jarStream.close();

			if (reqBytes != null) {
				// create and store SCPGCPastAppData
				this.node.getStrategy().handleDeployApplication(appName, new Requirements(reqBytes), bytes);

			} else {
				// No .req file!
				renderError(request, response, "Upload of App not successful, because there is no requirementfile in the jar. ");
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Redirect to home page
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		response.sendRedirect("/");
		return;
	}

	private void appRequest(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String[] tmp = target.split("/");
		String appName = tmp[1];
		String appTarget = "";
		for (int i = 2; i < tmp.length; i++) {
			appTarget += tmp[i] + "/";
		}

		String appResult;
		try {
			IStrategy strategy = this.node.getStrategy();
			AppInfo appInfo = strategy.getAppInfo(appName);
			if (appInfo == null)
				throw new IncorrectUICommandException("Could not find appinfo for app name " + appName);

			appResult = strategy.appRequest(appInfo, appTarget, request, response);
			if (appResult == null) {
				renderError(request, response, String.format("Application %s has not produced any output.", appName));
				return;
			}
		} catch (SCPException e) {
			renderError(request, response, String.format("Application %s has not produced error output: %s.", appName, e.getMessage()));
			return;
		}

		// Write as string
		OutputStreamWriter outputStream = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		outputStream.write(appResult);
		outputStream.close();
	}

	private void deleteApp(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String appName = target.substring(8);
		try {
			this.node.getStrategy().handleUndeployApplication(appName);
		} catch (IncorrectUICommandException e) {
			renderError(request, response, e.getMessage());
			return;
		}
		response.sendRedirect("/");
	}

	private void appInfo(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String appName = target.substring(6);

		URL resource = NodeBundleActivator.getContext().getBundle().getResource("res/html/appInfo.html");

		InputStream input = resource.openStream();
		ServletOutputStream output = response.getOutputStream();

		VelocityContext context = new VelocityContext();

		IStrategy strategy = this.node.getStrategy();
		AppInfo appInfo = strategy.getAppInfo(appName);
		if (appInfo == null) {
			renderError(request, response, "There are no information about the requested application.");
		}

		// put info into a context object
		context.put("localNode", this.node.getNodeInfo());
		context.put("appInfo", appInfo);
		context.put("requirements", appInfo.getRequirementProperties().entrySet());

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		OutputStreamWriter outputStream = new OutputStreamWriter(output, "UTF-8");
		Velocity.evaluate(context, outputStream, "scp.core", new InputStreamReader(input, "UTF-8"));
		outputStream.close();
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
