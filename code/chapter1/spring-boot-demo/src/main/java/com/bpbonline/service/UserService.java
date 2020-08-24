package com.bpbonline.service;

import com.bpbonline.mapper.UserMapper;
import com.bpbonline.model.AppUser;
import com.bpbonline.model.AuthenticationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser userObj = userMapper.getUserDetail(username);
        if ( userObj == null ) { throw new UsernameNotFoundException("user does not exist"); }

        AuthenticationUser authenticationUser = new AuthenticationUser(userObj);
        return authenticationUser;
    }
}
