package com.tomtom.woj.amazon.automation.operations.core;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Executes commands on remote host.
 * 
 */
public class RemoteHostExecutor {
	private static final Logger logger = Logger
			.getLogger(RemoteHostExecutor.class);
	private final RemoteHost remoteHost;
	private File workingDir;

	public RemoteHostExecutor(RemoteHost remoteHost, File workingDir) {
		this.remoteHost = remoteHost;
		this.workingDir = workingDir;
	}

	/**
	 * Executes command on remote host.
	 * 
	 * @param command
	 *            command which will be execute on remote host
	 * @throws RemoteHostExecutorException
	 */
	public void exec(String command) throws RemoteHostExecutorException {
		List<String> remoteCommand = new ArrayList<String>();
		prepareRemoteSshCommand(command, remoteCommand);
		tryExecuteCommand(remoteCommand);
	}

	/**
	 * Executes command on remote host and return command output in String.
	 * 
	 * @param command
	 *            command which will be execute on remote host
	 * @return command output in String
	 * @throws RemoteHostExecutorException
	 */
	public String execWithOutput(String command)
			throws RemoteHostExecutorException {
		List<String> remoteCommand = new ArrayList<String>();
		prepareRemoteSshCommand(command, remoteCommand);
		return tryExecuteBufferedCommand(remoteCommand);
	}

	/**
	 * Send file by scp to remote host.
	 * 
	 * @param localPath
	 *            path to file which will be send
	 * @param remotePath
	 *            path to which file will be copy on remote host
	 * @throws RemoteHostExecutorException
	 */
	public void send(String localPath, String remotePath)
			throws RemoteHostExecutorException {
		List<String> remoteCommand = new ArrayList<String>();
		remoteCommand.add("scp");
		remoteCommand.add("-oStrictHostKeyChecking=no");
		addPrivateKey(remoteCommand);
		remoteCommand.add(localPath);
		remoteCommand.add(getRemoteHost().getRemoteUser() + "@"
				+ getRemoteHost().getEndPoint() + ':' + remotePath);
		tryExecuteCommand(remoteCommand);
	}

	private void addPrivateKey(List<String> command) {
		if (isNotEmpty(getRemoteHost().getPrivateKeyPath())) {
			command.add("-i");
			command.add(getRemoteHost().getPrivateKeyPath());
		}
	}

	private void prepareRemoteSshCommand(String command,
			List<String> remoteCommand) {
		remoteCommand.add("ssh");
		remoteCommand.add("-oStrictHostKeyChecking=no");
		addPrivateKey(remoteCommand);
		remoteCommand.add(getRemoteHost().getRemoteUser() + "@"
				+ getRemoteHost().getEndPoint());
		remoteCommand.add(command);
	}

	private void tryExecuteCommand(List<String> command)
			throws RemoteHostExecutorException {
		try {
			logger.info(CommandUtils.convertCommandToString(command));
			CommandExecutor.executeCommand(workingDir,
					command.toArray(new String[command.size()]));
		} catch (Exception e) {
			throw new RemoteHostExecutorException(
					"Failed during execute command: "
							+ CommandUtils.convertCommandToString(command), e);
		}
	}

	private String tryExecuteBufferedCommand(List<String> command)
			throws RemoteHostExecutorException {
		try {
			logger.info(CommandUtils.convertCommandToString(command));
			return CommandExecutor.executeBufferedCommand(workingDir,
					command.toArray(new String[command.size()]));
		} catch (Exception e) {
			throw new RemoteHostExecutorException(
					"Failed during execute buffered command: "
							+ CommandUtils.convertCommandToString(command), e);
		}
	}

	public String getWorkingDir() {
		return workingDir.getAbsolutePath();
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = new File(workingDir);
	}

	public RemoteHost getRemoteHost() {
		return remoteHost;
	}

}
