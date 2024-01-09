package com.fansheng.fansojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.fansheng.fansojbackendquestionservice.mapper")
@ComponentScan("com.fansheng")
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.fansheng.fansojbackendserviceclient.service"})
public class FansojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FansojBackendQuestionServiceApplication.class, args);
    }

}
