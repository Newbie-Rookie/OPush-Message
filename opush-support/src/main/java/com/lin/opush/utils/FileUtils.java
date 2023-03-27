package com.lin.opush.utils;

import cn.hutool.core.io.IoUtil;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * 文件工具类
 */
@Slf4j
public class FileUtils {
    /**
     * 读取远程链接返回File对象
     * @param path      文件路径
     * @param remoteUrl cdn/oss文件访问链接(远程链接)
     * @return File对象
     */
    public static File getRemoteUrlToFile(String path, String remoteUrl) {
        try {
            URL url = new URL(remoteUrl);
            File file = new File(path, url.getPath());
            if (!file.exists()) {
                // 创建文件夹
                file.getParentFile().mkdirs();
                // 拷贝文件
                IoUtil.copy(url.openStream(), new FileOutputStream(file));
            }
            return file;
        } catch (Exception e) {
            log.error("FileUtils#getRemoteUrlToFile fail:{},remoteUrl:{}", Throwables.getStackTraceAsString(e), remoteUrl);
        }
        return null;
    }
}
