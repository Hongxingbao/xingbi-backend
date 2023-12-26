package com.sing.init.config;

import com.sing.init.bimq.BiMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于创建程序的交换机和队列
 */
@Configuration
public class BiRabbitMqInitConfig {

    /**
     * 声明死信队列和交换机
     */
    @Bean
    Queue BiDeadQueue() {
        return QueueBuilder.durable(BiMqConstant.DL_QUEUE_NAME).build();
    }

    @Bean
    DirectExchange BiDeadExchange() {
        return new DirectExchange(BiMqConstant.DL_EXCHANGE_NAME);
    }

    @Bean
    Binding BiDeadBinding(Queue BiDeadQueue, DirectExchange BiDeadExchange) {
        return BindingBuilder.bind(BiDeadQueue).to(BiDeadExchange).with(BiMqConstant.DL_ROUTING_KEY);
    }

    /**
     * 声明队列和交换机
     */
    @Bean
    Queue BiQueue() {
        Map<String, Object> arg = new HashMap<>();
        //信息参数
        arg.put("x-message-ttl", 90000);//设置过期时间为90秒，过期后消息将被移动到死信队列
        arg.put("x-dead-letter-exchange", BiMqConstant.DL_EXCHANGE_NAME);
        arg.put("x-dead-letter-routing-key", BiMqConstant.DL_ROUTING_KEY);
        return QueueBuilder.durable(BiMqConstant.BI_QUEUE_NAME).withArguments(arg).build();
    }

    @Bean
    DirectExchange BiExchange() {
        return new DirectExchange(BiMqConstant.BI_EXCHANGE_NAME);
    }

    @Bean
    Binding BiBinding(Queue BiQueue, DirectExchange BiExchange) {
        return BindingBuilder.bind(BiQueue).to(BiExchange).with(BiMqConstant.BI_ROUTING_KEY);
    }
}