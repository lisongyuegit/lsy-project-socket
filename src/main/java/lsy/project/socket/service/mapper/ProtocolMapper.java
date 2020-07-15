package lsy.project.socket.service.mapper;

import com.lsy.mybatisplus.mapper.BaseMapper;
import lsy.project.socket.api.entity.Protocol;

import java.util.List;
import java.util.Map;


/**
 * 协议表（指令表，相当于一个一个的请求接口信息）
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public interface ProtocolMapper extends BaseMapper<Protocol> {
    /**
     * 查询所有某一个状态下的协议
     *
     * @param activeStatus 协议激活状态
     * @return List<Map, Object>
     * @since 2017/11/8  20:44
     */
    List<Map> selectAllProtocol(String activeStatus);

    /**
     * 查询指定参数的协议
     *
     * @param map
     * @return List<Map, Object>
     * @since 2017/11/8  21:03
     */
    List<Map> selectProtocols(Map map);

    List<Map> selectProNameIdDic();
}
