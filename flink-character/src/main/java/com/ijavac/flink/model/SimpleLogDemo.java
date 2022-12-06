package com.ijavac.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: shichao
 * @date: 2022-12-06 14:35
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleLogDemo {
     private String date;
     private String host;
     private Integer port;
}
