package com.example.springbootredistest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfiguration {
    // lettuce 사용시
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel("localhost",5000)
                .sentinel("localhost",5001)
                .sentinel("localhost",5002);
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisSentinelConfiguration);
        return lettuceConnectionFactory;
    }

    // jedis 사용시
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory(){
//        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration()
//                .master("mymaster")
//                .sentinel("localhost",5000)
//                .sentinel("localhost",5001)
//                .sentinel("localhost",5002);
//        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisSentinelConfiguration);
//        return jedisConnectionFactory;
//    }
//
//    @Bean
//    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory){
//        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        return redisTemplate;
//    }
}
