package com.tomtom.woj.amazon.automation.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.auth.AWSCredentials;
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
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class AmazonEc2Operations {
	
	private AmazonEC2Client client;
	
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
	
	public String runInstance(String imageId, String instanceType, String userData, String keyPairName, ArrayList<String> securityGroupIds) {
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
		
		waitForInstanceOperationToComplete(instanceId, "running");
		
		return instanceId;
	}
	
	public void stopInstance(String instanceId) {
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
		stopInstancesRequest.withInstanceIds(instanceId);
		client.stopInstances(stopInstancesRequest);
		
		waitForInstanceOperationToComplete(instanceId, "stopped");
	}
	
	public void terminateInstance(String instanceId) {
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
		terminateInstancesRequest.withInstanceIds(instanceId);
		client.terminateInstances(terminateInstancesRequest);
		
		waitForInstanceOperationToComplete(instanceId, "terminated");
	}
	
	public String getInstanceState(String instanceId) {
		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		describeInstanceStatusRequest.withInstanceIds(instanceId);
		DescribeInstanceStatusResult describeInstanceStatusResult = client.describeInstanceStatus(describeInstanceStatusRequest);
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
	
	public void tagInstance(String instanceId, Map<String,String> tags) {
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
	
	public void tagImage(String imageId, Map<String,String> tags) {
		tagResource(imageId, tags);
	}
	
	private void tagResource(String imageId, Map<String, String> tags) {
		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		ArrayList<Tag> tagsArray = new ArrayList<Tag>();
		for(Entry<String, String> entry : tags.entrySet()) {
			tagsArray.add(new Tag(entry.getKey(), entry.getValue()));
		}
		createTagsRequest.withResources(imageId);
		createTagsRequest.setTags(tagsArray);
		
		client.createTags(createTagsRequest);
	}
	
	private void waitForInstanceOperationToComplete(String instanceId, String desiredState) {
	}

	private void waitForImageOperationToComplete(String imageId, String desiredState) {
	}

}
