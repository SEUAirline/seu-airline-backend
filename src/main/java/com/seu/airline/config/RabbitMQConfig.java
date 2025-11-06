package com.seu.airline.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // ========== 交换机和队列名称常量 ==========
    // 订单队列相关
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_QUEUE = "order.queue";
    public static final String ORDER_ROUTING_KEY = "order.routing.key";
    
    // 日志队列相关
    public static final String LOG_EXCHANGE = "log.exchange";
    public static final String LOG_QUEUE = "log.queue";
    public static final String LOG_ROUTING_KEY = "log.routing.key";
    
    // 通知队列相关
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";
    
    // 死信队列相关
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter.routing.key";
    
    // 重试次数和间隔配置
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 5000; // 5秒

    /**
     * 消息转换器，用于JSON序列化和反序列化
     * 添加对Java 8日期时间类型的支持
     */
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 添加Java 8日期时间类型支持
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    /**
     * 配置RabbitTemplate，设置消息转换器和确认机制
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        // 开启发送确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                if (correlationData != null) {
                    System.out.println("消息发送确认成功: " + correlationData.getId());
                } else {
                    System.out.println("消息发送确认成功");
                }
            } else {
                System.out.println("消息发送确认失败，原因: " + cause);
            }
        });
        return rabbitTemplate;
    }
    
    /**
     * 配置消息监听器容器工厂，启用手动确认模式
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        // 启用手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 设置消费者数量
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        // 设置预取计数，限制一次获取的消息数量，避免消息堆积
        factory.setPrefetchCount(5);
        return factory;
    }

    // ========== 订单队列配置 ==========
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                // 配置死信交换机
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                // 配置死信路由键
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                // 配置消息过期时间（毫秒）
                .withArgument("x-message-ttl", 30000)
                // 配置队列最大长度
                .withArgument("x-max-length", 1000)
                .build();
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    // ========== 日志数据采集队列配置 ==========
    @Bean
    public Queue logQueue() {
        return QueueBuilder.durable(LOG_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange logExchange() {
        return new TopicExchange(LOG_EXCHANGE, true, false);
    }

    @Bean
    public Binding logBinding() {
        return BindingBuilder.bind(logQueue()).to(logExchange()).with(LOG_ROUTING_KEY);
    }

    // ========== 系统解耦通知队列配置 ==========
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public FanoutExchange notificationExchange() {
        return new FanoutExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange());
    }

    // ========== 死信队列配置 ==========
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_ROUTING_KEY);
    }
}