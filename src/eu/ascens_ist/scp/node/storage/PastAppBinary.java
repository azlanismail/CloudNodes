/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.storage;

import eu.ascens_ist.scp.node.info.AppInfo;

/**
 * 
 * 
 * 
 * @author A. Zeblin
 * 
 */
public class PastAppBinary extends PastAppAbstractContent {

	private static final long serialVersionUID= 1L;

	private AppInfo appInfo;

	public PastAppBinary(AppInfo appInfo, byte[] bytes) {
		super(appInfo.getName(), bytes);
		this.appInfo= appInfo;
	}

	public AppInfo getAppInfo() {
		return appInfo;
	}

	@Override
	public String toString() {
		return getName() + " (App)";
	}

}
