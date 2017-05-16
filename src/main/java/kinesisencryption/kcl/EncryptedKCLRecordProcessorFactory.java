package kinesisencryption.kcl;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;

/**
 *
 */
public class EncryptedKCLRecordProcessorFactory implements IRecordProcessorFactory {
	public IRecordProcessor createProcessor() {
		return new EncryptedKCLRecordProcessor();
	}
}
