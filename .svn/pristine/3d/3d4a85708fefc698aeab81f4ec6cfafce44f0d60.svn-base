/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r;

import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;
import eu.ascens_ist.scp.node.info.AppInfo;

public class RDeployAppMessage extends RR2RMessage {

	private static final long serialVersionUID= 1L;

	private byte[] code;

	private AppInfo appInfo;

	public RDeployAppMessage(RoleId fromRole, RoleId toRole, AppInfo info, byte[] code) {
		super(fromRole, toRole);
		this.appInfo= info;
		this.code= code;
	}

	public byte[] getAppCode() {
		return code;
	}

	public AppInfo getAppInfo() {
		return appInfo;
	}


}
