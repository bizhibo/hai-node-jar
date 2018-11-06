package com.hai.log.collect.rpc;

import com.hai.log.collect.common.CloseUtils;
import com.hai.log.collect.common.JestClientFactoryUtils;
import com.hai.log.collect.common.LogUtils;
import hai.component.HaiServiceBase;
import hai.resource.DistributedResourcesManage;
import hai.spring.SpringContext;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @描述 :
 * @创建者：liuss
 * @创建时间： 2018/11/6
 */
public class ESRpcInitialization extends HaiServiceBase {

    @Override
    protected synchronized void initialization() {
        FileInputStream in = null;
        try {
            File file = DistributedResourcesManage.getFile("esConfig" + File.separator + "httpclient.properties", this);
            if (file != null) {
                Properties properties = new Properties();
                in = new FileInputStream(file);
                properties.load(in);
                List<String> addressList = new ArrayList<>();
                addressList.add(properties.get("haiLogCollect.service.address").toString());
                JestClientFactoryUtils.build(addressList);
            }
            SpringContext.setContext("config/rpc-config.xml");
            super.initialization();
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        } finally {
            CloseUtils.close(in);
        }
    }


}
