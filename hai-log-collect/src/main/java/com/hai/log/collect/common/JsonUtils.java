package com.hai.log.collect.common;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

/**
 * @描述 : json工具类
 * @创建者：liuss
 * @创建时间： 2014-6-5上午10:26:48
 */
public class JsonUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * @param entity
     * @return
     * @描述 : 获取对象的json格式字符串默认不格式化
     * @创建者：liuss
     * @创建时间： 2014-6-5上午10:19:19
     */
    public static String toJson(Object entity) {
        return toJson(entity, false);
    }

    /**
     * @param entity
     * @param prettyPrint
     * @return
     * @throws RuntimeException
     * @描述 : 获取对象的json格式字符串可设置是否格式化
     * @创建者：liuss
     * @创建时间： 2014-6-5上午10:19:38
     */
    public static String toJson(Object entity, boolean prettyPrint)
            throws RuntimeException {
        try {
            if (entity != null) {
                StringWriter sw = new StringWriter();
                JsonGenerator jg = OBJECT_MAPPER.getFactory().createGenerator(sw);
                if (prettyPrint) {
                    jg.useDefaultPrettyPrinter();
                }
                OBJECT_MAPPER.writeValue(jg, entity);
                return sw.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            LogUtils.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param str
     * @param clazz
     * @return
     * @throws RuntimeException
     * @描述 : 获取json字符串的对象
     * @创建者：liuss
     * @创建时间： 2014-6-5上午10:21:32
     */
    public static <T> T fromJson(String str, Class<T> clazz)
            throws RuntimeException {
        try {
            if (StringUtils.isNotBlank(str)) {
                return OBJECT_MAPPER.readValue(str, clazz);
            } else {
                return null;
            }
        } catch (IOException e) {
            LogUtils.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param str
     * @param t
     * @return
     * @throws RuntimeException
     * @描述 : 获取json字符串的对象泛型
     * @创建者：liuss
     * @创建时间： 2014-6-5上午10:22:41
     */
    public static <T> T fromJson(String str, TypeReference<T> t)
            throws RuntimeException {
        try {
            if (StringUtils.isNotBlank(str)) {
                return OBJECT_MAPPER.readValue(str, t);
            } else {
                return null;
            }
        } catch (IOException e) {
            LogUtils.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param str
     * @param collectionClass
     * @param elementClasses
     * @return
     * @描述 : 获取json字符串的对象非泛型
     * @创建者：liuss
     * @创建时间： 2014-6-5上午10:22:48
     */
    public static <T> T fromJson(String str, Class<?> collectionClass,
                                 Class<?> elementClasses) throws RuntimeException {
        try {
            if (StringUtils.isNotBlank(str)) {
                JavaType type = OBJECT_MAPPER.getTypeFactory()
                        .constructParametricType(collectionClass, elementClasses);
                return OBJECT_MAPPER.readValue(str, type);
            } else {
                return null;
            }
        } catch (IOException e) {
            LogUtils.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断字符串是否是JSON（对象）字符串
     * @param str
     * @return
     * @创建者：liuss
     * @创建时间： 2014-6-5上午10:22:48
     */
    public static boolean judgeJSONObject(String str) {
        try {
            JSONObject.parseObject(str);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否是JSON（数组）字符串
     * @param str
     * @return
     * @创建者：liuss
     * @创建时间： 2014-6-5上午10:22:48
     */
    public static boolean judgeJSONArray(String str) {
        try {
            JSONObject.parseArray(str);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /*
      初始化ObjectMapper
     */
    static {
        /* 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性 */
        OBJECT_MAPPER
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false);
        /* 设置不使用默认日期类型格式 */
        OBJECT_MAPPER.configure(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        /* 设置转换日期类型格式 */
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
        /* 设置转换时忽略空值 */
        //  OBJECT_MAPPER.setSerializationInclusion(Inclusion.NON_NULL);
        /* 设置键值可为非双引号形式 */
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
                true);
        /* 设置解析器支持解析单引号 */
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        /* 设置解析器支持解析结束符 */
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
                true);
        /* 设置可以带有转义字符 */
        OBJECT_MAPPER
                .configure(
                        JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
                        true);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

}
