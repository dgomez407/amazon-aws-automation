package com.tomtom.woj.amazon.automation.operations.core;

/**
 * Object representing a remote host.
 */
public class RemoteHost {
	private String endPoint;
	private String remoteUser;
	private String privateKeyPath;

	@Override
	public String toString() {
		return "RemoteHost [" + remoteUser + "@" + endPoint + "]";
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getPrivateKeyPath() {
		return privateKeyPath;
	}

	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath = privateKeyPath;
	}
}
