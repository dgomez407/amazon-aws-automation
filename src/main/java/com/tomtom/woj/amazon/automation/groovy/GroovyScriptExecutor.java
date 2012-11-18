package com.tomtom.woj.amazon.automation.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.control.CompilerConfiguration;

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
		AmazonStackOperations amazonExecutor;
		
		if(awsCredentialsFileName!=null) {
			amazonExecutor = new AmazonStackOperations(awsCredentialsFileName);
		} else {
			amazonExecutor = new AmazonStackOperations();
		}

		Binding binding = new Binding();
		binding.setProperty(ScriptBaseClass.AMAZON_EXECUTOR_PROPERTY_NAME, amazonExecutor);
		binding.setVariable(ScriptBaseClass.ARGS_VARIABLE_NAME, args);
		
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
	    compilerConfiguration.setScriptBaseClass(ScriptBaseClass.class.getName());
	    
		GroovyShell groovyShell = new GroovyShell(binding, compilerConfiguration);
		binding.setProperty(ScriptBaseClass.GROOVY_SHELL_PROPERTY_NAME, groovyShell);
		groovyShell.evaluate(new File(scriptFileName));
	}
}
