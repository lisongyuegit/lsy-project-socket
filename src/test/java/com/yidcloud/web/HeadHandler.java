package com.yidcloud.web;


import com.lsy.base.result.ResultVo;

/**
 * (一句话描述该类做什么)
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/9 19:42
 */
public class HeadHandler extends  AbstractHandler {

    @Override
    public ResultVo handler(String msg) {
        System.out.println("head");
        return null;
    }
}
