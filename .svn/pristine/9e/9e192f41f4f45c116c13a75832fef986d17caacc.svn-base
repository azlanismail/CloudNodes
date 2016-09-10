/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.storage;

/**
 * Content stored by an app.
 * 
 * @author A. Zeblin
 * 
 */
public class PastAppContent extends PastAppAbstractContent {
	private static final long serialVersionUID= 1L;

	private Object content;

	public PastAppContent(String name, byte[] data) {
		super(name, data);
	}

	public PastAppContent(String name, Object content) {
		super(name, null);
		this.content= content;
	}

	public Object getContent() {
		return content;
	}

}
