/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.messages;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;

/**
 * Generic SCP message. Includes full strings and msg type for the sake of gson and the monitor implementation...
 * 
 * @author P. Mayer
 * 
 */
public class AbstractSCPMessage implements Message {

	private static final long serialVersionUID= 1L;

	private String msgType;

	private Id fromNode;

	private String fullStringIdFrom;

	private Id toNode;

	private String fullStringIdTo;

	public AbstractSCPMessage(Id fromNode) {
		this.fromNode= fromNode;
		this.fullStringIdFrom= fromNode.toStringFull();

		this.toNode= null;
		this.fullStringIdTo= "Broadcast";

		msgType= this.getClass().getSimpleName();
	}


	public AbstractSCPMessage(Id fromNode, Id toNode) {
		this.fromNode= fromNode;
		this.fullStringIdFrom= fromNode.toStringFull();

		this.toNode= toNode;
		this.fullStringIdTo= toNode.toStringFull();

		msgType= this.getClass().getSimpleName();
	}

	@Override
	public int getPriority() {
		return MEDIUM_PRIORITY;
	}

	public Id getFrom() {
		return fromNode;
	}

	public Id getTo() {
		return toNode;
	}

	public void setMsgType(String msgType) {
		this.msgType= msgType;
	}

	public String getMsgType() {
		return msgType;
	}

	public String getFullStringIdFrom() {
		return fullStringIdFrom;
	}

	public String getFullStringIdTo() {
		return fullStringIdTo;
	}

}
