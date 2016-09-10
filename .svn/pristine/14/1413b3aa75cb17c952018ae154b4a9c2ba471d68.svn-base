/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * This class stores the requirements of an app on an executor. The .req file must contain the requirements one line each, with a colon (":") to separate key
 * and value.
 * 
 * @author A. Zeblin, A. Dittrich
 * 
 */
public class Requirements implements Serializable {

	private static final long serialVersionUID= 1L;

	private HashMap<String, String> req;

	/**
	 * Constructor to read the requirements from a byte[] array retrieved from the JAR file and stores them.
	 * 
	 * @param bytes
	 */
	public Requirements(byte[] bytes) {
		req= new HashMap<>();
		setRequirements(bytes);
	}

	public Requirements() {
		req= new HashMap<>();
		// empty
	}

	protected void setRequirements(byte[] bytes) {
		String tmp= new String(bytes);
		setRequirements(tmp);
	}

	protected void setRequirements(String string) {

		// Split on new line
		String[] values= string.split("\n");

		// go through all the lines
		for (int i= 0; i < values.length; i++) {
			// no value?
			if (values[i].trim().equals("") || values[i] == null) {
				continue;
			}

			// colon?
			String[] value= values[i].split(":");

			if (value.length != 2) {
				continue;
			}

			// value[0] is the key, value[1] is the value
			req.put(value[0], value[1]);
		}
	}

	protected void setRequirements(HashMap<String, String> values) {
		this.req= values;
	}

	// Getter
	public String getValue(String key) {
		return req.get(key);
	}

	public int getValueCount() {
		return req.size();
	}

	public long getCPUSpeed() {
		if (req.get("CPUSpeed") == null) {
			return 0;
		}

		return Long.parseLong(req.get("CPUSpeed"));
	}

	public long getCPUCores() {
		if (req.get("CPUCores") == null) {
			return 0;
		}

		return Long.parseLong(req.get("CPUCores"));
	}

	public double getCPULoad() {
		if (req.get("CPULoad") == null) {
			return 0.0;
		}

		return Double.parseDouble(req.get("CPULoad"));
	}

	public long getTotalMemory() {
		if (req.get("TotalMemory") == null) {
			return 0;
		}

		return Long.parseLong(req.get("TotalMemory"));
	}

	public long getFreeMemory() {
		if (req.get("FreeMemory") == null) {
			return 0;
		}

		return Long.parseLong(req.get("FreeMemory"));
	}

	public ArrayList<NodeLocation> getLocations() {
		ArrayList<NodeLocation> locations = new ArrayList<NodeLocation>();

		if (req.get("Locations") != null) {
			for (String locationString : req.get("Locations").split(",")) {
				locationString = locationString.trim();

				locations.add(NodeLocation.getLocationFromString(locationString));
			}
		}

		if (locations.isEmpty()) {
			for (NodeLocation nodeLocation : NodeLocation.values())
				locations.add(nodeLocation);
		}
		
		return locations;
	}

	public boolean getNoVirtualization() {
		if (req.get("NoVirtualization") == null) {
			return false;
		}

		return Boolean.parseBoolean(req.get("NoVirtualization"));
	}

	public Properties getProperties() {
		Properties prop= new Properties();
		Set<Map.Entry<String, String>> set= req.entrySet();
		for (Map.Entry<String, String> entry : set) {
			prop.put(entry.getKey(), entry.getValue());
		}
		return prop;
	}
}
