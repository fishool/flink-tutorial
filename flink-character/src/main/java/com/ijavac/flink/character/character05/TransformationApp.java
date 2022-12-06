package com.ijavac.flink.character.character05;

import com.ijavac.flink.character.character05.function.AccessParallelSource;
import com.ijavac.flink.character.character05.function.AccessSource;
import com.ijavac.flink.character.character05.function.MysqlSimpleLogDemoSource;
import com.ijavac.flink.character.character05.function.PKMapFunction;
import com.ijavac.flink.character.character05.partitioner.PKPartitioner;
import com.ijavac.flink.model.SimpleLogDemo;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;

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
 * <p>
 * 旁路输出
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/dev/datastream/side_output/
 *
 * @author shichao
 * @description
 * @date 2022/12/5 17:24
 * @return
 */
public class TransformationApp {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 自定义mapFunction
//        richMap(env)
        // 自定义SourceFunction (单并行度)
//        sourceFunction(env);
//        parallelSourceFunction(env);
        // 自定义mysql Source
//        mysqlSimpleLogSource(env);
        // union 合并stream (两个stream 数据类型必须一致)
//        union(env);
        // 使用“连接” 两个数据流并保留各自的数据类型。connect 允许在两个流的处理逻辑之间共享状态。
//         connect(env);
        // partitionCustom 自定义工作分区
        partitionCustom(env);
        env.execute("TransformationApp");
    }

    private static void partitionCustom(StreamExecutionEnvironment env) {
        DataStreamSource<SimpleLogDemo> source1 = env.addSource(new AccessSource());
        SingleOutputStreamOperator<Tuple2<String, SimpleLogDemo>> map = source1.map(new MapFunction<SimpleLogDemo, Tuple2<String, SimpleLogDemo>>() {
            @Override
            public Tuple2<String, SimpleLogDemo> map(SimpleLogDemo value) throws Exception {
                return Tuple2.of(value.getHost(), value);
            }
        });
        map.partitionCustom(new PKPartitioner(), (KeySelector<Tuple2<String, SimpleLogDemo>, String>) value -> value.f0)
                .map((MapFunction<Tuple2<String, SimpleLogDemo>, String>) value -> value.f0 + " : " + value.f1).print();
    }

    private static void connect(StreamExecutionEnvironment env) {
        DataStreamSource<SimpleLogDemo> source1 = env.addSource(new AccessSource());
        DataStreamSource<SimpleLogDemo> source2 = env.addSource(new AccessSource());
        SingleOutputStreamOperator<Tuple2<String, SimpleLogDemo>> otherType = source2.map(new MapFunction<SimpleLogDemo, Tuple2<String, SimpleLogDemo>>() {
            @Override
            public Tuple2<String, SimpleLogDemo> map(SimpleLogDemo value) throws Exception {
                return Tuple2.of("otherType", value);
            }
        });

        ConnectedStreams<SimpleLogDemo, Tuple2<String, SimpleLogDemo>> connect = source1.connect(otherType);
        // CoMapFunction 两个流的数据类型可以不一致, 但是返回的数据类型必须一致
        SingleOutputStreamOperator<String> map = connect.map(
                new CoMapFunction<SimpleLogDemo, Tuple2<String, SimpleLogDemo>, String>() {
                    @Override
                    public String map1(SimpleLogDemo value) throws Exception {
                        return value.toString();
                    }

                    @Override
                    public String map2(Tuple2<String, SimpleLogDemo> value) throws Exception {
                        return value.f0 + value.toString();
                    }
                });
        map.print();
    }

    private static void union(StreamExecutionEnvironment env) {
        DataStreamSource<String> dataStreamSource9527 = env.socketTextStream("192.168.200.132", 9527);
        DataStreamSource<String> dataStreamSource9628 = env.socketTextStream("192.168.200.132", 9528);

        DataStream<String> union = dataStreamSource9527.union(dataStreamSource9628);
        union.print();
    }

    private static void mysqlSimpleLogSource(StreamExecutionEnvironment env) {
        DataStreamSource<SimpleLogDemo> simpleLogDemoDataStreamSource = env.addSource(new MysqlSimpleLogDemoSource());
        simpleLogDemoDataStreamSource.print();
    }

    private static void parallelSourceFunction(StreamExecutionEnvironment env) {
        DataStreamSource<SimpleLogDemo> simpleLogDemoDataStreamSource =
                env.addSource(new AccessParallelSource())
                        .setParallelism(2);
        simpleLogDemoDataStreamSource.print();
    }

    private static void sourceFunction(StreamExecutionEnvironment env) {
        DataStreamSource<SimpleLogDemo> simpleLogDemoDataStreamSource =
                env.addSource(new AccessSource());
        simpleLogDemoDataStreamSource.print();
    }


    private static void richMap(StreamExecutionEnvironment env) {
        DataStreamSource<String> textFileSource =
                env.readTextFile("data/access.txt");
        env.setParallelism(2);
        // 设置自定义RichMapFunction
        SingleOutputStreamOperator<SimpleLogDemo> map
                = textFileSource.map(new PKMapFunction());

        map.print();


    }
}
