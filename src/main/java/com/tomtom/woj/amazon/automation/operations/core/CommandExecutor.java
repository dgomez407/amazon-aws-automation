package com.tomtom.woj.amazon.automation.operations.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Executes commands.
 * 
 * 
 */
public class CommandExecutor {

	private CommandExecutor() {
	}

	/**
	 * Executes command in a given directory.
	 */
	public static void executeCommand(File directory, String... params) throws IOException, InterruptedException {
		ProcessBuilder b = new ProcessBuilder().command(params).directory(directory);
		Process p = b.start();
		new Thread(new StreamRedirector(p.getErrorStream(), true)).start();
		new Thread(new StreamRedirector(p.getInputStream(), false)).start();
		p.waitFor();
		checkProcessExitValue(p);
		p.destroy();
	}

	private static void checkProcessExitValue(Process p) {
		if (p.exitValue() != 0) {
			throw new RuntimeException("The exit value for the subprocess was abnormal");
		}
	}

	/**
	 * Executes command in a given directory. No output redirection is done.
	 */
	public static void executeSilentCommand(File directory, String... params) throws IOException, InterruptedException {
		ProcessBuilder b = new ProcessBuilder().command(params).directory(directory);
		Process p = b.start();
		p.waitFor();
		checkProcessExitValue(p);
	}

	/**
	 * Executes command in a given directory. Standard output is saved into a buffer, error output is discarded.
	 * 
	 * @return command output
	 * @throws ExecutionException
	 */
	public static String executeBufferedCommand(File directory, String... params) throws IOException, InterruptedException, ExecutionException {
		ProcessBuilder b = new ProcessBuilder().command(params).directory(directory);
		ExecutorService exec = null;
		try {
			Process p = b.start();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			exec = Executors.newFixedThreadPool(1);
			Future<String> outputResult = exec.submit(new BufferedStreamRedirector(p.getInputStream(), output));
			p.waitFor();
			String outputMessage = outputResult.get();
			checkProcessExitValue(p);
			return outputMessage;
		} finally {
			if (exec != null) {
				exec.shutdownNow();
			}
		}
	}

	/**
	 * Redirects stream to standard output and error output.
	 */
	private static class StreamRedirector implements Runnable {
		private InputStream in;
		private boolean error;
		private byte[] buffer = new byte[1024];

		public StreamRedirector(InputStream in, boolean error) {
			this.in = in;
			this.error = error;
		}

		public void run() {
			try {
				int read;
				while ((read = in.read(buffer)) != -1) {
					if (error) {
						System.err.print(new String(buffer, 0, read));
					} else {
						System.out.print(new String(buffer, 0, read));
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Redirects stream to given buffer.
	 */
	private static class BufferedStreamRedirector implements Callable<String> {
		private InputStream in;
		private ByteArrayOutputStream out;
		private byte[] buffer = new byte[1024];

		public BufferedStreamRedirector(InputStream in, OutputStream out) {
			this.in = in;
			this.out = new ByteArrayOutputStream();
		}

		@Override
		public String call() throws Exception {
			try {
				int read;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return out.toString();
		}
	}

}
