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

public class RAcknowledgeExecutionMessage extends RR2RMessage {

	private static final long serialVersionUID = 1L;

	private RoleId executorRole;

	public RAcknowledgeExecutionMessage(RoleId fromRole, RoleId toRole, RoleId executorId) {
		super(fromRole, toRole);
		this.executorRole= executorId;
	}

	public RoleId getExecutorRole() {
		return executorRole;
	}


}
