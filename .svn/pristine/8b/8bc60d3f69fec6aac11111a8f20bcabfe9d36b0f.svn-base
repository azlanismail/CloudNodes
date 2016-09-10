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
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.HelenaRole;

public class RGetRoleInstanceMessage extends RFrameworkRequestMessage {

	private static final long serialVersionUID = 1L;

	private Class<? extends HelenaRole> roleType;

	public RGetRoleInstanceMessage(RoleId fromRole, Id targetNode, Class<? extends HelenaRole> roleType) {
		super(fromRole, targetNode);
		this.roleType = roleType;
	}

	public Class<? extends HelenaRole> getRoleType() {
		return roleType;
	}

	@Override
	public String toString() {
		return "Get Role Instance Message with type " + getRoleType();
	}

}
