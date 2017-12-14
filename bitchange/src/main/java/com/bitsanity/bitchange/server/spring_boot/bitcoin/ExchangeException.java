/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin;

/**
 * @author lou.paloma
 *
 */
public class ExchangeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ExchangeException() {
		super();
	}

	/**
	 * @param message
	 */
	public ExchangeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ExchangeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ExchangeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ExchangeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
