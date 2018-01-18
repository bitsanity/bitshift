/**
 * 
 */
package com.bitsanity.bitchange.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.bitsanity.bitchange.lang.MissingArgumentException;

/**
 * @author billsa
 *
 */
public class DateUtils {

	public static String getDurationString(Date start, Date end) throws MissingArgumentException {
		if (start == null) {
			throw new MissingArgumentException("Start date is required argument.");
		}
		
		if (end == null) {
			throw new MissingArgumentException("End date is required argument.");
		}

		return getDurationString(start.toInstant(), end.toInstant());
	}

	public static String getDurationString(Instant start, Instant end) throws MissingArgumentException { 
		if (start == null) {
			throw new MissingArgumentException("Start instant is required argument.");
		}
		
		if (end == null) {
			throw new MissingArgumentException("End instant is required argument.");
		}

		return getDurationString(Duration.between(start, end));
	}

	public static String getDurationString(Duration duration) throws MissingArgumentException { 
		if (duration == null) {
			throw new MissingArgumentException("Duration is required argument.");
		}
		
		return String.format("%d days, %d hrs, %d min, %.4f sec", duration.toDays(), 
				duration.minus(duration.toDays(), ChronoUnit.DAYS).toHours(),
				duration.minus(duration.toHours(), ChronoUnit.HOURS).toMinutes(),
				(duration.minus(duration.toMinutes(), ChronoUnit.MINUTES).toMillis() / 1000.0f)
				);

	}
}
