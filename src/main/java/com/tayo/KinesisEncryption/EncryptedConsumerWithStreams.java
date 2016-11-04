package com.tayo.KinesisEncryption;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.GetShardIteratorResult;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kinesis.model.Shard;
import com.amazonaws.services.kinesis.model.StreamDescription;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;



public class EncryptedConsumerWithStreams 
{
	private static final String OUT_FILE_PATH = "out_file_path";
	public static final String STREAM_NAME = "stream_name";
	private static final Logger log = LoggerFactory.getLogger(EncryptedConsumerWithStreams.class);
	//private final static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	
	public static void main (String [] args) 
	{
		AmazonKinesisClient kinesis = new AmazonKinesisClient(new ProfileCredentialsProvider()
    			.getCredentials()).withRegion(Regions.US_EAST_1);
		/*AWSKMSClient kms = new AWSKMSClient(new ProfileCredentialsProvider()
    			.getCredentials()).withRegion(Regions.US_EAST_1);*/
		try
		{
			//File file = new File(KinesisEncryptionUtils.getProperties().getProperty(OUT_FILE_PATH));	
			String streamName = KinesisEncryptionUtils.getProperties().getProperty(STREAM_NAME);
			DescribeStreamResult streamResult = kinesis.describeStream(streamName);
			StreamDescription streamDescription = streamResult.getStreamDescription();
			List<Shard> shardList = streamDescription.getShards();
			log.info("Shard size is " + shardList.size());
			
			for (Shard shard: shardList)
			{			
				String shardIterator = null;
				GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
				getShardIteratorRequest.setStreamName(streamName);
				getShardIteratorRequest.setShardId(shard.getShardId());
				getShardIteratorRequest.setShardIteratorType("LATEST");
				GetShardIteratorResult getShardIteratorResult = kinesis.getShardIterator(getShardIteratorRequest);
				shardIterator = getShardIteratorResult.getShardIterator();			
				DecryptShardConsumerThread consumer = new DecryptShardConsumerThread(shardIterator, shard.getShardId());
				Thread consumerThread = new Thread(consumer); 
				log.info("Starting thread to consumer for Shard :" + shard.getShardId()  + "with ShardIterator :" + shardIterator);
				consumerThread.start();
		   }
		}
		catch(Exception e)
		{
			log.error(e.getMessage());
		}
		
	}
	
	
}
