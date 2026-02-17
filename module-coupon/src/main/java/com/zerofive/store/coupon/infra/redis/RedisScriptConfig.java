package com.zerofive.store.coupon.infra.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

@Configuration
public class RedisScriptConfig {

    @Bean
    public DefaultRedisScript<List> enterQueueScript() {
        return loadScript("scripts/enter_queue.lua", List.class);
    }

    @Bean
    public DefaultRedisScript<List> getQueueStatusScript() {
        return loadScript("scripts/get_queue_status.lua", List.class);
    }

    @Bean
    public DefaultRedisScript<String> requestIssueScript() {
        return loadScript("scripts/request_issue.lua", String.class);
    }

    private <T> DefaultRedisScript<T> loadScript(String path, Class<T> resultType) {
        var script = new DefaultRedisScript<T>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(path)));
        script.setResultType(resultType);
        return script;
    }
}
