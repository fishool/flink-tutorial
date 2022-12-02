package com.ijavac.demo.flink;

import com.ijavac.demo.flink.module.JobParameters;
import com.ijavac.demo.flink.module.KafkaDemo;
import com.ijavac.demo.flink.serialization.KafkaDemoDeserialization;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.PropertyAccessor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class KafkaEsDemo {
    private static final Logger log = LoggerFactory.getLogger(KafkaEsDemo.class);
    private static final String application = "kafka-demo";
    private static final ObjectMapper objectMapper ;

    static {
        objectMapper = JsonMapper.builder().build();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static void main(String[] args) {

        // 获取命令行参数
        ParameterTool tool = ParameterTool.fromArgs(args);
        JobParameters parameters = JobParameters.getInstance(tool);
        log.info("parameters: {}", parameters);

        // 运行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        env.getConfig().setAutoWatermarkInterval(3000);
        log.info("executionConfig: {}", env.getConfig());



        final CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        checkpointConfig.setCheckpointInterval(5 * 60 * 1000);
        checkpointConfig.setMinPauseBetweenCheckpoints(60 * 1000);
        checkpointConfig.setCheckpointTimeout(60 * 60 *1000);


        KafkaSource<KafkaDemo> source = KafkaSource.<KafkaDemo>builder()
                .setBootstrapServers(parameters.getKafkaBootstrapServers())
                .setTopics(parameters.getKafkaTopicInput())
                // discover new partitions per 10 seconds
                .setProperty("partition.discovery.interval.ms", "10000")
                .setProperty("commit.offsets.on.checkpoint","true")
                .setGroupId(application + "-" + "group-id-kafka-source")
                .setClientIdPrefix(application + "-" + "client-id-kafka-source")
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .setValueOnlyDeserializer(new KafkaDemoDeserialization(objectMapper))
                .build();

//        WatermarkStrategy<KafkaDemo> watermarkStrategy = WatermarkStrategy
//                .<KafkaDemo>forMonotonousTimestamps()
//                .withIdleness(Duration.ofMinutes(1))
//                .withTimestampAssigner((kongLogBeat, timestamp)
//                        -> kongLogBeat.getDateTime());


        DataStreamSource<KafkaDemo> dataStreamSource = env.fromSource(source,
                WatermarkStrategy.noWatermarks(),
                application + "-" + "data-stream-source");


            


    }
}