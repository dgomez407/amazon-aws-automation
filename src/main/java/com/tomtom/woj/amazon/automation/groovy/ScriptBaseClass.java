package com.tomtom.woj.amazon.automation.groovy;

import groovy.json.JsonSlurper;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tomtom.woj.amazon.automation.operations.AmazonEc2Operations;
import com.tomtom.woj.amazon.automation.operations.AmazonStackOperations;
import com.tomtom.woj.amazon.automation.operations.InstanceOperations;

public abstract class ScriptBaseClass extends Script {

    public static final String AMAZON_STACK_OPERATIONS_PROPERTY_NAME = "amazonStackExecutor";
    public static final String AMAZON_EC2_OPERATIONS_PROPERTY_NAME = "amazonEc2Operations";
    public static final String GROOVY_SHELL_PROPERTY_NAME = "shell";
    public static final String ARGS_VARIABLE_NAME = "args";
    public static final String IGNORE_CREDENTIALS_COMMAND_PROPERTY_NAME = "ignoreCredentialsCommand";
    public static final String INSTANCE_OPERATIONS = "instanceOperations";

    public void includeGroovyScript(String scriptFileName) throws IOException {
        GroovyShell groovyShell = (GroovyShell) getProperty(GROOVY_SHELL_PROPERTY_NAME);
        groovyShell.evaluate(new File(scriptFileName));
    }

    public void loadAwsCredentialsFile(String credentialsFileName) {
        boolean ignoreCredentialsCommand = (Boolean) getProperty(IGNORE_CREDENTIALS_COMMAND_PROPERTY_NAME);
        if (ignoreCredentialsCommand) {
            System.out
                    .println("ignored load credentials command because they are overriden with --credentials comand line parameter");
            return;
        }
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        AmazonEc2Operations ec2Operations = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        executor.loadAwsCredentialsFile(credentialsFileName);
        ec2Operations.loadAwsCredentialsFile(credentialsFileName);
    }

    public void setRegionName(String regionName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        AmazonEc2Operations ec2Operations = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        executor.setRegionName(regionName);
        ec2Operations.setRegionName(regionName);
    }

    public Object loadJsonText(String text) {
        JsonSlurper slurper = new groovy.json.JsonSlurper();
        return slurper.parseText(text);
    }

    public Object loadJsonFile(String jsonFileName) throws FileNotFoundException {
        JsonSlurper slurper = new groovy.json.JsonSlurper();
        return slurper.parse(new FileReader(jsonFileName));
    }

    public void createStackFromText(String stackName, String templateBody, Map<String, Object> parameterValues) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        executor.createStack(stackName, templateBody, convertMapObjectToString(parameterValues));
    }

    public void createStackFromFile(String stackName, String templateFileName, Map<String, Object> parameterValues)
            throws IOException {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        File templateFile = new File(templateFileName);
        executor.createStack(stackName, templateFile, convertMapObjectToString(parameterValues));
    }

    public String getStackStatus(String stackName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        return executor.getStackStatus(stackName);
    }

    public Map<String, String> getStackOutputs(String stackName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        return executor.getStackOutputs(stackName);
    }

    public List<String> getStackCapabilities(String stackName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        return executor.getStackCapabilities(stackName);
    }

    public Map<String, String> getStackResourceIds(String stackName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        return executor.getStackResourceIds(stackName);
    }

    public String getStackTemplateBody(String stackName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        return executor.getStackTemplateBody(stackName);
    }

    public void deleteStack(String stackName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        executor.deleteStack(stackName);
    }

    public List<String> getStartedStackNames() {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        return executor.getStartedStackNames();
    }

    public Map<String, String> getStackParameters(String stackName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        return executor.getStackParameters(stackName);
    }

    public Map<String, String> getStackTags(String stackName) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        return executor.getStackTags(stackName);
    }

    public void updateStackParameters(String stackName, Map<String, Object> newParameterValues) {
        AmazonStackOperations executor = (AmazonStackOperations) getProperty(AMAZON_STACK_OPERATIONS_PROPERTY_NAME);
        executor.updateStackParameters(stackName, convertMapObjectToString(newParameterValues));
    }

    private Map<String, String> convertMapObjectToString(Map<String, Object> map) {
        LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
        for (Entry<String, Object> entry : map.entrySet()) {
            output.put(entry.getKey(), entry.getValue().toString());
        }
        return output;
    }

    public void printList(List<Object> list) {
        for (Object item : list) {
            System.out.println(item);
        }
    }

    public void printMap(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public String runInstance(String imageId, String instanceType, String userData, String keyPairName,
            ArrayList<String> securityGroupIds) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        return executor.runInstance(imageId, instanceType, userData, keyPairName, securityGroupIds);
    }

    public void stopInstance(String instanceId) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        executor.stopInstance(instanceId);
    }

    public void terminateInstance(String instanceId) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        executor.terminateInstance(instanceId);
    }

    public String getInstanceState(String instanceId) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        return executor.getInstanceState(instanceId);
    }

    public String getInstanceDnsName(String instanceId) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        return executor.getInstanceDnsName(instanceId);
    }

    public void tagInstance(String instanceId, Map<String, String> tags) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        executor.tagInstance(instanceId, tags);
    }

    public String createAmiFromInstance(String instanceId, String name, boolean noReboot) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        return executor.createAmiFromInstance(instanceId, name, noReboot);
    }

    public void deregisterImage(String imageId) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        executor.deregisterImage(imageId);
    }

    public String getImageState(String imageId) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        return executor.getImageState(imageId);
    }

    public void tagImage(String imageId, Map<String, String> tags) {
        AmazonEc2Operations executor = (AmazonEc2Operations) getProperty(AMAZON_EC2_OPERATIONS_PROPERTY_NAME);
        executor.tagImage(imageId, tags);
    }

    public void ssh(String host, List<String> commands, String remoteUser, String privateKey) {
        InstanceOperations executor = (InstanceOperations) getProperty(INSTANCE_OPERATIONS);
        executor.ssh(host, commands, remoteUser, privateKey);
    }

    public void scp(String host, Map<String, String> deploymentPaths, String remoteUser, String privateKey,
            String workingDir) {
        InstanceOperations executor = (InstanceOperations) getProperty(INSTANCE_OPERATIONS);
        executor.scp(host, deploymentPaths, remoteUser, privateKey, workingDir);
    }
}
