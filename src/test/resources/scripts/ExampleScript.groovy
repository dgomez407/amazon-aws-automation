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
 */

println 'script arguments: ' + args

json = loadJsonFile('src/test/resources/json/example_json_file.json')
println 'value of json_property: ' + json.json_property1
 
includeGroovyScript('src/test/resources/scripts/ExampleIncludedScript.groovy')

loadAwsCredentialsFile('src/test/resources/credentials/example_credentials.txt')

println 'list of stacks: ' + getStartedStackNames()

stackNames = ['HdtRegions', 'Initialize']

newProperties = ['ScalingDownAdjustment' : 30]

for(stackName in stackNames) {
	println "processing stack: ${stackName} parameters: " + getStackParameters(stackName)
	updateStackParameters(stackName, newProperties)
}

stackParameters = ['UserData':'some user data', 'AMIId':'ami-123456']
createStackFromFile('testStack', 'src/test/resources/stack_templates/some_stack_template.json', stackParameters)
