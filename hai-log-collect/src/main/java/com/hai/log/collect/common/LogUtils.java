package com.hai.log.collect.common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.spi.LocationAwareLogger.*;

/**
 * @author liuss
 * @title 日志工具，对slf4j的封装
 * @date 2015-01-05
 * <p>
 * 使用方法: Log.debug("message"); (自动根据调用者所在的类名动态生成 Logger 对象
 * (CONFIG_ALWAYS_USE_STATIC_LOGGER_OBJECT 为 true 的时候)) 或者
 * Log.LOG.debug("message"); (使用固定的静态 Logger 对象 LOG) 若静态引入(import static
 * xxx.Log.*;)，则可以这样调用： debug("message") 或者 LOG.debug("message")
 */
public class LogUtils {
    /**
     * 固定的静态 Logger 对象 LOG
     **/
    public static final Logger LOG = LoggerFactory.getLogger(LogUtils.class);
    /**
     * 在调用 debug() 等方法时候，是否使用固定的静态 Logger 对象 LOG
     **/
    public static final boolean CONFIG_ALWAYS_USE_STATIC_LOGGER_OBJECT = false;
    /**
     * 自动根据调用者所在的类名动态生成的 Logger 对象
     **/
    private static Map<String, Logger> loggers = new HashMap<>();
    /**
     * 本类的名字，用于在打印时候打印真正的调用者的类名而不是本类的名字
     **/
    private static String FQCN = LogUtils.class.getName();
    /**
     * 是否启用日志平台收集日志，默认是false不启用
     **/
    private static boolean enable = false;

    /**
     * 内部使用的 helper 方法, trace(), debug(), info(), warn(), error() 等方法调用此方法
     *
     * @param level
     * @param marker
     * @param message
     * @param params
     * @param e
     */
    private static void log(int level, Marker marker, String message,
                            Object[] params, Throwable e) {
        Logger logger;

        // 判断使用固定的静态 Logger 对象 LOG，还是使用动态生成的 Logger 对象
        if (CONFIG_ALWAYS_USE_STATIC_LOGGER_OBJECT) {
            logger = LOG;
        } else {
            // 获取调用者的类名
            String className = Thread.currentThread().getStackTrace()[3]
                    .getClassName(); // Or (deprecated):
            // sun.reflect.Reflection.getCallerClass(3).getName();
            if (loggers.containsKey(className)) {
                logger = loggers.get(className);
            } else {
                logger = LoggerFactory.getLogger(className);
                loggers.put(className, logger);
            }
        }

        // logger 对象必须是 LocationAwareLogger 的实例
        assert logger instanceof LocationAwareLogger : "SLF4J logger adaptor is not an instance of LocationAwareLogger, try to use static logger object instead";

        // 判断响应级别的日志是否已启用，若没有启用则直接返回
        switch (level) {
            case TRACE_INT: {
                if (!logger.isTraceEnabled(marker))
                    return;
            }
            case DEBUG_INT: {
                if (!logger.isDebugEnabled(marker))
                    return;
            }
            case INFO_INT: {
                if (!logger.isInfoEnabled(marker))
                    return;
            }
            case WARN_INT: {
                if (!logger.isWarnEnabled(marker))
                    return;
            }
            case ERROR_INT: {
                if (!logger.isErrorEnabled(marker))
                    return;
            }
        }

        // 根据 message 参数与 params 参数格式化将要打印的信息
        if (message != null && params != null && params.length != 0) {
            FormattingTuple ft = MessageFormatter.arrayFormat(message, params);
            message = ft.getMessage();
        }

        // 将 logger 对象转换成 LocationAwareLogger，再调用普遍的 log() 方法，注意要传入 FQCN
        // 参数，否则打印的类名将是本类的类名而不是真正的调用者的类名
        ((LocationAwareLogger) logger).log(marker, FQCN, level, message,
                params, e);
    }

