package com.sing.init.bimq;

import com.rabbitmq.client.Channel;
import com.sing.init.common.ErrorCode;
import com.sing.init.constant.ChartConstant;
import com.sing.init.exception.BusinessException;
import com.sing.init.exception.ThrowUtils;
import com.sing.init.manager.AiManager;
import com.sing.init.manager.BaiDuAiManager;
import com.sing.init.model.entity.Chart;
import com.sing.init.service.ChartService;
import com.sing.init.websocket.WebSocketServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;
    @Resource
    private WebSocketServer webSocketServer;

    @Resource
    private AiManager aiManager;

    @Resource
    private BaiDuAiManager baiDuAiManager;


    @Resource
    private RedissonClient redissonClient;

    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            // 如果失败，消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        String[] split = message.split("_");
        String strChartId = split[0];
        String strUserId = split[1];

        long chartId = Long.parseLong(strChartId);
        long userId = Long.parseLong(strUserId);

        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }
        // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
            return;
        }
        // 调用 AI
        //String result = aiManager.dochat(ChartConstant.YU_MODEL_ID, buildUserInput(chart), userId);
        String result = baiDuAiManager.chatSingle(ChartConstant.BAIDU_MODEL_ID, buildBaiduUserInput(chart), userId);

        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "AI 生成错误");
            return;
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus(ChartConstant.SUCCEED_STATUS);
        updateChartResult.setExecMessage("AI生成成功");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
        webSocketServer.sendToAllClient("图表生成好啦，快去看看吧！");
//        //手动清除缓存，避免拿到旧数据
//        String cacheKey = "ChartController_listMyChartVOByPage";
//        clearCacheByPattern(cacheKey);
        // 消息确认
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 构建用户输入(鱼聪明)
     *
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();

        //构建提问内容
        if (StringUtils.isNotBlank(chartType)) {
            userInput.append("分析目标：").append(goal + "请使用：").append(chartType).append("\n");
        } else {
            userInput.append("分析目标：").append(goal).append("\n");
        }
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }

    /**
     * 构建用户输入(文心一言)
     *
     * @param chart
     * @return
     */
    private Map<String, String> buildBaiduUserInput(Chart chart) {

        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "未指定分析目标");
        //要分析的csv数据
        Map map = new HashMap();
        if (StringUtils.isNotBlank(chartType)) {
            map.put("goal", goal + ",请使用：" + chartType);
        } else {
            map.put("goal", goal);
        }
        map.put("data", csvData);
        return map;
    }


    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus(ChartConstant.FAILED_STATUS);
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChartResult);
        webSocketServer.sendToAllClient("糟糕，好像出了点问题~");
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
