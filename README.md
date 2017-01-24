Incomplete / WIP
# KinesisEncryption
Encrypting records with AWS KMS before sending to Kinesis Endpoint and Decrypting the consumed records using AWS KMS
##Architecture Diagram:
![alt tag](https://github.com/cheefoo/kinesisencryption/blob/master/KinesisEncrypt.png)

Demo to demonstrate the use of KMS to encrypt records before sending to a Kinesis Stream and to also decrypt records consumed from the stream. One example each of encrypting records with the KPL and the Streams API and another example ofdecrypting the records with Streams API and KCL
The default AWS region used here is us-east-1 
A file containing car data (car_odom1.txt) is read by the producer at startup and is reloaded several times to simulate streaming data.
###Requirements:
1. An Amazon Web Services Account
2. AWS CLI Installed and configured
3. After following the steps in the Getting Started section, you will have set up the following resources:
3.1. An AWS kinesis Stream
3.2. Two IAM roles, Instance Profiles and Policies required for the KCL and KPL instances
3.3. Two AWS EC2 Instances based on AmazonLinux with dependencies pre-installed
3.3. An RDS mysql database
3.4. An Amazon S3 bucket

#To run the example application.
1. Create a Kinesis stream i
2. Create a KMS Key 
3. Create the Kinesis IAM roles required for EC2 Instances  
  ```
  aws iam create-role \  
  --role-name 12616-KPLRole \  
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

  aws iam create-role \  
  --role-name 12616-KCLRole \  
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

  aws iam create-instance-profile --instance-profile-name 12616-KCLRole  

  aws iam create-instance-profile --instance-profile-name 12616-KPLRole  

  aws iam add-role-to-instance-profile --instance-profile-name 12616-KPLRole --role-name 12616-KPLRole  

  aws iam add-role-to-instance-profile --instance-profile-name 12616-KCLRole --role-name 12616-KCLRole  
  ```
4. Create the Kinesis IAM Policies  (Please replace the account ids with your own account id)
  ```
  aws iam create-policy \  
  --policy-name 12616-KPLPolicy \  
  --policy-document '  
  {  
      "Version": "2012-10-17",  
      "Statement": [{  
          "Effect": "Allow",  
          "Action": ["kinesis:PutRecord","kinesis:PutRecords","kinesis:DescribeStream"],  
          "Resource": ["arn:aws:kinesis:us-east-1:111122223333:stream/12616-Stream"]  
      },
      {  
          "Sid": "Stmt1482832527000",  
          "Effect": "Allow",  
          "Action": ["cloudwatch:PutMetricData"],  
          "Resource": ["*"]  
      }
      ]  
  }'  

  aws iam create-policy \  
  --policy-name 12616-KCLPolicy \  
  --policy-document '  
  {  
      "Version": "2012-10-17",  
      "Statement": [{  
          "Effect": "Allow",  
          "Action": ["kinesis:Get*"],  
          "Resource": ["arn:aws:kinesis:us-east-1:111122223333:stream/12616-Stream"]  
      }, 
      {
            "Effect": "Allow",
            "Action": [
                "s3:*"
            ],
            "Resource": ["arn:aws:s3:::12616S3Bucket-","arn:aws:s3:::<BUCKET_NAME>/*"]
        },
      {  
          "Effect": "Allow",  
          "Action": ["kinesis:DescribeStream"],  
          "Resource": ["arn:aws:kinesis:us-east-1:111122223333:stream/12616-Stream"]  
      }, {  
          "Effect": "Allow",  
          "Action": ["kinesis:ListStreams"],  
          "Resource": ["*"]  
      }, {  
          "Effect": "Allow",  
          "Action": ["dynamodb:CreateTable", "dynamodb:DescribeTable", "dynamodb:Scan", "dynamodb:PutItem", "dynamodb:UpdateItem", "dynamodb:GetItem"],  
          "Resource": ["arn:aws:dynamodb:us-east-1:111122223333:table/Centos*"]  
      }, {  
          "Sid": "Stmt1482832527000",  
          "Effect": "Allow",  
          "Action": ["cloudwatch:PutMetricData"],  
          "Resource": ["*"]  
      }]  
  }'  
  ```
5. Attach the Policies to the Roles  
  ```
  aws iam attach-role-policy \  
  --policy-arn "arn:aws:iam::111122223333:policy/12616-KPLPolicy" \  
  --role-name 12616-KPLRole  

  aws iam attach-role-policy \  
  --policy-arn "arn:aws:iam::111122223333:policy/12616-KCLPolicy" \  
  --role-name 12616-KCLRole  
  ```
6. Create a Bootstrap script to automate the installation of the dependencies on newly launched instances  
  ```
  cat <<EOF > Bootstrap.sh  
  #!/bin/bash  
  sudo yum install -y java-1.8.0-* git gcc-c++ make  
  sudo yum remove -y java-1.7.0-*  
  curl --silent --location https://rpm.nodesource.com/setup_6.x | sudo bash -  
  sudo yum install -y nodejs 
  sudo yum install mysql -y
  sudo pip install faker  
  cd /home/ec2-user   
  wget http://mirrors.whoishostingthis.com/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip  
  unzip apache-maven-3.3.9-bin.zip  
  echo "export PATH=\$PATH:/home/ec2-user/apache-maven-3.3.9/bin" >> .bashrc  
  git clone https://github.com/cheefoo/centos.git  
  mkdir ./centos/logs  
  chown -R ec2-user ./centos  
  EOF  

  ```
7. Please note that image-id given in below command belongs to us-east-1, if you are launching in a different region please look up the image-id for that region [AWS Linux AMI IDs](https://aws.amazon.com/amazon-linux-ami/). Take note of the returned "InstanceId" after launching each instance in order to create tags
  ``` 
  aws ec2 run-instances \  
  --image-id ami-9be6f38c \  
  --key-name sshkeypair \  
  --security-groups default \  
  --instance-type m3.large \  
  --iam-instance-profile Name="12616-KPLRole" \  
  --user-data file://Bootstrap.sh  

  aws ec2 create-tags --resources i-000d3b6d9fexample --tags Key=Name,Value="12616-KPLInstance"  

  aws ec2 run-instances \  
  --image-id ami-9be6f38c \  
  --key-name sshkeypair \  
  --security-groups default \  
  --instance-type m3.large \  
  --iam-instance-profile Name="12616-KCLRole" \  
  --user-data file://Bootstrap.sh  

  aws ec2 create-tags --resources i-0879e274caexample --tags Key=Name,Value="12616-KCLInstance"  
  ```
8. Create an RDS Instance and take note of the JDBC Endpoint, username and password  
  ```
  aws rds create-db-instance \  
  --db-instance-identifier RDSInstance12616 \  
  --db-name DB12616 \  
  --engine mysql \  
  --master-username groot \  
  --master-user-password ********** \  
  --db-instance-class db.t1.micro \  
  --allocated-storage 8  

  ```
9. Create an Amazon S3 bucket  
  ```
  aws s3 mb s3://12616S3Bucket  

  ```
10. Dont forget to modify the default security group to allow ssh access. 

### Running the Example Application
| Key           | Default                                        | Description                                                                     |
| :------------ | :--------------------------------------------- | :------------------------------------------------------------------------------ |
| file_path     | /Users/xxxxxx/workspace/kinesisencryption/car_odom1.txt | path to the file containing the records                                                             |
| key_id  | xxxxxx-3f1c-4a77-a51d-a653b173fcdb    | Id of your KMS key                                         |
| stream_name | EncryptedStream    | Name of the AWS Kinesis Stream                                      |
                                           |
| kinesis_endpoint | Endpoint of the Kinesis Stream    | Name of the AWS Kinesis endpoint                                      |
                                           |
| kms_endpoint | EncryptedStream    | Name of the AWS KMS endpoint                                      |
                                           |
| sharditerator_type | TRIM_HORIZON    | Shard Iterator type for stream consumer                                    |
                                           |

6.Navigate to the root of your codebase
7. Startup the consumer
 mvn exec:java -Dexec.mainClass=kinesisencryption.streams.EncryptedConsumerWithStreams
8. Startup the producer
 mvn exec:java -Dexec.mainClass=com.tayo.KinesisEncryption.EncryptedProducerWithStreams
