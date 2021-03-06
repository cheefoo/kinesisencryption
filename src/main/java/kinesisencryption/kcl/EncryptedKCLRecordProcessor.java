package kinesisencryption.kcl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;

import kinesisencryption.utils.KinesisEncryptionUtils;

/**
 * A KCL RecordProcessor that processes encrypted Kinesis records
 */
public class EncryptedKCLRecordProcessor implements IRecordProcessor {
	private static final Logger log = LoggerFactory.getLogger(EncryptedKCLRecordProcessor.class);
	private String shardId, keyArn, encryptionContext;

	// Backoff and retry settings
	private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
	private static final int NUM_RETRIES = 10;

	// Checkpoint about once a minute
	private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L;
	private long nextCheckpointTimeInMillis;

	@Override
	public void initialize(String shardId) {
		log.info("Initializing record processor for shard :" + shardId);
		this.shardId = shardId;
	}

	@Override
	public void processRecords(List<Record> recordList, IRecordProcessorCheckpointer iRecordProcessorCheckpointer) {
		try {
			if (this.keyArn == null) {
				this.keyArn = KinesisEncryptionUtils.getProperties().getProperty("key_arn");
			}
			if (this.encryptionContext == null) {
				this.encryptionContext = KinesisEncryptionUtils.getProperties().getProperty("encryption_context");
			}

			final Map<String, String> context = Collections.singletonMap("Kinesis", encryptionContext);
			if (recordList.size() > 0) {
				log.info("Received record size is : " + recordList.size());
				RecordPrinterThread printer = new RecordPrinterThread(recordList, context, keyArn);
				Thread thread = new Thread(printer);
				thread.start();
			}

			if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
				checkpoint(iRecordProcessorCheckpointer);
				nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
			shutdown(iRecordProcessorCheckpointer, ShutdownReason.REQUESTED);
		}
	}

	@Override
	public void shutdown(IRecordProcessorCheckpointer iRecordProcessorCheckpointer, ShutdownReason shutdownReason) {
		log.info("Shutting down record processor for shard: " + shardId);
		// Important to checkpoint after reaching end of shard, so we can start
		// processing data from child shards.
		if (shutdownReason == ShutdownReason.TERMINATE) {
			checkpoint(iRecordProcessorCheckpointer);
		}
	}

	private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
		log.info("Checkpointing shard " + shardId);
		for (int i = 0; i < NUM_RETRIES; i++) {
			try {
				checkpointer.checkpoint();
				break;
			} catch (ShutdownException se) {
				// Ignore checkpoint if the processor instance has been shutdown
				// (fail over).
				log.info("Caught shutdown exception, skipping checkpoint.", se);
				break;
			} catch (ThrottlingException e) {
				// Backoff and re-attempt checkpoint upon transient failures
				if (i >= (NUM_RETRIES - 1)) {
					log.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
					break;
				} else {
					log.info("Transient issue when checkpointing - attempt " + (i + 1) + " of " + NUM_RETRIES, e);
				}
			} catch (InvalidStateException e) {
				// This indicates an issue with the DynamoDB table (check for
				// table, provisioned IOPS).
				log.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
				break;
			}

			try {
				Thread.sleep(BACKOFF_TIME_IN_MILLIS);
			} catch (InterruptedException e) {
				log.debug("Interrupted sleep", e);
			}
		}
	}
}