/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.storage;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.gc.GCPastContentHandle;

/**
 * 
 * 
 * 
 * @author A. Zeblin
 * 
 */
public class PastAppContentHandle implements GCPastContentHandle {

	private static final long serialVersionUID= 1L;

	// the node on which the content object resides
	private NodeHandle storageNode;

	// the object's id
	private Id id;

	// the object's version
	private long version;

	// the object's expiration
	private long expiration;

	/**
	 * 
	 * @param node
	 * @param id of the content
	 * @param version of the content
	 * @param expiration
	 */
	public PastAppContentHandle(NodeHandle node, Id id, long version, long expiration) {
		this.storageNode= node;
		this.id= id;
		this.version= version;
		this.expiration= expiration;
	}

	@Override
	public Id getId() {
		return id;
	}

	@Override
	public NodeHandle getNodeHandle() {
		return storageNode;
	}

	@Override
	public long getVersion() {
		return version;
	}

	@Override
	public long getExpiration() {
		return expiration;
	}

}
