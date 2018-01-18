/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin;

/**
 * @author lou.paloma
 *
 */
public class AddressException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public AddressException() {
		super();
	}

	/**
	 * @param message
	 */
	public AddressException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AddressException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AddressException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public AddressException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
