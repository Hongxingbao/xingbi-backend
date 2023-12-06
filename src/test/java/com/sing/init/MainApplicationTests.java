package com.sing.init;

import javax.annotation.Resource;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * 主类测试
 *
 * @author xing

 */
@SpringBootTest
class MainApplicationTests {


    @Test
    public void doImport() {
        JSON parse = JSONUtil.parse("{  \n" +
                "title: {\n" +
                "    text: '网站用户增长情况',\n" +
                "    subtext: ''\n" +
                "  },\n" +
                "  tooltip: {\n" +
                "    trigger: 'axis',\n" +
                "    axisPointer: {\n" +
                "      type: 'shadow'\n" +
                "    }\n" +
                "  },\n" +
                "  legend: {\n" +
                "    data: ['用户数']\n" +
                "  },\n" +
                "  xAxis: {\n" +
                "    type: 'category',\n" +
                "    data: ['1号', '2号', '3号']\n" +
                "  },\n" +
                "  yAxis: {\n" +
                "    type: 'value'\n" +
                "  },\n" +
                "  series: [\n" +
                "    {\n" +
                "      name:'用户数',\n" +
                "      data: [10, 20, 30],\n" +
                "      type: 'bar'\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        System.out.println(parse);

    }


}
