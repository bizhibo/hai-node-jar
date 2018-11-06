package com.hai.log.collect.common;
import java.io.Closeable;

/**
 * @描述 : io关闭的工具类
 * @创建者：liuss
 * @创建时间： 2014-6-16上午11:56:25
 */
public class CloseUtils {

    public static void close(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (Exception e) {
                LogUtils.error("Unable to close " + closeable, e);
            }
    }
}
