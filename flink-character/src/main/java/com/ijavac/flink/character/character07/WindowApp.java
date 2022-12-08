package com.ijavac.flink.character.character07;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * @author shichao
 * 实时处理概述
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/concepts/time/#introduction
 * <p>
 * 窗口api
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/dev/datastream/operators/windows/#window-assigners
 * @description
 * @date 2022/12/5 17:24
 * @return
 */
public class WindowApp {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        socketWindow(env);
        env.execute("WindowApp");
    }

    private static void socketWindow(StreamExecutionEnvironment env) {
        DataStreamSource<String> dataStreamSource = env.socketTextStream("192.168.200.132", 9527);

        SingleOutputStreamOperator<Tuple2<String, Integer>> map
                = dataStreamSource.map(new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String value) throws Exception {
                return Tuple2.of(value, 1);
            }
        });

        // 指定数据流事件水印
        DataStream<Tuple2<String, Integer>> afterWater = map
                // 指定事件水印
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Tuple2<String, Integer>>forMonotonousTimestamps()
                                // 在event中获取时间戳 单位毫秒
                                .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis() ));

        afterWater.keyBy((KeySelector<Tuple2<String, Integer>, String>) value -> value.f0)
                // 指定事件水印
                // 使用事件事件滚动 10s
                .window(TumblingEventTimeWindows.of(Time.seconds(30)))
                // 允许最大延时  5s
                .allowedLateness(Time.seconds(30))
                .sum(1)
                .print();
    }

}
