package com.hai.log.collect.rpc;

import com.hai.log.collect.common.JestClientFactoryUtils;
import com.hai.log.collect.common.JsonUtils;
import com.hai.log.collect.common.LogUtils;
import com.hai.log.collect.common.Result;
import ctd.util.annotation.RpcService;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import io.searchbox.core.search.sort.Sort;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.type.TypeExist;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @描述 :
 * @创建者：liuss
 * @创建时间： 2018/11/6
 */
public class ESOperateService {

    //固定错误索引名
    private final static String ERRORINDEX_NAME = "error";

    //固定typeName
    private final static String TYPE_NAME = "-type";

    //设置setting
    private final static String SETTING = "{\"index.mapping.depth.limit\":100}";

    //设置mapping
    private final static String MAPPING = "{\"-type\":{\"date_detection\":false,\"properties\":{\"bsoft.timeMillis\":{\"type\":\"text\",\"fielddata\": true}}}}";

    @RpcService
    public Result createDoc(Map<String, Object> paramMap) {
        try {
            if (!paramMap.containsKey("indexName") && !paramMap.containsKey("doc") && !paramMap.containsKey("docID")) {
                return Result.buildFaileResult("不能缺少索引名或者文档或者id");
            }
            String indexName = String.valueOf(paramMap.get("indexName"));
            TypeExist typeExist = new TypeExist.Builder(indexName.toLowerCase()).addType(TYPE_NAME).build();
            //验证索引和类型是否存在
            JestResult jr = JestClientFactoryUtils.getInstance().execute(typeExist);
            if (!jr.isSucceeded()) {
                //创建索引和类型
                if (!createIndex(indexName.toLowerCase())) {
                    return Result.buildFaileResult("创建索引失败");
                }
            }
            String id = String.valueOf(paramMap.get("docID"));
            Object operation = paramMap.get("operate");
            //判断是否有步骤的操作类型
            if (operation != null) {
                //如果是新增，查看是否有此id的update，如果有就不做操作;如果没有就新增进去
                String operate = String.valueOf(operation);
                if (operate.equals("insert")) {
                    JestResult jestr = getDocById(indexName, id);
                    if (jestr != null && jestr.isSucceeded()) {
                        Map<String, String> map = jestr.getSourceAsObject(Map.class);
                        String operateFlag = map.get("bsoft.operate");
                        if (operateFlag.equals("insert")) {
                            return Result.buildFaileResult("此id的insert消息已存在");
                        }
                        if (operateFlag.equals("update")) {
                            return Result.buildFaileResult("此id的update消息已存在,不需要再插入");
                        }
                    }
                }
            }
            //创建文档
            Object doc = paramMap.get("doc");
            Index.Builder builder = new Index.Builder(doc);
            builder.refresh(true);
            Index index = builder.index(indexName.toLowerCase()).type(TYPE_NAME).id(id).build();
            JestResult jestResult = JestClientFactoryUtils.getInstance().execute(index);
            if (jestResult.isSucceeded()) {
                return Result.buildSuccessResult("插入文档成功");
            } else {
                LogUtils.error(jestResult.getErrorMessage());
                return Result.buildFaileResult("插入文档失败");
            }
        } catch (IOException e) {
            LogUtils.error(e);
            return Result.buildFaileResult("系统错误");
        }
    }

    //根据ID获取doc文档
    private JestResult getDocById(String indexName, String id) {
        JestResult jestResult = null;
        try {
            Get get = new Get.Builder(indexName, id).type(TYPE_NAME).build();
            jestResult = JestClientFactoryUtils.getInstance().execute(get);
            return jestResult;
        } catch (IOException e) {
            LogUtils.error(e);
        }
        return jestResult;
    }

    //新建索引
    private Boolean createIndex(String indexName) {
        try {
            CreateIndex createIndex = new CreateIndex.Builder(indexName.toLowerCase()).settings(SETTING).mappings(MAPPING).build();
            JestResult jr = JestClientFactoryUtils.getInstance().execute(createIndex);
            return jr.isSucceeded();
        } catch (IOException e) {
            LogUtils.error(e);
            return false;
        }
    }


