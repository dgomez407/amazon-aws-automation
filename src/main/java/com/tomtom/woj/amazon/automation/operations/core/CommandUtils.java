package com.tomtom.woj.amazon.automation.operations.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for commands.
 * 
 */
public class CommandUtils {
	private final static Logger logger = LoggerFactory
			.getLogger(CommandUtils.class);

	private CommandUtils() {
	}

	/**
	 * Convert command in list of String to command representation in one
	 * String.
	 * 
	 * @param command
	 *            command and parameters in list
	 * @return command representation in one String
	 */
	public static String convertCommandToString(List<String> command) {
		StringBuilder sb = new StringBuilder();
		for (String commandPart : command) {
			sb.append(commandPart).append(" ");
		}
		return sb.deleteCharAt(sb.lastIndexOf(" ")).toString();
	}

	/**
	 * With use of md5sum command compare local version of file and version on
	 * remote host after scp command.
	 * 
	 * @param localFilePath
	 *            path to local version of file
	 * @param remoteFilePath
	 *            path to copied version of file
	 * @param remoteHostExecutor
	 *            remote host executor
	 * @return boolean
	 * @throws LocalHostExecutorException
	 * @throws RemoteHostExecutorException
	 */
	public static boolean checkFileAfterScp(
			RemoteHostExecutor remoteHostExecutor, String localFilePath,
			String remoteFilePath) throws LocalHostExecutorException,
			RemoteHostExecutorException {
		String inSum = getLocalMd5Sum(localFilePath);
		String outSum = getRemoteMd5Sum(remoteHostExecutor, remoteFilePath);

		logger.info("{} <=> {}", inSum, outSum);
		if ("".equals(inSum) || "".equals(outSum)) {
			throw new RuntimeException("Invalid MD5 sum");
		}
		return inSum.equals(outSum);
	}

	private static String getRemoteMd5Sum(
			RemoteHostExecutor remoteHostExecutor, String filePath)
			throws RemoteHostExecutorException {
		return getMd5Sum(remoteHostExecutor
				.execWithOutput("md5sum " + filePath));
	}

	private static String getLocalMd5Sum(String filePath)
			throws LocalHostExecutorException {
		List<String> md5SumCommand = new ArrayList<String>();
		md5SumCommand.add("md5sum");
		md5SumCommand.add(filePath);
		return getMd5Sum(LocalHostExecutor.execWithOutput(md5SumCommand));
	}

	private static String getMd5Sum(String md5Output) {
		return md5Output.split("\\s")[0];
	}
}
