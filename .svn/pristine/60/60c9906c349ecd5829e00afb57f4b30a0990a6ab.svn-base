/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleCreationException;

public class RCreateRoleInstanceResultMessage extends RFrameworkResponseMessage {
	
	private static final long serialVersionUID= 1L;

	private RoleId createdRole;

	private RRoleCreationException creationException;


	public RCreateRoleInstanceResultMessage(Id fromNode, RoleId toRole, RoleId createdRole) {
		super(fromNode, toRole);
		this.createdRole= createdRole;
	}

	public RCreateRoleInstanceResultMessage(Id fromNode, RoleId toRole, RRoleCreationException e) {
		super(fromNode, toRole);
		this.creationException= e;
	}

	public RoleId getRoleInstance() {
		return createdRole;
	}

	public void setCreatedRole(RoleId role) {
		this.createdRole= role;
	}

	public void setRemoteRoleCreationException(RRoleCreationException creationException) {
		this.creationException= creationException;
	}

	public RRoleCreationException getRemoteRoleCreationException() {
		return creationException;
	}

	@Override
	public String toString() {
		if (createdRole != null) {
			return "Role Instance Created Answer Message with role " + createdRole;
		} else
			return "Role Instance Created Answer Message with exception " + creationException;

	}

}
