package com.hai.log.collect.common;

import com.google.gson.GsonBuilder;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;

/**
 * @描述 :
 * @创建者：liuss
 * @创建时间： 2018/9/17
 */
public class JestClientFactoryUtils {

    private static JestClient jestClient = null;
    /**
     * 是否开启多线程
     **/
    private static boolean isMultiThreaded = true;
    /**
     * 连接ES超时时间
     **/
    private static int connTimeout = 3000;
    /**
     * 读取数据超时时间
     **/
    private static int readTimeout = 3000;
    /**
     * 所有路由连接总数
     **/
    private static Integer maxTotalConnection = 10;
    /**
     * 单个路由连接数
     **/
    private static Integer defaultMaxTotalConnectionPerRoute = 10;
    /**
     * ES用户名
     **/
    private static String esusername = "";
    /**
     * ES密码
     **/
    private static String espassword = "";
    /**
     * GSON时间格式，暂时不提供其他GSON参数设置
     **/
    private static String gsonDateFormat = "yyyy-MM-dd'T'HH:mm:ss";

    public static void build(List<String> esAddressList) {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(esAddressList)
                .defaultCredentials(esusername, espassword)
                .maxTotalConnection(maxTotalConnection)
                .defaultMaxTotalConnectionPerRoute(defaultMaxTotalConnectionPerRoute)
                .multiThreaded(isMultiThreaded)
                .connTimeout(connTimeout)
                .readTimeout(readTimeout)
                .gson(new GsonBuilder().setDateFormat(gsonDateFormat).create())
                .build());
        jestClient = factory.getObject();
    }

    public static JestClient getInstance() {
        return jestClient;
    }
}
