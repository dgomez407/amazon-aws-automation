/**
 * available commands:
 * 
 * void loadAwsCredentialsFile(String credentialsFileName) 
 * 
 * void includeGroovyScript(String scriptFileName)
 * 
 * void createStack(String stackName, String templateBody,	Map<String, Object> parameterValues)
 * void deleteStack(String stackName)
 * void listStacks();
 * void listStackParameters(String stackName);
 * void updateStackParameters(String stackName, Map<String, Object> newParameterValues);
 * 
 */

println 'script arguments: ' + args

json = loadJsonFile('src/test/resources/json/example_json_file.json')
println 'value of json_property: ' + json.json_property1
 
loadAwsCredentialsFile('src/test/resources/credentials/example_credentials.txt')

includeGroovyScript('src/test/resources/scripts/ExampleIncludedScript.groovy')

stacksNames = ['HdtRegions', 'Initialize']

newProperties = ['ScalingDownAdjustment' : 30]

for(stackName in stacksNames) {
	println("\nprocessing stack: ${stackName}")
	listStackParameters(stackName)
	updateStackParameters(stackName, newProperties)
}

