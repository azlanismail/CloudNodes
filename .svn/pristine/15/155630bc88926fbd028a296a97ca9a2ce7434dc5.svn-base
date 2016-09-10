/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.storage;

import rice.Continuation;
import rice.p2p.past.PastContent;

/**
 * 
 * 
 * 
 * @author A. Zeblin
 * 
 */
public class LookupContinuation implements Continuation<PastContent, Exception> {

	private PastContent content= null;

	public boolean hasResult() {
		if (content instanceof PastAppTombstone) {
			return false;
		} else if (content != null) {
			return true;
		} else {
			return false;
		}
	}

	public PastContent getResult() {
		return content;
	}

	@Override
	public void receiveResult(PastContent result) {
		content= result;
	}

	@Override
	public void receiveException(Exception exception) {
		exception.printStackTrace();
	}

}
