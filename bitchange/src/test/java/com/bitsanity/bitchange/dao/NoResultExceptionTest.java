/**
 * 
 */
package com.bitsanity.bitchange.dao;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.instanceOf;

import org.junit.Test;

import com.bitsanity.bitchange.dao.NoResultException;

/**
 * @author billsa
 *
 */
public class NoResultExceptionTest {

	/**
	 * Test method for {@link com.bitsanity.bitchange.dao.NoResultException#NoResultException()}.
	 */
	@Test
	public void testNoResultException() throws Exception {
		NoResultException exception = new NoResultException();
		assertNull("not null message", exception.getMessage());
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.dao.NoResultException#NoResultException(java.lang.String)}.
	 */
	@Test
	public void testNoResultExceptionString() throws Exception {
		NoResultException exception = new NoResultException("message");
		assertNotNull("not null message", exception.getMessage());
		assertEquals("invalid message", "message", exception.getMessage());
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.dao.NoResultException#NoResultException(java.lang.Throwable)}.
	 */
	@Test
	public void testNoResultExceptionThrowable() throws Exception {
		NoResultException exception = new NoResultException(new Exception());
		assertEquals("invalid message", "java.lang.Exception", exception.getMessage());
		assertThat("invalid cause", exception.getCause(), instanceOf(Exception.class));
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.dao.NoResultException#NoResultException(java.lang.String, java.lang.Throwable)}.
	 */
	@Test
	public void testNoResultExceptionStringThrowable() throws Exception {
		NoResultException exception = new NoResultException("message", new Exception());
		assertNotNull("not null message", exception.getMessage());
		assertEquals("invalid message", "message", exception.getMessage());
		assertThat("invalid cause", exception.getCause(), instanceOf(Exception.class));
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.dao.NoResultException#NoResultException(java.lang.String, java.lang.Throwable, boolean, boolean)}.
	 */
	@Test
	public void testNoResultExceptionStringThrowableBooleanBoolean() throws Exception {
		NoResultException exception = new NoResultException("message", new Exception(), true, true);
		assertNotNull("not null message", exception.getMessage());
		assertEquals("invalid message", "message", exception.getMessage());
		assertThat("invalid cause", exception.getCause(), instanceOf(Exception.class));
	}

}
