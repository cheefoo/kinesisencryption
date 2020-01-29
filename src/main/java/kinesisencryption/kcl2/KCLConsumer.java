package kinesisencryption.kcl2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import kinesisencryption.utils.KinesisEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.UUID;

/**
 * A consumer application that consumes encrypted records from kinesis streams.
 */
public class KCLConsumer {
    private static final InitialPositionInStream INITIAL_POSITION_IN_STREAM = InitialPositionInStream.TRIM_HORIZON;
    private static AWSCredentialsProvider credentialsProvider;
    private static final Logger log = LoggerFactory.getLogger(kinesisencryption.kcl.EncryptedConsumerWithKCL.class);

    private static void initialize() {
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
        credentialsProvider = new DefaultAWSCredentialsProviderChain();

        try {
            credentialsProvider.getCredentials();

        } catch (Exception e) {
            throw new AmazonClientException("Cannot find credentials");
        }

    }

    public static void main(String[] args) throws Exception {
        initialize();

        String workerId = InetAddress.getLocalHost().getCanonicalHostName() + ":" + UUID.randomUUID();
        String streamName = KinesisEncryptionUtils.getProperties().getProperty("stream_name");
        String appName = KinesisEncryptionUtils.getProperties().getProperty("kcl_name");
        String ddbRegion = KinesisEncryptionUtils.getProperties().getProperty("ddb_region_4_kcl");
        KinesisClientLibConfiguration kinesisClientLibConfiguration = new KinesisClientLibConfiguration(appName,
                streamName, credentialsProvider, workerId);
        kinesisClientLibConfiguration.withInitialPositionInStream(INITIAL_POSITION_IN_STREAM).withRegionName(ddbRegion);

        IRecordProcessorFactory recordProcessorFactory = new KCLRecordProcessorFactory();
        Worker worker = new Worker(recordProcessorFactory, kinesisClientLibConfiguration);

        log.info("Started KCL Worker process for Stream " + streamName + " " + "with workerId " + workerId);

        int exitCode = 0;
        try {
            worker.run();
        } catch (Throwable t) {
            System.err.println("Caught throwable while processing data.");
            t.printStackTrace();
            exitCode = 1;
        }
        System.exit(exitCode);
    }
}
