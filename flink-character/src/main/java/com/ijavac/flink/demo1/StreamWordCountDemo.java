package com.ijavac.flink.demo1;


import cn.hutool.core.util.StrUtil;
import com.ijavac.flink.FlinkParameterUtil;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author: shichao
 * @date: 2022-12-02 10:26
 * @Description: 基于flink 实时处理快速入门
 */
public class StreamWordCountDemo {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment environment
                = StreamExecutionEnvironment.getExecutionEnvironment();

        // 获取数据源
        // --port 9527 --hostname 192.168.200.132
        // 目标主机使用nc建立连接   nc -lk 9527
        DataStreamSource<String> stringDataStreamSource = environment
                .socketTextStream(FlinkParameterUtil.getParameterByArgs(args, "hostname"),
                        Integer.parseInt(FlinkParameterUtil.getParameterByArgs(args, "port")));

        // FlatMapFunction  泛型1入参类型 , 泛型2输出类型
        stringDataStreamSource
                .flatMap(new FlatMapFunction<String, String>() {
                    @Override
                    public void flatMap(String value, Collector<String> out) throws Exception {
                        String[] split = value.split(" ");
                        // 分割遍历
                        for (String s : split) {
                            out.collect(s.toLowerCase().trim());
                        }
                    }
                })
                // 过滤无效值
                .filter(StrUtil::isNotBlank)
                // 转换成元组
                /** ========使用匿名内部类Tuple2指定泛型，否则会报错=== **/
                .map(new MapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(String value) throws Exception {
                        return Tuple2.of(value, 1);
                    }
                })
                // 分组
                .keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
                    @Override
                    public String getKey(Tuple2<String, Integer> value) throws Exception {
                        return value.f0;
                    }
                })
                // 聚合
                .sum(1)
                // sink
                .print();

        // 启动
        environment.execute();
    }
}
