package com.sing.init.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sing.init.common.ErrorCode;
import com.sing.init.exception.BusinessException;
import com.sing.init.exception.ThrowUtils;
import com.sing.init.mapper.ChartMapper;
import com.sing.init.model.entity.Chart;
import com.sing.init.service.ChartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 24840
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-12-01 15:03:53
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

    @Override
    public void validChart(Chart chart, boolean add) {
        if (chart == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = chart.getId();
        String goal = chart.getGoal();
        String chartName = chart.getChartName();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(goal, chartName), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(goal) && goal.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标描述过长");
        }
        if (StringUtils.isNotBlank(chartName) && chartName.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }

}




