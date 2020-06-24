package com.yidcloud.web.util;


import com.lsy.redis.client.JedisClient;

public class TestRedis {
    public static void main(String[] args) {
        JedisClient client = JedisClient.getJedisClient();
        System.out.println(client.hget("com.yidcloud.web.protocol.2323_6789.02","analysis"));
    }
}
