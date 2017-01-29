package kinesisencryption.streams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.kms.AWSKMSClient;
import kinesisencryption.dao.BootCarObject;
import kinesisencryption.utils.KinesisEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;


public class EnryptedProducerWithStreams 
{

	private static final String DELIM = ",";

	private static final Logger log = LoggerFactory.getLogger(EnryptedProducerWithStreams.class);
	private List<BootCarObject> carObjectList;
	
	public List<BootCarObject> getCarObjectList() 
	{
		return carObjectList;
	}

	public void setCarObjectList(List<BootCarObject> carObjectList) 
	{
		this.carObjectList = carObjectList;
	}
	
	public static void main (String [] args)
	{
		AmazonKinesisClient kinesis = new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain()
    			.getCredentials());
		AWSKMSClient kms = new AWSKMSClient(new DefaultAWSCredentialsProviderChain()
				.getCredentials());
		String keyArn = null;
		String encryptionContext = null;
		final AwsCrypto crypto = new AwsCrypto();

		
       /*
        * Simulating the appearance of a steady flow of data by continuously loading data from file*/
        try
        {
        	List<BootCarObject> cars = getDataObjects();
        	EnryptedProducerWithStreams producer = new EnryptedProducerWithStreams();
        	producer.setCarObjectList(cars);
			keyArn = KinesisEncryptionUtils.getProperties().getProperty("key_arn");
			log.info("Successfully retrieved keyarn property " + keyArn);
			encryptionContext = KinesisEncryptionUtils.getProperties().getProperty("encryption_context");
			log.info("Successfully retrieved encryption context property " + encryptionContext);
        	String streamName = KinesisEncryptionUtils.getProperties().getProperty("stream_name");
			log.info("Successfully retrieved stream name property " + streamName);
        	String keyId = KinesisEncryptionUtils.getProperties().getProperty("key_id");
			log.info("Successfully retrieved key id property " + keyId);
			String kinesisEndpoint = KinesisEncryptionUtils.getProperties().getProperty("kinesis_endpoint");
			log.info("Successfully retrieved kinesis endpoint property " + kinesisEndpoint);
			kinesis.setEndpoint(kinesisEndpoint);
			String kmsEndpoint = KinesisEncryptionUtils.getProperties().getProperty("kms_endpoint");
			log.info("Successfully retrieved kms endpoint property " + kmsEndpoint);
			kms.setEndpoint(kmsEndpoint);

			final Map<String, String> context = Collections.singletonMap("Kinesis", encryptionContext);
			final KmsMasterKeyProvider prov = new KmsMasterKeyProvider(keyArn);
			PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
            
            List<PutRecordsRequestEntry> ptreList = new ArrayList<PutRecordsRequestEntry>();
            int batch = 1;
        	 while(true)
             {
             	int i = 1;
                 for (BootCarObject car: cars)
                 {
                 	if(i == 500)//Put Record batch requests are 500
                 	{
                 		putRecordsRequest.setRecords(ptreList);
                     	putRecordsRequest.setStreamName(streamName);
                     	PutRecordsResult putRecordsResult = 
                     			kinesis.putRecords(putRecordsRequest);
                     	log.info("PutRecordsResult  : " + putRecordsResult.toString() 
                     	+ " has Batch Number : " + batch);
                 		break;
                 	}
                 	PutRecordsRequestEntry ptre = new PutRecordsRequestEntry();
					ByteBuffer data = KinesisEncryptionUtils.toEncryptedByteStream(crypto, car, prov,context);
                 	ptre.setData(data);
                 	ptre.setPartitionKey(randomPartitionKey());
                 	ptreList.add(ptre);
                 	log.info("Car added :" + car.toString() + "Car Cipher :" + data.toString());
                    i++;
                 }
                 ptreList = new ArrayList();
                 cars = producer.getCarObjectList();
                 batch++;
                 Thread.sleep(100);
             }
        }
        catch(IOException ioe)
        {
        	log.error(ioe.toString());
        }
		catch (InterruptedException ie)
		{
			log.error(ie.toString());
		}
	}
	
	public static String randomPartitionKey() 
    {
        return new BigInteger(128, new Random()).toString(10);
    }
	
	public static  List<BootCarObject> getDataObjects() throws  IOException
	{
		List<BootCarObject> carObjectList= new ArrayList<BootCarObject>();
		
			FileInputStream fis = new FileInputStream(new File(KinesisEncryptionUtils.getProperties().getProperty("file_path")));
	    	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	    	String line = null;
	    	while ((line = br.readLine()) != null)
	    	{ 
	        	String [] tokens = line.split(DELIM);
	        	BootCarObject car = new BootCarObject(tokens[0], tokens[1], tokens[2]);
	        	carObjectList.add(car);
	    	}
						
		return carObjectList;
	}
	

}
