package com.blank.app.mapper;

import com.blank.app.entity.Postcard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PostcardMapper {
    Postcard selectById(Integer id);
    Postcard selectByIdForUpdate(Integer id);
    List<Postcard> selectDiscoverPage();
    List<Postcard> selectDriftingPage();
    List<Postcard> selectInboxPage(@Param("recipientInput") String recipientInput);
    List<Postcard> selectOutboxPage(@Param("userId") Integer userId);
    List<Postcard> selectAdminPage(@Param("keyword") String keyword,
                                    @Param("postcardType") String postcardType,
                                    @Param("status") String status);
    List<Postcard> selectScheduledToProcess();
    List<Postcard> selectPendingList();
    List<Postcard> selectByUserId(Integer userId);
    long countAll();
    long countDrifting();
    long countByUserIdAndToday(@Param("userId") Integer userId,
                                @Param("startOfDay") LocalDateTime startOfDay,
                                @Param("endOfDay") LocalDateTime endOfDay);
    int insert(Postcard postcard);
    int updateById(Postcard postcard);
    int deleteById(Integer id);
    int deleteByUserId(Integer userId);
}
