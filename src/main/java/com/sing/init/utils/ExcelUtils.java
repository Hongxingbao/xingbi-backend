package com.sing.init.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.sing.init.common.BaseResponse;
import com.sing.init.model.dto.chart.GenChartByAiRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@Slf4j
public class ExcelUtils {
    /**
     * 将Excel文件转换为CSV数据
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile){
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误");
            throw new RuntimeException(e);
        }
        if(CollUtil.isEmpty(list)){
            return "";
        }
        //读取表头第一行数据
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
        List<String> headerList = headerMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(StringUtils.join(headerList,",")).append("\n");

        //读取表数据
        for (int i = 1; i <list.size() ; i++) {
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataList,",")).append("\n");;
        }
      return stringBuilder.toString();
    }
}