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
 * 
 * Ping.
 * 
 * 
 * @author A. Dittrich
 * 
 */
public class ShutdownNodeRequest extends AbstractSCPDirectMessage implements Message {

	private static final long serialVersionUID = 1L;

	public ShutdownNodeRequest(Id from, Id to) {
		super(from, to);
	}

}
