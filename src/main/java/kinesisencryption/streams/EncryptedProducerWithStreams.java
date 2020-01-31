package kinesisencryption.streams;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import kinesisencryption.dao.TickerSalesObject;
import kinesisencryption.utils.KinesisEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.codeguruprofilerjavaagent.Profiler;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EncryptedProducerWithStreams {

	private static final Logger log = LoggerFactory.getLogger(EncryptedProducerWithStreams.class);
	private List<TickerSalesObject> tickerSymbolList;

	public List<TickerSalesObject> getTickerSymbolList() {
		return tickerSymbolList;
	}

	public void setTickerSymbolList(List<TickerSalesObject> tickerSymbolList) {
		this.tickerSymbolList = tickerSymbolList;
	}

	public static void main(String[] args) {
		/*AmazonKinesisClient kinesis = new AmazonKinesisClient(
				new DefaultAWSCredentialsProviderChain().getCredentials());*/
		AmazonKinesis kinesis = AmazonKinesisClientBuilder.standard().build();
		/*AWSKMSClient kms = new AWSKMSClient(new DefaultAWSCredentialsProviderChain().getCredentials());*/
		AWSKMS kms = AWSKMSClientBuilder.standard().build();
		String keyArn = null;
		String encryptionContext = null;
		final AwsCrypto crypto = new AwsCrypto();

		new Profiler.Builder()
				.profilingGroupName("one1")
				.build().start();
		/*
		 * Simulating the appearance of a steady flow of data by continuously
		 * loading data from file
		 */
		try {

			keyArn = KinesisEncryptionUtils.getProperties().getProperty("key_arn");
			log.info("Successfully retrieved keyarn property " + keyArn);
			encryptionContext = KinesisEncryptionUtils.getProperties().getProperty("encryption_context");
			log.info("Successfully retrieved encryption context property " + encryptionContext);
			String streamName = KinesisEncryptionUtils.getProperties().getProperty("stream_name");
			log.info("Successfully retrieved stream name property " + streamName);
			String keyId = KinesisEncryptionUtils.getProperties().getProperty("key_id");
			log.info("Successfully retrieved key id property " + keyId);
			String kinesisEndpoint = KinesisEncryptionUtils.getProperties().getProperty("kinesis_endpoint");
			log.info("Successfully retrieved kinesis endpoint property " + kinesisEndpoint);
			//kinesis.setEndpoint(kinesisEndpoint);
			String kmsEndpoint = KinesisEncryptionUtils.getProperties().getProperty("kms_endpoint");
			log.info("Successfully retrieved kms endpoint property " + kmsEndpoint);
			//kms.setEndpoint(kmsEndpoint);
			String fileLocation = KinesisEncryptionUtils.getProperties().getProperty("file_path");
			log.info("Successfully retrieved file location  property " + fileLocation);

			List<TickerSalesObject> tickerSymbolsList = KinesisEncryptionUtils.getDataObjects(fileLocation);
			EncryptedProducerWithStreams producer = new EncryptedProducerWithStreams();
			producer.setTickerSymbolList(tickerSymbolsList);

			final Map<String, String> context = Collections.singletonMap("Kinesis", encryptionContext);
			final KmsMasterKeyProvider prov = new KmsMasterKeyProvider(keyArn);

			while (true) {

				for (TickerSalesObject ticker : tickerSymbolsList) {
					PutRecordRequest putRecordRequest = new PutRecordRequest();
					putRecordRequest.setStreamName(streamName);
					log.info("Before encryption size of String Object is "
							+ KinesisEncryptionUtils.calculateSizeOfObject(ticker.toString()));
					// Encrypting the records
					String encryptedString = KinesisEncryptionUtils.toEncryptedString(crypto, ticker, prov, context);
					log.info("Size of encrypted object is : "
							+ KinesisEncryptionUtils.calculateSizeOfObject(encryptedString));
					// check if size of record is greater than 1MB
					if (KinesisEncryptionUtils.calculateSizeOfObject(encryptedString) > 1024000)
						log.warn("Record added is greater than 1MB and may be throttled");
					// UTF-8 encoding of encryptyed record
					ByteBuffer data = KinesisEncryptionUtils.toEncryptedByteStream(encryptedString);
					putRecordRequest.setData(data);
					putRecordRequest.setPartitionKey(randomPartitionKey());
					// putting the record into the stream
					kinesis.putRecord(putRecordRequest);
					log.info("Ticker added :" + ticker.toString() + "Ticker Cipher :" + data.toString() + "and size : "
							+ KinesisEncryptionUtils.calculateSizeOfObject(data.toString()));

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

	public static String randomPartitionKey() {
		return new BigInteger(128, new Random()).toString(10);
	}

}
