/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.exc;

public class REnsembleNotFoundException extends RException {

	private static final long serialVersionUID= 1L;

	public REnsembleNotFoundException() {
		super("Ensemble not found!");
	}

}