    @RpcService
    public Result search(Map<String, Object> paramMap) {
        try {
            if (!paramMap.containsKey("pageNum") || !paramMap.containsKey("pageSize")) {
                return Result.buildFaileResult("入参不能缺少pageNum与pageSize");
            }
            int pageNum = Integer.parseInt(String.valueOf(paramMap.get("pageNum")));
            int pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
            paramMap.remove("pageNum");
            paramMap.remove("pageSize");
            String indexName = "";
            if (paramMap.containsKey("indexName")) {
                indexName = String.valueOf(paramMap.get("indexName")).toLowerCase();
                paramMap.remove("indexName");
            }
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getKey().equals("startDate")) {
                    boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.rangeQuery("bsoft.timeMillis").gte(entry.getValue()));
                } else if (entry.getKey().equals("endDate")) {
                    boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.rangeQuery("bsoft.timeMillis").lte(entry.getValue()));
                } else {
                    boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.multiMatchQuery(entry.getValue(), entry.getKey()).type(MatchQuery.Type.PHRASE).slop(0));
                }
            }
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQueryBuilder);
            searchSourceBuilder.from((pageNum - 1) * pageSize);
            searchSourceBuilder.size(pageSize);
            Sort sort = new Sort("bsoft.timeMillis", Sort.Sorting.DESC);
            Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName.toLowerCase()).addSort(sort).build();
            SearchResult sr = JestClientFactoryUtils.getInstance().execute(search);
            if (!sr.isSucceeded()) {
                LogUtils.error(sr.getErrorMessage());
                return Result.buildFaileResult("查询失败");
            } else {
                Map<String, Object> map = JsonUtils.fromJson(sr.getJsonString(), Map.class);
                return Result.buildSuccessResult("查询成功", map);
            }
        } catch (IOException e) {
            LogUtils.error(e);
            return Result.buildFaileResult("系统错误");
        }

    }

    /**
     * 插入错误索引库文档
     *
     * @param paramMap
     * @return
     */
    @RpcService
    public Result createErrorDoc(Map<String, Object> paramMap) {
        try {
            if (!paramMap.containsKey("doc")) {
                return Result.buildFaileResult("不能缺少文档");
            }
            TypeExist typeExist = new TypeExist.Builder(ERRORINDEX_NAME).addType(TYPE_NAME).build();
            //验证索引和类型是否存在
            JestResult jr = JestClientFactoryUtils.getInstance().execute(typeExist);
            if (!jr.isSucceeded()) {
                return Result.buildFaileResult("索引库不存在");
            }
            //创建文档
            Object doc = paramMap.get("doc");
            Index.Builder builder = new Index.Builder(doc);
            builder.refresh(true);
            Index index = builder.index(ERRORINDEX_NAME).type(TYPE_NAME).build();
            JestResult jestResult = JestClientFactoryUtils.getInstance().execute(index);
            if (jestResult.isSucceeded()) {
                return Result.buildSuccessResult("插入文档成功");
            } else {
                LogUtils.error(jestResult.getErrorMessage());
                return Result.buildFaileResult("插入文档失败");
            }
        } catch (IOException e) {
            LogUtils.error(e);
            return Result.buildFaileResult("系统错误");
        }
    }


    /**
     * 创建错误索引方法
     */
    @RpcService
    public boolean createErrorIndex() {
        try {
            TypeExist typeExist = new TypeExist.Builder(ERRORINDEX_NAME).addType(TYPE_NAME).build();
            JestResult jestResult = JestClientFactoryUtils.getInstance().execute(typeExist);
            if (jestResult.isSucceeded()) {
                return false;
            }
            List<String> fieldNames = Arrays.asList("ERRIP", "ERRTIME", "ERRSTACK", "ERRMSG", "ERRCODE", "PARTTIME", "ERRID", "ERRSTATE");
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                    .field(TYPE_NAME).startObject()
                    .field("_source").startObject().field("enabled", "true").endObject()
                    .field("properties").startObject();
            for (String fieldName : fieldNames) {
                builder.field(fieldName).startObject().field("type", "text").field("index", "analyzed").endObject();
            }
            builder.field("bsoft.timeMillis").startObject().field("type", "text").field("fielddata", true).endObject();
            builder.endObject().endObject().endObject();
            CreateIndex createIndex = new CreateIndex.Builder(ERRORINDEX_NAME).mappings(builder.string()).build();
            JestResult jr = JestClientFactoryUtils.getInstance().execute(createIndex);
            return jr.isSucceeded();
        } catch (IOException e) {
            LogUtils.error(e);
            return false;
        }
    }

    @RpcService
    public Result deleteDocByTime(Map<String, Object> paramMap) {
        try {
            if (!paramMap.containsKey("timeStamp")) {
                return Result.buildFaileResult("入参不能缺少时间戳timeStamp");
            }
            String timeStamp = String.valueOf(paramMap.get("timeStamp"));
            if (StringUtils.isBlank(timeStamp)) {
                return Result.buildFaileResult("入参的时间戳timeStamp不能为空");
            }
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.rangeQuery("bsoft.timeMillis").lte(timeStamp));
            DeleteByQuery deleteByQuery = new DeleteByQuery.Builder(new SearchSourceBuilder().query(boolQueryBuilder).toString()).build();
            JestResult jr = JestClientFactoryUtils.getInstance().execute(deleteByQuery);
            if (jr.isSucceeded()) {
                return Result.buildSuccessResult("删除文档成功");
            } else {
                return Result.buildFaileResult("删除文档失败");
            }
        } catch (IOException e) {
            LogUtils.error(e);
            return Result.buildFaileResult("操作失败");
        }
    }
}
