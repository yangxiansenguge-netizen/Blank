package com.blank.app.mapper;

import com.blank.app.entity.Like;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LikeMapper {
    Like selectByUserAndPostcard(Integer userId, Integer postcardId);
    long countByPostcardId(Integer postcardId);
    List<Integer> selectUserLikeIds(Integer userId);
    List<Integer> selectFavoritesPage(Integer userId);
    long countByUserId(Integer userId);
    int insert(Like like);
    int deleteById(Integer id);
    int deleteByUserId(Integer userId);
    int deleteByPostcardId(Integer postcardId);
}
