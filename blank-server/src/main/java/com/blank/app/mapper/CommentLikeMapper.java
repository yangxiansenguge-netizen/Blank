package com.blank.app.mapper;

import com.blank.app.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentLikeMapper {
    CommentLike selectByUserAndComment(Integer userId, Integer commentId);
    List<Integer> selectLikedIds(Integer userId);
    int insert(CommentLike commentLike);
    int deleteById(Integer id);
    int deleteByCommentId(Integer commentId);
    int deleteByUserId(Integer userId);
}
