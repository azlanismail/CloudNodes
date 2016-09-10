/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg;

import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;

/**
 * Role-to-Role message. Source and target are roles.
 * 
 * @author P. Mayer
 * 
 */
public abstract class RR2RMessage extends RMessage {

	private static final long serialVersionUID= 1L;

	private RoleId fromRole;

	private RoleId toRole;

	public RR2RMessage(RoleId fromRole, RoleId toRole) {
		this.toRole= toRole;
		this.fromRole= fromRole;
	}

	public RoleId getToRole() {
		return toRole;
	}

	public RoleId getFromRole() {
		return fromRole;
	}

}
