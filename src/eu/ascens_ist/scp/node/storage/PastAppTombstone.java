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
 * This class shows the same behaviour as its superclass, except it always returns null instead of data
 * 
 * @author A. Zeblin
 * 
 */
public class PastAppTombstone extends PastAppAbstractContent {

	private static final long serialVersionUID= 1L;

	private AppInfo appInfo;

	public PastAppTombstone(AppInfo appInfo) {
		super(appInfo.getName(), null);
		this.appInfo= appInfo;
	}

	public AppInfo getAppInfo() {
		return appInfo;
	}

	/*
	 * @see eu.ascens_ist.scp.node.core.c.SCPGCPastContent#getName()
	 */
	@Override
	public String getName() {
		return super.getName() + " (removed)";
	}

	@Override
	public String toString() {
		return getName();
	}

	/*
	 * Not really necessary due to the constructors, but more obvious and simpler
	 * 
	 * @see eu.ascens_ist.scp.node.core.c.SCPGCPastContent#getData()
	 */
	@Override
	public byte[] getData() {
		return null;
	}

}
