/**
 * 
 */
package com.bitsanity.bitchange.lang;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.instanceOf;

import org.junit.Test;

import com.bitsanity.bitchange.lang.MissingArgumentException;

/**
 * @author billsa
 *
 */
public class MissingArgumentExceptionTest {

	/**
	 * Test method for {@link com.bitsanity.bitchange.lang.MissingArgumentException#MissingArgumentException()}.
	 */
	@Test
	public void testMissingArgumentException() throws Exception {
		MissingArgumentException exception = new MissingArgumentException();
		assertNull("not null message", exception.getMessage());
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.lang.MissingArgumentException#MissingArgumentException(java.lang.String)}.
	 */
	@Test
	public void testMissingArgumentExceptionString() throws Exception {
		MissingArgumentException exception = new MissingArgumentException("message");
		assertNotNull("not null message", exception.getMessage());
		assertEquals("invalid message", "message", exception.getMessage());
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.lang.MissingArgumentException#MissingArgumentException(java.lang.Throwable)}.
	 */
	@Test
	public void testMissingArgumentExceptionThrowable() throws Exception {
		MissingArgumentException exception = new MissingArgumentException(new Exception());
		assertEquals("invalid message", "java.lang.Exception", exception.getMessage());
		assertThat("invalid cause", exception.getCause(), instanceOf(Exception.class));
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.lang.MissingArgumentException#MissingArgumentException(java.lang.String, java.lang.Throwable)}.
	 */
	@Test
	public void testMissingArgumentExceptionStringThrowable() throws Exception {
		MissingArgumentException exception = new MissingArgumentException("message", new Exception());
		assertNotNull("not null message", exception.getMessage());
		assertEquals("invalid message", "message", exception.getMessage());
		assertThat("invalid cause", exception.getCause(), instanceOf(Exception.class));
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.lang.MissingArgumentException#MissingArgumentException(java.lang.String, java.lang.Throwable, boolean, boolean)}.
	 */
	@Test
	public void testMissingArgumentExceptionStringThrowableBooleanBoolean() throws Exception {
		MissingArgumentException exception = new MissingArgumentException("message", new Exception(), true, true);
		assertNotNull("not null message", exception.getMessage());
		assertEquals("invalid message", "message", exception.getMessage());
		assertThat("invalid cause", exception.getCause(), instanceOf(Exception.class));
	}

}
