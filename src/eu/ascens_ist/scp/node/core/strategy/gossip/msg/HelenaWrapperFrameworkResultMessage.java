/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg;

import rice.p2p.commonapi.Id;

public class HelenaWrapperFrameworkResultMessage extends HelenaWrapperAnswerMessage {

	private static final long serialVersionUID= 1L;

	private RFrameworkMessage frameworkBasedResult;

	public HelenaWrapperFrameworkResultMessage(Id from, Id to, RFrameworkMessage frameworkBasedResult, Id messageId) {
		super(from, to, messageId);
		this.frameworkBasedResult= frameworkBasedResult;
		setMsgType(getFrameworkBasedResult().getClass().getSimpleName());
	}

	public RFrameworkMessage getFrameworkBasedResult() {
		return frameworkBasedResult;
	}

	@Override
	public String toString() {
		return "WrapperFrameworkResultMessage with framework based result " + frameworkBasedResult;
	}

}
