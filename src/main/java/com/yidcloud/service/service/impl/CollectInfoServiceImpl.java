package com.yidcloud.service.service.impl;

import com.alibaba.fastjson.JSON;

import com.lsy.base.date.DateHelper;
import com.lsy.base.result.ResultVo;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.SystemSettingHelper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.yidcloud.api.service.ICollectInfoService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.DocumentCallbackHandler;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


import java.util.*;

/**
 * 对采集的原始数据做操作
 *
 * @author 胡洪瑜 huhongyu@edenep.net 易登科技
 * @version 2.0
 * @since 2018/3/20 21:47
 */
public class CollectInfoServiceImpl implements ICollectInfoService {
    private static Logger logger = LoggerFactory.getLogger(CollectInfoServiceImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME_PRE = "collect_info_";

    @Override
    public ResultVo saveCollentInfo(Map<String, String> map) {
        ResultVo vo  =  new ResultVo();
        map.remove("msgByte");
        map.remove("IMEI");
        Map<String,Object> mongoMap = new HashMap<>();
        mongoMap.putAll(map);
        try {
            Double y = Double.parseDouble(map.get("lng"));
            Double x = Double.parseDouble(map.get("lat"));
            Double [] location =new Double[]{x,y};
            mongoMap.put("location",location);
        }catch (Exception e){

        }
        String gpsTime = map.get("gpsTime");
        if (StringHelper.isBlank(gpsTime)){
            return vo;
        }
        Date gpsDate = DateHelper.parseString(gpsTime);
        mongoMap.put("gpsTime",gpsDate);
        Long gpsTimeLong = gpsDate.getTime();
        mongoMap.put("gpsTimeLong",gpsTimeLong);
        mongoMap.put("insertDataTime",new Date());


        String collectionName;
        String thisStr = DateHelper.formatDate(gpsDate);
        collectionName = COLLECTION_NAME_PRE+thisStr;
        try {
            //创建一个新的集合时，先创建索引
            if (!mongoTemplate.collectionExists(collectionName)){
                String mondogoIndexs = SystemSettingHelper.newInstance().getValue("mondogo.indexs");
                createMongoIndex(collectionName,mondogoIndexs);
            }
            mongoTemplate.save(mongoMap,collectionName);
        }catch (Exception e){
            vo.setError_no(-1);
            vo.setError_info("采集数据插入mongo失败");
            logger.error("采集数据插入mongo失败",e);
        }

        return vo;
    }

    /**
     * 创建集合索引
     * @author zhouliang@edenep.net
     * @version 2.0
     * @description:
     * @copyright: Copyright (c) 2018
     * @company: 易登科技
     * @date 2019/10/24 0024 下午 3:56
     */
    private void createMongoIndex(String collectionName,String mondogoIndexs) {

        if(StringHelper.isBlank(mondogoIndexs)){
            logger.info("没有配置需要创建的索引");
            return;
        }
        String [] indexArray = mondogoIndexs.split(";");
        for (String str:indexArray){
            Document keys = new Document();
            String [] indexs = str.split(",");
            for (String fieldName:indexs){
                if("gpsTimeLong".equalsIgnoreCase(fieldName)){
                    keys.put(fieldName,-1);
                }else{
                    keys.put(fieldName,1);
                }
            }
            CompoundIndexDefinition compoundIndex;
            compoundIndex = new CompoundIndexDefinition(keys) ;
            mongoTemplate.indexOps(collectionName).ensureIndex(compoundIndex);
            logger.info("集合文档"+collectionName+"创建索引成功"+ JSON.toJSONString(keys.toString()));
        }
    }

    @Override
    public ResultVo queryCollentInfo(Map<String, String> map,Set<Date> queryDate) {
        ResultVo vo = new ResultVo();
        Criteria criteria = new Criteria();
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            criteria.and(entry.getKey()).is(entry.getValue());
        }
        Query query = new Query(criteria);
        vo.setResult(queryCollentInfo(query,queryDate));
        return vo;
    }

    @Override
    public ResultVo queryCollentInfoByqueryBuilder(String queryBuilder, Set<Date> queryDate) {
        ResultVo vo = new ResultVo();
        Query query=new BasicQuery(queryBuilder).with(new Sort(Sort.Direction.ASC,"pgsTime"));
        QueryBuilder q = new QueryBuilder();
        vo.setResult(queryCollentInfo(query,queryDate));
        return vo;
    }
    @Override
    public ResultVo queryCollectSimpleInfo(String queryBuilder,Set<String> resultData , Set<Date> queryDate){
        //组装返回结果集
        logger.info("获取集合里面的信息");
        ResultVo vo = new ResultVo();
        BasicDBObject fieldsObject=new BasicDBObject();
        for (String result:resultData) {
            fieldsObject.put(result,1);
        }
        Query query=new BasicQuery(queryBuilder,fieldsObject.toJson()).with(new Sort(Sort.Direction.ASC,"gpsTime"));
        vo.setResult(queryCollentInfo(query,queryDate));
        return vo;
    }

    @Override
    public ResultVo queryCollectSimpleInfo(String queryBuilder,Set<String> resultData , Set<Date> queryDate, Boolean isAsc, Integer limit){
        ResultVo vo = new ResultVo();
        BasicDBObject fieldsObject=new BasicDBObject();
        for (String result:resultData) {
            fieldsObject.put(result,1);
        }

        Query query = new BasicQuery(queryBuilder,fieldsObject.toJson());
        query = query.with(new Sort(isAsc == null || isAsc == true ? Sort.Direction.ASC : Sort.Direction.DESC,"gpsTime"));

        if (limit != null && limit > 0) {
            query = query.limit(limit);
        }

        vo.setResult(queryCollentInfo(query,queryDate));
        return vo;
    }

    private  List<String> queryCollentInfo(Query query,Set<Date> queryDate){
        List<String> documentList = new ArrayList<>();
        if (queryDate == null){
            //查询当天的
            mongoTemplate.executeQuery(query,COLLECTION_NAME_PRE+DateHelper.formatDate(new Date()),new DocumentCallbackHandler(){
                @Override
                public void processDocument(Document document) throws MongoException, DataAccessException {
                    documentList.add(document.toJson());
                }
            });
        }else {
            for (Date d:queryDate) {
                mongoTemplate.executeQuery(query,COLLECTION_NAME_PRE+DateHelper.formatDate(d),new DocumentCallbackHandler(){
                    @Override
                    public void processDocument(Document document) throws MongoException, DataAccessException {
                        documentList.add(document.toJson());
                    }
                });
            }
        }
        return documentList;
    }

        public static void main(String[] args) {
           /* long msl = 1521880401939L;
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                String str=sdf.format(msl);
                System.out.println(str);
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            System.out.println(DateHelper.parseString("2018-03-29 00:00:01").getTime());
            System.out.println(DateHelper.parseString("2018-03-29 23:59:00").getTime());

    }
}
