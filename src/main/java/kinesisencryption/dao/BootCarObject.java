package kinesisencryption.dao;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;

public class BootCarObject 
{
	private String name;
	private String year;
	private String odometer;


	

	public BootCarObject(String name, String year, String odometer) 
	{
		super();
		this.name = name;
		this.year = year;
		this.odometer = odometer;
	}
	@Override
	public String toString() 
	{
		return  name + "," + year + "," + odometer;
	}
	


	public ByteBuffer toByteStream(String keyId) throws UnsupportedEncodingException
	{
		AWSKMSClient kms = new AWSKMSClient(new ProfileCredentialsProvider()
    			.getCredentials()).withRegion(Regions.US_EAST_1);
		EncryptRequest request = new EncryptRequest()
				.withKeyId(keyId)
				.withPlaintext(ByteBuffer.wrap(String.format(this.toString()).getBytes("UTF-8")));
		EncryptResult result = kms.encrypt(request);
		return result.getCiphertextBlob();
	}
	

}
