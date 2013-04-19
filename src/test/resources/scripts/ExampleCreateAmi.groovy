/**
 * commands:
 *
 * void loadAwsCredentialsFile(String credentialsFileName)
 * 
 * String runInstance(String imageId, String instanceType, String userData, String keyPairName, ArrayList<String> securityGroupIds) 
 * String getInstanceDnsName(String instanceId) 
 * 
 * ssh(String host, List<String> commands, String remoteUser, String privateKey)
 * scp(String host, Map<String, String> deploymentPaths, String remoteUser, String privateKey, String workingDir)
 *
 * String createAmiFromInstance(String instanceId, String name, boolean noReboot)
 */


println 'Program parameters: <amazon_credentials_location> <created_image_name> <local_amazon_certificate> <base_ami_id> <instance_key_pair> <instance_security_group>'

// get program parameters
loadAwsCredentialsFile(args[0])
amiName = args[1]
sshCertificateLocation = args[2]
baseAmiId = args[3]
instanceKeyPair = args[4]
instanceSecurityGroup = args[5]

// run base AMI
instanceId =  runInstance(baseAmiId, 'm1.medium', '', instanceKeyPair, [instanceSecurityGroup])
instanceDNS = getInstanceDnsName(instanceId)

// specify files to copy
filesToCopy = ['/home/user/localfile1':'/home/ubuntu/remotedestination1',
               '/home/user/localfile2':'/home/ubuntu/remotedestination2']
scp( instanceDNS, filesToCopy, "ubuntu", sshCertificateLocation, "")

// specify commands to execute
commands = [
    'remoteCommand1',
    'remoteCommand2'
]
ssh( instanceDNS, commands, "ubuntu", sshCertificateLocation)

// create new AMI
imageID = createAmiFromInstance(instanceId, amiName, false)

println 'Instance DNS name: ' + instanceDNS
println 'Image name: ' + imageID



