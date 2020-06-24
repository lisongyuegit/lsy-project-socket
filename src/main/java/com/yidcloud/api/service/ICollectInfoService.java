package com.yidcloud.api.service;


import com.lsy.base.result.ResultVo;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 对采集的原始数据做操作
 *
 * @author 胡洪瑜 huhongyu@edenep.net 易登科技
 * @version 2.0
 * @since 2018/3/20 21:44
 */
public interface ICollectInfoService {
    /**
     * 添加信息到mongo数据库
     * @param collectMap
     * @return
     */
    ResultVo saveCollentInfo(Map<String,String> collectMap);

    /**
     * 到mongo里面查询采集的信息
     * @param collectMap（key：字段名称，value：匹配的值）
     * @param queryDates 查询的时间
     * @return
     */
    ResultVo queryCollentInfo(Map<String,String> collectMap, Set<Date> queryDates);

    /**
     * 到mongo里面查询采集的信息
     * @param QueryBuilder（组装查询条件）
     * @param queryDates 查询的时间
     * @return
     */
    ResultVo queryCollentInfoByqueryBuilder(String queryBuilderJson, Set<Date> queryDates);

    /**
     *  到mongo里面查询采集的信息
     * @param queryBuilderJson 组装查询条件）
     * @param resultData 需要返回的字段
     * @param queryDate 查询的时间
     * @return
     */
     ResultVo queryCollectSimpleInfo(String queryBuilderJson, Set<String> resultData , Set<Date> queryDate);

    /**
     * 到mongo里面查询采集的信息
     * @param queryBuilder 组装查询条件
     * @param resultData 需要返回的字段
     * @param queryDate 查询的时间
     * @param isAsc  升序还是降序（默认按gpsTime 升序）
     * @param limit 取多少条，默认全部。
     * @return
     */
    ResultVo queryCollectSimpleInfo(String queryBuilder,Set<String> resultData , Set<Date> queryDate, Boolean isAsc, Integer limit);
}
