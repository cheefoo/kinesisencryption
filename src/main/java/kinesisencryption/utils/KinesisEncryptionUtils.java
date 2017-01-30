package kinesisencryption.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;

import kinesisencryption.dao.BootCarObject;
import kinesisencryption.dao.TickerSalesObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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


    public static ByteBuffer toEncryptedByteStream(AWSKMSClient kms, Object car, String keyId) throws UnsupportedEncodingException
    {

        EncryptRequest request = new EncryptRequest()
                .withKeyId(keyId)
                .withPlaintext(ByteBuffer.wrap(String.format(car.toString()).getBytes("UTF-8")));
        log.info(request.toString());
        EncryptResult result = kms.encrypt(request);
        log.info(result.toString());
        return result.getCiphertextBlob();
    }

    public static String toEncryptedString(AwsCrypto crypto, Object object, final KmsMasterKeyProvider prov,
                                                   final Map<String, String> context) throws IOException
    {
        final String ciphertext = crypto.encryptString(prov, object.toString(), context).getResult();

        return ciphertext;
    }

    public static ByteBuffer toEncryptedByteStream(String ciphertext) throws IOException
    {

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

    public static int calculateSizeOfObject(Object obj) throws IOException
    {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        objectOutputStream.close();

        return byteOutputStream.toByteArray().length;

    }

    public static List<String> getFilesToSend(final File folder)
    {
        List<String> filesToSend = new ArrayList<String>();
        for (final File fileEntry : folder.listFiles())
        {
            if (fileEntry.isDirectory())
            {
                log.info("File is a directory.... skipping");
            }
            else
            {
                filesToSend.add(fileEntry.getAbsolutePath());
            }
        }

        return filesToSend;
    }

    public static List<TickerSalesObject> getDataObjects(String fileName) throws Exception
    {
        List<TickerSalesObject> userObjectList= new ArrayList<TickerSalesObject>();
        try
        {
            // read the json file
            FileReader reader = new FileReader(fileName);
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray)jsonParser.parse(reader);

            for(Object obj : jsonArray)
            {
                JSONObject jsonObject = (JSONObject) obj;
                String symbol = (String) jsonObject.get("symbol");
                String salesPrice = (String) jsonObject.get("salesPrice");
                String orderId = (String) jsonObject.get("orderId");
                String activityTimestamp = (String) jsonObject.get("activityTimestamp");

                TickerSalesObject user = new TickerSalesObject(symbol,salesPrice, orderId,activityTimestamp);
                userObjectList.add(user);
            }

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        catch (ParseException ex)
        {
            ex.printStackTrace();
        }

        log.info("Finished reading all objects from file");

        return userObjectList;
    }

    public static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
