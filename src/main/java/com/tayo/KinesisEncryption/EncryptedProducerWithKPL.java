package com.tayo.KinesisEncryption;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.producer.Attempt;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecordFailedException;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;



public class EncryptedProducerWithKPL 
{
	private static final Logger log = LoggerFactory.getLogger(EncryptedProducerWithKPL.class);
	private static final String filePath = "C:\\Users\\temitayo\\workspace\\kinesisbootcamp\\car_odom1.txt";
	private static final String DELIM = ",";
	public static final String STREAM_NAME = "EncryptedStream";
	public static final String KPL_TMP_DIR = "C:\\Users\\temitayo\\workspace\\KinesisEncryption\\bin";
	
	public static KinesisProducer getKinesisProducer() 
	{
		KinesisProducerConfiguration config = KinesisProducerConfiguration.fromPropertiesFile("default_config.properties");
		config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());
		config.setTempDirectory(KPL_TMP_DIR);
		return new KinesisProducer(config);
	}

	
	public static void main (String [] args)
	{
		KinesisProducer producer = getKinesisProducer();
		
		List<BootCarObject> cars = getDataObjects();
		final FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>() {
            @Override
            public void onFailure(Throwable t) {
               
                if (t instanceof UserRecordFailedException) {
                    Attempt last = Iterables.getLast(
                            ((UserRecordFailedException) t).getResult().getAttempts());
                    log.error(String.format(
                            "Record failed to put - %s : %s",
                            last.getErrorCode(), last.getErrorMessage()));
                }
                log.error("Exception during put", t);
                System.exit(1);
            }

            @Override
            public void onSuccess(UserRecordResult result) {
                log.info("Success");;
            }
        };
		for(BootCarObject car: cars)
		{
			try
			{
				//ByteBuffer data = ByteBuffer.wrap(String.format(car.toString()).getBytes("UTF-8"));
				ByteBuffer data = ByteBuffer.wrap(String.format(car.encryptedToString()).getBytes("UTF-8"));
				ListenableFuture<UserRecordResult> f = producer.addUserRecord(STREAM_NAME, randomPartitionKey(), data);
				Futures.addCallback(f, callback);
				log.info("record added :");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
		}
	       
	}
	
	public static String randomPartitionKey() 
    {
        return new BigInteger(128, new Random()).toString(10);
    }
	public static  List<BootCarObject> getDataObjects()
	{
		List<BootCarObject> carObjectList= new ArrayList<BootCarObject>();
		try 
		{
			FileInputStream fis = new FileInputStream(new File(filePath));
	    	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	    	String line = null;
	    	while ((line = br.readLine()) != null)
	    	{
	        	System.out.println("Line put in stream is " + line);
	        	String [] tokens = line.split(DELIM);
	        	BootCarObject car = new BootCarObject(tokens[0], tokens[1], tokens[2]);
	        	carObjectList.add(car);
	    	}
						
		} 
		catch (FileNotFoundException ex) 
		{
			ex.printStackTrace();
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		} 
		
		return carObjectList;
	}
	
}
