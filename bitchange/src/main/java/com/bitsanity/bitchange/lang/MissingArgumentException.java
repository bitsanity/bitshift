/**
 * 
 */
package com.bitsanity.bitchange.lang;

/**
 * @author billsa
 *
 */
public class MissingArgumentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3267233549676042512L;

	/**
	 * 
	 */
	public MissingArgumentException() {
		super();
	}

	/**
	 * @param message
	 */
	public MissingArgumentException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MissingArgumentException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MissingArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public MissingArgumentException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
