package com.tomtom.woj.amazon.automation.operations.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static methods useful in all application.
 * 
 */
public class GeneralUtils {
	private final static Logger logger = LoggerFactory.getLogger(GeneralUtils.class);
	private static final int MILIS_TO_SECONDS = 1000;

	private GeneralUtils() {
	}

	/**
	 * Wait for given number of seconds. Do nothing (aside from logging the event) on sleep interruption.
	 * 
	 * @param seconds
	 */
	public static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * MILIS_TO_SECONDS);
		} catch (InterruptedException e) {
			logger.trace("Sleep interrupted.", e);
		}
	}

}
