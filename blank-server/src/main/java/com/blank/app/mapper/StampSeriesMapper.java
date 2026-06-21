package com.blank.app.mapper;

import com.blank.app.entity.StampSeries;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StampSeriesMapper {
    List<StampSeries> selectAll();
    StampSeries selectById(Integer id);
    StampSeries selectByName(String name);
    int insert(StampSeries series);
    int updateById(StampSeries series);
    int deleteById(Integer id);
}
