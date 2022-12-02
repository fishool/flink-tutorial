package com.ijavac.demo.flink.module;


import lombok.*;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

/**
 * KONG租户日志格式，基于KONG原始日志格式及附加租户信息
 *
 * @author ZuoGuangDe
 * @version 1.0
 * @date 2022/11/15 18:25
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KafkaDemo {

    protected Long tenantId;
    protected String tenantName;

    protected String dpName;
    protected String dpAddress;

    protected Long dateTime;

}
