package com.sing.init.controller;

import com.gearwenxin.client.PromptBotClient;
import com.gearwenxin.client.ernie.ErnieBot4Client;
import com.gearwenxin.client.stable.StableDiffusionXLClient;
import com.gearwenxin.entity.chatmodel.ChatPromptRequest;
import com.gearwenxin.entity.enums.SamplerType;
import com.gearwenxin.entity.request.ImageBaseRequest;
import com.gearwenxin.entity.response.ChatResponse;
import com.gearwenxin.entity.response.ImageResponse;
import com.gearwenxin.entity.response.PromptResponse;
import com.sing.init.common.BaseResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatController { // 要调用的模型的客户端（示例为文心4.0）
    @Resource
    private ErnieBot4Client ernieBot4Client;
    @Resource
    private StableDiffusionXLClient stableDiffusionXLClient;
    // 要调用的模型的客户端
    @Resource
    private PromptBotClient promptClient;


    // 模板对话
    @PostMapping
    public Mono<PromptResponse> chatSingle(int id) {

        Map<String, String> map = new HashMap<>();
        map.put("goal", "分析网站用户增长情况");
        map.put("data", "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,80");
        ChatPromptRequest promptRequest = new ChatPromptRequest();
        promptRequest.setId(id);
        promptRequest.setParamMap(map);

        return promptClient.chatPrompt(promptRequest);
    }


    // 单次对话
    @PostMapping("/chat")
    public Mono<ChatResponse> chatSingle(String msg) {
       // msg = "";
        return ernieBot4Client.chatSingle(msg);
    }

    // 连续对话
    @PostMapping("/chats")
    public Mono<ChatResponse> chatCont(String msg) {
        String chatUID = "test-user-1001";
        return ernieBot4Client.chatCont(msg, chatUID);
    }

    // 流式返回，单次对话
    @GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatSingleStream(@RequestParam String msg) {
        return ernieBot4Client.chatSingleOfStream(msg)
                .map(response -> "data: " + response.getResult() + "\n\n");
    }

    // 流式返回，连续对话
    @GetMapping(value = "/stream/chats", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatContStream(@RequestParam String msg, @RequestParam String msgUid) {
        return ernieBot4Client.chatContOfStream(msg, msgUid)
                .map(response -> "data: " + response.getResult() + "\n\n");
    }


     // 模板对话
    @PostMapping("/prompt")
    public BaseResponse<String> chatSingle() {
        return null;
//        Map<String, String> map = new HashMap<>();
//        map.put("goal", "请分析下该表运营情况，以及如何改进,请使用：折线图");
//        map.put("data", "运营数据报表\n" +
//                "概览数据\n" +
//                "营业额,¥3,445.00,订单完成率,100.00%,新增用户数,108\n" +
//                "有效订单,414,平均客单价,¥21.00\n" +
//                "明细数据\n" +
//                "日期,营业额,有效订单,订单完成率,平均客单价,新增用户数\n" +
//                "2023/12/18,¥259.00,25,100.00%,¥15.00,5\n" +
//                "2023/12/19,¥260.00,33,100.00%,¥16.00,6\n" +
//                "2023/12/20,¥261.00,34,100.00%,¥17.00,7\n" +
//                "2023/12/21,¥262.00,44,100.00%,¥18.00,5\n" +
//                "2023/12/22,¥263.00,36,100.00%,¥19.00,9\n" +
//                "2023/12/23,¥264.00,37,100.00%,¥20.00,5\n" +
//                "2023/12/24,¥265.00,25,100.00%,¥21.00,11\n" +
//                "2023/12/25,¥266.00,39,100.00%,¥22.00,12\n" +
//                "2023/12/26,¥267.00,15,100.00%,¥23.00,13\n" +
//                "2023/12/27,¥268.00,34,100.00%,¥24.00,4\n" +
//                "2023/12/28,¥269.00,42,100.00%,¥25.00,15\n" +
//                "2023/12/29,¥270.00,14,100.00%,¥26.00,4\n" +
//                "2023/12/30,¥271.00,36,100.00%,¥27.00,12");
//        ChatPromptRequest promptRequest = new ChatPromptRequest();
//        //promptRequest.setId(13996);
//        promptRequest.setId(14007);
//        promptRequest.setParamMap(map);
//        PromptResponse promptResponse = promptClient.chatPrompt(promptRequest).block();
//        String content = promptResponse.getResult().getContent();//获取 将变量插值填充到模板原始内容后得到的模板内容
//        ChatResponse c = ernieBot4Client.chatSingle(content).block(); //此处得到的是没有系统人设的回答
//        System.out.println(c.getResult());
//        String cleanedJsonString = removeCodeBlocks(c.getResult());
//        System.out.println(cleanedJsonString);
//        return ResultUtils.success(c.getResult());
    }

    private static String removeCodeBlocks(String jsonString) {
        return jsonString.replaceAll("```json", "】】】】】").replaceAll("```", "】】】】】").trim();
    }

    // 文生图
    @PostMapping("/image")
    public Mono<ImageResponse> chatImage() {
        ImageBaseRequest imageBaseRequest = ImageBaseRequest.builder()
                // 提示词
                .prompt("一个头发中分并且穿着背带裤的人")
                // 大小
                .size("1024x1024")
                // 反向提示词（不包含什么）
                .negativePrompt("鸡")
                // 生成图片数量（1-4）
                .n(1)
                // 迭代轮次（10-50）
                .steps(20)
                // 采样方式
                .samplerIndex(SamplerType.Euler_A.getValue())
                .userId("1001")
                .build();

        return stableDiffusionXLClient.chatImage(imageBaseRequest);
    }
}