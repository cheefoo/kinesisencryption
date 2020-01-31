#!/bin/bash  
sudo yum install -y java-1.8.0-* git gcc-c++ make  
sudo yum remove -y java-1.7.0-*  
cd /home/ec2-user   
wget http://mirrors.whoishostingthis.com/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip  
unzip apache-maven-3.3.9-bin.zip  
echo "export PATH=$PATH:/home/ec2-user/apache-maven-3.3.9/bin" >> .bashrc  
git clone https://github.com/cheefoo/kinesisencryption.git  
mkdir ./kinesisencryption/logs  
chown -R ec2-user ./kinesisencryption 
