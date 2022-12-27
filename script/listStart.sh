#!/bin/bash
flinkpath=/data/flink/flink-1.16.0
appName=mp-flink-list-1.0-SNAPSHOT.jar
mainClass=com.jxtii.kong.job.KongLogListJob

$flinkpath/bin/flink run $flinkpath/app/$appName -c $mainClass --kafka_bootstrap_servers 134.224.183.213:9093,134.224.183.213:9094,134.224.183.213:9095 --kafka_topic_input kong-log-cleaning --elasticsearch_hosts http://134.224.183.81:9201,http://134.224.183.81:9202,http://134.224.183.81:9203 --elasticsearch_username elastic  --elasticsearch_password Es7@2022

