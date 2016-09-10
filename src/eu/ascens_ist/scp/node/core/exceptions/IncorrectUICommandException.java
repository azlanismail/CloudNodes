/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.exceptions;

public class IncorrectUICommandException extends SCPException {

	private static final long serialVersionUID= 1L;

	public IncorrectUICommandException(String arg0) {
		super(arg0);
	}

	public IncorrectUICommandException(Throwable arg0) {
		super(arg0);
	}

	public IncorrectUICommandException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public IncorrectUICommandException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
