package kinesisencryption.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import kinesisencryption.dao.BootCarObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;


public class KinesisEncryptionUtils
{
    private static final Logger log = LoggerFactory.getLogger(KinesisEncryptionUtils.class);
    private final static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

    public static java.util.Properties getProperties() throws IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("app.properties");
        Properties prop = new Properties();
        log.info("Input from classloader is :" + input.toString());
        prop.load(input);

        return prop;
    }


    public static ByteBuffer toByteStream(AWSKMSClient kms, BootCarObject car, String keyId) throws UnsupportedEncodingException
    {

        EncryptRequest request = new EncryptRequest()
                .withKeyId(keyId)
                .withPlaintext(ByteBuffer.wrap(String.format(car.toString()).getBytes("UTF-8")));
        log.info(request.toString());
        EncryptResult result = kms.encrypt(request);
        log.info(result.toString());
        return result.getCiphertextBlob();
    }

    public static ByteBuffer toByteStream(AwsCrypto crypto, Object car, final KmsMasterKeyProvider prov, final Map<String, String> context) throws UnsupportedEncodingException
    {
        final String ciphertext = crypto.encryptString(prov, car.toString(), context).getResult();

        return ByteBuffer.wrap(String.format(ciphertext.toString()).getBytes("UTF-8"));
    }

    public static String decryptByteStream(AwsCrypto crypto,  ByteBuffer buffer, final KmsMasterKeyProvider prov, String keyArn, final Map<String, String> context) throws CharacterCodingException
    {

        String decoded = decoder.decode(buffer).toString();
        final CryptoResult<String, KmsMasterKey> decryptedResult = crypto.decryptString(prov, decoded);

        // We need to check the encryption context (and ideally key) to ensure that
        // this was the ciphertext we expected
        if (!decryptedResult.getMasterKeyIds().get(0).equals(keyArn))
        {
            throw new IllegalStateException("Wrong key id!");
        }

        // The SDK may add information to the encryption context, so we check to ensure
        // that all of our values are present
        for (final Map.Entry<String, String> e : context.entrySet()) {
            if (!e.getValue().equals(decryptedResult.getEncryptionContext().get(e.getKey()))) {
                throw new IllegalStateException("Wrong Encryption Context!");
            }
        }

        return decryptedResult.getResult();

    }

}
