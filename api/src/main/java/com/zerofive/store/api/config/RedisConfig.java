package com.zerofive.store.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String primaryHost;

    @Value("${spring.data.redis.port}")
    private int primaryPort;

    @Value("${spring.data.redis.username:}")
    private String primaryUsername;

    @Value("${spring.data.redis.password:}")
    private String primaryPassword;

    @Value("${coupon.issue-redis.host:${spring.data.redis.host}}")
    private String issueHost;

    @Value("${coupon.issue-redis.port:${spring.data.redis.port}}")
    private int issuePort;

    @Value("${coupon.issue-redis.username:${spring.data.redis.username:}}")
    private String issueUsername;

    @Value("${coupon.issue-redis.password:${spring.data.redis.password:}}")
    private String issuePassword;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return createConnectionFactory(primaryHost, primaryPort, primaryUsername, primaryPassword);
    }

    @Bean
    public RedisConnectionFactory issueRedisConnectionFactory() {
        return createConnectionFactory(issueHost, issuePort, issueUsername, issuePassword);
    }

    @Bean
    @Primary
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean("issueRedisTemplate")
    public StringRedisTemplate issueRedisTemplate() {
        return new StringRedisTemplate(issueRedisConnectionFactory());
    }

    private LettuceConnectionFactory createConnectionFactory(String host, int port, String username, String password) {
        var config = new RedisStandaloneConfiguration(host, port);

        if (StringUtils.hasText(username)) {
            config.setUsername(username);
        }
        if (StringUtils.hasText(password)) {
            config.setPassword(RedisPassword.of(password));
        }

        return new LettuceConnectionFactory(config);
    }
}
