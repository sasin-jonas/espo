package muni.fi.bl.service.impl;

import muni.fi.bl.exceptions.NotFoundException;
import muni.fi.bl.mappers.UserMapper;
import muni.fi.bl.service.UserService;
import muni.fi.dal.entity.User;
import muni.fi.dal.repository.UserRepository;
import muni.fi.dtos.UserDto;
import muni.fi.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    // tested class
    private UserService userService;

    @BeforeEach
    void setUp() {
        openMocks(this);

        userService = new UserServiceImpl(userRepository, userMapper);
    }

    @Test
    void getUser() {
        // prepare
        User entity = new User();
        when(userRepository.findByJwtIdentifier(eq("123456"))).thenReturn(Optional.of(entity));

        // tested method
        userService.getUser("123456");

        // verify
        verify(userRepository).findByJwtIdentifier(eq("123456"));
        verify(userMapper).toDto(entity);
    }

    @Test
    void getUserNotFound() {
        // prepare
        when(userRepository.findByJwtIdentifier(eq("123456"))).thenReturn(Optional.empty());

        // tested method
        Throwable exception = assertThrows(NotFoundException.class, () -> userService.getUser("123456"));

        // verify
        assertThat(exception.getMessage(), equalTo("User with identifier '123456' not found"));
    }

    @Test
    void getAllUsers() {
        // prepare
        List<User> users = List.of(new User());
        when(userRepository.findAll()).thenReturn(users);

        // tested method
        userService.getAllUsers();

        // verify
        verify(userRepository).findAll();
        verify(userMapper).toDtos(users);
    }

    @Test
    void updateUser() {
        // prepare
        User entity = new User();
        UserDto dto = new UserDto();
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(entity));

        // tested method
        userService.updateUser(1L, dto);

        // verify
        verify(userRepository).findById(1L);
        verify(userRepository).save(any());
        verify(userMapper).toEntity(dto);
    }

    @Test
    void updateNotFound() {
        // prepare
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // tested method
        Throwable exception = assertThrows(NotFoundException.class, () -> userService.updateUser(1L, new UserDto()));

        // verify
        assertThat(exception.getMessage(), equalTo("User with id 1 not found"));
    }

    @Test
    void getAdmins() {
        // prepare
        User testAdmin = new User();
        testAdmin.setName("Admin");
        UserDto testAdminDto = new UserDto();
        testAdminDto.setName("Admin");
        List<User> users = List.of(testAdmin);
        when(userRepository.findAllByRolesName(Role.ROLE_ADMIN.name())).thenReturn(users);
        when(userMapper.toDtos(eq(users))).thenReturn(List.of(testAdminDto));

        // tested method
        List<UserDto> admins = userService.getAdmins();

        // verify
        assertThat(admins.size(), equalTo(1));
        assertThat(admins.get(0).getName(), equalTo("Admin"));
        verify(userRepository).findAllByRolesName(Role.ROLE_ADMIN.name());
    }
}
