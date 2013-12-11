package com.tomtom.woj.amazon.automation.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.ImageState;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.tomtom.woj.amazon.automation.operations.core.GeneralUtils;

public class AmazonEc2Operations {

    private AmazonEC2Client client;

    private static final int WAIT_FOR_INSTANCE_DELAY = 10;
    private static final int MAX_TIME_FOR_WAITING_FOR_INSTANCE = 600;

    private static final Logger logger = LoggerFactory.getLogger(AmazonEc2Operations.class);

    public AmazonEc2Operations(String awsCredentialsFileName) {
        AWSCredentials credentials = AmazonStackOperations.loadCredentials(awsCredentialsFileName);
        client = new AmazonEC2Client(credentials);
    }

    public AmazonEc2Operations() {
        client = new AmazonEC2Client();
    }

    public void loadAwsCredentialsFile(String awsCredentialsFileName) {
        AWSCredentials credentials = AmazonStackOperations.loadCredentials(awsCredentialsFileName);
        client = new AmazonEC2Client(credentials);
    }

	public void setRegionName(String regionName) {
		Regions regions = Regions.fromName(regionName);
		Region region = Region.getRegion(regions);
		client.setRegion(region);
	}

    public String runInstance(String imageId, String instanceType, String userData, String keyPairName,
            ArrayList<String> securityGroupIds) {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.setMinCount(1);
        runInstancesRequest.setMaxCount(1);
        runInstancesRequest.setImageId(imageId);
        runInstancesRequest.setInstanceType(instanceType);
        runInstancesRequest.setUserData(userData);
        runInstancesRequest.setKeyName(keyPairName);
        runInstancesRequest.setSecurityGroupIds(securityGroupIds);
        RunInstancesResult runInstancesResult = client.runInstances(runInstancesRequest);
        Reservation reservation = runInstancesResult.getReservation();
        List<Instance> instances = reservation.getInstances();
        Instance instance = instances.get(0);
        String instanceId = instance.getInstanceId();

        waitForInstanceOperationToComplete(instanceId, InstanceStateName.Running);

        return instanceId;
    }

    public void stopInstance(String instanceId) {
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
        stopInstancesRequest.withInstanceIds(instanceId);
        client.stopInstances(stopInstancesRequest);

        waitForInstanceOperationToComplete(instanceId, InstanceStateName.Stopped);
    }

    public void terminateInstance(String instanceId) {
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        terminateInstancesRequest.withInstanceIds(instanceId);
        client.terminateInstances(terminateInstancesRequest);

        waitForInstanceOperationToComplete(instanceId, InstanceStateName.Terminated);
    }

    public String getInstanceState(String instanceId) {
        DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
        describeInstanceStatusRequest.withInstanceIds(instanceId);
        DescribeInstanceStatusResult describeInstanceStatusResult = client
                .describeInstanceStatus(describeInstanceStatusRequest);
        InstanceStatus instanceStatus = describeInstanceStatusResult.getInstanceStatuses().get(0);
        return instanceStatus.getInstanceState().getName();
    }

    public String getInstanceDnsName(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.setInstanceIds(Collections.singletonList(instanceId));
        DescribeInstancesResult describeInstancesResult = client.describeInstances(describeInstancesRequest);
        List<Reservation> reservations = describeInstancesResult.getReservations();
        Reservation reservation = reservations.get(0);
        List<Instance> instances = reservation.getInstances();
        Instance instance = instances.get(0);
        return instance.getPublicDnsName();
    }

    public void tagInstance(String instanceId, Map<String, String> tags) {
        tagResource(instanceId, tags);
    }

    public String createAmiFromInstance(String instanceId, String name, boolean noReboot) {
        CreateImageRequest createImageRequest = new CreateImageRequest();
        createImageRequest.setInstanceId(instanceId);
        createImageRequest.setName(name);
        createImageRequest.setNoReboot(noReboot);
        CreateImageResult createImageResult = client.createImage(createImageRequest);
        String imageId = createImageResult.getImageId();

        waitForImageOperationToComplete(imageId, ImageState.Available.toString());

        return imageId;
    }

    public void deregisterImage(String imageId) {
        DeregisterImageRequest deregisterImageRequest = new DeregisterImageRequest(imageId);
        client.deregisterImage(deregisterImageRequest);
        waitForImageOperationToComplete(imageId, ImageState.Deregistered.toString());
    }

