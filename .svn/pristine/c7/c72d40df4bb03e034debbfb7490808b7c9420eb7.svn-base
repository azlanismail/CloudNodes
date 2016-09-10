/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

import rice.p2p.commonapi.Message;

import com.google.gson.Gson;

import eu.ascens_ist.scp.node.Configuration;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.logging.LogFactory;
import eu.ascens_ist.scp.node.logging.Logger;

/**
 * Client side for communication with the monitoring server (via HTTP and JSON)
 * 
 * @author E. Englmeier
 * 
 */
public class MonitorClient {
	private Logger log;

	private final HttpClient client;
	private final Gson gson;
	private boolean connected = true;

	public MonitorClient(SCPNode node) {
		if (node != null) {
			log = LogFactory.get(node.getId() + " MONITORCLIENT");
		} else {
			log = LogFactory.get("MONITORCLIENT");
		}
		this.client = new HttpClient();
		this.client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		this.client.setMaxConnectionsPerAddress(300);
		this.client.setConnectTimeout(5000);
		try {
			this.client.start();
		} catch (Exception e) {
			log.warn("MonitorClient could not be started");
		}

		this.gson = new Gson();

		Runnable watchdogRunnable = new WatchdogRunnable();
		Thread watchdogThread = new Thread(watchdogRunnable);
		watchdogThread.start();
	}

	private final class WatchdogRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					log.debug("Ping MonitorServer");
					try {
						ContentExchange exchange = new ContentExchange() {
							@Override
							protected void onConnectionFailed(Throwable x) {
								connected = false;
							}

							@Override
							protected void onResponseComplete() throws UnsupportedEncodingException {
								String response = getResponseContent();
								if (response.equals("pong")) {
									log.debug("Pong received");
									connected = true;
								}
							}
						};

						sendExchange("ping", exchange);

					} catch (Exception e) {
						log.warn("Ping could not be sent to MonitorServer");
					}
					if (connected)
						Thread.sleep(10000);
					else
						Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.error("Watchdog for connection to MonitorServer interrupted");
				}
			}
		}
	};

	public void sendDirectMessageInfo(Message message) {
		try {
			String msg = gson.toJson(message);
			sendToMonitorServer(msg);
		} catch (UnsupportedOperationException e) {
			// catches exception raised when serialization to json does not work
		}
	}

	public void sendNodeInfo(NodeInfo nodeInfo) {
		sendToMonitorServer(gson.toJson(nodeInfo));
	}

	public void sendToMonitorServer(String str) {
		if (connected) {
			try {
				ContentExchange exchange = new ContentExchange() {
					@Override
					protected void onConnectionFailed(Throwable x) {
						connected = false;
					}
				};

				sendExchange(str, exchange);

			} catch (Exception e) {
				log.info("Data could not be sent to MonitorServer");
			}
		}
	}

	private void sendExchange(String str, ContentExchange exchange) throws IOException {
		if (Configuration.validMonitorAddress()) {
			exchange.setMethod("POST");
			exchange.setURL("http://" + Configuration.MONITOR_SERVER_IP + ":" + Configuration.MONITOR_SERVER_PORT + "/nodeInfo/");
			exchange.setRequestContentSource(new ByteArrayInputStream(str.getBytes("UTF-8")));

			client.send(exchange);
		}
	}

	public boolean isConnected() {
		return connected;
	}
}
