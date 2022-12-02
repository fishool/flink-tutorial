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
    private String postgresqlDriver;
    private String postgresqlUrl;
    private String postgresqlUser;
    private String postgresqlPassword;

    private String kafkaBootstrapServers;
    private String kafkaTopicInput;
    private String kafkaTopicOutput;

    private List<HttpHost> elasticsearchHosts;


    private static final String getAndValidate(ParameterTool tool, String parameterName) {
        String parameterValue = tool.get(parameterName);
        if (StringUtils.isNullOrWhitespaceOnly(parameterValue)) {
            throw new IllegalArgumentException("command-line argument " + parameterName + " is not set!");
        }
        return parameterValue;
    }

    public static final JobParameters getInstance(ParameterTool tool) {

        // 命令行参数，Postgresql的驱动类
        final String postgresqlDriver = getAndValidate(tool, "postgresql_driver");

        // 命令行参数，Postgresql的URL
        final String postgresqlUrl = getAndValidate(tool, "postgresql_url");

        // 命令行参数，Postgresql的用户名
        final String postgresqlUser = getAndValidate(tool, "postgresql_user");

        // 命令行参数，Postgresql的用户密码
        final String postgresqlPassword = getAndValidate(tool, "postgresql_password");

        // 命令行参数，Kafka服务端点，格式形如：ip:port[,ip:port]*
        final String kafkaBootstrapServers = getAndValidate(tool, "kafka_bootstrap_servers");

        // 命令行参数，kafka输入主题TOPIC
        final String kafkaTopicInput = getAndValidate(tool, "kafka_topic_input");

        // 命令行参数，kafka输出主题TOPIC
        final String kafkaTopicOutput = getAndValidate(tool, "kafka_topic_output");

        // 命令行参数，ElasticSearch的Hosts
        final String elasticsearchHosts = getAndValidate(tool,"elasticsearch_hosts");
        List<HttpHost> list = new ArrayList<HttpHost>();
        for (String elasticsearchHost: elasticsearchHosts.split(",")) {
            HttpHost host = HttpHost.create(elasticsearchHost);
            list.add(host);
        }

        return JobParameters.builder()
                .postgresqlDriver(postgresqlDriver)
                .postgresqlUrl(postgresqlUrl)
                .postgresqlUser(postgresqlUser)
                .postgresqlPassword(postgresqlPassword)
                .kafkaBootstrapServers(kafkaBootstrapServers)
                .kafkaTopicInput(kafkaTopicInput)
                .kafkaTopicOutput(kafkaTopicOutput)
                .elasticsearchHosts(list)
                .build();
    }
}
