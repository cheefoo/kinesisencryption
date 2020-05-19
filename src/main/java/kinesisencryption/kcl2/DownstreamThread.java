package kinesisencryption.kcl2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kinesisencryption.dao.TickerSalesObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.Record;

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
        Map <String, Object>resultMap = new HashMap();
        for (Record record : this.getRecordList()) {
            try {
                ByteBuffer buffer = record.getData();

                String result = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();
                log.info("Cipher Blob :" + record.getData().toString() + " : " + "Decrypted Text is :"
                        + result);
                TickerSalesObject salesObject = new TickerSalesObject();
                ObjectMapper mapper = new ObjectMapper();
                salesObject = mapper.readValue(result, TickerSalesObject.class);
                resultMap.put(UUID.randomUUID().toString(), salesObject);
            } catch (CharacterCodingException e) {
                log.error("Unable to decode result for " + record.getData().toString() + "with equence number "
                        + record.getSequenceNumber());
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

