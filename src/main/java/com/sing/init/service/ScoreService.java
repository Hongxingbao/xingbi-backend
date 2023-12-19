package com.sing.init.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sing.init.model.entity.Score;

/**
* @author 24840
* @description 针对表【score(积分表)】的数据库操作Service
* @createDate 2023-12-19 17:15:31
*/
public interface ScoreService extends IService<Score> {
    /**
     * 签到
     * @param userId
     * @return
     */
    void checkIn(Long userId);

    /**
     * 消耗积分
     * @param userId
     * @param points 积分数
     * @return
     */
    void deductPoints(Long userId, Long points);

    /**
     *获取积分
     * @param userId
     * @return
     */
    Long getUserPoints(Long userId);


}
