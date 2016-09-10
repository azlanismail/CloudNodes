/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.webservice;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.info.NodeLocation;

public class SCPAPICaller {

	private String apiIP;
	private int apiPort;

	public SCPAPICaller(String scpIP, int scpPort) {
		super();
		this.apiIP = scpIP;
		this.apiPort = scpPort + 1000;
	}

	/*
	 * HTTP-POST
	 */

	public String startLocalNode(int newPort, boolean connectToLocal) throws IOException {
		String body = "port=" + newPort + "&connectToLocal=" + connectToLocal;
		HttpURLConnection con = postConnection("startLocalNode", body);

		return IOUtils.toString(con.getInputStream());
	}

	public void shutdown() throws IOException {
		HttpURLConnection con = postConnection("shutdown", "");

		con.getResponseCode();
	}

	public void setDeploymentId(int deploymentId) throws IOException {
		String body = "deploymentId=" + deploymentId;
		HttpURLConnection con = postConnection("deploymentId", body);

		con.getResponseCode();
	}

	public void setApplianceId(int applianceId) throws IOException {
		String body = "applianceId=" + applianceId;
		HttpURLConnection con = postConnection("applianceId", body);

		con.getResponseCode();
	}

	public void setNetworkId(int networkId) throws IOException {
		String body = "networkId=" + networkId;
		HttpURLConnection con = postConnection("networkId", body);

		con.getResponseCode();
	}

	public void setLocation(NodeLocation location) throws IOException {
		String body = "location=" + location.toString();
		HttpURLConnection con = postConnection("location", body);

		con.getResponseCode();
	}

	public void setCPULoad(double load) throws IOException {
		String body = "cpuLoad=" + load;
		HttpURLConnection con = postConnection("cpuLoad", body);

		con.getResponseCode();
	}

	public void connect(String ip, int port) throws IOException {
		String body = "ip=" + ip + "&port=" + port;
		HttpURLConnection con = postConnection("connect", body);

		con.getResponseCode();
	}

	/*
	 * HTTP-GET
	 */

	public boolean getAlive() throws IOException {
		HttpURLConnection con = getConnection("alive");

		con.setConnectTimeout(3000);
		con.setDoInput(true);

		return Boolean.parseBoolean(IOUtils.toString(con.getInputStream()));
	}

	public Id getId() throws IOException {
		HttpURLConnection con = getConnection("id");

		con.setConnectTimeout(3000);
		con.setDoInput(true);

		return rice.pastry.Id.build(IOUtils.toString(con.getInputStream()));
	}

	/*
	 * Util Methods 
	 */

	private HttpURLConnection getConnection(String call) throws IOException {
		URL requestURL = new URL("HTTP", apiIP, apiPort, "/api/" + call);
		HttpURLConnection con = (HttpURLConnection) requestURL.openConnection();
		con.setRequestMethod("GET");
		return con;
	}

	private HttpURLConnection postConnection(String call, String body) throws IOException {
		URL requestURL = new URL("HTTP", apiIP, apiPort, "/api/" + call);
		HttpURLConnection con = (HttpURLConnection) requestURL.openConnection();
		con.setRequestMethod("POST");

		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Content-Length", String.valueOf(body.length()));
		con.setDoOutput(true);
		OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
		writer.write(body);
		writer.flush();

		return con;
	}
}
