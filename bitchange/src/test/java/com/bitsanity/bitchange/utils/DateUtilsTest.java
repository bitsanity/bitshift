/**
 * 
 */
package com.bitsanity.bitchange.utils;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Date;

import org.junit.Test;

import com.bitsanity.bitchange.lang.MissingArgumentException;
import com.bitsanity.bitchange.utils.DateUtils;

/**
 * @author billsa
 *
 */
public class DateUtilsTest {

	/**
	 * Test method for {@link com.bitsanity.bitchange.utils.DateUtils#getDurationString(java.util.Date, java.util.Date)}.
	 */
	@Test
	public void testGetDurationStringDateDate() throws Exception {
		Date start = new Date(),
				end = null;
		try{ 
			DateUtils.getDurationString(start, null);
		} catch (MissingArgumentException mae) {
			//expected
		}

		try{ 
			DateUtils.getDurationString(null, start);
		} catch (MissingArgumentException mae) {
			//expected
		}
		
		end = new Date();
		assertNotNull("invalid duration", DateUtils.getDurationString(start, end));
	}

	/**
	 * Test method for {@link com.bitsanity.bitchange.utils.DateUtils#getDurationString(java.time.Instant, java.time.Instant)}.
	 */
	@Test
	public void testGetDurationStringInstantInstant() throws Exception {
		Instant start = Instant.now(),
				end = null;
		try{ 
			DateUtils.getDurationString(start, null);
		} catch (MissingArgumentException mae) {
			//expected
		}

		try{ 
			DateUtils.getDurationString(null, start);
		} catch (MissingArgumentException mae) {
			//expected
		}
		
		end = Instant.now();
		assertNotNull("invalid duration", DateUtils.getDurationString(start, end));
	}

}
