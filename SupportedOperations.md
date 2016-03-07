# Supported Amazon AWS Operations #

Below is the list of operations supported by [Amazon AWS Automation](http://code.google.com/p/amazon-aws-automation/) tool. Those operations are available from groovy scripts.

## Basic operations ##

<pre>
* void loadAwsCredentialsFile(String credentialsFileName)<br>
* void includeGroovyScript(String scriptFileName)<br>
* Object loadJsonFile(String jsonFileName)<br>
</pre>

## Stack Operations ##

<pre>
* void createStackFromFile(String stackName, String templateFileName, Map<String, Object> parameterValues)<br>
* void createStackFromText(String stackName, String templateBody, Map<String, Object> parameterValues)<br>
* void deleteStack(String stackName)<br>
* List<String> getStartedStackNames()<br>
* String getStackStatus(String stackName)<br>
* Map<String, String> getStackOutputs(String stackName)<br>
* List<String> getStackCapabilities(String stackName)<br>
* Map<String, String> getStackResourceIds(String stackName) {<br>
* Map<String, String> getStackTags(String stackName) {<br>
* String getStackTemplateBody(String stackName)<br>
* Map<String, String> getStackParameters(String stackName)<br>
* void updateStackParameters(String stackName, Map<String, Object> newParameterValues)<br>
</pre>

## Instance Operations ##

<pre>
* String runInstance(String imageId, String instanceType, String userData, String keyPairName, ArrayList<String> securityGroupIds)<br>
* String getInstanceDnsName(String instanceId)<br>
* ssh(String host, List<String> commands, String remoteUser, String privateKey)<br>
* scp(String host, Map<String, String> deploymentPaths, String remoteUser, String privateKey, String workingDir)<br>
* String createAmiFromInstance(String instanceId, String name, boolean noReboot)<br>
</pre>