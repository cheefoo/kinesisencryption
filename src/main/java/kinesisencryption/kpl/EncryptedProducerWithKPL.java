package kinesisencryption.kpl;

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

import kinesisencryption.dao.BootCarObject;
import kinesisencryption.utils.KinesisEncryptionUtils;
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
	private static final String DELIM = ",";

	
	public static KinesisProducer getKinesisProducer() 
	{
		KinesisProducerConfiguration config = KinesisProducerConfiguration.fromPropertiesFile("default_config.properties");
		config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());
		return new KinesisProducer(config);
	}

	
	public static void main (String [] args) throws Exception
	{
		KinesisProducer producer = getKinesisProducer();
		String filePath = null;
		try
		{
			filePath = KinesisEncryptionUtils.getProperties().getProperty("file_path");
		}
		catch( IOException ioe)
		{
			log.error("Could not load properties file " + ioe.toString());
			throw new Exception("Could not load properties file");
		}

		List<BootCarObject> cars = getDataObjects(filePath);
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
                log.info("Success");
            }
        };
        
        try
        {
        	String streamName = KinesisEncryptionUtils.getProperties().getProperty("stream_name");
        	String keyId = KinesisEncryptionUtils.getProperties().getProperty("key_id");
        	for(BootCarObject car: cars)
    		{
    			
    				log.info("Before encryption record is "+ car  );
    				ByteBuffer data = KinesisEncryptionUtils.toByteStream(car, keyId);
    				ListenableFuture<UserRecordResult> f = producer.addUserRecord(streamName, randomPartitionKey(), data);

    				Futures.addCallback(f, callback);
    				log.info("Encrypted record " + data.toString() + " " + "added successfully");
    			
    		}
        }
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	       
	}
	
	public static String randomPartitionKey() 
    {
        return new BigInteger(128, new Random()).toString(10);
    }
	public static  List<BootCarObject> getDataObjects(String filePath)
	{
		List<BootCarObject> carObjectList= new ArrayList<BootCarObject>();

		try 
		{
			FileInputStream fis = new FileInputStream(new File(filePath));
	    	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	    	String line = null;
	    	while ((line = br.readLine()) != null)
	    	{
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
