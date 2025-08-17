package ru.practicum.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.practicum.project.dto.ItemDto;

@Configuration
public class RedisConfig {
	 @Bean
	 ReactiveRedisTemplate<String, ItemDto> itemDtoRedisTemplate(
	            ReactiveRedisConnectionFactory connectionFactory,
	            ObjectMapper objectMapper
	    ) {
	        StringRedisSerializer keySerializer = new StringRedisSerializer();
	        Jackson2JsonRedisSerializer<ItemDto> valueSerializer =
	                new Jackson2JsonRedisSerializer<>(ItemDto.class);
	        valueSerializer.setObjectMapper(objectMapper);

	        RedisSerializationContext.RedisSerializationContextBuilder<String, ItemDto> builder =
	                RedisSerializationContext.newSerializationContext(keySerializer);

	        RedisSerializationContext<String, ItemDto> context = builder
	                .value(valueSerializer)
	                .hashKey(keySerializer)
	                .hashValue(valueSerializer)
	                .build();

	        return new ReactiveRedisTemplate<>(connectionFactory, context);
	    }
}
