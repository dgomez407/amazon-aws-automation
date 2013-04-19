package com.tomtom.woj.amazon.automation.operations.core;

/**
 * Exception thrown by {@link LocalHostExecutor}
 * 
 */
public class LocalHostExecutorException extends Exception {
	private static final long serialVersionUID = 5158924160171892353L;

	private final String message;

	public LocalHostExecutorException(String message) {
		this.message = message;
	}

	public LocalHostExecutorException(String message, Exception cause) {
		this.message = message;
		this.initCause(cause);
	}

	@Override
	public String getMessage() {
		return message;
	}
}
