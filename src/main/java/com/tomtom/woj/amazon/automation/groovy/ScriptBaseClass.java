package com.tomtom.woj.amazon.automation.groovy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tomtom.woj.amazon.automation.operations.AmazonStackOperations;

import groovy.json.JsonSlurper;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public abstract class ScriptBaseClass extends Script {

	public static final String AMAZON_EXECUTOR_PROPERTY_NAME = "amazonExecutor";
	public static final String GROOVY_SHELL_PROPERTY_NAME = "shell";
	public static final String ARGS_VARIABLE_NAME = "args";
	public static final String IGNORE_CREDENTIALS_COMMAND_PROPERTY_NAME = "ignoreCredentialsCommand";
	
	public void includeGroovyScript(String scriptFileName) throws IOException {
		GroovyShell groovyShell = (GroovyShell) getProperty(GROOVY_SHELL_PROPERTY_NAME);
		groovyShell.evaluate(new File(scriptFileName));
		
	}
	
	public Object loadJsonFile(String jsonFileName) throws FileNotFoundException {
		JsonSlurper slurper = new groovy.json.JsonSlurper();
		return slurper.parse(new FileReader(jsonFileName));
	}
	
	public void loadAwsCredentialsFile(String credentialsFileName) {
		boolean ignoreCredentialsCommand = (Boolean) getProperty(IGNORE_CREDENTIALS_COMMAND_PROPERTY_NAME);
		if(ignoreCredentialsCommand) {
			System.out.println("ignored load credentials command");
			return;
		}
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		executor.loadAwsCredentialsFile(credentialsFileName);
	}

	public void createStack(String stackName, String templateBody,
			Map<String, Object> parameterValues) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		executor.createStack(stackName, templateBody, convertMapObjectToString(parameterValues));
	}
	
	public void deleteStack(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		executor.deleteStack(stackName);
	}
	
	public void listStacks() {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		List<String> names = executor.getStartedStackNames();
		for(String name : names) {
			System.out.println(name);
		}
	}
	
	public void listStackParameters(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		Map<String, String> parameters = executor.getStackParameters(stackName);
    	for(Entry<String, String> entry : parameters.entrySet()) {
    		System.out.println(entry.getKey() + " : " + entry.getValue());
    	}
	}
	
	public void updateStackParameters(String stackName, Map<String, Object> newParameterValues) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		executor.updateStackParameters(stackName, convertMapObjectToString(newParameterValues));
	}
	
	private Map<String,String> convertMapObjectToString(Map<String,Object> map) {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		for(Entry<String, Object> entry : map.entrySet()) {
			output.put(entry.getKey(), entry.getValue().toString());
		}
		return output;
	}
}
