/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

import eu.ascens_ist.scp.iaas.zimory.ZimoryConnection;
import eu.ascens_ist.scp.iaas.zimory.ZimoryException;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * Configuration values for the node
 * 
 * @author A. Zeblin, A. Dittrich
 * 
 */
public class Configuration {
	private static Logger log = LogFactory.get("CONFIGURATION");

	/**
	 * Boolean whether SCP is in local test mode
	 */
	public static boolean IS_LOCAL_TEST = false;
	/**
	 * Amount of nodes to be started in this instance
	 */
	public static int START_NODE_COUNT = 1;
	/**
	 * Port number from where to start assigning port to SCPi
	 */
	public static int START_NODE_PORT = 9000;
	/**
	 * IP/Host of MonitorServer to whom nodes send their NodeInfo
	 */
	public static String MONITOR_SERVER_IP = "localhost"; // IS_LOCAL_TEST ? "localhost" : EnvironmentSensor.getVirtualized() ? "172.16.4.2" : "10.153.3.170";
	/**
	 * Port of MonitorServer to whom nodes send their NodeInfo
	 */
	public static int MONITOR_SERVER_PORT = 8181;
	/**
	 * IP/Host of Zimory API
	 */
	public static String ZIMORY_API_IP = ""; // EnvironmentSensor.getVirtualized() ? "10.153.3.181" : "129.187.228.133";
	/**
	 * Path on Zimory Server for API
	 */
	public static String ZIMORY_BASE_API_STRING = ""; // "/ZimoryManage/services/api/";
	/**
	 * Path for certificate for communication with zimory relative to
	 * monitor.jar location
	 */
	public static URL ZIMORY_CERT_PATH = null;
	/**
	 * Password for certificate for communication with zimory relative to
	 * monitor.jar location
	 */
	public static String ZIMORY_CERT_PASS = "";
	/**
	 * ID of Appliances for Deployments in Zimory Software
	 */
	public static int ZIMORY_APPLIANCE_ID = 0;
	/**
	 * ID of Network for Deployments in Zimory Software
	 */
	public static int ZIMORY_NETWORK_ID = 0;
	/**
	 * InetAddress of Node for bootstrapping
	 */
	public static InetAddress BOOTSTRAP_INET_ADRESS = null;
	/**
	 * Port of Node for bootstrapping
	 */
	public static int BOOTSTRAP_PORT = 0;

	/**
	 * Milliseconds how long information about blocked port number is cached
	 */
	public static final int PORT_CACHE_INTERVAL = 5000;
	/**
	 * Milliseconds how long executor can exceed requirements before search is
	 * initiated
	 */
	public static final int EXECUTOR_REQUIREMENTS_EXCEEDING_INTERVAL = 1000 * 15;
	/**
	 * Number of "safety copies" of data in PAST
	 */
	public static final int GCPAST_REPLICATION_FACTOR = 3;
	/**
	 * Interval in milliseconds when Executor starts executing app
	 */
	public static final int STARTING_TIME_FOR_EXECUTER = 1000 * 4;
	/**
	 * Interval in milliseconds how often Initiator checks liveness of eExecutor
	 */
	public static final int LIVENESS_CHECK_INTERVAL = 1000 * 10;
	/**
	 * Interval in milliseconds of expiration of NodeInfo
	 */
	public static final long NODE_INFO_EXPIRATION_INTERVAL = 1000 * 30;

	/**
	 * Number of unsuccessful searches for executor before crdation of
	 * virtualized deployment
	 */
	public static final int ZIMORY_UNSUCCESSFUL_ATTEMPTS_BEFORE_VM_CREATION = 5;
	/**
	 * Number of unsuccessful searches for executor before reinitialisation
	 */
	public static final int ZIMORY_UNSUCCESSFUL_ATTEMPTS_BEFORE_REINITIALISATION = ZIMORY_UNSUCCESSFUL_ATTEMPTS_BEFORE_VM_CREATION * 3;
	/**
	 * Interval between two checks whether virtualized nodes are needed
	 */
	public static final int ZIMORY_CHECK_VIRTUALIZED_NODE_NEEDED_INTERVAL = 1000 * 60;
	/**
	 * Default amount of virtualized memory
	 */
	public static final int ZIMORY_DEFAULT_RAM_MB = 2048;
	/**
	 * Default number of virtualized CPU cores
	 */
	public static final int ZIMORY_DEFAULT_CPU_COUNT = 1;

	private static boolean validZimoryHost = false;
	private static boolean zimoryValidityChecked = false;

