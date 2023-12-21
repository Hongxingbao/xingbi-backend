package com.sing.init.utils;

import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ClearCache {

    @Resource
    private  RedissonClient redissonClient;
    public  void clearCacheByPattern(String pattern) {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> matchingKeys = keys.getKeysByPattern(pattern + "*");
        if(matchingKeys!=null){
            for (String matchingKey : matchingKeys) {
                // 删除匹配的键
                redissonClient.getKeys().delete(matchingKey);
            }
        }
    }
}
