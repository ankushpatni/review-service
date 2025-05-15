package com.zuzu.reviewservice.config;

import com.zuzu.reviewservice.application.dto.ReviewDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:review-service-group}")
    private String groupId;

    @Value("${spring.kafka.topics.review-events:review-events}")
    private String reviewEventsTopic;

    @Bean
    public NewTopic reviewEventsTopic() {
        return TopicBuilder.name(reviewEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Producer configuration
    @Bean
    public ProducerFactory<String, ReviewDto> reviewProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ReviewDto> reviewKafkaTemplate() {
        return new KafkaTemplate<>(reviewProducerFactory());
    }

    // Consumer configuration
    @Bean
    public ConsumerFactory<String, ReviewDto> reviewConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.zuzu.reviewservice.application.dto");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, 
                new StringDeserializer(),
                new JsonDeserializer<>(ReviewDto.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReviewDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReviewDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reviewConsumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
} 