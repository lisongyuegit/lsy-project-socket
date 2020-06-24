package com.yidcloud.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 *web启动类
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/7 14:21
 */
@SpringBootApplication(scanBasePackages = {"com.yidcloud.web"})
@ImportResource(locations = { "classpath:spring-web-service.xml" , "classpath:dubbo/dubbo-web-service-base.xml"})
public class CollectServiceApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(CollectServiceApplication.class);
        app.run(args);
    }


}
