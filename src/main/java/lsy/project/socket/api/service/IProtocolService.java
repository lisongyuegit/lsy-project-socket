package lsy.project.socket.api.service;

import com.lsy.base.exception.BaseException;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.IService;
import lsy.project.socket.api.entity.Protocol;

import java.util.List;
import java.util.Map;

/**
 * 协议表
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public interface IProtocolService extends IService<Protocol> {
    Page<Protocol> queryList(Protocol protocol, Map<String, String> params, int current, int pageSize);

    /**
     * 把所有的协议信息同步到缓存中
     *
     * @since 2017/11/8  17:16
     */
    void synProtocolToCache();

    /**
     * 同步指定的协议信息到缓存
     *
     * @param protocolIds
     * @return
     * @since 2017/11/8  17:16
     */
    void synProtocolToCache(String[] protocolIds);

    /**
     * 查询名字和id的字典
     *
     * @return
     * @throws BaseException
     */
    public List<Map> selectProNameIdDic() throws BaseException;
}
