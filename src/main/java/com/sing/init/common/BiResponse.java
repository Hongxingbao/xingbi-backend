package com.sing.init.common;

import cn.hutool.json.JSON;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 *
 * @param <T>
 * @author xing

 */
@Data
public class BiResponse<T> implements Serializable {

    /**
     * 图表数据
     */
    private JSON genChart;

    /**
     * 分析结论
     */
    private String genResult;

    /**
     * 图表id
     */
    private Long chartId;


    private static final long serialVersionUID = 1L;


}
