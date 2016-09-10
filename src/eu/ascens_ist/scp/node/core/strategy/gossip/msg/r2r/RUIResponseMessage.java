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


public class RUIResponseMessage extends RR2RMessage {

	private static final long serialVersionUID= 1L;

	private String result;

	public RUIResponseMessage(RoleId fromRole, RoleId toRole, String result) {
		super(fromRole, toRole);
		this.result= result;
	}

	public String getResult() {
		return result;
	}

}
