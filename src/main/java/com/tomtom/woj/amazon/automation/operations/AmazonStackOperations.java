package com.tomtom.woj.amazon.automation.operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.GetTemplateRequest;
import com.amazonaws.services.cloudformation.model.GetTemplateResult;
import com.amazonaws.services.cloudformation.model.ListStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.ListStackResourcesResult;
import com.amazonaws.services.cloudformation.model.ListStacksRequest;
import com.amazonaws.services.cloudformation.model.ListStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResourceSummary;
import com.amazonaws.services.cloudformation.model.StackSummary;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;

public class AmazonStackOperations {

	private static final int COMPLETE_OPERATION_WAITING_TIME_MS = 10000; 
	
	private AmazonCloudFormationClient client;

	public AmazonStackOperations() {
	}
	
	public AmazonStackOperations(String credentialsFileName) {
		loadAwsCredentialsFile(credentialsFileName);
	}

	public void loadAwsCredentialsFile(String credentialsFileName) {
		AWSCredentials credentials = loadCredentials(credentialsFileName);
		client = new AmazonCloudFormationClient(credentials);
	}

	public void createStack(String stackName, File templateFile,
			Map<String, String> parameterValues) throws IOException {
		String templateBody = FileUtils.readFileToString(templateFile);
		createStack(stackName, templateBody, parameterValues);
	}
	
	public void createStack(String stackName, String templateBody,
			Map<String, String> parameterValues) {
		checkIfCredentialsFileIsSpecified();
		
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

		waitForStackToCompleteTheOperation(stackName, false);
	}

	public String getStackStatus(String stackName) {
		DescribeStacksRequest describeStacksRequest;
		DescribeStacksResult describeStacksResult;
		List<Stack> stacks;
		Stack stack;
		describeStacksRequest = new DescribeStacksRequest();
		describeStacksRequest.setStackName(stackName);
		describeStacksResult = client.describeStacks(describeStacksRequest);
		stacks = describeStacksResult.getStacks();
		stack = stacks.get(0);
		return stack.getStackStatus();
	}
	
	public String getStackTemplateBody(String stackName) {
		checkIfCredentialsFileIsSpecified();

		// get stack template body
		GetTemplateRequest getTemplateRequest = new GetTemplateRequest();
		getTemplateRequest.setStackName(stackName);
		GetTemplateResult getTemplateResult = client
				.getTemplate(getTemplateRequest);
		return getTemplateResult.getTemplateBody();
	}
	
	public void deleteStack(String stackName) {
		checkIfCredentialsFileIsSpecified();
		
		DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
		deleteStackRequest.setStackName(stackName);
		client.deleteStack(deleteStackRequest);
		
		// wait to delete the stack (ignore error as stack might not exist after deleting)
		waitForStackToCompleteTheOperation(stackName, true);
	}

	public List<String> getStartedStackNames() {
		checkIfCredentialsFileIsSpecified();
		
		List<String> stackStatusFilters = new ArrayList<String>();
		stackStatusFilters.add("UPDATE_COMPLETE");
		stackStatusFilters.add("CREATE_COMPLETE");
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
		for (Parameter parameter : parameters) {
			map.put(parameter.getParameterKey(), parameter.getParameterValue());
		}
		return map;
	}

	public Map<String, String> getStackOutputs(String stackName) {
		checkIfCredentialsFileIsSpecified();
		
		DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
		describeStacksRequest.setStackName(stackName);
		DescribeStacksResult describeStacksResult = client
				.describeStacks(describeStacksRequest);
		List<Stack> stacks = describeStacksResult.getStacks();
		Stack stack = stacks.get(0);
		List<Output> outputs = stack.getOutputs();
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (Output output : outputs) {
			map.put(output.getOutputKey(), output.getOutputValue());
		}
		return map;
	}

	public List<String> getStackCapabilities(String stackName) {
		checkIfCredentialsFileIsSpecified();
		
		DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
		describeStacksRequest.setStackName(stackName);
		DescribeStacksResult describeStacksResult = client
				.describeStacks(describeStacksRequest);
		List<Stack> stacks = describeStacksResult.getStacks();
		Stack stack = stacks.get(0);
		return stack.getCapabilities();
	}

	public Map<String, String> getStackResourceIds(String stackName) {
		checkIfCredentialsFileIsSpecified();
		
		ListStackResourcesRequest listStackResourcesRequest = new ListStackResourcesRequest();
		listStackResourcesRequest.setStackName(stackName);
		ListStackResourcesResult resources = client.listStackResources(listStackResourcesRequest);
		List<StackResourceSummary> summaries = resources.getStackResourceSummaries();
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (StackResourceSummary summary : summaries) {
			map.put(summary.getLogicalResourceId(), summary.getPhysicalResourceId());
		}
		return map;

//		DescribeStackResourcesRequest describeStackResourcesRequest = new DescribeStackResourcesRequest();
//		describeStackResourcesRequest.setStackName(stackName);
//		DescribeStackResourcesResult describeStackResourcesResult = client.describeStackResources(describeStackResourcesRequest);
//		List<StackResource> resources = describeStackResourcesResult.getStackResources();
//		Map<String, String> map = new LinkedHashMap<String, String>();
//		for (StackResource resource : resources) {
//			map.put(resource.getLogicalResourceId(), resource.getPhysicalResourceId());
//		}
//		return map;
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
			System.out.println("stack " + stackName + " no parameters to update");
			return;
		}

		// update stack
		UpdateStackRequest updateStackRequest = new UpdateStackRequest();
		updateStackRequest.setStackName(stackName);
		updateStackRequest.setParameters(parameters);
		updateStackRequest.setTemplateBody(templateBody);
		client.updateStack(updateStackRequest);

		waitForStackToCompleteTheOperation(stackName, false);
	}

	private void checkIfCredentialsFileIsSpecified() {
		if(client==null) {
			throw new CredentialsFileNotSpecifiedException();
		}
	}

	private void waitForStackToCompleteTheOperation(String stackName, boolean ignoreError) {
		DescribeStacksRequest describeStacksRequest = null;
		DescribeStacksResult describeStacksResult = null;
		List<Stack> stacks;
		Stack stack;
		describeStacksRequest = new DescribeStacksRequest();
		describeStacksRequest.setStackName(stackName);
		while (true) {
			try {
				describeStacksResult = client.describeStacks(describeStacksRequest);
			} catch (AmazonServiceException e) {
				if(!ignoreError) {
					// swallow exception, the stack might not exist
					e.printStackTrace();
				} else {
					// stack does not exist, deleting is finished
					return;
				}
			}
			stacks = describeStacksResult.getStacks();
			stack = stacks.get(0);
			if (stack.getStackStatus().endsWith("_COMPLETE")) {
				break;
			}
			System.out.println("stack " + stackName + " operation status: " + stack.getStackStatus() + ", waiting for operation to complete");
			try {
				Thread.sleep(COMPLETE_OPERATION_WAITING_TIME_MS);
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
		File credentialsFile = new File(path);
		AWSCredentials creds = null;
		try {
			creds = new PropertiesCredentials(credentialsFile);
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
