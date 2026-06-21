package com.blank.app.mapper;

import com.blank.app.entity.VipActivationCode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VipActivationCodeMapper {
    VipActivationCode selectByCode(String code);
    List<VipActivationCode> selectAll();
    long countAll();
    long countUnused();
    int insert(VipActivationCode code);
    int updateById(VipActivationCode code);
    int deleteById(Integer id);
}
