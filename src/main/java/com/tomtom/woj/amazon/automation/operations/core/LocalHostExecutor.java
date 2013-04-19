package com.tomtom.woj.amazon.automation.operations.core;

import java.io.File;
import java.util.List;

/**
 * Static methods useful for execute commands.
 * 
 */
public class LocalHostExecutor {
	private static final File CURRENT_DIR = new File(System.getenv("HOME"));

	private LocalHostExecutor() {
	}

	/**
	 * Executes command.
	 * 
	 * @param command
	 *            command which will be execute on remote host
	 * @throws LocalHostExecutorException
	 */
	public static void exec(List<String> command) throws LocalHostExecutorException {
		try {
			CommandExecutor.executeCommand(CURRENT_DIR, command.toArray(new String[command.size()]));
		} catch (Exception e) {
			throw new LocalHostExecutorException("Failed during execute command: " + CommandUtils.convertCommandToString(command), e);
		}
	}

	/**
	 * Executes command.
	 * 
	 * @param command
	 *            command which will be execute on remote host
	 * @return command output in String
	 * @throws LocalHostExecutorException
	 */
	public static String execWithOutput(List<String> command) throws LocalHostExecutorException {
		try {
			return CommandExecutor.executeBufferedCommand(CURRENT_DIR, command.toArray(new String[command.size()]));
		} catch (Exception e) {
			throw new LocalHostExecutorException("Failed during execute command: " + CommandUtils.convertCommandToString(command), e);
		}
	}
}
