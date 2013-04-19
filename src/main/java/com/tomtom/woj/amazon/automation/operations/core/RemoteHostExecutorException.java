package com.tomtom.woj.amazon.automation.operations.core;

/**
 * Exception thrown by {@link RemoteHostExecutor}
 * 
 */
public class RemoteHostExecutorException extends Exception {
	private static final long serialVersionUID = 2848789399173895713L;

	private final String message;

	public RemoteHostExecutorException(String message) {
		this.message = message;
	}

	public RemoteHostExecutorException(String message, Exception cause) {
		this.message = message;
		this.initCause(cause);
	}

	@Override
	public String getMessage() {
		return message;
	}
}
