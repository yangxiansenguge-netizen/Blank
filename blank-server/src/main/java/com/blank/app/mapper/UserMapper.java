package com.blank.app.mapper;

import com.blank.app.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectById(Integer id);
    User selectByEmail(String email);
    User selectByUid(String uid);
    long countByEmail(String email);
    long countByUsername(String username);
    long countByUid(String uid);
    long countByUsernameExcludeSelf(String username, Integer excludeId);
    long countByIdentity(String identity);
    long countAll();
    java.util.List<User> searchByKeyword(String keyword, Integer excludeId);
    int insert(User user);
    int updateById(User user);
    int deleteById(Integer id);
}
