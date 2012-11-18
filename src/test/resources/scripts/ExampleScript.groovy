/**
 * available commands:
 * 
 * void loadAwsCredentialsFile(String credentialsFileName) 
 * void includeGroovyScript(String scriptFileName)
 * Object loadJsonFile(String jsonFileName)
 * 
 * void createStackFile(String stackName, String templateFileName, Map<String, Object> parameterValues)
 * void createStackText(String stackName, String templateBody, Map<String, Object> parameterValues)
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
 
loadAwsCredentialsFile('src/test/resources/credentials/example_credentials.txt')

includeGroovyScript('src/test/resources/scripts/ExampleIncludedScript.groovy')

println 'list of stacks: ' + getStartedStackNames()

stacksNames = ['HdtRegions', 'Initialize']

newProperties = ['ScalingDownAdjustment' : 30]

for(stackName in stacksNames) {
	println "processing stack: ${stackName} parameters: " + getStackParameters(stackName)
	updateStackParameters(stackName, newProperties)
}
