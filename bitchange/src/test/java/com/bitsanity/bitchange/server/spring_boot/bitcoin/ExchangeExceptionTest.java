/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author lou.paloma
 *
 */
public class ExchangeExceptionTest {

	/**
	 * Test method for {@link com.bitsanity.bitchange.server.spring_boot.bitcoin.ExchangeException#ExchangeException()}.
	 */
	@Test
	public void testExchangeException() throws Exception {
		ExchangeException exception = new ExchangeException();
		assertNotNull("null exception", exception);
		
		String msg = "Message";
		exception = new ExchangeException(msg);
		assertNotNull("null exception", exception);
		assertEquals("null message", msg, exception.getMessage());
		
		Exception e = new Exception("test");
		exception = new ExchangeException(e);
		assertNotNull("null exception", exception);
		assertNotNull("null cause", exception.getCause());
		assertEquals("null cause message", "test", exception.getCause().getMessage());
		
		exception = new ExchangeException(msg, e);
		assertNotNull("null exception", exception);
		assertEquals("null message", msg, exception.getMessage());
		assertNotNull("null cause", exception.getCause());
		assertEquals("null cause message", "test", exception.getCause().getMessage());
		
		exception = new ExchangeException(msg, e, false, false);
		assertNotNull("null exception", exception);
		assertEquals("null message", msg, exception.getMessage());
		assertNotNull("null cause", exception.getCause());
		assertEquals("null cause message", "test", exception.getCause().getMessage());
	}

}
