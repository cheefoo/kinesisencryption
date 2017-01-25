package kinesisencryption.kcl;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;

/**
 * Created by temitayo on 1/24/17.
 */
public class EncryptedKCLRecordProcessorFactory implements IRecordProcessorFactory
{

    @Override
    public IRecordProcessor createProcessor()
    {
        return new EncryptedKCLRecordProcessor();
    }
}
