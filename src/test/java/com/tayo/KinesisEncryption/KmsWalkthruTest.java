package com.tayo.KinesisEncryption;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;
import com.amazonaws.services.kms.model.KeyListEntry;
import com.amazonaws.services.kms.model.ListKeysResult;

public class KmsWalkthruTest 
{
	private final static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	public static void main (String [] args) throws UnsupportedEncodingException, CharacterCodingException
	{
		AWSKMSClient kms = new AWSKMSClient(new DefaultAWSCredentialsProviderChain()
    			.getCredentials()).withRegion(Regions.US_EAST_1);
		ListKeysResult keyResults = kms.listKeys();
		List<KeyListEntry> keys = keyResults.getKeys();
		int i = 0;
		for(KeyListEntry key : keys)
		{
			System.out.println("Key :" + i + ": " + key.getKeyId());
			++i;
		}
		
		EncryptRequest request = new EncryptRequest()
				.withKeyId("mykey-3f1c-4a77-a51d-isinaws")
				.withPlaintext(ByteBuffer.wrap(String.format("temitayo").getBytes("UTF-8")));
		
		
		EncryptResult result = kms.encrypt(request);
		
		System.out.println(result.getCiphertextBlob().toString());
		
		
		DecryptRequest decrypter = new DecryptRequest().withCiphertextBlob(result.getCiphertextBlob());
		
	    DecryptResult dresult = kms.decrypt(decrypter);
	    
	    System.out.println(decoder.decode(dresult.getPlaintext()).toString());
		
	}

}
