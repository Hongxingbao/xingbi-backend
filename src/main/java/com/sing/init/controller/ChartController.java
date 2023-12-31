package com.sing.init.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sing.init.annotation.AuthCheck;
import com.sing.init.bimq.BiMessageProducer;
import com.sing.init.common.*;
import com.sing.init.constant.CommonConstant;
import com.sing.init.constant.UserConstant;
import com.sing.init.exception.BusinessException;
import com.sing.init.exception.ThrowUtils;
import com.sing.init.model.dto.chart.*;
import com.sing.init.model.entity.Chart;
import com.sing.init.model.entity.User;
import com.sing.init.service.ChartService;
import com.sing.init.service.UserService;
import com.sing.init.utils.ClearCache;
import com.sing.init.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.MapOptions;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 图表信息接口
 *
 * @author xing
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    private ClearCache cache;

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        // todo 参数校验
        chartService.validChart(chart, true);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldchart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        //手动清除缓存，避免拿到旧数据
        String cacheKey = "ChartController_listMyChartVOByPage";
        cache.clearCacheByPattern(cacheKey);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        // todo 参数校验
        chartService.validChart(chart, false);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        //手动清除缓存，避免拿到旧数据
        String cacheKey = "ChartController_listMyChartVOByPage";
        cache.clearCacheByPattern(cacheKey);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前图表的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/")
    public BaseResponse<Page<Chart>> listMyChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                         HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = new Page<Chart>();
        // 检查缓存,保证每次点击缓存的数据不一样，否则会导致点击不同页数返回的数据相同
        String cacheKey = "ChartController_listMyChartVOByPage" + current + size;
        RMap<String, Object> cachedResult = getCachedResult(cacheKey);
        if (cachedResult.size() > 0 && chartQueryRequest.getChartName()==null) {
            chartPage = (Page<Chart>) cachedResult.get(cacheKey);
            return ResultUtils.success(chartPage);
        }
        // 如果缓存中没有结果，则查询数据库
        chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        // 将查询结果放入缓存
        putCachedResult(cacheKey, chartPage);
        return ResultUtils.success(chartPage);

    }

    /**
     * 编辑（图表）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editchart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        // todo 参数校验
        chartService.validChart(chart, false);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldchart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        //手动清除缓存，避免拿到旧数据
        String cacheKey = "ChartController_listMyChartVOByPage";
        cache.clearCacheByPattern(cacheKey);
        return ResultUtils.success(result);
    }

    /**
     * 对表内容进行智能分析，可以实时查看分析结论（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> analysisBySynchronize(@RequestPart("file") MultipartFile multipartFile,
                                                          GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        BiResponse biResponse = chartService.analysisBySynchronize(multipartFile,genChartByAiRequest,request);
        return ResultUtils.success(biResponse);
    }

    /**
     * 对表内容进行智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> analysisByAsync(@RequestPart("file") MultipartFile multipartFile,
                                                    GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        BiResponse biResponse = chartService.analysisByAsync(multipartFile,genChartByAiRequest,request);
        return ResultUtils.success(biResponse);
    }

    /**
     * 对表内容进行智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> analysisByAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        BiResponse biResponse = chartService.analysisByAsyncMq(multipartFile,genChartByAiRequest,request);
        return ResultUtils.success(biResponse);
    }

    /**
     * 生成失败重试(此处通过发送消息到MQ的方式)
     * @param chartId
     * @return
     */
    @GetMapping("/retry")
    public BaseResponse<BiResponse> retry(long chartId) {

        Chart chart = chartService.getById(chartId);
        //将消息传给生产者,由消费端进行处理
        biMessageProducer.sendMessage(String.valueOf(chartId) + "_" + chart.getUserId());
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chartId);
        return ResultUtils.success(biResponse);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String chartName = chartQueryRequest.getChartName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(chartName), "chartName", chartName);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    private RMap<String, Object> getCachedResult(String cacheKey) {
        RMap<String, Object> cache = redissonClient.getMap(cacheKey);
        return cache;
    }

    private void putCachedResult(String cacheKey, Page<Chart> chartPage) {
        //使用Redisson提供的getMap方法从RedissonClient中获取一个RMap实例。
        RMap<String, Object> cache = redissonClient.getMap(cacheKey, MapOptions.defaults());
        cache.put(cacheKey, chartPage);
        //60秒清空一次缓存
        cache.expire(60L, TimeUnit.SECONDS);
    }
}
