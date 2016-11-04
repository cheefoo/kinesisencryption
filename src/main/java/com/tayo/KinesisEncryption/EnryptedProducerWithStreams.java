package com.tayo.KinesisEncryption;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;


public class EnryptedProducerWithStreams 
{
	private static final String filePath = "file_path";
	private static final String DELIM = ",";
	public static final String STREAM_NAME = "stream_name";
	public static final String KEY_ID = "key_id";
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
	
	public static void main (String [] args) throws UnsupportedEncodingException, InterruptedException
	{
		AmazonKinesisClient kinesis = new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain()
    			.getCredentials()).withRegion(Regions.US_EAST_1);
        
		
       /*
        * Simulating the appearance of a steady flow of data by continuously loading data from file*/
        try
        {
        	List<BootCarObject> cars = getDataObjects();
        	EnryptedProducerWithStreams producer = new EnryptedProducerWithStreams();
        	producer.setCarObjectList(cars);
        	String streamName = KinesisEncryptionUtils.getProperties().getProperty(STREAM_NAME);
        	String keyId = KinesisEncryptionUtils.getProperties().getProperty(KEY_ID);
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
                 	ptre.setData(car.toByteStream()); 	
                 	ptre.setPartitionKey(randomPartitionKey());
                 	ptreList.add(ptre);
                 	log.info("Car added :" + car.toString() + "Car Cipher :" + car.toByteStream(keyId));
                    i++;
                 }
                 ptreList = new ArrayList<PutRecordsRequestEntry>(); 
                 cars = producer.getCarObjectList();
                 batch++;
                 Thread.sleep(100);
             }
        }
        catch(Exception e)
        {
        	log.error(e.toString());
        }	
	}
	
	public static String randomPartitionKey() 
    {
        return new BigInteger(128, new Random()).toString(10);
    }
	
	public static  List<BootCarObject> getDataObjects() throws FileNotFoundException, IOException
	{
		List<BootCarObject> carObjectList= new ArrayList<BootCarObject>();
		
			FileInputStream fis = new FileInputStream(new File(KinesisEncryptionUtils.getProperties().getProperty(filePath)));
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
