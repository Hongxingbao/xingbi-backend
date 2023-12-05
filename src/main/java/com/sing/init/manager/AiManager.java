package com.sing.init.manager;

import com.sing.init.common.ErrorCode;
import com.sing.init.exception.ThrowUtils;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    public String dochat(Long modelId,String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        //模型id
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        //获取响应结果
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        ThrowUtils.throwIf(response == null, ErrorCode.SYSTEM_ERROR, "AI响应错误");
        return response.getData().getContent();
    }
}
