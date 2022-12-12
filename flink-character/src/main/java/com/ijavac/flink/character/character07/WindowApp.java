package com.ijavac.flink.character.character07;

import cn.hutool.core.date.DateUtil;
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

import java.time.Duration;

/**
 * @author shichao
 * 实时处理概述
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/concepts/time/#introduction
 * <p>
 * 窗口api
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/dev/datastream/operators/windows/#window-assigners
 * 事件时间水印
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/dev/datastream/event-time/generating_watermarks/
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
                        WatermarkStrategy
                                // 乱序水印延时 允许5s延时
                                .<Tuple2<String, Integer>>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                // 在event中获取事件时间戳与窗口起始时间戳的差值 单位毫秒
                                .withTimestampAssigner((event, timestamp) -> {
                                    // 模拟从event中获取的事件时间戳
                                    long current = System.currentTimeMillis();
                                    // 已今天为窗口起始锚点
                                    long dateTime = DateUtil.beginOfDay(DateUtil.date()).getTime();
                                    return current - dateTime;
                                }));

        afterWater.keyBy((KeySelector<Tuple2<String, Integer>, String>) value -> value.f0)
                // 配合Watermark使用
                // 使用事件时间滚动窗口 5s
                // [0, 5000) [5000, 10000)
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                // 窗口允许最大延时  5s
                .allowedLateness(Time.seconds(5))
                .sum(1)
                .print();
    }

}
