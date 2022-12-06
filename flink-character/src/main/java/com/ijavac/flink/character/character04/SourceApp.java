package com.ijavac.flink.character.character04;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.NumberSequenceIterator;

/**
 * source 概览
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/dev/datastream/overview/#data-sources
 *
 * flink自带source 实现
 * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/connectors/datastream/overview/
 * @author: shichao
 * @date: 2022-12-05 15:41
 * @Description:
 */
public class SourceApp {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // socket 并行度
//        socketParallelTest(env);
        // 并行集合
        parallelCollectionTest(env);
        env.execute();
    }

    /**
     * 并行集合测试
     * @description
     * org.apache.flink.util.SplittableIterator
     * @author shichao 
     * @date 2022/12/5 15:53
     * @param env
     * @return void
     */
    private static void parallelCollectionTest(StreamExecutionEnvironment env) {
        DataStreamSource<Long> longDataStreamSource = env.fromParallelCollection(new NumberSequenceIterator(1, 20), Long.class);
        System.out.println("longDataStreamSource parallelism ====="+ longDataStreamSource.getParallelism());


        longDataStreamSource
                .filter(new FilterFunction<Long>() {
                    @Override
                    public boolean filter(Long value) throws Exception {
                        return value % 2 == 0;
                    }
                })
                .print();
        System.out.println("filter parallelism ====="+ longDataStreamSource.getParallelism());
    }


    /**
     * socket 并行度
     * @param env
     * @return void
     * @description
     * https://nightlies.apache.org/flink/flink-docs-release-1.16/zh/docs/dev/datastream/overview/#data-sources
     * Flink 自带了许多预先实现的 source functions，
     * 不过你仍然可以通过实现 SourceFunction 接口编写自定义的非并行 source，
     * 也可以通过实现 ParallelSourceFunction 接口或者继承 RichParallelSourceFunction 类编写自定义的并行 sources。
     * @author shichao
     * @date 2022/12/5 15:43
     */
    private static void socketParallelTest(StreamExecutionEnvironment env) {
        // SourceFunction并行度为1
        DataStreamSource<String> stringDataStreamSource = env
                .socketTextStream("192.168.200.132", 9527);
        System.out.println("source parallelism ====="+ stringDataStreamSource.getParallelism());

        // filter source 并行度默认为机器内核
        SingleOutputStreamOperator<String> afterFilterStream = stringDataStreamSource
                .filter(new FilterFunction<String>() {
                    @Override
                    public boolean filter(String value) throws Exception {
                        return value.startsWith("hello");
                    }
                });

        System.out.println("filter parallelism ====="+ afterFilterStream.getParallelism());
        afterFilterStream.print();
    }


}
