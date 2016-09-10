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

public class RCreateDeploymentMessage extends RR2RMessage {

	private static final long serialVersionUID= 1L;

	private AppInfo appInfo;

	public RCreateDeploymentMessage(RoleId fromRole, RoleId toRole, AppInfo appInfo) {
		super(fromRole, toRole);
		this.appInfo= appInfo;
	}

	public AppInfo getAppInfo() {
		return appInfo;
	}

}