	/**
	 * Reads configuration for node from given String
	 * 
	 * @param config
	 *            String representation of configuration: Each line contains one
	 *            configuration pair, key and value separated by a colon
	 */
	public static void readConfigurationString(String config) {
		String[] configPairs = config.split("\n");

		for (String configPair : configPairs) {
			if (configPair.trim().equals("") || configPair == null)
				continue;

			configPair = StringUtils.substringBefore(configPair, "#");

			String[] pair = configPair.split(":");
			if (pair.length != 2)
				continue;

			String key = pair[0].trim();
			String value = pair[1].trim();

			log.info("Value " + value + " for key \"" + key + "\"");

			if (key.equals("LocalTest")) {
				try {
					Boolean localTest = Boolean.parseBoolean(value);
					IS_LOCAL_TEST = localTest;
				} catch (NumberFormatException e) {
					logValueError(key, value, "is no valid bool");
				}
			} else if (key.equals("NodeCount")) {
				Integer count = validateInteger(key, value);
				if (count != null) {
					START_NODE_COUNT = count;
				}
			} else if (key.equals("NodePort")) {
				Integer port = validateInteger(key, value);
				if (port != null) {
					START_NODE_PORT = port;
				}
			} else if (key.equals("MonitorAddress")) {
				InetAddress address = validateInetAddress(key, value);
				if (address != null) {
					MONITOR_SERVER_IP = address.getHostName();
				}
			} else if (key.equals("MonitorPort")) {
				Integer port = validateInteger(key, value);
				if (port != null) {
					MONITOR_SERVER_PORT = port;
				}
			} else if (key.equals("ZimoryServer")) {
				InetAddress address = validateInetAddress(key, value);
				if (address != null) {
					ZIMORY_API_IP = address.getHostName();
				}
			} else if (key.equals("ZimoryAPIPath")) {
				ZIMORY_BASE_API_STRING = value;

			} else if (key.equals("ZimoryCertPath")) {
				try {
					File certFile = new File(value);
					if (certFile.exists())
						ZIMORY_CERT_PATH = certFile.toURI().toURL();
					else {
						ClassLoader loader = Thread.currentThread().getContextClassLoader();
						URL url = loader.getResource(value);
						if (url != null) {
							ZIMORY_CERT_PATH = url;
						} else {
							log.error("Certification file could not be read");
						}
					}
				} catch (IOException e) {
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					URL url = loader.getResource(value);
					if (url != null) {
						ZIMORY_CERT_PATH = url;
					} else {
						log.error("Certification file could not be read");
					}
				}

			} else if (key.equals("ZimoryCertPass")) {
				ZIMORY_CERT_PASS = value;

			} else if (key.equals("ZimoryAppliance")) {
				Integer id = validateInteger(key, value);
				if (id != null) {
					ZIMORY_APPLIANCE_ID = id;
				}
			} else if (key.equals("ZimoryNetwork")) {
				Integer id = validateInteger(key, value);
				if (id != null) {
					ZIMORY_NETWORK_ID = id;
				}
			} else if (key.equals("BootAddress")) {
				InetAddress address = validateInetAddress(key, value);
				if (address != null) {
					BOOTSTRAP_INET_ADRESS = address;
				}
			} else if (key.equals("BootPort")) {
				Integer port = validateInteger(key, value);
				if (port != null) {
					BOOTSTRAP_PORT = port;
				}
			} else {
				log.error("Key \"" + key + "\" is no valid key");
			}
		}

		if (validIaasConnection()) {
			log.info("Configuration has valid connection credentials for zimory software.");
		}
	}

	private static void logValueError(String key, String value, String error) {
		log.error("Value " + value + " for key \"" + key + "\" " + error);
	}

	private static InetAddress validateInetAddress(String key, String value) {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(value);
		} catch (UnknownHostException e) {
			logValueError(key, value, "is no valid IP-Address/Hostname");
		}
		return address;
	}

	private static Integer validateInteger(String key, String value) {
		return validateInteger(key, value, 1);
	}

	private static Integer validateInteger(String key, String value, int minValue) {
		try {
			int port = Integer.parseInt(value);
			if (port >= minValue) {
				return port;
			} else {
				logValueError(key, value, "must be greater than " + minValue);
			}
		} catch (NumberFormatException e) {
			logValueError(key, value, "is no valid integer");
		}
		return null;
	}

	/**
	 * Returns id of current SCPi-appliance in Zimory Software
	 * 
	 * @return appliance-id
	 */
	public static int getApplianceId() {
		int appliance = ZIMORY_APPLIANCE_ID;

		if (!MONITOR_SERVER_IP.equals("")) {

			try {
				URL requestURL = new URL("HTTP", MONITOR_SERVER_IP, 8181, "/zimory/scpAppliance");
				HttpURLConnection con = (HttpURLConnection) requestURL.openConnection();
				con.setRequestMethod("GET");
				con.setDoInput(true);

				InputStream stream = con.getInputStream();

				StringBuilder inputStringBuilder = new StringBuilder();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
				String line = bufferedReader.readLine();
				while (line != null) {
					inputStringBuilder.append(line);
					line = bufferedReader.readLine();
				}
				appliance = Integer.parseInt(inputStringBuilder.toString());
			} catch (IOException e) {
			}
		}

		return appliance;
	}

	public static boolean validMonitorAddress() {
		return !MONITOR_SERVER_IP.equals("") && MONITOR_SERVER_PORT > 0;
	}

	public static boolean validIaasConnection() {
		if (!zimoryValidityChecked) {
			try {
				String host = Configuration.ZIMORY_API_IP;
				String apiPath = Configuration.ZIMORY_BASE_API_STRING;
				URL certFileUrl = Configuration.ZIMORY_CERT_PATH;
				String passwd = Configuration.ZIMORY_CERT_PASS;
				int applianceId = Configuration.ZIMORY_APPLIANCE_ID;
				int networkId = Configuration.ZIMORY_NETWORK_ID;
				ZimoryConnection conn = new ZimoryConnection(host, apiPath, certFileUrl, passwd, applianceId, networkId);
				validZimoryHost = conn.canConnect();
			} catch (UnknownHostException | ZimoryException e) {
				validZimoryHost = false;
			}
			zimoryValidityChecked = true;
		}
		return validZimoryHost;
	}
}
