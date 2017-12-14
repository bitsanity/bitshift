package com.bitsanity.bitchange.server.spring_boot.crypto;

import static org.junit.Assert.*;

import org.junit.Test;

import com.bitsanity.bitchange.server.spring_boot.crypto.CryptographyException;

public class CryptographyExceptionTest {

	@Test
	public void testCryptographyExceptionString() throws Exception {
		CryptographyException e = new CryptographyException("test");
		assertEquals("invalid message", "test", e.getMessage());
	}

	@Test
	public void testCryptographyExceptionStringThrowable() throws Exception {
		CryptographyException e = new CryptographyException("test", new Exception("nested"));
		assertEquals("invalid message", "test", e.getMessage());
		assertNotNull("invalid Throwable", e.getCause());
		assertEquals("invalid throwable message", "nested", e.getCause().getMessage());
	}

}
