package com.tomtom.woj.amazon.automation.operations;

import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.tomtom.woj.amazon.automation.operations.core.Deployer;
import com.tomtom.woj.amazon.automation.operations.core.RemoteHost;

public class InstanceOperations {

    public InstanceOperations() {
    }

    public void ssh(String host, List<String> commands, String remoteUser, String privateKey) {
        RemoteHost remoteHost = new RemoteHost();
        remoteHost.setEndPoint(host);
        remoteHost.setRemoteUser(remoteUser);
        remoteHost.setPrivateKeyPath(privateKey);

        Deployer deployer = new Deployer(remoteHost, FileUtils.getTempDirectoryPath());

        deployer.runRemoteCommand(commands);
    }

    public void scp(String host, Map<String, String> deploymentPaths, String remoteUser, String privateKey,
            String workingDir) {
        RemoteHost remoteHost = new RemoteHost();
        remoteHost.setEndPoint(host);
        remoteHost.setRemoteUser(remoteUser);
        remoteHost.setPrivateKeyPath(privateKey);

        Deployer deployer = new Deployer(remoteHost, FileUtils.getTempDirectoryPath());

        deployer.uploadFiles(workingDir, deploymentPaths);
    }

}
