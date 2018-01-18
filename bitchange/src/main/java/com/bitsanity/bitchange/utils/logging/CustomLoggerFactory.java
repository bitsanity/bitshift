package com.bitsanity.bitchange.utils.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.LoggerContext;

/**
 * 
 * Custom AUDIT logger factory.
 * 
 * For better implementation look to:  http://www.slf4j.org/faq.html#slf4j_compatible which say:
 *		In summary, to create an SLF4J binding for your logging system, follow these steps:
 *			1. start with a copy of an existing module,
 *			2. create an adapter between your logging system and org.slf4j.Logger interface
 *			3. create a factory for the adapter created in the previous step,
 *			4. modify StaticLoggerBinder class to use the factory you created in the previous step
 *
 * See code example at: https://github.com/qos-ch/slf4j/tree/master/slf4j-jcl/src/main/java/org/slf4j/impl
 *
 */
public class CustomLoggerFactory {

	static {
		//Hadoop storm breaks this by using org.apache.logging.slf4j.Log4jLoggerFactory
		ILoggerFactory logger = StaticLoggerBinder.getSingleton().getLoggerFactory();
		if (logger instanceof LoggerContext) {
			((LoggerContext)logger).getFrameworkPackages().add("com.bitsanity.bitchange.utils.logging");
		}
	}
	
	/**
	 * Return a logger named according to the name parameter using the
	 * statically bound {@link ILoggerFactory} instance.
	 *
	 * @param name
	 *            The name of the logger.
	 * @return logger
	 */
	public static Logger getLogger(String name) {
		return new AuditLogger(LoggerFactory.getLogger(name));
	}

	/**
	 * Return a logger named corresponding to the class passed as parameter
	 * 
	 * @param clazz
	 *            the returned logger will be named after clazz
	 * @return logger
	 *
	 *
	 */
	public static Logger getLogger(Class<?> clazz) {
		return new AuditLogger(LoggerFactory.getLogger(clazz));
	}
}
