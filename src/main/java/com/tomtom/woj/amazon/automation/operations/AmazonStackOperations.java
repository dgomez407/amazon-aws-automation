package com.tomtom.woj.amazon.automation.operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.GetTemplateRequest;
import com.amazonaws.services.cloudformation.model.GetTemplateResult;
import com.amazonaws.services.cloudformation.model.ListStacksRequest;
import com.amazonaws.services.cloudformation.model.ListStacksResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackSummary;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;

public class AmazonStackOperations {

	private static AmazonCloudFormationClient client;

	public AmazonStackOperations() {
	}
	
	public AmazonStackOperations(String credentialsFileName) {
		loadAwsCredentialsFile(credentialsFileName);
	}

	public void loadAwsCredentialsFile(String credentialsFileName) {
		AWSCredentials credentials = loadCredentials(credentialsFileName);
		client = new AmazonCloudFormationClient(credentials);
	}

	public void createStack(String stackName, String templateBody,
			Map<String, String> parameterValues) {
		checkIfCredentialsFileIsSpecified();
		
		System.err.println("Never tested!");
		
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		for (Entry<String, String> entry : parameterValues.entrySet()) {
			Parameter parameter = new Parameter();
			parameter.setParameterKey(entry.getKey());
			parameter.setParameterValue(entry.getValue());
			parameters.add(parameter);
		}

		CreateStackRequest createStackRequest = new CreateStackRequest();
		createStackRequest.setStackName(stackName);
		createStackRequest.setTemplateBody(templateBody);
		createStackRequest.setParameters(parameters);
		client.createStack(createStackRequest);

		// wait for operation to complete
		waitForStackToCompleteTheUpdate(stackName);
	}

	public void deleteStack(String stackName) {
		checkIfCredentialsFileIsSpecified();
		
		DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
		deleteStackRequest.setStackName(stackName);
		client.deleteStack(deleteStackRequest);
	}

	public List<String> getStartedStackNames() {
		checkIfCredentialsFileIsSpecified();
		
		List<String> stackStatusFilters = new ArrayList<String>();
		stackStatusFilters.add("UPDATE_COMPLETE");
		ListStacksRequest listStacksRequest = new ListStacksRequest();
		listStacksRequest.setStackStatusFilters(stackStatusFilters);
		ListStacksResult result = client.listStacks(listStacksRequest);
		List<StackSummary> stackSummaries = result.getStackSummaries();
		List<String> stackNames = new ArrayList<String>();
		for (StackSummary summary : stackSummaries) {
			stackNames.add(summary.getStackName());
		}
		return stackNames;
	}

	public Map<String, String> getStackParameters(String stackName) {
		checkIfCredentialsFileIsSpecified();
		
		DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
		describeStacksRequest.setStackName(stackName);
		DescribeStacksResult describeStacksResult = client
				.describeStacks(describeStacksRequest);
		List<Stack> stacks = describeStacksResult.getStacks();
		Stack stack = stacks.get(0);
		List<Parameter> parameters = stack.getParameters();
		Map<String, String> map = new LinkedHashMap<String, String>();
		System.out.println("parameters of stack: " + stackName);
		for (Parameter parameter : parameters) {
			map.put(parameter.getParameterKey(), parameter.getParameterValue());
		}
		return map;
	}

	public void updateStackParameters(String stackName,
			Map<String, String> newParameterValues) {
		checkIfCredentialsFileIsSpecified();

		// get stack parameters
		DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
		describeStacksRequest.setStackName(stackName);
		DescribeStacksResult describeStacksResult = client
				.describeStacks(describeStacksRequest);
		List<Stack> stacks = describeStacksResult.getStacks();
		Stack stack = stacks.get(0);
		List<Parameter> parameters = stack.getParameters();

		// get stack template body
		GetTemplateRequest getTemplateRequest = new GetTemplateRequest();
		getTemplateRequest.setStackName(stackName);
		GetTemplateResult getTemplateResult = client
				.getTemplate(getTemplateRequest);
		String templateBody = getTemplateResult.getTemplateBody();

		boolean updated=false;
		
		// change parameter list
		for (Entry<String, String> entry : newParameterValues.entrySet()) {
			updated = updateParameterInList(parameters, entry.getKey(), entry.getValue()) || updated;
		}
		
		if(!updated) {
			System.out.println("no parameters to update");
			return;
		}

		// update stack
		UpdateStackRequest updateStackRequest = new UpdateStackRequest();
		updateStackRequest.setStackName(stackName);
		updateStackRequest.setParameters(parameters);
		updateStackRequest.setTemplateBody(templateBody);
		client.updateStack(updateStackRequest);

		// wait for operation to complete
		waitForStackToCompleteTheUpdate(stackName);
	}

	private void checkIfCredentialsFileIsSpecified() {
		if(client==null) {
			throw new CredentialsFileNotSpecifiedException();
		}
	}

	private void waitForStackToCompleteTheUpdate(String stackName) {
		DescribeStacksRequest describeStacksRequest;
		DescribeStacksResult describeStacksResult;
		List<Stack> stacks;
		Stack stack;
		while (true) {
			describeStacksRequest = new DescribeStacksRequest();
			describeStacksRequest.setStackName(stackName);
			describeStacksResult = client.describeStacks(describeStacksRequest);
			stacks = describeStacksResult.getStacks();
			stack = stacks.get(0);
			if ("UPDATE_COMPLETE".equals(stack.getStackStatus())) {
				break;
			}
			System.out.println("waiting for operation to complete");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
	}

	private boolean updateParameterInList(List<Parameter> parameters, String key,
			String newValue) {
		for (Parameter parameter : parameters) {
			if (key.equals(parameter.getParameterKey())) {
				if(!parameter.getParameterValue().equals(newValue)) {
					parameter.setParameterValue(newValue);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	private AWSCredentials loadCredentials(String path) {
		File fin = new File(path);
		AWSCredentials creds = null;
		try {
			creds = new PropertiesCredentials(fin);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return creds;
	}
}
