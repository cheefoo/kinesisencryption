package kinesisencryption.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Properties;

import kinesisencryption.dao.BootCarObject;
import kinesisencryption.streams.EncryptedConsumerWithStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;


public class KinesisEncryptionUtils 
{
	private static final Logger logger = LoggerFactory.getLogger(KinesisEncryptionUtils.class);
	public static java.util.Properties getProperties() throws IOException
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("app.properties");
        Properties prop = new Properties();
        logger.info("Input from classloader is :" + input.toString());
        prop.load(input);

        return prop;
	}

	/*public static ByteBuffer toByteStream(BootCarObject car, String keyId) throws UnsupportedEncodingException
	{
	AWSKMSClient kms = new AWSKMSClient(new ProfileCredentialsProvider()
			.getCredentials()).withRegion(Regions.US_EAST_1);
	EncryptRequest request = new EncryptRequest()
			.withKeyId(keyId)
			.withPlaintext(ByteBuffer.wrap(String.format(car.toString()).getBytes("UTF-8")));
	EncryptResult result = kms.encrypt(request);
	return result.getCiphertextBlob();
	}*/

	public static ByteBuffer toByteStream(AWSKMSClient kms, BootCarObject car, String keyId) throws UnsupportedEncodingException
	{

		EncryptRequest request = new EncryptRequest()
				.withKeyId(keyId)
				.withPlaintext(ByteBuffer.wrap(String.format(car.toString()).getBytes("UTF-8")));
		EncryptResult result = kms.encrypt(request);
		return result.getCiphertextBlob();
	}

}
