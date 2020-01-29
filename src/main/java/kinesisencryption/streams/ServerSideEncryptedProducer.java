package kinesisencryption.streams;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.EncryptionType;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.StartStreamEncryptionRequest;
import kinesisencryption.dao.TickerSalesObject;
import kinesisencryption.utils.KinesisEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static kinesisencryption.streams.EncryptedProducerWithStreams.randomPartitionKey;

public class ServerSideEncryptedProducer
{
    private static final Logger log = LoggerFactory.getLogger(EncryptedProducerWithStreams.class);
    private List<TickerSalesObject> tickerSymbolList;

    public List<TickerSalesObject> getTickerSymbolList() {
        return tickerSymbolList;
    }

    public void setTickerSymbolList(List<TickerSalesObject> tickerSymbolList) {
        this.tickerSymbolList = tickerSymbolList;
    }

    public static void main(String[] args) {
        AmazonKinesisClient kinesis = new AmazonKinesisClient(
                new DefaultAWSCredentialsProviderChain().getCredentials());


        /*
         * Simulating the appearance of a steady flow of data by continuously
         * loading data from file
         */
        try {

            String streamName = KinesisEncryptionUtils.getProperties().getProperty("stream_name");
            log.info("Successfully retrieved stream name property " + streamName);
            String keyId = KinesisEncryptionUtils.getProperties().getProperty("key_id");
            log.info("Successfully retrieved key id property " + keyId);
            String kinesisEndpoint = KinesisEncryptionUtils.getProperties().getProperty("kinesis_endpoint");
            log.info("Successfully retrieved kinesis endpoint property " + kinesisEndpoint);
            kinesis.setEndpoint(kinesisEndpoint);
            String fileLocation = KinesisEncryptionUtils.getProperties().getProperty("file_path");
            log.info("Successfully retrieved file location  property " + fileLocation);

            List<TickerSalesObject> tickerSymbolsList = KinesisEncryptionUtils.getDataObjects(fileLocation);
            ServerSideEncryptedProducer producer = new ServerSideEncryptedProducer();
            producer.setTickerSymbolList(tickerSymbolsList);
            StartStreamEncryptionRequest startStreamEncryptionRequest = new StartStreamEncryptionRequest();
            startStreamEncryptionRequest.setEncryptionType(EncryptionType.KMS);
            startStreamEncryptionRequest.setKeyId(keyId);
            startStreamEncryptionRequest.setStreamName(streamName);


            while (true) {

                for (TickerSalesObject ticker : tickerSymbolsList) {
                    PutRecordRequest putRecordRequest = new PutRecordRequest();
                    putRecordRequest.setStreamName(streamName);
                    log.info("Before encryption size of String Object is "
                            + KinesisEncryptionUtils.calculateSizeOfObject(ticker.toString()));
                    // UTF-8 encoding of encryptyed record
                    putRecordRequest.setData(ByteBuffer.wrap(String.format(ticker.toString()).getBytes("UTF-8")));
                    putRecordRequest.setPartitionKey(randomPartitionKey());
                    // putting the record into the stream
                    kinesis.putRecord(putRecordRequest);
                    log.info("Ticker added :" + ticker.toString() + "Ticker  :" + ticker.toString());

                }
                tickerSymbolsList = producer.getTickerSymbolList();
                Thread.sleep(100);
            }
        } catch (IOException ioe) {
            log.error(ioe.toString());
        } catch (InterruptedException ie) {
            log.error(ie.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
