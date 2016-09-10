/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.logging;

/**
 * Simplistic logging interface to allow changing the underlying implementation.
 * 
 * @author V. Horky
 * 
 */
public interface Logger {
	/** Debugging-only message in printf-style. */
	public void debug(String format, Object... args);

	/** Informative message in printf-style. */
	public void info(String format, Object... args);

	/** Warning in printf-style. */
	public void warn(String format, Object... args);

	/** Serious error in printf-style. */
	public void error(String format, Object... args);

	/** Serious error accompanied by exception in printf-style. */
	public void error(Throwable exception, String format, Object... args);
}
