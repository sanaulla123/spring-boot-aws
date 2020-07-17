package com.bpbonline.model;

import lombok.Data;

@Data
public class AppUser {
    private String username;
    private String password;
    private String email;
    private String name;
}
