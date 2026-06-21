package com.blank.app.mapper;

import com.blank.app.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectByPostcardId(Integer postcardId);
    Comment selectById(Integer id);
    int insert(Comment comment);
    int updateById(Comment comment);
    int deleteById(Integer id);
    int deleteByPostcardId(Integer postcardId);
    int deleteByUserId(Integer userId);
}
