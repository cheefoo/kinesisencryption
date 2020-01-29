package kinesisencryption.kcl2;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kms.AWSKMSClient;

import kinesisencryption.utils.KinesisEncryptionUtils;

/**
 * Thread used for decrypting and printing consumed records to the logs
 *
 */
public class DownstreamThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(kinesisencryption.kcl.RecordPrinterThread.class);
    private List<Record> recordList;
    private AmazonKinesisClient kinesis;

    public AmazonKinesisClient getKinesis() {
        return kinesis;
    }


    public List<Record> getRecordList() {
        return recordList;
    }

    public DownstreamThread(List<Record> recordList) {
        this.recordList = recordList;
    }



    @Override
    public void run() {
        for (Record record : this.getRecordList()) {
            try {
                ByteBuffer buffer = record.getData();

                String result = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();
                log.info("Cipher Blob :" + record.getData().toString() + " : " + "Decrypted Text is :"
                        + result);
            } catch (CharacterCodingException e) {
                log.error("Unable to decode result for " + record.getData().toString() + "with equence number "
                        + record.getSequenceNumber());
            }
        }
    }
}

