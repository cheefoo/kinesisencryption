# kinesisencryption
Encrypting records with AWS KMS before sending to Kinesis Endpoint, Decrypting consumed records using AWS KMS

Demo to demonstrate the use of KMS to encrypt records before sending to a Kinesis Stream and to also decrypt records consumed from the stream
The default AWS region used here is us-east-1 
A file containing car data (car_odom1.txt) is read by the producer at startup and is reloaded several times to simulate streaming data.
#To use this application.
1. Create a stream in US_EAST_1 
2. Create a KMS Key in US_EAST as well
3. Clone the repository
git clone https://github.com/cheefoo/kinesisencryption.git
4. navigate to the resources folder kinesisencryption/src/main/resources/app.properties
5. Replace the values for 
file_path=/<path to >car_odom1.txt
key_arn=<Your KEY_ARN>
key_id=<Your KEY_ID>
stream_name=<Your Stream Name>
out_file_path=<>
6.Navigate to the root of your codebase
7. Startup the consumer
 mvn exec:java -Dexec.mainClass=com.tayo.KinesisEncryption.EncryptedConsumerWithStreams
8. Startup the producer
 mvn exec:java -Dexec.mainClass=com.tayo.KinesisEncryption.EncryptedProducerWithStreams
