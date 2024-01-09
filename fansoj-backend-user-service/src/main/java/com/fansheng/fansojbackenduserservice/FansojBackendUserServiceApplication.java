package com.fansheng.fansojbackenduserservice;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@MapperScan("com.fansheng.fansojbackenduserservice.mapper")
@EnableScheduling
@ComponentScan("com.fansheng")
//@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.fansheng.fansojbackendserviceclient.service"})
public class FansojBackendUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FansojBackendUserServiceApplication.class, args);
    }
}
