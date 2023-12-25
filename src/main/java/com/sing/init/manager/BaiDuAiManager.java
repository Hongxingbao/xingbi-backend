package com.sing.init.manager;

import com.gearwenxin.client.PromptBotClient;
import com.gearwenxin.client.ernie.ErnieBot4Client;
import com.gearwenxin.entity.chatmodel.ChatPromptRequest;
import com.gearwenxin.entity.response.ChatResponse;
import com.gearwenxin.entity.response.PromptResponse;
import com.sing.init.common.ErrorCode;
import com.sing.init.exception.BusinessException;
import com.sing.init.service.ScoreService;
import com.sing.init.utils.ClearCache;
import com.sing.init.websocket.WebSocketServer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class BaiDuAiManager {

    @Resource
    private ScoreService scoreService;

    @Resource
    private ClearCache cache;

    @Resource
    private ErnieBot4Client ernieBot4Client;

    // 要调用的模型的客户端
    @Resource
    private PromptBotClient promptClient;

    @Resource
    private WebSocketServer webSocketServer;

    public String chatSingle(Integer modelId,Map<String,String> map,Long userId) {

        ChatPromptRequest promptRequest = new ChatPromptRequest();
        promptRequest.setId(14007);//直接传modelId可能会出问题？？？
        promptRequest.setParamMap(map);

        PromptResponse promptResponse = promptClient.chatPrompt(promptRequest).block();
        String content = promptResponse.getResult().getContent();//获取 将变量插值填充到模板原始内容后得到的模板内容
        ChatResponse chatResponse = ernieBot4Client.chatSingle(content).block();
        if(chatResponse == null){
            webSocketServer.sendToAllClient("糟糕，好像出了点问题~");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI响应错误");
        }
        String cleanedJsonString = removeCodeBlocks(chatResponse.getResult());
        System.out.println("成功结果："+cleanedJsonString);
        scoreService.deductPoints(userId, 20L);//调用成功后扣除积分
        //手动清除缓存，避免拿到旧数据
        String cacheKey = "ChartController_listMyChartVOByPage";
        cache.clearCacheByPattern(cacheKey);
        return cleanedJsonString;
    }

    private static String removeCodeBlocks(String jsonString) {
        return jsonString.replaceAll("```json", "【【【【【").replaceAll("```", "【【【【【").trim();
    }
}
