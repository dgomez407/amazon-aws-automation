package com.tomtom.woj.amazon.automation.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.tomtom.woj.amazon.automation.operations.AmazonEc2Operations;
import com.tomtom.woj.amazon.automation.operations.AmazonStackOperations;

public class GroovyScriptExecutor {

	public static void executeAmazonGroovyScript(
			String awsCredentialsFileName, String scriptFileName)
			throws IOException {
		executeAmazonGroovyScript(awsCredentialsFileName, scriptFileName, new ArrayList<String>());
	}
	
	public static void executeAmazonGroovyScript(
			String awsCredentialsFileName, String scriptFileName, List<String> args)
			throws IOException {
		
		Binding binding = new Binding();
		
		AmazonStackOperations amazonExecutor;
		AmazonEc2Operations amazonEc2Operations;
		
		// when credentials will be given from command line they will override credentials loaded from the script
		// any attempt to load credentials from the script will be ignored
		if(awsCredentialsFileName!=null) {
			amazonExecutor = new AmazonStackOperations(awsCredentialsFileName);
			amazonEc2Operations = new AmazonEc2Operations(awsCredentialsFileName);
			binding.setProperty(ScriptBaseClass.IGNORE_CREDENTIALS_COMMAND_PROPERTY_NAME, true);
		} else {
			amazonExecutor = new AmazonStackOperations();
			amazonEc2Operations = new AmazonEc2Operations();
			binding.setProperty(ScriptBaseClass.IGNORE_CREDENTIALS_COMMAND_PROPERTY_NAME, false);
		}

		binding.setProperty(ScriptBaseClass.AMAZON_STACK_OPERATIONS_PROPERTY_NAME, amazonExecutor);
		binding.setProperty(ScriptBaseClass.AMAZON_EC2_OPERATIONS_PROPERTY_NAME, amazonEc2Operations);
		binding.setVariable(ScriptBaseClass.ARGS_VARIABLE_NAME, args);
		
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
	    compilerConfiguration.setScriptBaseClass(ScriptBaseClass.class.getName());
	    
		GroovyShell groovyShell = new GroovyShell(binding, compilerConfiguration);
		binding.setProperty(ScriptBaseClass.GROOVY_SHELL_PROPERTY_NAME, groovyShell);
		groovyShell.evaluate(new File(scriptFileName));
	}
}
