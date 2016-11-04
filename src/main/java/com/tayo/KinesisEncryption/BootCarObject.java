package com.tayo.KinesisEncryption;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;

public class BootCarObject 
{
	private String name;
	private String year;
	private String odometer;
	private static final String KEY_ARN= "arn:aws:kms:us-east-1:573906581002:key/37dc90dc-3f1c-4a77-a51d-a653b173fcdb";
	final AwsCrypto crypto = new AwsCrypto();
    final KmsMasterKeyProvider prov = new KmsMasterKeyProvider(KEY_ARN);
    
	
	public String getName() 
	{
		return name;
	}
	public void setName(String name) 
	{
		this.name = name;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) 
	{
		this.year = year;
	}
	public String getOdometer() 
	{
		return odometer;
	}
	public void setOdometer(String odometer) 
	{
		this.odometer = odometer;
	}
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
	
	//Relevant to the encryption SDK not working currently
	public String encryptedToString()
	{
		prov.setRegion(Region.getRegion(Regions.US_EAST_1));
		return crypto.encryptString(prov, name).getResult() + "," 
				+ crypto.encryptString(prov, year).getResult() + "," + crypto.encryptString(prov, odometer).getResult();
	}
	
	public ByteBuffer toByteStream() throws UnsupportedEncodingException
	{
		AWSKMSClient kms = new AWSKMSClient(new ProfileCredentialsProvider()
    			.getCredentials()).withRegion(Regions.US_EAST_1);
		EncryptRequest request = new EncryptRequest()
				.withKeyId("37dc90dc-3f1c-4a77-a51d-a653b173fcdb")
				.withPlaintext(ByteBuffer.wrap(String.format(this.toString()).getBytes("UTF-8")));
		EncryptResult result = kms.encrypt(request);
		return result.getCiphertextBlob();
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
	
	public String decryptedToString(String cipher)
	{
		return new AwsCrypto().decryptString(prov, cipher).getResult();
	}
	
	public String decryptedToString(String cipher, String keyArn)
	{
		final KmsMasterKeyProvider prov = new KmsMasterKeyProvider(keyArn);
		return new AwsCrypto().decryptString(prov, cipher).getResult();
	}
	
	
	

}
