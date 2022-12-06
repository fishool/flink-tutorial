package com.ijavac.flink.character.character05.function;

import com.ijavac.flink.MySqlUtil;
import com.ijavac.flink.model.SimpleLogDemo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author: shichao
 * @date: 2022-12-06 15:50
 * @Description:
 */
public class MysqlSimpleLogDemoSource extends RichSourceFunction<SimpleLogDemo> {
    boolean running = true;

    Connection connection;
    PreparedStatement preparedStatement;
    @Override
    public void open(Configuration parameters) throws Exception {
        System.out.println("===execute open===");
        connection = MySqlUtil.getConnection();
        preparedStatement = connection.prepareStatement("select * from access_log");
    }

    @Override
    public void close() {
        MySqlUtil.close(connection, preparedStatement);
    }


    @Override
    public void run(SourceContext<SimpleLogDemo> ctx) throws Exception {
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            SimpleLogDemo simpleLogDemo = new SimpleLogDemo();
            simpleLogDemo.setPort(resultSet.getInt("port"));
            simpleLogDemo.setDate(resultSet.getString("date"));
            simpleLogDemo.setHost(resultSet.getString("host"));
            ctx.collect(simpleLogDemo);
        }
    }


    @Override
    public void cancel() {
        this.running = false;
    }
}
