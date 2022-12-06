package com.ijavac.flink.character.character05.function;

import com.ijavac.flink.model.SimpleLogDemo;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;

/**
 * @author: shichao
 * @date: 2022-12-06 15:32
 * @Description:
 */
public class PKMapFunction extends RichMapFunction<String, SimpleLogDemo> {

    /** 初始化操作 ==创建连接  每个并行度task会单独进行连接资源初始化 **/
    @Override
    public void open(Configuration parameters) throws Exception {
        System.out.println("===execute open===");
        super.open(parameters);
    }
    /** 清理操作 关闭连接**/
    @Override
    public void close() throws Exception {
        System.out.println("===execute close===");
        super.close();
    }

    @Override
    public RuntimeContext getRuntimeContext() {
        System.out.println("===execute getRuntimeContext===");
        return super.getRuntimeContext();
    }

    @Override
    public SimpleLogDemo map(String value) throws Exception {
        System.out.println("execute map");
        SimpleLogDemo dict = SimpleLogDemo.builder().build();
        String[] split = value.split(",");
        dict.setDate(split[0]);
        dict.setHost(split[1]);
        dict.setPort(Integer.valueOf(split[2]));
        return dict;
    }
}
