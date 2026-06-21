package com.blank.app.mapper;

import com.blank.app.entity.Stamp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StampMapper {
    Stamp selectById(Integer id);
    Stamp selectByIdForUpdate(Integer id);
    List<Stamp> selectAll();
    List<Stamp> selectBySeriesId(String seriesId);
    List<Stamp> selectByKeyword(@Param("keyword") String keyword, @Param("seriesId") String seriesId);
    long countAll();
    long countBySeriesId(String seriesId);
    int insert(Stamp stamp);
    int updateStampsSeriesName(@Param("oldName") String oldName, @Param("newName") String newName);
    int updateById(Stamp stamp);
    int deleteById(Integer id);
}
