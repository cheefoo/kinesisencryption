package com.tayo.KinesisEncryption;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kms.AWSKMSClient;
import junit.framework.Assert;
import junit.framework.TestCase;
import kinesisencryption.dao.BootCarObject;
import kinesisencryption.utils.KinesisEncryptionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * Created by temitayo on 1/26/17.
 */
public class TestEncryptionSDK extends TestCase
{
    BootCarObject car;
    String keyId;

    AWSKMSClient kms;
    final static String keyArn = "arn:aws:kms:us-east-1:xxxxxxx:key/mykey-3f1c-4a77-a51d-isinaws";
    final AwsCrypto crypto = new AwsCrypto();
    final KmsMasterKeyProvider prov = new KmsMasterKeyProvider(keyArn);

    public void setUp() throws Exception
    {
        super.setUp();
        car = new BootCarObject("Volvo 740 GL", "2012","134000");
        keyId="mykey-3f1c-4a77-a51d-isinaws";

        kms = new AWSKMSClient(new DefaultAWSCredentialsProviderChain()
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
            String encryptedString = KinesisEncryptionUtils.toEncryptedString(crypto, car.toString(), prov, context);
            ByteBuffer buffer  = KinesisEncryptionUtils.toEncryptedByteStream(encryptedString);
            System.out.println(buffer.toString());

            Assert.assertNotNull(buffer);

            int sizeOfCar = KinesisEncryptionUtils.calculateSizeOfObject(car.toString());
            int sizeOfEncryptedCar = KinesisEncryptionUtils.calculateSizeOfObject(encryptedString);
            System.out.println("Size of Car is : " +sizeOfCar);
            System.out.println("Size of Encrypted Car is : " +sizeOfEncryptedCar);

            Assert.assertTrue("Correct", sizeOfCar < sizeOfEncryptedCar);

            String result = KinesisEncryptionUtils.decryptByteStream(crypto,buffer,prov, keyArn, context);
            Assert.assertEquals(car.toString(), result);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (CharacterCodingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void testToBytStreamForLargeRecord()
    {
        final Map<String, String> context = Collections.singletonMap("Kinesis", "cars");
        Charset encoding = StandardCharsets.UTF_8.defaultCharset();

        try
        {
            String record = readFile(filePath, encoding);
            String encryptedString = KinesisEncryptionUtils.toEncryptedString(crypto, record, prov, context);
            ByteBuffer buffer  = KinesisEncryptionUtils.toEncryptedByteStream(encryptedString);

            Assert.assertNotNull(buffer);

            int sizeOfCar = KinesisEncryptionUtils.calculateSizeOfObject(record);
            int sizeOfEncryptedCar = KinesisEncryptionUtils.calculateSizeOfObject(encryptedString);
            System.out.println("Size of Record is : " +sizeOfCar);
            System.out.println("Size of Encrypted Record is : " +sizeOfEncryptedCar);

            Assert.assertTrue("Correct", sizeOfCar < sizeOfEncryptedCar);

            String result = KinesisEncryptionUtils.decryptByteStream(crypto,buffer,prov, keyArn, context);
            Assert.assertEquals(record, result);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (CharacterCodingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }


    }

    private static final String filePath = "/Users/temitayo/Downloads/jeopardy-questions.json";

    static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }



}
