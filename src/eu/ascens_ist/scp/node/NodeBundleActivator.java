/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.ascens_ist.scp.node.core.exceptions.NodeEnvironmentException;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * The activator.
 * 
 * @author A. Zeblin, A. Dittrich
 * 
 */
public class NodeBundleActivator implements BundleActivator {

	private static Logger log = LogFactory.get("BUNDLEACTIVATOR");

	private static BundleContext context;

	public static String BUNDLE_NAME = "eu.ascens_ist.cloud.node";

	public static String getBundleName() {
		return BUNDLE_NAME;
	}

	public static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
		log.info("***** Starting bundle %s... *****", context.getBundle().getSymbolicName());

		/*
		URL configFileUrl = NodeBundleActivator.getContext().getBundle().getResource("scpi.conf");
		if (configFileUrl != null) {
			String config;
			try {
				config = IOUtils.toString(configFileUrl.openStream(), StandardCharsets.UTF_8);
				Configuration.readConfigurationString(config);
			} catch (IOException e) {
				log.error("Configuration file could not be read");
			}
		} else {
			log.error("No configuration file present");
		}*/

		try {
			File configFile = new File("scpi.conf");
			String config = IOUtils.toString(new FileInputStream(configFile), StandardCharsets.UTF_8);
			Configuration.readConfigurationString(config);
		} catch (IOException e) {

			try {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				URL url = loader.getResource("scpi.conf");
				if (url != null) {
					String config = IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
					Configuration.readConfigurationString(config);
				}
			} catch (IOException e1) {
				log.error("Configuration file could not be read");
			}
		}

		if (Configuration.IS_LOCAL_TEST)
			log.info("IS LOCAL TEST RUN");

		NodeEnvironment nodeEnvironment = NodeEnvironment.get();
		if (nodeEnvironment != null) {
			//			throw new RuntimeException("Assumption invalid: At start of the bundle, the node environment has to be null.");
		}

		try {
			NodeEnvironment.start();
		} catch (NodeEnvironmentException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		log.info("***** Bundle %s started *****", context.getBundle().getSymbolicName());
	}

	public static Object getService(String serviceName) {
		Object service = null;
		BundleContext context = NodeBundleActivator.getContext();
		ServiceReference<?> reference = context.getServiceReference(serviceName);
		if (reference != null) {
			service = context.getService(reference);
		}
		return service;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		log.info("***** Stopping bundle %s... *****", context.getBundle().getSymbolicName());
		NodeEnvironment nEnv = NodeEnvironment.get();
		if (nEnv != null)
			nEnv.stop();
		log.info("***** Bundle %s stopped *****", context.getBundle().getSymbolicName());
		NodeBundleActivator.context = null;
	}
}
