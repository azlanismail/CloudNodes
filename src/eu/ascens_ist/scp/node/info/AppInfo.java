/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.info;

import java.io.Serializable;
import java.util.Properties;

/**
 * Contains information about an app.
 * 
 * 
 * @author A. Zeblin
 * 
 */
public class AppInfo implements Serializable, Comparable<AppInfo> {

	private static final long serialVersionUID= 1L;

	protected String name;

	/**
	 * Name of the service started by the app.
	 */
	protected String serviceName;

	protected Requirements req;

	public AppInfo(String name, Requirements req) {
		init(name, req);
	}

	private void init(String name, Requirements req) {
		this.name= name;
		this.req= req;

		serviceName= name + "Service";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name= name;
	}

	public void setServiceName(String serviceName) {
		this.serviceName= serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Requirements getRequirements() {
		return req;
	}

	public Properties getRequirementProperties() {
		return req.getProperties();
	}

	public void setReq(Requirements req) {
		this.req= req;
	}

	/**
	 * Compares app names.
	 */
	@Override
	public int compareTo(AppInfo appInfo) {
		return name.compareTo(appInfo.getName());
	}

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + ( (name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppInfo other= (AppInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}
