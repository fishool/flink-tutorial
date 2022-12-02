package com.ijavac.demo.flink.serialization;


import com.ijavac.demo.flink.module.KafkaDemo;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.PropertyAccessor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * @author ZuoGuangDe
 * @version 1.0
 * @date 2022/4/7 15:30
 */
public class KafkaDemoDeserialization extends org.apache.flink.api.common.serialization.AbstractDeserializationSchema<KafkaDemo> {
    private final ObjectMapper objectMapper;

    public KafkaDemoDeserialization() {
        this.objectMapper = JsonMapper.builder().build();
        this.objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public KafkaDemoDeserialization(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public KafkaDemo deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, KafkaDemo.class);
    }
}
