package com.ijavac.flink.demo1;


import cn.hutool.core.util.StrUtil;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

/**
 * @author: shichao
 * @date: 2022-12-02 10:26
 * @Description: 基于flink 实时处理快速入门
 */
public class BatchWordCountDemo {


    public static void main(String[] args) throws Exception {
        ExecutionEnvironment environment = ExecutionEnvironment.getExecutionEnvironment();

        // 获取数据源
        DataSource<String> stringDataSource = environment.readTextFile("C:\\Temp\\wc.data");
        // FlatMapFunction  泛型1入参类型 , 泛型2输出类型
        stringDataSource.flatMap(new FlatMapFunction<String, String>() {
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
                /** ========使用匿名内部类Tuple2指定泛型，否则会报错=== **/.map(new MapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(String value) throws Exception {
                        return Tuple2.of(value, 1);
                    }
                })
                // 分组
                .groupBy(0)
                // 聚合
                .sum(1).print();
    }
}
