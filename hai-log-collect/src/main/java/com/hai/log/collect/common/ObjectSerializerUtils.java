package com.hai.log.collect.common;

import java.io.*;

/**
 * @描述 :
 * @创建者：liuss
 * @创建时间： 2018/10/16
 */
public class ObjectSerializerUtils {
    /**
     * 对象转数组
     *
     * @param obj
     * @return
     */
    public static byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } catch (IOException e) {
            LogUtils.error(e.getMessage(), e);
        } finally {
            CloseUtils.close(oos);
            CloseUtils.close(bos);
        }
        return bytes;
    }

    /**
     * 数组转对象
     *
     * @param bytes
     * @return
     */
    public static Object toObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LogUtils.error(e.getMessage(), e);
        } finally {
            CloseUtils.close(ois);
            CloseUtils.close(bis);
        }
        return obj;
    }
}
