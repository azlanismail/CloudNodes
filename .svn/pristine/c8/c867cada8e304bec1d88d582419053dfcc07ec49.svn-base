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

public abstract class HelenaWrapperAnswerMessage extends AbstractSCPDirectMessage {

	private static final long serialVersionUID= 1L;

	protected Id messageId;

	public HelenaWrapperAnswerMessage(Id from, Id to, Id messageId) {
		super(from, to);
		this.messageId= messageId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Id getMessageId() {
		return messageId;
	}

}
