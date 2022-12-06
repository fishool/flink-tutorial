package com.ijavac.flink.character.character05;

import com.ijavac.flink.character.character05.function.AccessParallelSource;
import com.ijavac.flink.character.character05.function.AccessSource;
import com.ijavac.flink.character.character05.function.PKMapFunction;
import com.ijavac.flink.model.SimpleLogDemo;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author: shichao
 * @date: 2022-12-06 15:29
 * @Description:
 */
public class TransformationApp {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 自定义mapFunction
//        richMap(env)
        // 自定义SourceFunction (单并行度)
//        sourceFunction(env);
        parallelSourceFunction(env);
        env.execute("TransformationApp");
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
