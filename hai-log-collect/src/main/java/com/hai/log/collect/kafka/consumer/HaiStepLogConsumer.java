package com.hai.log.collect.kafka.consumer;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hai.log.collect.common.LogMessageConsumerUtils;
import com.hai.log.collect.common.LogUtils;
import com.hai.log.collect.common.ObjectSerializerUtils;
import hai.component.HaiServiceBase;
import hai.dao.HaiIbatisDaoHelper;
import hai.mq.IMQConsumer;
import hai.mq.MQDao;
import hai.mq.MQDaoManage;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author shenwb
 * @date 2018年8月17日
 * @describe 步骤日志消费者
 */
public class HaiStepLogConsumer extends HaiServiceBase implements IMQConsumer {

    protected MQDao mqDao;

    private HaiIbatisDaoHelper daoHelper;

    private final String VARIABLEKEY = "SetpLog";

    @Override
    protected synchronized void initialization() throws Exception {
        mqDao = MQDaoManage.getMqDao("HaiMQLogAccess");
        synchronized (mqDao) {
            mqDao.registerConsumer("HaiMQLogAccess", VARIABLEKEY, this);
        }
        daoHelper = new HaiIbatisDaoHelper("MonitorDBAccess");
        daoHelper.addSqlMap("config/MonitorSqlMap.xml");
        super.initialization();
    }

    @Override
    public void destroy() throws Exception {
        if (mqDao != null) {
            synchronized (mqDao) {
                mqDao.close();
            }
        }
        super.destroy();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean messageArrive(String topic, Map args, Object msg)
            throws Exception {
        try {
            List<ConsumerRecord<byte[], byte[]>> records = (List<ConsumerRecord<byte[], byte[]>>) msg;
            List<Map<String, Object>> updateRecord = new ArrayList<>();
            for (ConsumerRecord<byte[], byte[]> re : records) {
                Map<String, Object> record = (Map<String, Object>) ObjectSerializerUtils.toObject(re.value());
                if ("insert".equals(record.get("operate"))) {
                    recordToDB(record);
                } else {
                    updateRecord.add(record);
                }
                LogMessageConsumerUtils.recordToES(record);
            }
            recordUpdateToDB(updateRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void recordToDB(Map<String, Object> record) throws SQLException, Exception {
        try {
            daoHelper.getDao().insert("monitor.insertStepLog", record);
        } catch (Exception e) {
            LogUtils.error("==日志已存在，插入失败==", e);
        }
    }


    private void recordUpdateToDB(List<Map<String, Object>> record) throws Exception {
        if (record.isEmpty()) {
            return;
        }
        daoHelper.getDao().delete("monitor.deleteStepLog", record);
        daoHelper.getDao().insert("monitor.insertReturnStepLog", record);
    }
}
