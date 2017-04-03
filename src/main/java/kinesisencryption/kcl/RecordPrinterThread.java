package kinesisencryption.kcl;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
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
public class RecordPrinterThread implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(RecordPrinterThread.class);
	private List<Record> recordList;
	private AmazonKinesisClient kinesis;
	private AWSKMSClient kms;
	private Map<String, String> context;
	private String keyArn;
	private final AwsCrypto crypto = new AwsCrypto();
	private KmsMasterKeyProvider prov;

	public AmazonKinesisClient getKinesis() {
		return kinesis;
	}

	public AWSKMSClient getKms() {
		return kms;
	}

	public List<Record> getRecordList() {
		return recordList;
	}

	public RecordPrinterThread(List<Record> recordList, Map<String, String> context, String keyArn) {
		this.recordList = recordList;
		this.context = context;
		this.keyArn = keyArn;
		this.prov = new KmsMasterKeyProvider(keyArn);
	}

	public Map<String, String> getContext() {
		return this.context;
	}

	public String getKeyArn() {
		return this.keyArn;
	}

	@Override
	public void run() {
		for (Record record : this.getRecordList()) {
			try {
				ByteBuffer buffer = record.getData();
				String decryptedResult = KinesisEncryptionUtils.decryptByteStream(crypto, buffer, prov,
						this.getKeyArn(), this.getContext());

				log.info("Cipher Blob :" + record.getData().toString() + " : " + "Decrypted Text is :"
						+ decryptedResult);
			} catch (CharacterCodingException e) {
				log.error("Unable to decode result for " + record.getData().toString() + "with equence number "
						+ record.getSequenceNumber());
			}
		}
	}
}
