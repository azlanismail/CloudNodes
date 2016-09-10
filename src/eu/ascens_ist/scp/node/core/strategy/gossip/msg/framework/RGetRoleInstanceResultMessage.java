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
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RException;

public class RGetRoleInstanceResultMessage extends RFrameworkResponseMessage {

	private static final long serialVersionUID= 1L;

	private RoleId grabbedRole;

	private RException originException;

	public RGetRoleInstanceResultMessage(Id fromNode, RoleId toRole, RoleId grabbedRole) {
		super(fromNode, toRole);
		this.grabbedRole= grabbedRole;
	}

	public RGetRoleInstanceResultMessage(Id fromNode, RoleId toRole, RException exc) {
		super(fromNode, toRole);
		originException= exc;
	}

	public RoleId getRoleInstance() {
		return grabbedRole;
	}

	public RException getOriginException() {
		return originException;
	}

	@Override
	public String toString() {
		if (grabbedRole != null) {
			return "Role Instance Retrieved Answer Message with role " + grabbedRole;
		} else
			return "Role Instance Retrieved Answer Message with exception " + originException;

	}

}
