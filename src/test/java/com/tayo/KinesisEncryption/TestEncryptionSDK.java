package com.tayo.KinesisEncryption;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kms.AWSKMSClient;
import junit.framework.Assert;
import junit.framework.TestCase;
import kinesisencryption.dao.BootCarObject;
import kinesisencryption.utils.KinesisEncryptionUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collections;
import java.util.Map;

/**
 * Created by temitayo on 1/26/17.
 */
public class TestEncryptionSDK extends TestCase
{
    BootCarObject car;
    String keyId;
    AmazonKinesisClient kinesis;
    private static final String STREAM_NAME = "UnitTestStream";
    AWSKMSClient kms;
    private final static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
    final static String keyArn = "arn:aws:kms:us-east-1:573906581002:key/37dc90dc-3f1c-4a77-a51d-a653b173fcdb";
    final AwsCrypto crypto = new AwsCrypto();
    final KmsMasterKeyProvider prov = new KmsMasterKeyProvider(keyArn);

    public void setUp() throws Exception
    {
        super.setUp();
        car = new BootCarObject("Volvo 740 GL", "134000", "2012");
        keyId="37dc90dc-3f1c-4a77-a51d-a653b173fcdb";
        kinesis = new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain()
                .getCredentials()).withRegion(Regions.US_EAST_1);
        kms = new AWSKMSClient(new ProfileCredentialsProvider()
                .getCredentials()).withRegion(Regions.US_EAST_1);



    }

    public void tearDown() throws Exception
    {
        super.tearDown();

    }

    public void testToBytStream()
    {
        final Map<String, String> context = Collections.singletonMap("Kinesis", "cars");
        try
        {
            ByteBuffer buffer  = KinesisEncryptionUtils.toByteStream(crypto, car, prov, context);
            System.out.println(buffer.toString());
            Assert.assertNotNull(buffer);
            String result = KinesisEncryptionUtils.decryptByteStream(crypto,buffer,prov, keyArn, context);
            Assert.assertEquals(car.toString(), result);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (CharacterCodingException e)
        {
            e.printStackTrace();
        }
    }


}