    public String getImageState(String imageId) {
        DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
        describeImagesRequest.withImageIds(imageId);
        DescribeImagesResult describeImagesResult = client.describeImages(describeImagesRequest);
        Image image = describeImagesResult.getImages().get(0);
        return image.getState();
    }

    public void tagImage(String imageId, Map<String, String> tags) {
        tagResource(imageId, tags);
    }

    private void tagResource(String imageId, Map<String, String> tags) {
        CreateTagsRequest createTagsRequest = new CreateTagsRequest();
        ArrayList<Tag> tagsArray = new ArrayList<Tag>();
        for (Entry<String, String> entry : tags.entrySet()) {
            tagsArray.add(new Tag(entry.getKey(), entry.getValue()));
        }
        createTagsRequest.withResources(imageId);
        createTagsRequest.setTags(tagsArray);

        client.createTags(createTagsRequest);
    }

    /**
     * Periodically ask the service if the instances are running.
     * 
     * @param instanceIds
     */
    private void waitForInstanceOperationToComplete(String instanceId, InstanceStateName instanceState) {
        int waitedSeconds = 0;
        while (true) {
            GeneralUtils.sleep(WAIT_FOR_INSTANCE_DELAY);
            waitedSeconds += WAIT_FOR_INSTANCE_DELAY;
            if (waitedSeconds > MAX_TIME_FOR_WAITING_FOR_INSTANCE) {
                throw new RuntimeException("Instances are not " + instanceState.name() + " after " + waitedSeconds
                        + " seconds");
            }
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            request.setInstanceIds(Arrays.asList(new String[] { instanceId }));
            DescribeInstancesResult response = client.describeInstances(request);

            if (checkReservationsReady(instanceState, response)) {
                return;
            }

        }

    }

    private boolean checkReservationsReady(InstanceStateName instanceState, DescribeInstancesResult response) {

        for (Reservation reservation : response.getReservations()) {
            if (checkInstancesReady(instanceState, reservation)) {
                logger.info("All instances are {}", instanceState.name());
                return true;
            }
        }
        return false;
    }

    private boolean checkInstancesReady(InstanceStateName instanceState, Reservation reservation) {
        for (Instance instance : reservation.getInstances()) {
            if (checkInstanceReady(instance, instanceState)) {
                logger.info("Instance: {} is {}", instance.getInstanceId(), instanceState.name());
            } else {
                logger.info("Instance: {} is not {}", instance.getInstanceId(), instanceState.name());
                return false;
            }
        }
        return true;
    }

    private boolean checkInstanceReady(Instance instance, InstanceStateName instanceState) {
        String state = instance.getState().getName();

        if (InstanceStateName.Running.name().equalsIgnoreCase(instanceState.name())) {
            logger.info("Instance {} state: {} ", instance.getInstanceId(), state);
            return state.equalsIgnoreCase(instanceState.name()) && isStatusReady(instance);
        } else {
            logger.info("Instance {} state: {} ", instance.getInstanceId(), state);
            return state.equalsIgnoreCase(instanceState.name());
        }

    }

    private boolean isStatusReady(Instance instance) {

        DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest()
                .withInstanceIds(instance.getInstanceId());
        DescribeInstanceStatusResult describeInstanceResult = client.describeInstanceStatus(describeInstanceRequest);
        List<InstanceStatus> statuses = describeInstanceResult.getInstanceStatuses();
        if (statuses.size() == 0) {
            return false;
        }
        for (InstanceStatus instanceStatus : statuses) {

            String status = instanceStatus.getInstanceStatus().getStatus();
            logger.info("Instance status: {}", status);

            if (!StringUtils.equals(status, "ok")) {
                return false;
            }
        }
        return true;
    }

    private void waitForImageOperationToComplete(String imageId, String desiredState) {
        int waitedSeconds = 0;

        while (true) {
            GeneralUtils.sleep(WAIT_FOR_INSTANCE_DELAY);
            waitedSeconds += WAIT_FOR_INSTANCE_DELAY;
            if (waitedSeconds > MAX_TIME_FOR_WAITING_FOR_INSTANCE) {
                throw new RuntimeException("Image isn't ready after " + waitedSeconds + " seconds");
            }
            String state = getImageState(imageId);
            logger.info("Image state: {} ", state);
            if (desiredState.equals(state)) {
                logger.info("Image: {} is ready", imageId);
                return;
            }
        }

    }

}
