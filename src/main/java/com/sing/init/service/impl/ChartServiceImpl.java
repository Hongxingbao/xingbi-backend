package com.sing.init.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sing.init.bimq.BiMessageProducer;
import com.sing.init.common.BiResponse;
import com.sing.init.common.ErrorCode;
import com.sing.init.constant.ChartConstant;
import com.sing.init.exception.BusinessException;
import com.sing.init.exception.ThrowUtils;
import com.sing.init.manager.AiManager;
import com.sing.init.manager.BaiDuAiManager;
import com.sing.init.manager.RedisLimiterManager;
import com.sing.init.mapper.ChartMapper;
import com.sing.init.model.dto.chart.GenChartByAiRequest;
import com.sing.init.model.entity.Chart;
import com.sing.init.model.entity.User;
import com.sing.init.service.ChartService;
import com.sing.init.service.UserService;
import com.sing.init.utils.ClearCache;
import com.sing.init.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 24840
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2023-12-01 15:03:53
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    @Resource
    private UserService userService;
    @Resource
    private AiManager aiManager;
    @Resource
    private BaiDuAiManager baiDuAiManager;
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;
    @Resource
    private ClearCache cache;

    @Override
    public void validChart(Chart chart, boolean add) {
        if (chart == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = chart.getId();
        String goal = chart.getGoal();
        String chartName = chart.getChartName();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(goal, chartName), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(goal) && goal.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标描述过长");
        }
        if (StringUtils.isNotBlank(chartName) && chartName.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }

    @Override
    public BiResponse analysisBySynchronize(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        //对文件大小和类型进行限制
        fileCheck(multipartFile);
        User loginUser = userService.getLoginUser(request);
        // 添加限流器,每秒最多允许两个请求通过
        redisLimiterManager.doRateLimit("analysisBySynchronize" + loginUser.getId());

        String goal = genChartByAiRequest.getGoal();
        String chartName = genChartByAiRequest.getChartName();
        String chartType = genChartByAiRequest.getChartType();

        //要分析的csv数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        //String userInput = buildUserInput(goal, chartType, multipartFile, chartName);
        Map userInput = buildBaiduUserInput(goal, chartType, multipartFile, chartName);

        //返回结果
        //String result = aiManager.dochat(ChartConstant.YU_MODEL_ID, userInput.toString(), loginUser.getId());//鱼聪明API
        String result = baiDuAiManager.chatSingle(ChartConstant.BAIDU_MODEL_ID, userInput, loginUser.getId());//文心一言API

        String[] split = result.split("【【【【【");
        ThrowUtils.throwIf(split.length < 3, ErrorCode.SYSTEM_ERROR, "生成内容出错了");
        //图表信息
        String genChartData = split[1].trim();
        //分析结论
        String genChartResult = split[2].trim();

        //对ai生成的代码解析成json对象返回给前端
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartName(chartName);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChartData);
        chart.setGenResult(genChartResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus(ChartConstant.SUCCEED_STATUS);
        //保存到数据库中
        boolean saverResult = this.save(chart);
        ThrowUtils.throwIf(!saverResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        JSON genChartDataJson = JSONUtil.parse(genChartData);
        BiResponse biResponse = new BiResponse();
        ThrowUtils.throwIf(StringUtils.isBlank(genChartData), ErrorCode.SYSTEM_ERROR, "图表代码生成错误");

        biResponse.setGenChart(genChartDataJson);
        biResponse.setGenResult(genChartResult);
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    @Override
    public BiResponse analysisByAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        //对文件大小和类型进行限制
        fileCheck(multipartFile);
        User loginUser = userService.getLoginUser(request);
        // 添加限流器,每秒最多允许两个请求通过
        redisLimiterManager.doRateLimit("analysisBySynchronize" + loginUser.getId());

        String goal = genChartByAiRequest.getGoal();
        String chartName = genChartByAiRequest.getChartName();
        String chartType = genChartByAiRequest.getChartType();

        //要分析的csv数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        //String userInput = buildUserInput(goal, chartType, multipartFile, chartName);
        Map userInput = buildBaiduUserInput(goal, chartType, multipartFile, chartName);

        //对ai生成的代码解析成json对象返回给前端
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartName(chartName);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        // chart.setGenChart(genChartData);
        // chart.setGenResult(genChartResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus(ChartConstant.WAIT_STATUS);
        //保存到数据库中
        boolean saverResult = this.save(chart);
        ThrowUtils.throwIf(!saverResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        //手动清除缓存，避免拿到旧数据
        String cacheKey = "ChartController_listMyChartVOByPage";
        cache.clearCacheByPattern(cacheKey);
        CompletableFuture.runAsync(() -> {
            Chart updateChartRunning = new Chart();
            updateChartRunning.setId(chart.getId());
            updateChartRunning.setStatus(ChartConstant.RUNNING_STATUS);
            boolean chartResult = this.updateById(updateChartRunning);
            if (!chartResult) {
                updateChartError(chart.getId(), "更新图表状态为执行中出错了");
            }

            //调用AI
            //String result = aiManager.dochat(ChartConstant.YU_MODEL_ID, userInput.toString(), loginUser.getId());//鱼聪明API
            String result = baiDuAiManager.chatSingle(ChartConstant.BAIDU_MODEL_ID, userInput, loginUser.getId());//文心一言API
            String[] split = result.split("【【【【【");
            ThrowUtils.throwIf(split.length < 3, ErrorCode.SYSTEM_ERROR, "生成内容出错了");
            //图表信息
            String genChartData = split[1].trim();
            //分析结论
            String genChartResult = split[2].trim();
            Chart updateChartSucceed = new Chart();
            updateChartSucceed.setId(chart.getId());
            updateChartSucceed.setGenChart(genChartData);
            updateChartSucceed.setGenResult(genChartResult);
            updateChartSucceed.setStatus(ChartConstant.SUCCEED_STATUS);
            boolean updateResult = this.updateById(updateChartSucceed);
            if (!updateResult) {
                updateChartError(chart.getId(), "更新图表状态为成功出错了");
            }
        }, threadPoolExecutor);
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());

        return biResponse;
    }

    @Override
    public BiResponse analysisByAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //对文件大小和类型进行限制
        fileCheck(multipartFile);

        String goal = genChartByAiRequest.getGoal();
        String chartName = genChartByAiRequest.getChartName();
        String chartType = genChartByAiRequest.getChartType();

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "未指定分析目标");
        ThrowUtils.throwIf(StringUtils.isBlank(chartName) && chartName.length() > 30, ErrorCode.PARAMS_ERROR, "图表名称过长");
        //要分析的csv数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);

        User loginUser = userService.getLoginUser(request);
        // 添加限流器,每秒最多允许两个请求通过
        redisLimiterManager.doRateLimit("analysisBySynchronize" + loginUser.getId());

        // 插入到数据库
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus(ChartConstant.WAIT_STATUS);
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        long newChartId = chart.getId();
        //手动清除缓存，避免拿到旧数据
        String cacheKey = "ChartController_listMyChartVOByPage";
        cache.clearCacheByPattern(cacheKey);
        //将消息传给生产者,由消费端进行处理
        biMessageProducer.sendMessage(String.valueOf(newChartId) + "_" + loginUser.getId());
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(newChartId);
        return biResponse;
    }

    /**
     * 构建用户输入（文心一言）
     *
     * @param goal
     * @param chartType
     * @param multipartFile
     * @param chartName
     * @return
     */
    private Map<String,String> buildBaiduUserInput(String goal, String chartType, MultipartFile multipartFile, String chartName) {

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "未指定分析目标");
        ThrowUtils.throwIf(StringUtils.isBlank(chartName) && chartName.length() > 30, ErrorCode.PARAMS_ERROR, "图表名称过长");

        //要分析的csv数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        Map map = new HashMap();
        if (StringUtils.isNotBlank(chartType)) {
            map.put("goal",goal+",请使用："+chartType);
        } else {
            map.put("goal",goal);
        }
        map.put("data",csvData);
        return map;
    }

    private static void fileCheck(MultipartFile multipartFile) {
        // 文件名
        String fileName = multipartFile.getOriginalFilename();
        // 文件大小
        long size = multipartFile.getSize();
        final long longSize = 1024 * 1024 * 2L;

        ThrowUtils.throwIf(size > longSize, ErrorCode.PARAMS_ERROR, "文件大小超过2M！");
        final List validFileSuffix = Arrays.asList("xls", "xlsx");
        // 文件后缀名
        String suffix = FileUtil.getSuffix(fileName);
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀不合法！");
    }

    private void updateChartError(long chartId, String message) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus(ChartConstant.FAILED_STATUS);
        chart.setExecMessage(message);
        boolean isSave = this.updateById(chart);
        ThrowUtils.throwIf(!isSave, ErrorCode.SYSTEM_ERROR, "更新图表状态为失败出错了！");
    }
}




