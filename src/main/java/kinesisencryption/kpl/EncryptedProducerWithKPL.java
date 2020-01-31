package kinesisencryption.kpl;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.kinesis.producer.*;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
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

public class EncryptedProducerWithKPL {
	private static final Logger log = LoggerFactory.getLogger(EncryptedProducerWithKPL.class);
	private static final String DELIM = ",";
	private List<TickerSalesObject> tickerSymbolList;

	public List<TickerSalesObject> getTickerSymbolList() {
		return tickerSymbolList;
	}

	public void setTickerSymbolList(List<TickerSalesObject> tickerSymbolList) {
		this.tickerSymbolList = tickerSymbolList;
	}

	public static KinesisProducer getKinesisProducer() {
		KinesisProducerConfiguration config = KinesisProducerConfiguration
				.fromPropertiesFile("default_config.properties");
		config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());
		return new KinesisProducer(config);
	}

	public static void main(String[] args) throws Exception {

		String filePath = null;
		String kmsEndpoint = null;
		/*AWSKMSClient kms = new AWSKMSClient(new DefaultAWSCredentialsProviderChain().getCredentials());*/
		final AWSKMS kms = AWSKMSClientBuilder.standard().build();
		String keyArn = null;
		String encryptionContext = null;
		final AwsCrypto crypto = new AwsCrypto();
		new Profiler.Builder()
				.profilingGroupName("one1")
				.build().start();

		try {
			keyArn = KinesisEncryptionUtils.getProperties().getProperty("key_arn");
			log.info("Successfully retrieved keyarn property " + keyArn);
			encryptionContext = KinesisEncryptionUtils.getProperties().getProperty("encryption_context");
			log.info("Successfully retrieved encryption context property " + encryptionContext);
			filePath = KinesisEncryptionUtils.getProperties().getProperty("file_path");
			log.info("Successfully retrieved file path property " + filePath);
			kmsEndpoint = KinesisEncryptionUtils.getProperties().getProperty("kms_endpoint");
			log.info("Successfully retrieved kms endpoint property " + kmsEndpoint);
//			kms.setEndpoint(kmsEndpoint);
		} catch (IOException ioe) {
			log.error("Could not load properties file " + ioe.toString());
			throw new Exception("Could not load properties file");
		}
		final Map<String, String> context = Collections.singletonMap("Kinesis", encryptionContext);
		final KmsMasterKeyProvider prov = new KmsMasterKeyProvider(keyArn);

		List<TickerSalesObject> tickerObjectList = KinesisEncryptionUtils.getDataObjects(filePath);
		EncryptedProducerWithKPL encryptedProducerWithKPLProducer = new EncryptedProducerWithKPL();
		encryptedProducerWithKPLProducer.setTickerSymbolList(tickerObjectList);

		final KinesisProducer producer = getKinesisProducer();

		final FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>() {
			@Override
			public void onFailure(Throwable t) {

				if (t instanceof UserRecordFailedException) {
					Attempt last = Iterables.getLast(((UserRecordFailedException) t).getResult().getAttempts());
					log.error(String.format("Record failed to put - %s : %s", last.getErrorCode(),
							last.getErrorMessage()));
				}
				log.error("Exception during put", t);
				System.exit(1);
			}

			@Override
			public void onSuccess(UserRecordResult result) {
				log.info("Success");
			}
		};

		try {
			String streamName = KinesisEncryptionUtils.getProperties().getProperty("stream_name");
			while (true) {
				for (TickerSalesObject ticker : tickerObjectList) {
					log.info("Before encryption record is : " + ticker + "and size is : "
							+ KinesisEncryptionUtils.calculateSizeOfObject(ticker.toString()));
					// Encrypting the records
					String encryptedString = KinesisEncryptionUtils.toEncryptedString(crypto, ticker, prov, context);
					log.info("Size of encrypted object is : "
							+ KinesisEncryptionUtils.calculateSizeOfObject(encryptedString));
					// check if size of record is greater than 1MB
					if (KinesisEncryptionUtils.calculateSizeOfObject(encryptedString) > 1024000)
						log.warn("Record added is greater than 1MB and may be throttled");
					// UTF-8 encoding of encrypted record
					ByteBuffer data = KinesisEncryptionUtils.toEncryptedByteStream(encryptedString);
					// Adding the encrypted record to stream
					ListenableFuture<UserRecordResult> f = producer.addUserRecord(streamName, randomPartitionKey(),
							data);
					//Futures.addCallback(f, callback);
					log.info("Encrypted record " + data.toString() + " " + "added successfully");
				}
				tickerObjectList = encryptedProducerWithKPLProducer.getTickerSymbolList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String randomPartitionKey() {
		return new BigInteger(128, new Random()).toString(10);
	}
}
