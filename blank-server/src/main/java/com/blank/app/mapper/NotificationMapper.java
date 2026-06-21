package com.blank.app.mapper;

import com.blank.app.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationMapper {
    Notification selectById(Integer id);
    List<Notification> selectByUserId(Integer userId);
    long countUnread(Integer userId);
    int insert(Notification notification);
    int updateById(Notification notification);
    int markAllRead(Integer userId);
}
