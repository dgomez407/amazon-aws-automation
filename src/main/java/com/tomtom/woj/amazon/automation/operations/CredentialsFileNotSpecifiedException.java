package com.tomtom.woj.amazon.automation.operations;

public class CredentialsFileNotSpecifiedException extends RuntimeException {

	private static final long serialVersionUID = -3369417805310503663L;

	public CredentialsFileNotSpecifiedException() {
		super("Credentials file not specified");
	}

}
