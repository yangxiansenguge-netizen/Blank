package com.blank.app.mapper;

import com.blank.app.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendMapper {
    List<Friend> selectFriends(Integer userId);
    List<Friend> selectPendingRequests(Integer userId);
    Friend selectById(Integer id);
    long countExisting(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
    long checkIsFriend(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
    int insert(Friend friend);
    int updateStatus(@Param("id") Integer id, @Param("status") String status);
    int deleteById(Integer id);
    int deleteByUserId(Integer userId);
}
