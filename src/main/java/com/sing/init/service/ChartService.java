package com.sing.init.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sing.init.model.entity.Chart;


/**
* @author 24840
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-12-01 15:03:53
*/
public interface ChartService extends IService<Chart> {

    /**
     * 校验
     *
     * @param post
     * @param add
     */
    void validChart(Chart chart, boolean add);

}
