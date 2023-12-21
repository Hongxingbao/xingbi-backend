package com.sing.init.aop;

import com.sing.init.common.ErrorCode;
import com.sing.init.exception.BusinessException;
import com.sing.init.model.entity.User;
import com.sing.init.service.ScoreService;
import com.sing.init.service.UserService;
import com.sing.init.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 积分控制 AOP
 *
 * @author xing
 **/
@Aspect
@Component
@Slf4j
public class ScoreInterceptor {
//
//    @Resource
//    private UserService userService;
//
//    @Resource
//    private ScoreService scoreService;
//
//    @Resource
//    private RedissonClient redissonClient;
//
//    /**
//     * 执行拦截
//     */
////    @Around("execution(* com.sing.init.controller.ChartController.analysisBySynchronize(..)) || " +
////            "execution(* com.sing.init.controller.ChartController.analysisByAsync(..)) || " +
////            "execution(* com.sing.init.controller.ChartController.analysisByAsyncMq(..))")
//    //@Around("execution(* com.sing.init.manager.AiManager(..))")
//    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
//        // 执行原方法
//        Object result;
//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = requestAttributes.getRequest();
//        //手动清除缓存，避免拿到旧数据
//        String cacheKey = "ChartController_listMyChartVOByPage";
//        clearCacheByPattern(cacheKey);
//        // 当前登录用户
//        User loginUser = userService.getLoginUser(request);
//        log.error(String.valueOf(loginUser.getId()));
//        // 当前登录用户
//        try {
//            // 执行原方法
//            result = point.proceed();
//            // 在方法成功执行后，扣除积分
//            scoreService.deductPoints(loginUser.getId(), 5L);
//            clearCacheByPattern(cacheKey);
//        } catch (Exception e) {
//            // 在方法抛出异常时，
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成好像出问题了呦");
//        }
//        // 通过权限校验，放行
//        return result;
//    }
//    private void clearCacheByPattern(String pattern) {
//        RKeys keys = redissonClient.getKeys();
//        Iterable<String> matchingKeys = keys.getKeysByPattern(pattern + "*");
//        if(matchingKeys!=null){
//            for (String matchingKey : matchingKeys) {
//                // 删除匹配的键
//                redissonClient.getKeys().delete(matchingKey);
//            }
//        }
//    }
}

