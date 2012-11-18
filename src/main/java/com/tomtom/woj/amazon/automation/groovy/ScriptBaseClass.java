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

	public void loadAwsCredentialsFile(String credentialsFileName) {
		boolean ignoreCredentialsCommand = (Boolean) getProperty(IGNORE_CREDENTIALS_COMMAND_PROPERTY_NAME);
		if(ignoreCredentialsCommand) {
			System.out.println("ignored load credentials command");
			return;
		}
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		executor.loadAwsCredentialsFile(credentialsFileName);
	}

	public Object loadJsonText(String text) {
		JsonSlurper slurper = new groovy.json.JsonSlurper();
		return slurper.parseText(text);
	}

	public Object loadJsonFile(String jsonFileName) throws FileNotFoundException {
		JsonSlurper slurper = new groovy.json.JsonSlurper();
		return slurper.parse(new FileReader(jsonFileName));
	}
	
	public void createStackText(String stackName, String templateBody,
			Map<String, Object> parameterValues) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		executor.createStack(stackName, templateBody, convertMapObjectToString(parameterValues));
	}
	
	public void createStackFile(String stackName, String templateFileName,
			Map<String, Object> parameterValues) throws IOException {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		File templateFile = new File(templateFileName);
		executor.createStack(stackName, templateFile, convertMapObjectToString(parameterValues));
	}
	
	public String getStackStatus(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		return executor.getStackStatus(stackName);
	}
	
	public Map<String, String> getStackOutputs(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		return executor.getStackOutputs(stackName);
	}
	
	public List<String> getStackCapabilities(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		return executor.getStackCapabilities(stackName);
	}
	
	public Map<String, String> getStackResourceIds(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		return executor.getStackResourceIds(stackName);
	}
	
	public String getStackTemplateBody(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		return executor.getStackTemplateBody(stackName);
	}
	
	public void deleteStack(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		executor.deleteStack(stackName);
	}
	
	public List<String> getStartedStackNames() {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		return executor.getStartedStackNames();
	}
	
	public Map<String, String> getStackParameters(String stackName) {
		AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_EXECUTOR_PROPERTY_NAME);
		return executor.getStackParameters(stackName);
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
	
	public void printList(List<Object> list) {
		for(Object item : list) {
			System.out.println(item);
		}
	}
	
	public void printMap(Map<String, Object> map) {
    	for(Entry<String, Object> entry : map.entrySet()) {
    		System.out.println(entry.getKey() + " : " + entry.getValue());
    	}
	}

}
