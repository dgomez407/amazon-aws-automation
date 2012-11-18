package com.tomtom.woj.amazon.automation;

import java.io.IOException;
import java.util.ArrayList;

import com.tomtom.woj.amazon.automation.groovy.GroovyScriptExecutor;

public class Main {
	
	public static void main(String[] args) throws IOException {
		if(args.length<1) {
			displayHelp();
			System.exit(1);
		}
		
		String awsCredentialsFileName = null; //"src/test/resources/credentials/example_credentials.txt";
		String scriptFileName = args[0]; //"src/test/resources/scripts/ExampleScript.groovy";
		
		ArrayList<String> argsArray = new ArrayList<String>();
		for(int i=1; i<args.length; i++) {
			if("--credentials".equals(args[i])) {
				awsCredentialsFileName = args[i+1];
				i+=2;
			} else {
				argsArray.add(args[i]);
			}
		}
		
		GroovyScriptExecutor.executeAmazonGroovyScript(awsCredentialsFileName, scriptFileName, argsArray);
	 }

	private static void displayHelp() {
		System.out.println("\n" +
				"Please provide at least the script name as a parameter. " +
				"Credentials parameter is optional because you can load credentials from within the script. " +
				"If you use command line credentials parameter then it will override the credentials loaded from the script. " +
				"You can pass the command line parameters to the script. They will be available in a list called args.\n\n" +
				"Usage:\n" +
				"\tjava -jar amazon-aws-automation-x.x.jar SCRIPT_NAME [--credentials CREDENTIALS_FILE] [SCRITPT ARGS]\n");
	}

}
