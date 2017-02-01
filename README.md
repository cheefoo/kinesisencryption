Incomplete / WIP
# KinesisEncryption
Encrypting records with AWS KMS before sending to Kinesis Endpoint and Decrypting the consumed records using AWS KMS
##Architecture Diagram:
![alt tag](https://github.com/cheefoo/kinesisencryption/blob/master/KinesisEncrypt.png)

Demo to demonstrate the use of KMS to encrypt records before sending to a Kinesis Stream and to also decrypt records consumed from the stream. One example each of encrypting records with the KPL and the Streams API and another example of decrypting the records with Streams API and KCL
The default AWS region used here is us-east-1 
A file containing stock sales data is read by the producer(s) at startup and is reloaded several times to simulate streaming data.
###Requirements:
1. An Amazon Web Services [Account](https://aws.amazon.com/free/?sc_channel=PS&sc_campaign=acquisition_ZA&sc_publisher=google&sc_medium=cloud_computing_b&sc_content=aws_account_e&sc_detail=aws%20account&sc_category=cloud_computing&sc_segment=77706639422&sc_matchtype=e&sc_country=ZA&s_kwcid=AL!4422!3!77706639422!e!!g!!aws%20account&ef_id=V9u@TgAABMH86aOm:20161227051709:s)
2. AWS CLI Installed and configured
3. After following the steps in the Getting Started section, you will have set up the following resources:
    
    3.1.  An AWS kinesis Stream
    
    3.2.  One IAM role, Instance Profile and Policy required for the ec2 instance
    
    3.3.  One AWS EC2 Instance based on AmazonLinux with dependencies pre-installed
  
4. When the KCL is initiated, a DynamoDB table is created
5. [Java Crystography Extension Unlimited Strength Jurisdiction Policy Files] (http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) extract the contents from the zipped file and save them to ${java.home}/jre/lib/security/.
  
#To run the example application.
1. Create a Kinesis stream 

```
aws kinesis create-stream --stream-name 012417-Stream --shard-count 2 
```

2. Create a KMS Key 

```
aws kms create-key 
```

3. Create the Kinesis IAM role required for EC2 Instances  
  ```
  aws iam create-role \  
  --role-name 012417-EncryptionRole \  
  --assume-role-policy-document '  
  {  
      "Version": "2012-10-17",  
      "Statement": [{  
          "Sid": "",  
          "Effect": "Allow",  
          "Principal": {  
              "Service": "ec2.amazonaws.com"  
          },  
          "Action": "sts:AssumeRole"  
      }]  
  }'  

  aws iam create-instance-profile --instance-profile-name 012417-EncryptionRole  

  aws iam add-role-to-instance-profile --instance-profile-name 012417-EncryptionRole  --role-name 012417-EncryptionRole 
   ```

4. Create the Kinesis IAM Policy  (Please replace the account ids with your own account id)
  ```
  aws iam create-policy \  
  --policy-name 012417-EncryptionPolicy \  
  --policy-document '  
  {  
      "Version": "2012-10-17",  
      "Statement": 
      [
      {  
          "Effect": "Allow",  
          "Action": ["kinesis:PutRecord","kinesis:PutRecords","kinesis:DescribeStream","kinesis:Get*"],  
          "Resource": ["arn:aws:kinesis:us-east-1:111122223333:stream/012417-Stream"]  
      },
      {  
          "Sid": "Stmt1482832527000",  
          "Effect": "Allow",  
          "Action": ["cloudwatch:PutMetricData"],  
          "Resource": ["*"]  
      },
      {  
          "Effect": "Allow",  
          "Action": ["kinesis:ListStreams"],  
          "Resource": ["*"]  
      }, 
      {  
          "Effect": "Allow",  
          "Action": ["dynamodb:CreateTable", "dynamodb:DescribeTable", "dynamodb:Scan", "dynamodb:PutItem",        "dynamodb:UpdateItem", "dynamodb:GetItem"],  
          "Resource": ["arn:aws:dynamodb:us-east-1:111122223333:table/Encrypted*"]  
      },
      {
    "Effect": "Allow",
    "Action": ["kms:Encrypt","kms:Decrypt"],
    "Resource": [
      "arn:aws:kms:us-west-2:111122223333:key/1234abcd-12ab-34cd-56ef-1234567890ab"
      ]
  }
  ]  
  }'  
 
  ```
5. Attach the Policies to the Roles  
  ```
  aws iam attach-role-policy \  
  --policy-arn "arn:aws:iam::111122223333:policy/012417-EncryptionPolicy" 
  --role-name 012417-EncryptionRole  
  ```
6. Create a Bootstrap script to automate the installation of the dependencies on newly launched instances  
  ```
  cat <<EOF > Bootstrap.sh  
  #!/bin/bash  
  sudo yum install -y java-1.8.0-* git gcc-c++ make  
  sudo yum remove -y java-1.7.0-* 
  sudo pip install faker
  cd /home/ec2-user   
  wget http://mirrors.whoishostingthis.com/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip  
  unzip apache-maven-3.3.9-bin.zip  
  echo "export PATH=\$PATH:/home/ec2-user/apache-maven-3.3.9/bin" >> .bashrc  
  git clone https://github.com/cheefoo/kinesisencryption.git  
  mkdir ./kinesisencryption/logs  
  chown -R ec2-user ./kinesisencryption  
  EOF  

  ```
7. Please note that image-id given in below command belongs to us-east-1, if you are launching in a different region please look up the image-id for that region [AWS Linux AMI IDs](https://aws.amazon.com/amazon-linux-ami/). Take note of the returned "InstanceId" after launching each instance in order to create tags
  ``` 
  aws ec2 run-instances \  
  --image-id ami-9be6f38c \  
  --key-name sshkeypair \  
  --security-groups default \  
  --instance-type m3.large \  
  --iam-instance-profile Name="012417-EncryptionRole" \  
  --user-data file://Bootstrap.sh  

  aws ec2 create-tags --resources i-000d3b6d9fexample --tags Key=Name,Value="012417-EncryptionInstance"  

    ```
8. Dont forget to modify the default security group to allow ssh access. 

### Running the Example Application 
1. SSH into the created Instance and modify the app.properties file located in src/main/resources according to created resources below 


| Key           | Default                                        | Description                                                                     |
| :------------ | :--------------------------------------------- | :------------------------------------------------------------------------------ |
| file_path     | /home/ec2-user/kinesisencryption/script/kplWatch/ | path to the file containing the records                                                             |
| key_id  | xxxxxx-3f1c-4a77-a51d-a653b173fcdb    | Id of your KMS key                                         |
| key_arn  | arn:aws:kms:us-east-1:1111111111111:key/xxxxxx-3f1c-4a77-a51d-a653b173fcdb    | ARN of the KMS  |                                        
| stream_name | EncryptedStream    | Name of the AWS Kinesis Stream                                      |                                  
| kinesis_endpoint | Endpoint of the Kinesis Stream    | Name of the AWS Kinesis endpoint  |   
| kms_endpoint | EncryptedStream    | Name of the AWS KMS endpoint                                      |
| sharditerator_type | TRIM_HORIZON    | Shard Iterator type for stream consumer                                    |
| kcl_name | TRIM_HORIZON    | Name of the KCL app                                     |
| ddb_region_4_kcl | us-east-1    | AWS region in which dynamodb table is created for the KCL                                  |
| encryption_context | cars    | Encryption context is a set of key-value pairs that you can pass to AWS KMS  |                                  

                                         
2. Modify the default_config.properties and replace the region property with the region in which your stream is located

3. Navigate to the root of your codebase 

```
cd kinesisencryption
mvn compile
```

4.Startup the Streams consumer
```
 nohup bash -c "(mvn exec:java -Dexec.mainClass=kinesisencryption.streams.EncryptedConsumerWithStreams > ~/kinesisencryption/logs/EncryptedConsumerWithStreams.log) &> ~/kinesisencryption/logs/EncryptedConsumerWithStreams.log" & 
 ```
 
5.Startup the Streams producer
```
 nohup bash -c "(mvn exec:java -Dexec.mainClass=kinesisencryption.streams.EncryptedProducerWithStreams > ~/kinesisencryption/logs/EncryptedProducerWithStreams.log) &> ~/kinesisencryption/logs/EncryptedProducerWithStreams.log" &
 ```
 
6.Startup the KCL consumer
```
 nohup bash -c "(mvn exec:java -Dexec.mainClass=kinesisencryption.kcl.EncryptedConsumerWithKCL > ~/kinesisencryption/logs/EncryptedConsumerWithKCL.log) &> ~/kinesisencryption/logs/EncryptedConsumerWithKCL.log" & 
 ```
 
7.Startup the KPL producer
```
 nohup bash -c "(mvn exec:java -Dexec.mainClass=kinesisencryption.kpl.EncryptedProducerWithKPL > ~/kinesisencryption/logs/EncryptedProducerWithKPL.log) &> ~/kinesisencryption/logs/EncryptedProducerWithKPL.log" &  
 ```
