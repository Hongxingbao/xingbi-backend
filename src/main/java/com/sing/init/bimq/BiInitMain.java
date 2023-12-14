package com.sing.init.bimq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 */
public class BiInitMain {

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.88.130");
        factory.setUsername("itcast");
        factory.setPassword("123321");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        String EXCHANGE_NAME = BiMqConstant.BI_EXCHANGE_NAME;
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 创建队列，随机分配一个队列名称
        String queueName = BiMqConstant.BI_QUEUE_NAME;
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY);
    }
}