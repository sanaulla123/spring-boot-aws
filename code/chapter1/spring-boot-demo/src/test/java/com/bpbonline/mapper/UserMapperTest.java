package com.bpbonline.mapper;

import com.bpbonline.model.AppUser;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@MybatisTest
public class UserMapperTest {
    @Autowired UserMapper userMapper;

    @Test
    public void test_getUserDetail(){
        AppUser userDetail = userMapper.getUserDetail("mohamed");
        assertThat(userDetail).isNotNull();
        assertThat(userDetail.getUsername()).isEqualTo("mohamed");

        userDetail = userMapper.getUserDetail("non_existent");
        assertThat(userDetail).isNull();;
    }
}
