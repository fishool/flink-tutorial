package com.ijavac.demo.flink;

import cn.hutool.core.date.DateUtil;
import com.ijavac.demo.flink.module.JobParameters;
import com.ijavac.demo.flink.module.KafkaDemo;
import com.ijavac.demo.flink.serialization.KafkaDemoDeserialization;
import com.ijavac.demo.flink.serialization.KafkaToEsDemoEmitter;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch7SinkBuilder;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.elasticsearch.sink.FlushBackoffType;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.PropertyAccessor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * flink 自带的source和sink
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/connectors/datastream/overview/
 * https://www.6aiq.com/article/1649203757609?p=1&m=0
 * @author shichao
 * @description
 * @date 2022/12/5 16:01
 * @return
 */
public class KafkaEsDemo {
    private static final Logger log = LoggerFactory.getLogger(KafkaEsDemo.class);
    private static final String application = "kafka-demo";
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = JsonMapper.builder().build();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void main(String[] args) throws Exception {

        // 获取命令行参数
        /**
         --kafka_bootstrap_servers 192.168.200.132:9092 --kafka_topic_input flink_to_es --elasticsearch_hosts http://192.168.200.132:9200
         */
        ParameterTool tool = ParameterTool.fromArgs(args);
        JobParameters parameters = JobParameters.getInstance(tool);
        log.info("parameters: {}", parameters);

        // 运行环境配置
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 配置重启策略
        // 如果程序没有启用 Checkpoint，则采用不重启策略，
        // 如果开启了 Checkpoint 且没有设置重启策略，那么采用固定延时重启策略，最大重启次数为 Integer.MAX_VALUE。
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(5, TimeUnit.SECONDS)));
        // env.setRestartStrategy(RestartStrategies.noRestart());

        env.setParallelism(2);
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        env.getConfig().setAutoWatermarkInterval(3000);
        log.info("executionConfig: {}", env.getConfig());


        final CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        checkpointConfig.setCheckpointInterval(5 * 60 * 1000);
        checkpointConfig.setMinPauseBetweenCheckpoints(60 * 1000);
        checkpointConfig.setCheckpointTimeout(60 * 60 * 1000);


        KafkaSource<KafkaDemo> source = KafkaSource.<KafkaDemo>builder()
                .setBootstrapServers(parameters.getKafkaBootstrapServers())
                .setTopics(parameters.getKafkaTopicInput())
                // discover new partitions per 10 seconds
                .setProperty("partition.discovery.interval.ms", "10000")
                .setProperty("commit.offsets.on.checkpoint", "true")
                .setGroupId(application + "-" + "group-id-kafka-source")
                .setClientIdPrefix(application + "-" + "client-id-kafka-source")
                // 默认从已提交的偏移地址开始消费
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                // 设置反序列化器
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

        SingleOutputStreamOperator<KafkaDemo> afterDealStream = dataStreamSource.map(new MapFunction<KafkaDemo, KafkaDemo>() {
            @Override
            public KafkaDemo map(KafkaDemo kafkaDemo) throws Exception {
                log.info("kafkaDemo: {}", kafkaDemo);
                // 模拟业务处理
                kafkaDemo.setTenantName("tenantName" + DateUtil.now());
                return kafkaDemo;
            }
        });

        // es sink
        ElasticsearchSink<KafkaDemo> sink = new Elasticsearch7SinkBuilder<KafkaDemo>()
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .setHosts(parameters.getElasticsearchHosts().toArray(
                        new HttpHost[parameters.getElasticsearchHosts().size()]))
                .setConnectionUsername("elastic")
                .setConnectionPassword("elastic")
                // 这里启用了一个指数退避重试策略，初始延迟为 1000 毫秒且最大重试次数为 5
                .setBulkFlushBackoffStrategy(FlushBackoffType.EXPONENTIAL, 5, 1000)
                // 刷新的时间间隔5000 毫秒
                .setBulkFlushInterval(5000)
                .setEmitter(new KafkaToEsDemoEmitter()).build();
        afterDealStream.sinkTo(sink);

        JobExecutionResult result = env.execute();
    }
}