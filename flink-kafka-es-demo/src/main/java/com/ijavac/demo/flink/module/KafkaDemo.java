package com.ijavac.demo.flink.module;


import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 演示数据
 *
 * @author shichao
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
    // https://toutiao.io/posts/x55lkqo/preview
    protected JSONObject errorData;

}
