package com.mapr.music.service;

import com.mapr.music.model.User;

import java.security.Principal;

public interface UserService {

    User register(User user);

    User getUserByUsername(String username);

    User getUserByPrincipal(Principal principal);
}
