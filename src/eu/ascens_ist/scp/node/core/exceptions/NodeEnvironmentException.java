/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.exceptions;


public class NodeEnvironmentException extends SCPException {

	private static final long serialVersionUID= 1L;

	public NodeEnvironmentException(String msg) {
		super(msg);
	}

	public NodeEnvironmentException(String msg, Exception exception) {
		super(msg, exception);
	}

}
