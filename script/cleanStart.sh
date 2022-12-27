#!/bin/bash
flinkpath=/root/flink-1.16.0
appName=mp-flink-cleaning-1.0-SNAPSHOT.jar
mainClass=com.jxtii.kong.job.KongLogCleaningJob

$flinkpath/bin/flink run $flinkpath/app/$appName -c $mainClass --kafka_bootstrap_servers 134.224.183.213:9093,134.224.183.213:9094,134.224.183.213:9095 --kafka_topic_input kong-log-beat --kafka_topic_output kong-log-cleaning --postgresql_driver org.postgresql.Driver --postgresql_url jdbc:postgresql://134.224.183.211:45432/devops472?stringtype=unspecified --postgresql_user devops --postgresql_password It-Cloud-Support_Devops@0629
