package com.bitsanity.bitchange.canonical.request;

import org.junit.Test;

import com.bitsanity.bitchange.canonical.request.Meta;

import static org.junit.Assert.assertEquals;

public class MetaTest {
	
	@Test
	public void testGetRequestCode() throws Exception {
		Meta meta = new Meta();
		meta.setRequestCode("ABC");
		assertEquals("invalid request code", "ABC", meta.getRequestCode());
	}

	@Test
	public void testGetTimeStamp() throws Exception {
		Meta meta = new Meta();
		meta.setTimeStamp("ABC");
		assertEquals("invalid timestamp", "ABC", meta.getTimeStamp());
	}

}
