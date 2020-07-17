package com.bpbonline.mapper;

import com.bpbonline.model.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    AppUser getUserDetail(@Param("username") String username);
}
