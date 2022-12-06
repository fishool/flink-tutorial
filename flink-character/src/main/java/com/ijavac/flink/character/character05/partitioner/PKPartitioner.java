package com.ijavac.flink.character.character05.partitioner;

import org.apache.flink.api.common.functions.Partitioner;

/**
 * @author: shichao
 * @date: 2022-12-06 17:02
 * @Description:
 */
public class PKPartitioner implements Partitioner<String> {
    @Override
    public int partition(String key, int numPartitions) {
        if ("baidu.com".equals(key)) {
            return 0;
        } else if ("weibo.com".equals(key)) {
            return 1;
        } else {
            return 3;
        }
    }
}
