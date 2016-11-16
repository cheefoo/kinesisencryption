package com.tayo.KinesisEncryption;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;

public class DecryptShardConsumerThread implements Runnable
{
	private String shardIterator;
	private String shardId;
	private AmazonKinesisClient kinesis = new AmazonKinesisClient(new ProfileCredentialsProvider()
			.getCredentials()).withRegion(Regions.US_EAST_1);
	private AWSKMSClient kms = new AWSKMSClient(new ProfileCredentialsProvider()
			.getCredentials()).withRegion(Regions.US_EAST_1);
	private final static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	private static final Logger log = LoggerFactory.getLogger(DecryptShardConsumerThread.class);
	
	public DecryptShardConsumerThread(String shardIterator, String shardId)
	{
		this.shardIterator = shardIterator;
		this.shardId = shardId;
	}
	public String getShardIterator()
	{
		return shardIterator;
	}

	public void setShardIterator(String shardIterator) 
	{
		this.shardIterator = shardIterator;
	}

	public String getShardId() 
	{
		return shardId;
	}

	public void setShardId(String shardId) 
	{
		this.shardId = shardId;
	}
	
	@Override
	public void run() 
	{
		List<Record> records = new ArrayList<Record>();
		while (true) 
		{		
		  GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
		  getRecordsRequest.setShardIterator(this.getShardIterator());
		  getRecordsRequest.setLimit(1000); 
		  GetRecordsResult result = kinesis.getRecords(getRecordsRequest);		  
		  records = result.getRecords();
		  if(records.size() > 0)
		  {
			  for (Record record : records)
			  {    
					 try 
				     {
						  /*
						   * Now trying the KMS directly*/
						 DecryptRequest decrypter = new DecryptRequest().withCiphertextBlob(record.getData());
						 DecryptResult dresult = kms.decrypt(decrypter);
						 log.info("Cipher Blob :" + record.getData().toString() + " : " + "Decrypted Text is :" 
						 + decoder.decode(dresult.getPlaintext()).toString());
						 //System.out.println(decoder.decode(dresult.getPlaintext()).toString());
					 } 
					 catch (CharacterCodingException e) 
					 {
						log.error("Exception decoding record: " + record.getData() + "with Exception : " + e.toString());	
					 }
			  }
			  
		  }
		  log.info("Records Size is : " + records.size() + " Records : " + records.toString());			  
		  try 
		  {
			  Thread.sleep(1000);
		  } 
		  catch (InterruptedException exception) 
		  {
		    throw new RuntimeException(exception);
		  }
		shardIterator = result.getNextShardIterator();
		this.setShardIterator(shardIterator);
	   }		
		
	}
	
	

}
