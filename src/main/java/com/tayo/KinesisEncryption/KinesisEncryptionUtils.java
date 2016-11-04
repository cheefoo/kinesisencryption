package com.tayo.KinesisEncryption;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

}
