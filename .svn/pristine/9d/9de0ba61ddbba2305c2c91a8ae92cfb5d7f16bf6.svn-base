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

public class RAppCodeMessage extends RR2RMessage {

	private static final long serialVersionUID = 1L;

	private byte[] appInfo;

	public RAppCodeMessage(RoleId fromRole, RoleId toRole, byte[] appCode) {
		super(fromRole, toRole);
		this.appInfo= appCode;
	}

	public byte[] getAppCode() {
		return appInfo;
	}


}
