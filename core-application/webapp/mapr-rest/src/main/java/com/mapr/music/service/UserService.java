package com.mapr.music.service;

import com.mapr.music.dto.UserDto;

import java.security.Principal;

public interface UserService {

    UserDto register(UserDto user);

    UserDto getUserByUsername(String username);

    UserDto getUserByPrincipal(Principal principal);
}
