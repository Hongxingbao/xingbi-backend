package com.sing.init.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sing.init.annotation.AuthCheck;
import com.sing.init.common.BaseResponse;
import com.sing.init.common.DeleteRequest;
import com.sing.init.common.ErrorCode;
import com.sing.init.common.ResultUtils;
import com.sing.init.constant.UserConstant;
import com.sing.init.exception.BusinessException;
import com.sing.init.exception.ThrowUtils;
import com.sing.init.model.dto.user.*;
import com.sing.init.model.entity.User;
import com.sing.init.model.vo.LoginUserVO;
import com.sing.init.model.vo.UserVO;
import com.sing.init.service.ScoreService;
import com.sing.init.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 积分接口
 *
 * @author xing

 */
@RestController
@RequestMapping("/score")
@Slf4j
public class ScoreController {

    @Resource
    private UserService userService;

    @Resource
    private ScoreService scoreService;

    /**
     * 用于签到时添加积分
     * @param request
     * @return
     */
    @PostMapping("/checkIn")
    public BaseResponse<String> checkIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        scoreService.checkIn(loginUser.getId());
        return ResultUtils.success("签到成功");
    }

    /**
     * 查询积分
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Long> getUserById(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long totalPoints = scoreService.getUserPoints(loginUser.getId());
        return ResultUtils.success(totalPoints);
    }
}
