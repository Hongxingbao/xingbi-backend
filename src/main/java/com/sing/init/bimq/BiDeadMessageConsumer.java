package com.sing.init.bimq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BiDeadMessageConsumer {

    /**
     * @param message     接收到的消息内容。
     * @param channel     与 RabbitMQ 通信的通道对象。
     * @param deliveryTag 消息的交付标签，用来唯一标识消息的标签,用于手动确认消息。初始值为1
     */
    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.DL_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        log.info("dead message = {}", message);
        //此处暂且不做处理
        channel.basicAck(deliveryTag, false);
    }
}
