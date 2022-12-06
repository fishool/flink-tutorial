package com.ijavac.flink.character.character05.function;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.ijavac.flink.model.SimpleLogDemo;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * @author: shichao
 * @date: 2022-12-06 15:50
 * @Description:
 */
public class AccessSource implements SourceFunction<SimpleLogDemo> {
    boolean running = true;
    @Override
    public void run(SourceContext<SimpleLogDemo> ctx) throws Exception {
        while (running) {
            for (int i = 0; i < 10; i++) {
                SimpleLogDemo simpleLogDemo = new SimpleLogDemo();
                simpleLogDemo.setDate(DateUtil.now());
                simpleLogDemo.setHost("weibo.com"+i);
                simpleLogDemo.setPort(RandomUtil.randomInt(1,65535));
                ctx.collect(simpleLogDemo);
            };
            ThreadUtil.sleep(10000);
        }
    }


    @Override
    public void cancel() {
        this.running = false;
    }
}
