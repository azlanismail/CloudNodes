/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.exc;

public class RException extends Exception {

	private static final long serialVersionUID= 1L;

	public RException() {
		super(null, null);
	}

	public RException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * !!!!!! GOTCHA: Always pass a cause to super to prevent adding this as cause, which confuses gson (=>StackOverflowError)
	 * 
	 * @param message
	 */
	public RException(String message) {
		super(message, null);
	}

	public RException(Throwable cause) {
		super(cause);
	}



}
