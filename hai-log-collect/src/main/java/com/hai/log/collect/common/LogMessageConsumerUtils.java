package com.hai.log.collect.common;

import ctd.net.rpc.Client;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @描述 :
 * @创建者：liuss
 * @创建时间： 2018/11/6
 */
public class LogMessageConsumerUtils {

    private static final String PREFIX_NAME = "bsoft.";
    private static final String ERRORID_NULL = "1";
    private static final String ERRORID_NOTNULL = "2";
    private static final String INDEX_STEP_PREFIX = "step-";
    private static final String INDEX_PROCEDURE_PREFIX = "procedure-";

    public static void recordToES(Map<String, Object> msg) {
        Result esResult = null;
        try {
            Map<String, Object> params = new HashMap<>();
            if (MapUtils.isNotEmpty(msg)) {
                String nowDateTime = DateUtils.formatDateTime(new Date());
                Date nowDate = DateUtils.parseDateTime(nowDateTime);
                msg.put("timeMillis", nowDate.getTime());
                if (msg.get("LogType") != null && msg.get("LogType").toString().equals("3")) {
                    params.put("doc", keyToBsoft(msg));
                    esResult = (Result) Client.rpcInvoke("log.esOperateService", "createErrorDoc", new Object[]{params});
                } else {
                    Map<String, Object> argMap = null;
                    Map<String, Object> returnMap = null;
                    List<Object> argList = null;
                    List<Object> returnList = null;
                    String args = msg.get("Args") != null ? msg.get("Args").toString() : "";
                    String result = msg.get("Return") != null ? msg.get("Return").toString() : "";
                    if (XmlUtils.judgeXML(args)) {
                        argMap = XmlUtils.xmlToMap(args);
                    } else if (JsonUtils.judgeJSONObject(args)) {
                        argMap = JsonUtils.fromJson(args, Map.class);
                    } else if (JsonUtils.judgeJSONArray(args)) {
                        argList = JsonUtils.fromJson(args, List.class);
                    }
                    if (XmlUtils.judgeXML(result)) {
                        returnMap = XmlUtils.xmlToMap(result);
                    } else if (JsonUtils.judgeJSONObject(result)) {
                        returnMap = JsonUtils.fromJson(result, Map.class);
                    } else if (JsonUtils.judgeJSONArray(result)) {
                        returnList = JsonUtils.fromJson(result, List.class);
                    }
                    if (MapUtils.isNotEmpty(argMap)) {
                        msg.put("ArgsObject", argMap);
                    }
                    if (MapUtils.isNotEmpty(returnMap)) {
                        msg.put("ReturnObject", returnMap);
                    }
                    if (CollectionUtils.isNotEmpty(argList)) {
                        msg.put("ArgsObject", argList);
                    }
                    if (CollectionUtils.isNotEmpty(returnList)) {
                        msg.put("ReturnObject", returnList);
                    }
                    if (msg.get("LogType").toString().equals("2")) {
                        params.put("indexName", INDEX_STEP_PREFIX + msg.get("InputName"));
                    }
                    if (msg.get("LogType").toString().equals("1")) {
                        params.put("indexName", INDEX_PROCEDURE_PREFIX + msg.get("InputName"));
                    }
                    if (msg.get("ErrId") != null && StringUtils.isNotBlank(msg.get("ErrId").toString())) {
                        msg.put("ErrorStats", ERRORID_NOTNULL);
                    } else {
                        msg.put("ErrorStats", ERRORID_NULL);
                    }
                    params.put("operate", msg.get("operate"));
                    if (msg.get("LogType") != null && msg.get("LogType").toString().equals("1")) {
                        params.put("docID", msg.get("ProcedeureID"));
                    } else if (msg.get("LogType") != null && msg.get("LogType").toString().equals("2")) {
                        params.put("docID", msg.get("ProcedeureID") + "-" + msg.get("StepID") + "-" + msg.get("Index"));
                    }
                    params.put("doc", keyToBsoft(msg));
                    esResult = (Result) Client.rpcInvoke("log.esOperateService", "createDoc", new Object[]{params});
                }
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
        LogUtils.info("收到消息! ------" + esResult.toJson() + "----> LogType:" + msg.get("LogType") + "  流程1 步骤2  错误3 " );
    }

    private static Map<String, Object> keyToBsoft(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (String key : map.keySet()) {
            Object object = map.get(key);
            if (object instanceof Map) {
                result.put(PREFIX_NAME + key, keyToBsoft((Map<String, Object>) object));
            } else if (object instanceof List) {
                result.put(PREFIX_NAME + key, keyToBsoft((List<Object>) object));
            } else {
                result.put(PREFIX_NAME + key, object.toString());
            }
        }
        return result;
    }

    private static List<Object> keyToBsoft(List<Object> list) {
        List<Object> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Map) {
                result.add(keyToBsoft((Map<String, Object>) object));
            } else if (object instanceof List) {
                result.add(keyToBsoft((List<Object>) object));
            } else {
                result.add(object.toString());
            }
        }
        return result;
    }


}
