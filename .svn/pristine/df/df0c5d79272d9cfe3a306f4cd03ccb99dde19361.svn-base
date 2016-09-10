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

public abstract class RFrameworkRequestMessage extends RFrameworkMessage {

	private static final long serialVersionUID= 1L;

	private RoleId fromRole;

	private Id targetNode;

	public RFrameworkRequestMessage(RoleId fromRole, Id targetNode) {
		super();
		this.fromRole= fromRole;
		this.targetNode= targetNode;
	}

	public RoleId getFromRole() {
		return fromRole;
	}

	public Id getTargetNode() {
		return targetNode;
	}



}
