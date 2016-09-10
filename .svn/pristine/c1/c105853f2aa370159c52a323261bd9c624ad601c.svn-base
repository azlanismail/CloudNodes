/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.messages.AbstractSCPDirectMessage;

public class HelenaWrapperMessage extends AbstractSCPDirectMessage {

	private static final long serialVersionUID= 1L;

	private RMessage wrapped;

	private Id messageId;

	public HelenaWrapperMessage(Id from, Id to, RMessage wrapped, Id messageId) {
		super(from, to);
		this.wrapped= wrapped;
		this.messageId= messageId;
		setMsgType(wrapped.getClass().getSimpleName());
	}

	public RMessage getWrapped() {
		return wrapped;
	}

	public Id getMessageId() {
		return messageId;
	}

}
