/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r;

import java.io.Serializable;

import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RException;

public class RRouteRoleMessageResult implements Serializable {

	private static final long serialVersionUID= 1L;

	private RException rexception;

	private boolean ok;

	public RRouteRoleMessageResult() {
		this.ok= true;
	}

	public RRouteRoleMessageResult(RException e) {
		this.ok= false;
		this.rexception= e;
	}

	public RException getRemoteException() {
		return rexception;
	}

	@Override
	public String toString() {
		return "Result is " + (ok ? "OK" : ("NOT OK; exception is " + rexception.getMessage()));
	}


}
