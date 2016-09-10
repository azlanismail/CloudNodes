/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.storage;

import rice.p2p.commonapi.Id;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastException;
import rice.p2p.past.gc.GCPast;
import rice.p2p.past.gc.GCPastContent;
import rice.p2p.past.gc.GCPastMetadata;
import eu.ascens_ist.scp.node.NodeEnvironment;

/**
 * 
 * 
 * 
 * @author A. Zeblin
 * 
 */
public class PastAppAbstractContent implements GCPastContent {

	private static final long serialVersionUID = 1L;

	private Id id;

	private String name;

	private byte[] data;

	private long version;

	public PastAppAbstractContent(String name, byte[] data) {

		// Create an ID from the name of the file
		Id id = NodeEnvironment.get().createIdHashFromArbitraryString(name);

		// use timestamp as version number
		long version = NodeEnvironment.get().getCurrentTimeMillis();

		this.id = id;
		this.name = name;
		this.data = data;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rice.p2p.past.PastContent#checkInsert(rice.p2p.commonapi.Id, rice.p2p.past.PastContent)
	 */
	@Override
	public PastContent checkInsert(Id id, PastContent existingContent) throws PastException {

		// only allow correct content hash key
		if (!id.equals(getId())) {
			throw new PastException("ContentHashPastContent: can't insert, content hash incorrect");
		}

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rice.p2p.past.PastContent#getId()
	 */
	@Override
	public Id getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rice.p2p.past.PastContent#isMutable()
	 */
	@Override
	public boolean isMutable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rice.p2p.past.gc.GCPastContent#getVersion()
	 */
	@Override
	public long getVersion() {
		return version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rice.p2p.past.gc.GCPastContent#getHandle(rice.p2p.past.gc.GCPast, long)
	 */
	@Override
	public PastAppContentHandle getHandle(GCPast local, long expiration) {
		return new PastAppContentHandle(local.getLocalNodeHandle(), id, version, expiration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rice.p2p.past.gc.GCPastContent#getMetadata(long)
	 */
	@Override
	public GCPastMetadata getMetadata(long expiration) {
		return new GCPastMetadata(expiration);
	}

	@Override
	public PastAppContentHandle getHandle(Past local) {
		return new PastAppContentHandle(local.getLocalNodeHandle(), id, version, GCPast.INFINITY_EXPIRATION);
	}

}
