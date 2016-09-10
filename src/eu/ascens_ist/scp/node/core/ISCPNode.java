/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core;

import rice.p2p.commonapi.Id;

/**
 * 
 * Interface of an SCPi node, to be used by apps.
 * 
 * @author A. Zeblin
 * 
 */
public interface ISCPNode {

	public void saveContent(String key, Object content);

	public Object getContent(String key, int waitingTimeInMs);

	public Id getId();

	public boolean isAlive();
}
