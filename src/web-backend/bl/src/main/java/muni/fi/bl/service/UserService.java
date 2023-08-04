package muni.fi.bl.service;

import muni.fi.dtos.UserDto;

import java.util.List;

public interface UserService {

    /**
     * Finds user based on the unique string identifier used by the JWT token and context holder authentication
     *
     * @param jwtIdentifier The unique user identifier used by JWT
     * @return The user DTO if found
     * @throws muni.fi.bl.exceptions.NotFoundException When user with identifier couldn't be found
     */
    UserDto getUser(String jwtIdentifier);

    /**
     * Retrieves all available application users
     *
     * @return List of application user DTOs
     */
    List<UserDto> getAllUsers();

    /**
     * Updates user
     *
     * @param id      The user database id
     * @param userDto The user update DTO with the update information
     * @throws muni.fi.bl.exceptions.NotFoundException When user with id doesn't exist be found
     */
    void updateUser(Long id, UserDto userDto);

    /**
     * Retrieves all admin users
     *
     * @return List of admin user DTOs
     */
    List<UserDto> getAdmins();
}
