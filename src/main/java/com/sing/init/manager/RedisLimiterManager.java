package com.sing.init.manager;

import com.sing.init.common.ErrorCode;
import com.sing.init.exception.BusinessException;
import com.sing.init.exception.ThrowUtils;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 通用的Redis限流器，提供限流服务
 */
@Component
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key) {
        //创建限流器
        RRateLimiter rRateLimiter = redissonClient.getRateLimiter(key);
        // 限流器规则：每秒两个请求，最多只能允许两个请求被通过
        // RateType.OVERALL表示限流作用于整个令牌桶，即限制所有请求的速率
        rRateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        boolean canOp = rRateLimiter.tryAcquire(1);
        if(!canOp){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }


    }
}
