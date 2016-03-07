The purpose of this utility is to automate manual tasks done during releases, especially when doing continuous delivery. One example may be changing stack parameters when changing environment from test to production. The scripts are written in groovy. The application is standalone executable jar file.

**Scripts can normally be debugged by running the tool from Eclipse with groovy plugin and setting the breakpoints inside the scripts.**

### Usage ###

1. Download the Java executable [amazon-aws-automation-0.2-SNAPSHOT.jar](http://amazon-aws-automation.googlecode.com/files/amazon-aws-automation-0.2-SNAPSHOT.jar)

2. Create Amazon AWS credentials file (see the example at the bottom of page)

3. Execute the tool with script file name as parameter (credentials loaded within the script)

```
java -jar amazon-aws-automation-0.2-SNAPSHOT.jar src/test/resources/scripts/ExampleScript.groovy
```

You can override the credentials file loaded in the script by giving the location to the credentials file as a prameter. You can also pass the arguments to the script by adding them ot the command line

```
java -jar amazon-aws-automation-0.2-SNAPSHOT.jar \
    src/test/resources/scripts/ExampleScript.groovy \
    --credentials src/test/resources/credentials/example_credentials.txt
    some_argument1 some_argument2
```

### Features ###

  * Supports operations on AWS account - more support will be added as application will be developed
  * Supports including other groovy scripts from within the script
  * Supports reading of JSON files and representing them as objects (may be useful to separate parameters from the script)
  * Supports passing arguments to the script from the command line. Command line arguments will be passed to the script inside the `args` list
  * Supports overriding credentials file name from command line

### Supported Operations ###

List of supported operations are described here [Supported Amazon Operations](SupportedOperations.md). Below please find the summary

```
/**
 * available commands:
 * 
 * void loadAwsCredentialsFile(String credentialsFileName) 
 * void includeGroovyScript(String scriptFileName)
 * Object loadJsonFile(String jsonFileName)
 * 
 * void createStackFromFile(String stackName, String templateFileName, Map<String, Object> parameterValues)
 * void createStackFromText(String stackName, String templateBody, Map<String, Object> parameterValues)
 * void deleteStack(String stackName)
 * List<String> getStartedStackNames()
 * String getStackStatus(String stackName)
 * Map<String, String> getStackOutputs(String stackName)
 * List<String> getStackCapabilities(String stackName)
 * Map<String, String> getStackResourceIds(String stackName) {
 * Map<String, String> getStackTags(String stackName) {
 * String getStackTemplateBody(String stackName)
 * Map<String, String> getStackParameters(String stackName)
 * void updateStackParameters(String stackName, Map<String, Object> newParameterValues)
 * 
 * String runInstance(String imageId, String instanceType, String userData, String keyPairName, ArrayList<String> securityGroupIds) 
 * String getInstanceDnsName(String instanceId) 
 * 
 * ssh(String host, List<String> commands, String remoteUser, String privateKey)
 * scp(String host, Map<String, String> deploymentPaths, String remoteUser, String privateKey, String workingDir)
 *
 * String createAmiFromInstance(String instanceId, String name, boolean noReboot)
 */
```

### Example Script in Groovy ###

The script below demonstrates some of the capabilities of the scripting

```
loadAwsCredentialsFile('src/test/resources/credentials/example_credentials.txt')

println 'list of stacks: ' + getStartedStackNames()

stackNames = ['HdtRegions', 'Initialize']

newProperties = ['ScalingDownAdjustment' : 30]

for(stackName in stackNames) {
	println "processing stack: ${stackName} parameters: " + getStackParameters(stackName)
	updateStackParameters(stackName, newProperties)
}
```

example showing how to access command line arguments

```
println 'script arguments: ' + args
```

example showing how to load json file

```
json = loadJsonFile('src/test/resources/json/example_json_file.json')
println 'value of json_property: ' + json.json_property1
```

example showing how to include another groovy script

```
includeGroovyScript('src/test/resources/scripts/ExampleIncludedScript.groovy')
```

example showing how to create stack

```
stackParameters = ['UserData':'some user data', 'AMIId':'ami-123456']
createStackFromFile('testStack', 'src/test/resources/stack_templates/some_stack_template.json', stackParameters)
```

### Contents of the Credentials File ###

```
accessKey=your_aws_account_access_key
secretKey=your_aws_account_secret_key
```

### From the author ###

Please send comments and questions