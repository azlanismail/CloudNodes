/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.storage;

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastObserver;
import rice.pastry.Id;
import eu.ascens_ist.scp.node.core.SCPNode;

/**
 * 
 * Notification mechanism for new local past content.
 * 
 * @author A. Zeblin
 * 
 */
public class PastObserverImpl implements PastObserver {

	protected SCPNode scpNode;

	public PastObserverImpl(SCPNode coreApp) {
		this.scpNode= coreApp;
	}

	@Override
	public void notifyInsert(Id msgid, NodeHandle nodeHandle, PastContent content) {
		scpNode.newLocalPastContent(msgid, nodeHandle, content);
	}

}
