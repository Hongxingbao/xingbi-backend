package com.sing.init.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void dochat() {
        long modelId = 1731965085540634626L;
//        String message = aiManager.dochat(modelId,"分析需求：\n" +
//                "分析网站用户增长情况\n" +
//                "原始数据：\n" +
//                "日期,用户数\n" +
//                "1号,10\n" +
//                "2号,20\n" +
//                "3号,30");

       // System.out.println(message);

    }
}