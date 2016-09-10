/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg;

import java.util.Set;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.info.NodeInfo;
import eu.ascens_ist.scp.node.messages.AbstractSCPDirectMessage;

public class GossipInfoMessage extends AbstractSCPDirectMessage {

	private static final long serialVersionUID= 1L;

	private Set<NodeInfo> knownNodes;

	public GossipInfoMessage(Id from, Id to, Set<NodeInfo> knownNodes2) {
		super(from, to);
		this.knownNodes= knownNodes2;
		setMsgType(getClass().getSimpleName());
	}

	public Set<NodeInfo> getKnownNodes() {
		return knownNodes;
	}

}
