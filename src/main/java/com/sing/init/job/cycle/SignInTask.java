package com.sing.init.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sing.init.common.ErrorCode;
import com.sing.init.exception.ThrowUtils;
import com.sing.init.model.entity.Score;
import com.sing.init.service.ScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.scheduling.annotation.Scheduled;
import com.sing.init.websocket.WebSocketServer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class SignInTask {
    @Resource
    private ScoreService scoreService;

    /**
     * 每天凌晨0点更新积分表的isSign为0
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void sendMessageToClient() {
        UpdateWrapper<Score> updateWrapper = new UpdateWrapper<>();
        //更新Score表中isSign为1的数据
        updateWrapper.set("isSign",0)
                .eq("isSign",1);
        boolean updateResult = scoreService.update(updateWrapper);
        ThrowUtils.throwIf(!updateResult,ErrorCode.OPERATION_ERROR);
        log.info("Check-in status has been updated!");
    }
}
