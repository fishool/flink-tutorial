package com.ijavac.flink.character.character04;

import cn.hutool.core.lang.Dict;
import com.ijavac.flink.model.SimpleLogDemo;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;

/**
 * 转换算子
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/dev/datastream/operators/overview/
 * map 输入一个元素同时输出一个元素
 * FlatMap   输入一个元素同时产生零个、一个或多个元素
 * Filter   为每个元素执行一个布尔 function，并保留那些 function 输出值为 true 的元素
 * KeyBy  在逻辑上将流划分为不相交的分区。具有相同 key 的记录都分配到同一个分区
 * Reduce  在相同 key 的数据流上“滚动”执行 reduce。
 * Window 可以在已经分区的 KeyedStreams 上定义 Window。Window 根据某些特征（例如，最近 5 秒内到达的数据）对每个 key Stream 中的数据进行分组
 * WindowAll 可以在普通 DataStream 上定义 Window
 * Window Apply  将通用 function 应用于整个窗口
 * WindowReduce  对窗口应用 reduce function 并返回 reduce 后的值
 * Union 将两个或多个数据流联合来创建一个包含所有流中数据的新流。注意：如果一个数据流和自身进行联合，这个流中的每个数据将在合并后的流中出现两次
 * Window Join  根据指定的 key 和窗口 join 两个数据流
 * Interval Join 根据 key 相等并且满足指定的时间范围内（e1.timestamp + lowerBound <= e2.timestamp <= e1.timestamp + upperBound）的条件将分别属于两个 keyed stream 的元素 e1 和 e2 Join 在一起。
 * Window CoGroup  根据指定的 key 和窗口将两个数据流组合在一起。
 * Connect “连接” 两个数据流并保留各自的类型。connect 允许在两个流的处理逻辑之间共享状态。
 * CoMap, CoFlatMap 类似于在连接的数据流上进行 map 和 flatMap。
 * Cache 把算子的结果缓存起来。目前只支持批执行模式下运行的作业。算子的结果在算子第一次执行的时候会被缓存起来，之后的 作业中会复用该算子缓存的结果。如果算子的结果丢失了，它会被原来的算子重新计算并缓存
 *
 * @author shichao
 * @description
 * @date 2022/12/5 17:24
 * @return
 */
public class Transformation {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // map 输入一个元素同时输出一个元素
//        map(env);
        // 为每个元素执行一个布尔 function，并保留那些 function 输出值为 true 的元素
//        filter(env);
        // 输入一个元素同时产生零个、一个或多个元素
//        flatMap(env);
        // 在逻辑上将流划分为不相交的分区。具有相同 key 的记录都分配到同一个分区
//        keyBy(env);
        // 在相同 key 的数据流上“滚动”执行 reduce。
        reduce(env);
        env.execute();
    }

    /**
     * Reduce  在相同 key 的数据流上“滚动”执行 reduce。
     **/
    private static void reduce(StreamExecutionEnvironment env) {
        DataStreamSource<String> textFileSource =
                env.readTextFile("data/access.txt");

        SingleOutputStreamOperator<SimpleLogDemo> map = textFileSource.map(new MapFunction<String, SimpleLogDemo>() {
            @Override
            public SimpleLogDemo map(String value) throws Exception {
                SimpleLogDemo dict = SimpleLogDemo.builder().build();
                String[] split = value.split(",");
                dict.setDate(split[0]);
                dict.setHost(split[1]);
                dict.setPort(Integer.valueOf(split[2]));
                return dict;
            }
        });

        KeyedStream<SimpleLogDemo, String> keyedStream
                = map.keyBy(new KeySelector<SimpleLogDemo, String>() {
            @Override
            public String getKey(SimpleLogDemo value) throws Exception {
                return value.getHost();
            }
        });

        // 将数据进行分组聚合
        SingleOutputStreamOperator<SimpleLogDemo> reduce = keyedStream.reduce(new ReduceFunction<SimpleLogDemo>() {
            @Override
            public SimpleLogDemo reduce(SimpleLogDemo value1, SimpleLogDemo value2) throws Exception {
                value1.setPort(value1.getPort() + value2.getPort());
                return value1;
            }
        });
        reduce.print();
    }

    /**
     * KeyBy  在逻辑上将流划分为不相交的分区。具有相同 key 的记录都分配到同一个分区
     **/
    private static void keyBy(StreamExecutionEnvironment env) {
        DataStreamSource<String> textFileSource =
                env.readTextFile("data/access.txt");

        SingleOutputStreamOperator<SimpleLogDemo> map = textFileSource.map(new MapFunction<String, SimpleLogDemo>() {
            @Override
            public SimpleLogDemo map(String value) throws Exception {
                SimpleLogDemo dict = SimpleLogDemo.builder().build();
                String[] split = value.split(",");
                dict.setDate(split[0]);
                dict.setHost(split[1]);
                dict.setPort(Integer.valueOf(split[2]));
                return dict;
            }
        });

        KeyedStream<SimpleLogDemo, String> keyedStream = map.keyBy(new KeySelector<SimpleLogDemo, String>() {
            @Override
            public String getKey(SimpleLogDemo value) throws Exception {
                return value.getHost();
            }
        });

        // 将数据进行分组
//        keyedStream.print();
        keyedStream.sum("port").print();
    }

    /**
     * 输入一个元素同时产生零个、一个或多个元素
     * 将一行数据拆分为多个
     */
    private static void flatMap(StreamExecutionEnvironment env) {
        DataStreamSource<String> textFileSource =
                env.fromCollection(Arrays.asList("pk,jk,ck",
                        "holy,shit,flink"));
        SingleOutputStreamOperator<String> operator = textFileSource.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String value, Collector<String> out) throws Exception {
                String[] split = value.split(",");
                for (String s : split) {
                    out.collect(s);
                }
            }
        });

        operator.print();

    }

    /**
     * 为每个元素执行一个布尔 function，并保留那些 function 输出值为 true 的元素
     */
    private static void filter(StreamExecutionEnvironment env) {
        DataStreamSource<String> textFileSource =
                env.readTextFile("data/access.txt");

        SingleOutputStreamOperator<Dict> map = textFileSource.map(new MapFunction<String, Dict>() {
            @Override
            public Dict map(String value) throws Exception {
                Dict dict = Dict.create();
                String[] split = value.split(",");
                dict.set("date", split[0]);
                dict.set("host", split[1]);
                dict.set("port", Integer.valueOf(split[2]));
                return dict;
            }
        });

        SingleOutputStreamOperator<Dict> filter = map.filter(new FilterFunction<Dict>() {
            @Override
            public boolean filter(Dict value) throws Exception {
                Integer port = value.getInt("port");
                return port >= 3000;
            }
        });
        filter.print();
    }

    private static void map(StreamExecutionEnvironment env) {
        DataStreamSource<String> textFileSource =
                env.readTextFile("data/access.txt");

        SingleOutputStreamOperator<Dict> map = textFileSource.map(new MapFunction<String, Dict>() {
            @Override
            public Dict map(String value) throws Exception {
                Dict dict = Dict.create();
                String[] split = value.split(",");
                dict.set("date", split[0]);
                dict.set("host", split[1]);
                dict.set("port", split[2]);
                return dict;
            }
        });
        map.print();
    }


}
