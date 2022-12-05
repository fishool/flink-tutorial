package com.ijavac.demo.flink.module;

import lombok.*;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.util.StringUtils;
import org.apache.http.HttpHost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZuoGuangDe
 * @version 1.0
 * @date 2022/11/15 19:33
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobParameters implements Serializable {


    private String kafkaBootstrapServers;
    private String kafkaTopicInput;

    private List<HttpHost> elasticsearchHosts;


    private static final String getAndValidate(ParameterTool tool, String parameterName) {
        String parameterValue = tool.get(parameterName);
        if (StringUtils.isNullOrWhitespaceOnly(parameterValue)) {
            throw new IllegalArgumentException("command-line argument " + parameterName + " is not set!");
        }
        return parameterValue;
    }

    public static final JobParameters getInstance(ParameterTool tool) {

        // 命令行参数，Kafka服务端点，格式形如：ip:port[,ip:port]*
        final String kafkaBootstrapServers = getAndValidate(tool, "kafka_bootstrap_servers");

        // 命令行参数，kafka输入主题TOPIC
        final String kafkaTopicInput = getAndValidate(tool, "kafka_topic_input");

        // 命令行参数，ElasticSearch的Hosts
        final String elasticsearchHosts = getAndValidate(tool, "elasticsearch_hosts");
        List<HttpHost> list = new ArrayList<HttpHost>();
        for (String elasticsearchHost : elasticsearchHosts.split(",")) {
            HttpHost host = HttpHost.create(elasticsearchHost);
            list.add(host);
        }

        return JobParameters.builder()
                .kafkaBootstrapServers(kafkaBootstrapServers)
                .kafkaTopicInput(kafkaTopicInput)
                .elasticsearchHosts(list)
                .build();
    }
}
