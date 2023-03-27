package com.lin.opush.csv;

import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvRowHandler;
import lombok.Data;

/**
 * Csv文件行数统计处理类
 */
@Data
public class CountCsvFileRowNumberHandler implements CsvRowHandler {
    /**
     * Csv文件数据行数
     */
    private long rowNumber;

    /**
     * 对于Csv文件每行的处理逻辑
     * @param row Csv文件行对象
     */
    @Override
    public void handle(CsvRow row) {
        rowNumber++;
    }

    /**
     * 获取Csv文件行数
     * @return 行数
     */
    public long getRowNumber() {
        return rowNumber;
    }
}