    /**
     * Exception 转 String
     */
    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringBuilder message = new StringBuilder("Fatal error occurred:\n");
        try {
            e.printStackTrace(pw);
            message.append(sw.toString())
                    .append("====================End Exception Message====================");
        } finally {
            pw.close();
        }
        return message.toString();
    }

    /**
     * TRACE 级别日志
     *
     * @param message
     */
    public static void trace(String message) {
        log(TRACE_INT, null, message, null, null);
    }

    /**
     * TRACE 级别日志
     *
     * @param message
     * @param params
     */
    public static void trace(String message, Object... params) {
        log(TRACE_INT, null, message, params, null);
    }

    /**
     * TRACE 级别日志
     *
     * @param message
     * @param e
     */
    public static void trace(String message, Throwable e) {
        log(TRACE_INT, null, message, null, e);
    }

    /**
     * TRACE 级别日志
     *
     * @param marker
     * @param message
     */
    public static void trace(Marker marker, String message) {
        log(TRACE_INT, marker, message, null, null);
    }

    /**
     * TRACE 级别日志
     *
     * @param marker
     * @param message
     * @param params
     */
    public static void trace(Marker marker, String message, Object... params) {
        log(TRACE_INT, marker, message, params, null);
    }

    /**
     * TRACE 级别日志
     *
     * @param marker
     * @param message
     * @param e
     */
    public static void trace(Marker marker, String message, Throwable e) {
        log(TRACE_INT, marker, message, null, e);
    }

    /**
     * DEBUG 级别日志
     *
     * @param message
     */
    public static void debug(String message) {
        log(DEBUG_INT, null, message, null, null);
    }

    /**
     * DEBUG 级别日志
     *
     * @param message
     * @param params
     */
    public static void debug(String message, Object... params) {
        log(DEBUG_INT, null, message, params, null);
    }

    /**
     * DEBUG 级别日志
     *
     * @param message
     * @param e
     */
    public static void debug(String message, Throwable e) {
        log(DEBUG_INT, null, message, null, e);
    }

    /**
     * DEBUG 级别日志
     *
     * @param marker
     * @param message
     */
    public static void debug(Marker marker, String message) {
        log(DEBUG_INT, marker, message, null, null);
    }

    /**
     * DEBUG 级别日志
     *
     * @param marker
     * @param message
     * @param params
     */
    public static void debug(Marker marker, String message, Object... params) {
        log(DEBUG_INT, marker, message, params, null);
    }

    /**
     * DEBUG 级别日志
     *
     * @param marker
     * @param message
     * @param e
     */
    public static void debug(Marker marker, String message, Throwable e) {
        log(DEBUG_INT, marker, message, null, e);
    }

    /**
     * INFO 级别日志
     *
     * @param message
     */
    public static void info(String message) {
        log(INFO_INT, null, message, null, null);
    }

    /**
     * INFO 级别日志
     *
     * @param message
     * @param params
     */
    public static void info(String message, Object... params) {
        log(INFO_INT, null, message, params, null);
    }

    /**
     * INFO 级别日志
     *
     * @param message
     * @param e
     */
    public static void info(String message, Throwable e) {
        log(INFO_INT, null, message, null, e);
    }

    /**
     * INFO 级别日志
     *
     * @param marker
     * @param message
     */
    public static void info(Marker marker, String message) {
        log(INFO_INT, marker, message, null, null);
    }

    /**
     * INFO 级别日志
     *
     * @param marker
     * @param message
     * @param params
     */
    public static void info(Marker marker, String message, Object... params) {
        log(INFO_INT, marker, message, params, null);
    }

    /**
     * INFO 级别日志
     *
     * @param marker
     * @param message
     * @param e
     */
    public static void info(Marker marker, String message, Throwable e) {
        log(INFO_INT, marker, message, null, e);
    }

    /**
     * WARN 级别日志
     *
     * @param message
     */
    public static void warn(String message) {
        log(WARN_INT, null, message, null, null);
    }

    /**
     * WARN 级别日志
     *
     * @param message
     * @param params
     */
    public static void warn(String message, Object... params) {
        log(WARN_INT, null, message, params, null);
    }

    /**
     * WARN 级别日志
     *
     * @param message
     * @param e
     */
    public static void warn(String message, Throwable e) {
        log(WARN_INT, null, message, null, e);
    }

    /**
     * WARN 级别日志
     *
     * @param marker
     * @param message
     */
    public static void warn(Marker marker, String message) {
        log(WARN_INT, marker, message, null, null);
    }

    /**
     * WARN 级别日志
     *
     * @param marker
     * @param message
     * @param params
     */
    public static void warn(Marker marker, String message, Object... params) {
        log(WARN_INT, marker, message, params, null);
    }

    /**
     * WARN 级别日志
     *
     * @param marker
     * @param message
     * @param e
     */
    public static void warn(Marker marker, String message, Throwable e) {
        log(WARN_INT, marker, message, null, e);
    }

    /**
     * ERROR 级别日志
     *
     * @param message
     */
    public static void error(String message) {
        log(ERROR_INT, null, message, null, null);
    }

    /**
     * ERROR 级别日志
     *
     * @param message
     * @param params
     */
    public static void error(String message, Object... params) {
        log(ERROR_INT, null, message, params, null);
    }

    /**
     * ERROR 级别日志
     *
     * @param message
     * @param e
     */
    public static void error(String message, Throwable e) {
        log(ERROR_INT, null, message, null, e);
    }

    /**
     * ERROR 级别日志
     *
     * @param e
     */
    public static void error(Throwable e) {
        log(ERROR_INT, null, "", null, e);
    }

    /**
     * ERROR 级别日志
     *
     * @param marker
     * @param message
     */
    public static void error(Marker marker, String message) {
        log(ERROR_INT, marker, message, null, null);
    }

    /**
     * ERROR 级别日志
     *
     * @param marker
     * @param message
     * @param params
     */
    public static void error(Marker marker, String message, Object... params) {
        log(ERROR_INT, marker, message, params, null);
    }

    /**
     * ERROR 级别日志
     *
     * @param marker
     * @param message
     * @param e
     */
    public static void error(Marker marker, String message, Throwable e) {
        log(ERROR_INT, marker, message, null, e);
    }

}
