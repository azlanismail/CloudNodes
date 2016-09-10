/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RRouteRoleMessageResult;

public class HelenaWrapperR2RAnswerMessage extends HelenaWrapperAnswerMessage {

	private static final long serialVersionUID= 1L;

	private RRouteRoleMessageResult roleBasedResult;

	public HelenaWrapperR2RAnswerMessage(Id from, Id to, RRouteRoleMessageResult roleBasedResult, Id messageId) {
		super(from, to, messageId);
		this.roleBasedResult= roleBasedResult;
		setMsgType("R2RAnswerMessage" + getRoleBasedResult().toString());
	}

	public RRouteRoleMessageResult getRoleBasedResult() {
		return roleBasedResult;
	}

	@Override
	public String toString() {
		return "WrapperR2RAnswerMessage with role based result " + roleBasedResult;
	}

}
