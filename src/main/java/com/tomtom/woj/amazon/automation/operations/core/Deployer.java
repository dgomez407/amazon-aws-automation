package com.tomtom.woj.amazon.automation.operations.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deploys the test configuration to Amazon
 * 
 */
public class Deployer {
	private final static Logger logger = LoggerFactory
			.getLogger(Deployer.class);

	private final static int MAX_RETRIES = 4;
	private final static int SLEEP_TIME_BEFORE_RETRY = 5000;

	private RemoteHostExecutor remoteHostExecutor;

	public Deployer(RemoteHost remoteHost, String workingDir) {
		this.remoteHostExecutor = new RemoteHostExecutor(remoteHost, new File(
				workingDir));
	}

	public void runRemoteCommand(Collection<String> commands) {
		logger.info("Running commands{} on {}...", commands,
				remoteHostExecutor.getRemoteHost());
		try {
			runCustomScripts(remoteHostExecutor, commands);
		} catch (Exception e) {
			throw new RuntimeException("Failed during deployment on "
					+ remoteHostExecutor.getRemoteHost(), e);

		}

	}

	public void uploadFiles(String rootDir, Map<String, String> deploymentPaths) {
		logger.info("Deploying {} on {}...", deploymentPaths,
				remoteHostExecutor.getRemoteHost());
		try {
			uploadConfigFiles(remoteHostExecutor, deploymentPaths, rootDir);
		} catch (Exception e) {
			throw new RuntimeException("Failed during deployment on "
					+ remoteHostExecutor.getRemoteHost(), e);
		}
	}

	private void uploadConfigFiles(RemoteHostExecutor remoteHostExecutor,
			Map<String, String> deploymentPaths, String rootDir)
			throws Exception {

		for (Entry<String, String> entry : deploymentPaths.entrySet()) {
			File path = new File(entry.getKey());
			if (!path.isAbsolute()) {
				path = new File(rootDir, entry.getKey());
			}
			sendWithCheckToRemoteHost(remoteHostExecutor,
					path.getAbsolutePath(), entry.getValue());
		}
	}

	private void runCustomScripts(RemoteHostExecutor remoteHostExecutor,
			Collection<String> commands) throws IOException,
			RemoteHostExecutorException {
		for (String line : commands) {

			remoteHostExecutor.exec(line);
		}
	}

	private void sendWithCheckToRemoteHost(
			RemoteHostExecutor remoteHostExecutor, String localFilePath,
			String remoteFilePath) throws Exception {
		logger.info("Sending file '" + localFilePath + "' with check...");

		for (int retries = 0; retries < MAX_RETRIES; retries++) {
			try {
				if (retries != 0) {
					logger.info("Retrying...");
					Thread.sleep(SLEEP_TIME_BEFORE_RETRY);
				}
				remoteHostExecutor.send(localFilePath, remoteFilePath);
				break;
			} catch (RemoteHostExecutorException e) {
				logger.info("Sendind failed after " + retries + " tries.");
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		if (!CommandUtils.checkFileAfterScp(remoteHostExecutor, localFilePath,
				remoteFilePath)) {
			throw new RemoteHostExecutorException(
					"Filed to copy file on to remote host");
		}
		logger.info("File sent");
	}

}
