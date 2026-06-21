package com.blank.app.mapper;

import com.blank.app.entity.VipPlan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VipPlanMapper {
    List<VipPlan> selectActiveAll();
    VipPlan selectByPlanKey(String planKey);
}
