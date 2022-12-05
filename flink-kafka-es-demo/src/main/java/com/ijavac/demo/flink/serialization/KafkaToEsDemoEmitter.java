package com.ijavac.demo.flink.serialization;

import cn.hutool.core.date.DateUtil;
import com.ijavac.demo.flink.module.KafkaDemo;
import lombok.SneakyThrows;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.PropertyAccessor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.json.JsonMapper;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * @author ZuoGuangDe
 * @version 1.0
 * @date 2022/11/17 9:45
 */
public class KafkaToEsDemoEmitter implements ElasticsearchEmitter<KafkaDemo> {

    private final ObjectMapper objectMapper;

    private static final long serialVersionUID = 1L;

    private static final String indexPrefix = "kafka-to-es";


    public KafkaToEsDemoEmitter() {
        objectMapper = JsonMapper.builder().build();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SneakyThrows
    @Override
    public void emit(KafkaDemo kafkaDemo, SinkWriter.Context context, RequestIndexer requestIndexer) {
        final String index = indexPrefix + DateUtil
                .date(kafkaDemo.getDateTime()).toString("yyyy-MM-dd");
        UpdateRequest request = new UpdateRequest(index, String.valueOf(kafkaDemo.getTenantId()));
        String json = objectMapper.writeValueAsString(kafkaDemo);
        request.doc(json, XContentType.JSON);
        request.upsert(json, XContentType.JSON);
        request.docAsUpsert(true);
        requestIndexer.add(request);
    }
}
