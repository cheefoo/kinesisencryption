package com.tayo.KinesisEncryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;


public class KinesisEncryptionUtils 
{
	private static final Logger logger = LoggerFactory.getLogger(EncryptedConsumerWithStreams.class);
	public static Properties getProperties() throws IOException
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("app.properties");
        Properties prop = new Properties();
        logger.info("Input from classloader is :" + input.toString());
        prop.load(input);
        Properties props = new Properties();
        props.setProperty("dbuser", prop.getProperty("dbuser"));
        props.setProperty("dburl", prop.getProperty("dburl"));
        props.setProperty("dbpwd", prop.getProperty("dbpwd"));
        props.setProperty("file_path", prop.getProperty("file_path"));
        props.setProperty("key_arn", prop.getProperty("key_arn"));
        props.setProperty("key_id", prop.getProperty("key_id"));
        props.setProperty("stream_name", prop.getProperty("stream_name"));
        props.setProperty("out_file_path", prop.getProperty("out_file_path"));
        
        return props;
	}
	
	public static ByteBuffer toByteStream(BootCarObject car, String keyId) throws UnsupportedEncodingException
	{
		AWSKMSClient kms = new AWSKMSClient(new ProfileCredentialsProvider()
    			.getCredentials()).withRegion(Regions.US_EAST_1);
		EncryptRequest request = new EncryptRequest()
				.withKeyId(keyId)
				.withPlaintext(ByteBuffer.wrap(String.format(car.toString()).getBytes("UTF-8")));
		EncryptResult result = kms.encrypt(request);
		return result.getCiphertextBlob();
	}

}
