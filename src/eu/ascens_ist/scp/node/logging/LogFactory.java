/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.logging;

/**
 * Log factory
 * 
 * @author V. Horky
 * 
 */
public class LogFactory {
	/** Retrieve logger for given name (typically a class name). */
	public static Logger get(String name) {
		return new Log4jAdapter(org.apache.log4j.Logger.getLogger(name), name);
	}
}
