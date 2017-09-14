package com.mapr.music.service.impl;


import com.mapr.music.model.User;
import com.mapr.music.service.UserService;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {

    private static final String addUserUtility;
    private static final String appUsersPropertiesPath;

    static {
        String jbossHome = System.getProperty("jboss.home.dir");
        addUserUtility = jbossHome + "/bin/add-user.sh";
        appUsersPropertiesPath = jbossHome + "/standalone/configuration/application-users.properties";
    }

    @Override
    public User register(User user) {

        if (user == null) {
            throw new IllegalArgumentException("User can not be null");
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new BadRequestException("User name can not be empty");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BadRequestException("User password can not be empty");
        }

        if (!isUsernameAvailable(user.getUsername())) {
            throw new BadRequestException("User name is not available");
        }

        try {

            String username = user.getUsername();
            String password = user.getPassword();
            String groups = "user, admin";
            Runtime.getRuntime().exec(new String[]{addUserUtility, "-a", "-u", username, "-p", password, "-g", groups});

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    @Override
    public User getUserByUsername(String username) {

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username can not be empty");
        }

        // TODO change it as soon as User Profile feature will be implemented
        return new User(username, null);
    }

    @Override
    public User getUserByPrincipal(Principal principal) {

        if (principal == null) {
            throw new IllegalArgumentException("User principal can not be null");
        }

        return getUserByUsername(principal.getName());
    }

    private boolean isUsernameAvailable(String username) {

        boolean available = false;
        try (Stream<String> stream = Files.lines(Paths.get(appUsersPropertiesPath))) {

            available = stream.filter(string -> !string.startsWith("#")) // filter comments
                    .map(usernameHexPairStr -> { // map to username
                        String[] usernameHexPair = usernameHexPairStr.split("=");
                        return (usernameHexPair.length > 0) ? usernameHexPair[0] : usernameHexPairStr;
                    })
                    .noneMatch(username::equals);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return available;
    }
}
