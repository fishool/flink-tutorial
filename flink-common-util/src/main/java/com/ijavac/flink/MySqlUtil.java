package com.ijavac.flink;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * @author: shichao
 * @date: 2022-12-06 16:17
 * @Description:
 */
public class MySqlUtil {
    @SneakyThrows
    public static Connection getConnection() {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection root = DriverManager
                .getConnection("jdbc:mysql://127.0.0.1:13306/flink-demo?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowLoadLocalInfile=true",
                "root", "851158220");
        return root;
    };

    @SneakyThrows
    public static void close(Connection connection, PreparedStatement pstm) {
        connection.close();
        pstm.close();
    }
}
