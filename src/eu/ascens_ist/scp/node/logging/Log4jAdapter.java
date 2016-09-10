/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.logging;

/**
 * Log4J adapter
 * 
 * @author V. Horky
 * 
 */
public class Log4jAdapter implements Logger {
	private org.apache.log4j.Logger backend;

	public Log4jAdapter(org.apache.log4j.Logger actual, String name) {
		backend= actual;
	}

	@Override
	public void debug(String format, Object... args) {
		backend.debug(String.format(format, args));
	}

	@Override
	public void info(String format, Object... args) {
		backend.info(String.format(format, args));
	}

	@Override
	public void warn(String format, Object... args) {
		backend.warn(String.format(format, args));
	}

	@Override
	public void error(String format, Object... args) {
		backend.error(String.format(format, args));
	}

	@Override
	public void error(Throwable exception, String format, Object... args) {
		backend.error(String.format(format, args) + formatStackTrace(exception));
	}

	protected String formatStackTrace(Throwable exception) {
		StringBuilder result= new StringBuilder();
		for (StackTraceElement line : exception.getStackTrace()) {
			result.append("\n        ");
			result.append(line);
		}
		return result.toString();
	}
}
