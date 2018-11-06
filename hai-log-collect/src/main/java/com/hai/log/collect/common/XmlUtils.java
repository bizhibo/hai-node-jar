package com.hai.log.collect.common;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @描述 : xml转换工具类
 * @创建者：liuss
 * @创建时间： 2017/11/27
 */
public class XmlUtils {

    private static final String VALUE_NAME = "#text";
    private static final String ATTRIBUTE_NAME = "@";

    public static Map<String, Object> xmlToMap(String xmlString) {
        try {
            if (StringUtils.isNotBlank(xmlString)) {
                Document document = DocumentHelper.parseText(xmlString);
                if (document != null) {
                    Element root = document.getRootElement();
                    return elementToMap(root);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            return null;
        }
    }

    public static String mapToXml(Map<String, Object> map, String rootName) {
        try {
            Document document = DocumentHelper.createDocument();
            Element rootElement = DocumentHelper.createElement(rootName);
            document.add(rootElement);
            mapToElement(map, rootElement);
            return document.asXML();
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            return null;
        }
    }

    private static Element mapToElement(Map<String, Object> map, Element body) {
        if (MapUtils.isNotEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.startsWith(ATTRIBUTE_NAME)) {    //属性
                    body.addAttribute(key.substring(ATTRIBUTE_NAME.length(), key.length()), value != null ? value.toString() : "");
                } else if (key.startsWith(VALUE_NAME)) { //有属性时的文本
                    body.setText(value != null ? value.toString() : "");
                } else {
                    if (value instanceof List) {
                        List list = (List) value;
                        Object obj;
                        for (int i = 0; i < list.size(); i++) {
                            obj = list.get(i);
                            //list里是map或String，不会存在list里直接是list的，
                            if (obj instanceof Map) {
                                Element subElement = body.addElement(key);
                                mapToElement((Map) list.get(i), subElement);
                            } else {
                                body.addElement(key).setText((String) list.get(i));
                            }
                        }
                    } else if (value instanceof Map) {
                        Element subElement = body.addElement(key);
                        mapToElement((Map) value, subElement);
                    } else {
                        body.addElement(key).setText(value != null ? value.toString() : "");
                    }
                }
            }
            return body;
        } else {
            return null;
        }
    }

    private static Map<String, Object> elementToMap(Element element) {
        if (element != null) {
            Map<String, Object> map = new HashMap<>();
            List<Element> list = element.elements();
            List<Attribute> listAttr0 = element.attributes(); // 当前节点的所有属性的list
            for (Attribute attr : listAttr0) {
                map.put(ATTRIBUTE_NAME + attr.getName(), attr.getValue());
            }
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    Element iter = list.get(i);
                    List mapList = new ArrayList();
                    if (iter.elements().size() > 0) {
                        Map<String, Object> m = elementToMap(iter);
                        if (map.get(iter.getName()) != null) {
                            Object obj = map.get(iter.getName());
                            if (!(obj instanceof List)) {
                                mapList = new ArrayList();
                                mapList.add(obj);
                                mapList.add(m);
                            }
                            if (obj instanceof List) {
                                mapList = (List) obj;
                                mapList.add(m);
                            }
                            map.put(iter.getName(), mapList);
                        } else {
                            map.put(iter.getName(), m);
                        }
                    } else {
                        List<Attribute> listAttr = iter.attributes();
                        Map<String, Object> attrMap = null;
                        boolean hasAttributes = false;
                        if (listAttr.size() > 0) {
                            hasAttributes = true;
                            attrMap = new HashMap<>();
                            for (Attribute attr : listAttr) {
                                attrMap.put(ATTRIBUTE_NAME + attr.getName(), attr.getValue());
                            }
                        }
                        if (map.get(iter.getName()) != null) {
                            Object obj = map.get(iter.getName());
                            if (!(obj instanceof List)) {
                                mapList = new ArrayList();
                                mapList.add(obj);
                                if (hasAttributes) {
                                    attrMap.put(VALUE_NAME, iter.getText());
                                    mapList.add(attrMap);
                                } else {
                                    mapList.add(iter.getText());
                                }
                            }
                            if (obj instanceof List) {
                                mapList = (List) obj;
                                if (hasAttributes) {
                                    attrMap.put(VALUE_NAME, iter.getText());
                                    mapList.add(attrMap);
                                } else {
                                    mapList.add(iter.getText());
                                }
                            }
                            map.put(iter.getName(), mapList);
                        } else {
                            if (hasAttributes) {
                                attrMap.put(VALUE_NAME, iter.getText());
                                map.put(iter.getName(), attrMap);
                            } else {
                                map.put(iter.getName(), iter.getText());
                            }
                        }
                    }
                }
            } else {
                if (listAttr0.size() > 0) {
                    map.put(VALUE_NAME, element.getText());
                } else {
                    map.put(element.getName(), element.getText());
                }
            }
            return map;
        } else {
            return null;
        }
    }

    public static boolean judgeXML(String str) {
        try {
            DocumentHelper.parseText(str);
            return true;
        } catch (DocumentException e) {
            return false;
        }
    }
}
