package muni.fi.bl.service.impl;

import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.exceptions.NotFoundException;
import muni.fi.bl.mappers.UserMapper;
import muni.fi.bl.service.UserService;
import muni.fi.dal.entity.User;
import muni.fi.dal.repository.UserRepository;
import muni.fi.dtos.UserDto;
import muni.fi.enums.Role;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto getUser(String jwtIdentifier) {
        Optional<User> user = userRepository.findByJwtIdentifier(jwtIdentifier);
        if (user.isEmpty()) {
            String message = String.format("User with identifier '%s' not found", jwtIdentifier);
            log.info(message);
            throw new NotFoundException(message);
        }
        return userMapper.toDto(user.get());
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userMapper.toDtos(userRepository.findAll());
    }

    @Override
    public void updateUser(Long id, UserDto userDto) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            String message = String.format("User with id %d not found", id);
            log.warn(message);
            throw new NotFoundException(message);
        }
        userRepository.save(userMapper.toEntity(userDto));
    }

    @Override
    public List<UserDto> getAdmins() {
        return userMapper.toDtos(userRepository.findAllByRolesName(Role.ROLE_ADMIN.name()));
    }
}
