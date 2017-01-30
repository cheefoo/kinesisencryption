package kinesisencryption.streams;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.Map;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import kinesisencryption.utils.KinesisEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kms.AWSKMSClient;


public class DecryptShardConsumerThread implements Runnable
{
	private String shardIterator;
	private String shardId;
	private AmazonKinesisClient kinesis;
	private AWSKMSClient kms;
	private Map<String, String> context ;
	private String keyArn;
	private final static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	private static final Logger log = LoggerFactory.getLogger(DecryptShardConsumerThread.class);


	public DecryptShardConsumerThread(String shardIterator, String shardId, AmazonKinesisClient kinesis, Map<String, String> context, String keyArn)
	{
		this.shardIterator = shardIterator;
		this.shardId = shardId;
		this.context = context;
		this.keyArn = keyArn;
		this.kinesis = kinesis;

	}

	public Map<String, String> getContext()
	{
		return context;
	}

	public String getShardIterator()
	{
		return shardIterator;
	}

	public void setShardIterator(String shardIterator) 
	{
		this.shardIterator = shardIterator;
	}

	public AWSKMSClient getKms()
	{
		return kms;
	}

	public AmazonKinesisClient getKinesis()
	{
		return kinesis;
	}

	public String getKeyArn()
	{
		return keyArn;
	}

	@Override
	public void run() 
	{
		final AwsCrypto crypto = new AwsCrypto();
		final KmsMasterKeyProvider prov = new KmsMasterKeyProvider(this.getKeyArn());
		List<Record> records;
		while (true) 
		{		
		  GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
		  getRecordsRequest.setShardIterator(this.getShardIterator());
		  getRecordsRequest.setLimit(1000); 
		  GetRecordsResult result = this.getKinesis().getRecords(getRecordsRequest);
		  records = result.getRecords();
		  if(records.size() > 0)
		  {
			  for (Record record : records)
			  {    
					 try 
				     {
						 ByteBuffer buffer = record.getData();
						 String decryptedResult = KinesisEncryptionUtils.decryptByteStream(crypto,buffer,prov,this.getKeyArn(), this.getContext());
						 log.info("Decrypted Text Result is " + decryptedResult);
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
