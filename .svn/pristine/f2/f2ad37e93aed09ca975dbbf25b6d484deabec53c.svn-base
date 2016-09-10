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
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RFrameworkMessage;

public abstract class RFrameworkResponseMessage extends RFrameworkMessage {

	private static final long serialVersionUID= 1L;

	private Id fromNode;

	private RoleId toRole;

	public RFrameworkResponseMessage(Id fromNode, RoleId toRole) {
		super();
		this.fromNode= fromNode;
		this.toRole= toRole;
	}

	public Id getFromNode() {
		return fromNode;
	}

	public RoleId getToRole() {
		return toRole;
	}



}
