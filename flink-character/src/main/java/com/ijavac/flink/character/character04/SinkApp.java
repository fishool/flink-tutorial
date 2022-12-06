package com.ijavac.flink.character.character04;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 *
 * @author: shichao
 * @date: 2022-12-06 14:51
 * @Description:
 */
public class SinkApp {
    // nc -lk 9527
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> dataStreamSource = env.socketTextStream("192.168.200.132", 9527);
        dataStreamSource.setParallelism(2);
        dataStreamSource.print();
        env.execute("sink app");
    }
}
