package kinesisencryption.streams;

import java.io.IOException;
import java.util.List;

import com.amazonaws.services.kms.AWSKMSClient;
import kinesisencryption.utils.KinesisEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.GetShardIteratorResult;
import com.amazonaws.services.kinesis.model.Shard;
import com.amazonaws.services.kinesis.model.StreamDescription;


public class EncryptedConsumerWithStreams
{
	private static final Logger log = LoggerFactory.getLogger(EncryptedConsumerWithStreams.class);
	//private final static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	
	public static void main (String [] args) throws IOException
	{
		AmazonKinesisClient kinesis = new AmazonKinesisClient(new ProfileCredentialsProvider()
    			.getCredentials());
		AWSKMSClient kms = new AWSKMSClient(new ProfileCredentialsProvider()
    			.getCredentials());
		kinesis.setEndpoint(KinesisEncryptionUtils.getProperties().getProperty("kinesis_endpoint"));
		kms.setEndpoint(KinesisEncryptionUtils.getProperties().getProperty("kms_endpoint"));

		try
		{
			String streamName = KinesisEncryptionUtils.getProperties().getProperty("stream_name");
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
				getShardIteratorRequest.setShardIteratorType(KinesisEncryptionUtils.getProperties().getProperty("sharditerator_type"));
				GetShardIteratorResult getShardIteratorResult = kinesis.getShardIterator(getShardIteratorRequest);
				shardIterator = getShardIteratorResult.getShardIterator();			
				DecryptShardConsumerThread consumer = new DecryptShardConsumerThread(shardIterator, shard.getShardId(), kinesis, kms);
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
