package com.sing.init.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sing.init.common.BiResponse;
import com.sing.init.model.dto.chart.GenChartByAiRequest;
import com.sing.init.model.entity.Chart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


/**
* @author 24840
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-12-01 15:03:53
*/
public interface ChartService extends IService<Chart> {

    /**
     * 参数校验
     * @param chart
     * @param add
     */
    void validChart(Chart chart, boolean add);

    /**
     * 对表内容进行智能分析，可以实时查看分析结论（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    BiResponse analysisBySynchronize(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    /**
     * 对表内容进行智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    BiResponse analysisByAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    /**
     * 对表内容进行智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    BiResponse analysisByAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);
}
