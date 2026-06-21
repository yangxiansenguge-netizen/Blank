package com.blank.app.mapper;

import com.blank.app.entity.VipOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VipOrderMapper {
    VipOrder selectByOrderNo(String orderNo);
    VipOrder selectLatestByUserId(Integer userId);
    VipOrder selectByOrderNoAndUser(String orderNo, Integer userId);
    int insert(VipOrder order);
    int updateById(VipOrder order);
}
