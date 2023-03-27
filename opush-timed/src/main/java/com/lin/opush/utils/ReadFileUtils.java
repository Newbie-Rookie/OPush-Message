package com.lin.opush.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.csv.*;
import com.google.common.base.Throwables;
import com.lin.opush.csv.CountCsvFileRowNumberHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.util.*;

/**
 * 文件读取工具类
 */
@Slf4j
public class ReadFileUtils {
    /**
     * csv文件存储接收者的列名
     */
    public static final String RECEIVER_KEY = "userId";

    /**
     * 读取csv文件
     * 每读取一行就调用CsvRowHandler【实现该接口并重写handler方法】的handler方法【行处理逻辑】并传入行对象【CsvRow】
     * @param path 文件路径【已拷贝到服务器特定路径下】
     * @param csvRowHandler Csv文件行处理类
     */
    public static void getCsvRow(String path, CsvRowHandler csvRowHandler) {
        try {
            // 把首行当做是标题，获取reader
            CsvReader reader = CsvUtil.getReader(new FileReader(path),
                                new CsvReadConfig().setContainsHeader(true));
            reader.read(csvRowHandler);
        } catch (Exception e) {
            log.error("ReadFileUtils#getCsvRow fail!{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 读取csv文件，统计csv文件行数
     * @param path 文件路径【已拷贝到服务器特定路径下】
     * @param countCsvFileRowNumberHandler Csv文件行数统计处理类
     * @return 行数
     */
    public static long countCsvFileRowNumber(String path,
                                             CountCsvFileRowNumberHandler countCsvFileRowNumberHandler) {
        try {
            // 使用CsvUtil获取CSVReader【读取csv文件，设置首行为标题行】
            CsvReader reader = CsvUtil.getReader(new FileReader(path),
                                new CsvReadConfig().setContainsHeader(true));
            // 使用Csv文件行数统计处理类统计数据行数
            reader.read(countCsvFileRowNumberHandler);
        } catch (Exception e) {
            log.error("ReadFileUtils#countCsvFileRowNumber fail!{}", Throwables.getStackTraceAsString(e));
        }
        return countCsvFileRowNumberHandler.getRowNumber();
    }

    /**
     * 将Csv文件每行数据对应的Map【key-标题，value-标题对应值】转换为消息内容中的可变部分【params】
     * @param fieldMap Csv文件每行数据对应的Map【key-标题，value-标题对应值】
     * @return 消息内容中的可变部分【params】
     */
    public static Map<String, String> getParamsFromCsvRow(Map<String, String> fieldMap) {
        Map<String, String> params = MapUtil.newHashMap();
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            // 不将接收者id放入消息内容中的可变部分
            if (!ReadFileUtils.RECEIVER_KEY.equals(entry.getKey())) {
                params.put(entry.getKey(), entry.getValue());
            }
        }
        return params;
    }
}
