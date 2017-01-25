package kinesisencryption.kcl;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

/**
 * Created by temitayo on 1/24/17.
 */
public class RecordPrinterThread implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(RecordPrinterThread.class);
    private List<Record> recordList;
    private static final CharsetDecoder DECODER = Charset.forName("UTF-8").newDecoder();
    private AmazonKinesisClient kinesis;
    private AWSKMSClient kms;

    public RecordPrinterThread(List<Record> recordList, AmazonKinesisClient kinesis, AWSKMSClient kms)
    {
        this.recordList = recordList;
        this.kinesis = kinesis;
        this.kms = kms;
    }

    public AmazonKinesisClient getKinesis()
    {
        return kinesis;
    }

    public AWSKMSClient getKms()
    {
        return kms;
    }

    public List<Record> getRecordList()
    {
        return recordList;
    }

    public void setRecordList(List<Record> recordList)
    {
        this.recordList = recordList;
    }

    public static CharsetDecoder getDECODER()
    {
        return DECODER;
    }

    @Override
    public void run()
    {
        for (Record record: this.getRecordList())
        {
            DecryptRequest decrypter = new DecryptRequest().withCiphertextBlob(record.getData());
            DecryptResult dresult = this.getKms().decrypt(decrypter);
            try
            {
                log.info("Cipher Blob :" + record.getData().toString() + " : " + "Decrypted Text is :"
                        + DECODER.decode(dresult.getPlaintext()).toString());
            }
            catch (CharacterCodingException e)
            {
                log.error("Unable to decode result for " + dresult.getPlaintext().toString());
            }
        }

    }
}
