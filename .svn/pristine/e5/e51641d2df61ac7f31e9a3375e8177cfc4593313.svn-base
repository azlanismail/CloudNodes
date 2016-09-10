/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.exceptions;

import eu.ascens_ist.scp.node.info.AppExecutionStatus;

public class BundleStartException extends SCPException {

	private static final long serialVersionUID= 1L;

	private AppExecutionStatus status;

	public BundleStartException(String arg0, AppExecutionStatus status, Throwable e) {
		super(arg0, e);
		this.status= status;

	}

	public AppExecutionStatus getStatus() {
		return status;
	}
}
