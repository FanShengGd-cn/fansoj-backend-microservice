package com.fansheng.fansojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class InitRabbitMq {
    public static void doInit(){
        try{
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = "code_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            // 创建队列
            String queueName = "code_queue";
            channel.queueDeclare(queueName, true, true ,false, null);
            channel.queueBind(queueName, EXCHANGE_NAME , "my_routingKey");
            log.info("消息队列启动成功");
        } catch (IOException | TimeoutException e) {
            System.out.println(e.getMessage());
            log.error("消息队列启动失败");
        }
    }
//    public static void main(String[] args) {
//        doInit();
//    }
}
