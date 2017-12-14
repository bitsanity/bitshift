package com.bitsanity.bitchange.utils.logging;

public interface Logger extends org.slf4j.Logger {

	  /**
	   * Is the logger instance enabled for the AUDIT level?
	   *
	   * @return True if this Logger is enabled for the AUDIT level,
	   *         false otherwise.
	   */
	  public boolean isAuditEnabled();


	  /**
	   * Log a message at the AUDIT level.
	   *
	   * @param msg the message string to be logged
	   */
	  public void audit(String msg);


	  /**
	   * Log a message at the AUDIT level according to the specified format
	   * and argument.
	   * <p/>
	   * <p>This form avoids superfluous object creation when the logger
	   * is disabled for the AUDIT level. </p>
	   *
	   * @param format the format string
	   * @param arg    the argument
	   */
	  public void audit(String format, Object arg);


	  /**
	   * Log a message at the AUDIT level according to the specified format
	   * and arguments.
	   * <p/>
	   * <p>This form avoids superfluous object creation when the logger
	   * is disabled for the AUDIT level. </p>
	   *
	   * @param format the format string
	   * @param arg1   the first argument
	   * @param arg2   the second argument
	   */
	  public void audit(String format, Object arg1, Object arg2);

	  /**
	   * Log a message at the AUDIT level according to the specified format
	   * and arguments.
	   * <p/>
	   * <p>This form avoids superfluous string concatenation when the logger
	   * is disabled for the AUDIT level. However, this variant incurs the hidden
	   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
	   * even if this logger is disabled for AUDIT. The variants taking {@link #audit(String, Object) one} and
	   * {@link #audit(String, Object, Object) two} arguments exist solely in order to avoid this hidden cost.</p>
	   *
	   * @param format    the format string
	   * @param arguments a list of 3 or more arguments
	   */
	  public void audit(String format, Object... arguments);

	  /**
	   * Log an exception (throwable) at the AUDIT level with an
	   * accompanying message.
	   *
	   * @param msg the message accompanying the exception
	   * @param t   the exception (throwable) to log
	   */
	  public void audit(String msg, Throwable t);
}
