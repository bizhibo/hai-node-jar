package com.hai.log.collect.kafka.consumer;

import com.hai.log.collect.common.LogMessageConsumerUtils;
import com.hai.log.collect.common.LogUtils;
import com.hai.log.collect.common.ObjectSerializerUtils;
import hai.component.HaiServiceBase;
import hai.dao.HaiIbatisDaoHelper;
import hai.mq.IMQConsumer;
import hai.mq.MQDao;
import hai.mq.MQDaoManage;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @描述 :
 * @创建者：liuss
 * @创建时间： 2018/10/24
 */
public class HaiExceptionLogConsumer extends HaiServiceBase implements IMQConsumer {

    protected MQDao mqDao;

    private HaiIbatisDaoHelper daoHelper;

    private final static String VARIABLEKEY = "ExceptionLog";

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
            List<Map<String, Object>> insertRecord = new ArrayList<>();
            for (ConsumerRecord<byte[], byte[]> re : records) {
                Map<String, Object> record = (Map<String, Object>) ObjectSerializerUtils.toObject(re.value());
                insertRecord.add(record);
                LogMessageConsumerUtils.recordToES(record);
            }
            recordToDB(insertRecord);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
        return true;
    }

    private void recordToDB(List<Map<String, Object>> record) throws Exception {
        if (record.isEmpty()) {
            return;
        }
        daoHelper.getDao().insert("monitor.insertHaiExceptionLog", record);
    }

}
