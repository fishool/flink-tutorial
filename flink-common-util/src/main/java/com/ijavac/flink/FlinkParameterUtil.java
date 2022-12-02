package com.ijavac.flink;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.util.StringUtils;

/**
 * @author: shichao
 * @date: 2022-12-02 10:31
 * @Description:
 */
public class FlinkParameterUtil {
    /**
     * 获取命令行参数
     *
     * @param args          入参
     * @param parameterName 参数名   --kafka_topic_input kong-log-beat
     * @return java.lang.String
     * @description
     * @author shichao
     * @date 2022/12/2 10:33
     */
    public static String getParameterByArgs(String[] args,
                                            String parameterName) {
        // 获取命令行参数
        ParameterTool tool = ParameterTool.fromArgs(args);
        String parameterValue = tool.get(parameterName);

        if (StringUtils.isNullOrWhitespaceOnly(parameterValue)) {
            throw new IllegalArgumentException("command-line argument " + parameterName + " is not set!");
        }

        return parameterValue;
    }
}
