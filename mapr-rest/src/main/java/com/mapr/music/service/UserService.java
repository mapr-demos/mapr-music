package com.mapr.music.service;


import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dto.UserDto;
import com.mapr.music.exception.ResourceNotFoundException;
import com.mapr.music.exception.ValidationException;
import com.mapr.music.model.User;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.stream.Stream;

public class UserService {

    private static final String addUserUtility;
    private static final String appUsersPropertiesPath;

    static {
        String jbossHome = System.getProperty("jboss.home.dir");
        addUserUtility = jbossHome + "/bin/add-user.sh";
        appUsersPropertiesPath = jbossHome + "/standalone/configuration/application-users.properties";
    }

    private final MaprDbDao<User> userDao;

    @Inject
    public UserService(@Named("userDao") MaprDbDao<User> userDao) {
        this.userDao = userDao;
    }

    /**
     * Registers User according to the specified User Data Transfer Object.
     *
     * @param user User Data Transfer Object, which contains registration data.
     * @return User Data Transfer Object, which contains registered user info.
     */
    public UserDto register(UserDto user) {

        if (user == null) {
            throw new IllegalArgumentException("User can not be null");
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new ValidationException("User name can not be empty");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new ValidationException("User password can not be empty");
        }

        if (!isUsernameAvailable(user.getUsername())) {
            throw new ValidationException("User name is not available");
        }

        try {

            String username = user.getUsername();
            String password = user.getPassword();
            String groups = "user, admin";
            Runtime.getRuntime().exec(new String[]{addUserUtility, "-a", "-u", username, "-p", password, "-g", groups});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        User createdUser = userDao.create(dtoToUser(user));
        return userToDto(createdUser);
    }

    /**
     * Returns User by specified username.
     *
     * @param username user's username.
     * @return User with specified username.
     */
    public UserDto getUserByUsername(String username) {

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username can not be empty");
        }

        User user = userDao.getById(username);
        if (user == null) {
            throw new ResourceNotFoundException("User with username '" + username + "' not found");
        }

        return userToDto(user);
    }

    /**
     * Returns User by specified principal.
     *
     * @param principal user's principal.
     * @return User which corresponds to the specified principal.
     */
    public UserDto getUserByPrincipal(Principal principal) {

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

    private UserDto userToDto(User user) {

        UserDto userDto = new UserDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(userDto, user);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create user Data Transfer Object", e);
        }

        userDto.setUsername(user.getId());

        return userDto;
    }

    private User dtoToUser(UserDto userDto) {

        User user = new User();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(user, userDto);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create user from Data Transfer Object", e);
        }

        user.setId(userDto.getUsername());

        return user;
    }
}
