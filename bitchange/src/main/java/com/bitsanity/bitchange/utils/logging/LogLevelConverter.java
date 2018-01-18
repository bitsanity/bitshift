package com.bitsanity.bitchange.utils.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class LogLevelConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		if (event.getMarker() != null) {
			return event.getMarker().toString();
		}
		
		return event.getLevel().toString();
	}

}
