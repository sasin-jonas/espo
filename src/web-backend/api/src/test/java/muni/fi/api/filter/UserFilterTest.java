package muni.fi.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import muni.fi.api.config.UserConfigProperties;
import muni.fi.bl.config.ApiConfigProperties;
import muni.fi.bl.service.MailService;
import muni.fi.bl.service.UserService;
import muni.fi.dal.entity.Role;
import muni.fi.dal.entity.User;
import muni.fi.dal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static muni.fi.enums.Role.ROLE_ADMIN;
import static muni.fi.enums.Role.ROLE_USER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class UserFilterTest {

    @Mock
    private UserRepository userRepositoryMock;
    @Mock
    private UserConfigProperties userConfigPropertiesMock;
    @Mock
    private RestTemplate restTemplateMock;

    @Mock
    private HttpServletRequest servletRequestMock;
    @Mock
    private HttpServletResponse servletResponseMock;
    @Mock
    private FilterChain filterChainMock;
    @Mock
    private ApiConfigProperties apiConfigProperties;
    @Mock
    private MailService mailServiceMock;
    @Mock
    private UserService userServiceMock;

    @Captor
    private ArgumentCaptor<User> userEntityCaptor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    // helper variables
    private String adminUser;
    private String responseBody;

    // tested class
    private UserFilter userFilter;

    @BeforeEach
    void setUp() {
        openMocks(this);

        when(apiConfigProperties.getIssuerUserinfoUri()).thenReturn("http://localhost");
        userFilter = new UserFilter(userRepositoryMock, userConfigPropertiesMock,
                restTemplateMock, apiConfigProperties, mailServiceMock, userServiceMock);

        responseBody = """
                {
                "preferred_username": "123456",
                "name": "John Doe",
                "email": "johndoe@mail.muni.cz"
                }
                """;

        HashMap<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT");
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("scope", "openid");
        Jwt jwt = new Jwt("tokenValue", null, null, headers, claims);
        // Create an authentication token for the user
        adminUser = "123456@muni.cz";
        Authentication authentication = new JwtAuthenticationToken(jwt, null, adminUser);

        // Set the authentication token to be used by the test
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternalAdmin() throws ServletException, IOException {
        // prepare
        User user = new User();
        Role role1 = new Role();
        role1.setName(ROLE_ADMIN.name());
        Role role2 = new Role();
        role2.setName(ROLE_USER.name());
        user.setRoles(Set.of(role1, role2));
        when(userRepositoryMock.findByJwtIdentifier(adminUser)).thenReturn(Optional.empty());
        when(userRepositoryMock.saveAndFlush(any())).thenReturn(user);
        when(userConfigPropertiesMock.getInitialAdminId()).thenReturn(adminUser);
        when(restTemplateMock.exchange(any(RequestEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.of(Optional.of(responseBody)));

        // tested method
        userFilter.doFilterInternal(servletRequestMock, servletResponseMock, filterChainMock);

        // verify
        verify(userRepositoryMock).findByJwtIdentifier(stringCaptor.capture());
        assertThat(stringCaptor.getValue(), equalTo(adminUser));

        verify(userConfigPropertiesMock).getInitialAdminId();

        verify(userRepositoryMock).saveAndFlush(userEntityCaptor.capture());
        User capturedUser = userEntityCaptor.getValue();
        assertThat(capturedUser.getUco(), equalTo("123456"));
        assertThat(capturedUser.getName(), equalTo("John Doe"));
        assertThat(capturedUser.getEmail(), equalTo("johndoe@mail.muni.cz"));
        assertThat(capturedUser.getJwtIdentifier(), equalTo(adminUser));
        assertThat(capturedUser.getRoles().size(), equalTo(2));
        List<String> roles = capturedUser.getRoles().stream().map(Role::getName).toList();
        assertThat(roles, hasItem(ROLE_ADMIN.name()));
        assertThat(roles, hasItem(ROLE_USER.name()));

        verify(restTemplateMock).exchange(any(RequestEntity.class), eq(String.class));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        assertThat(jwtAuth.getAuthorities().size(), equalTo(2));
        List<String> grantedAuthorities = jwtAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(grantedAuthorities, hasItem(ROLE_ADMIN.name()));
        assertThat(grantedAuthorities, hasItem(ROLE_USER.name()));
    }

    @Test
    void doFilterInternalNonAdmin() throws ServletException, IOException {
        // prepare
        User user = new User();
        user.setRoles(Set.of());
        when(userRepositoryMock.findByJwtIdentifier(adminUser)).thenReturn(Optional.empty());
        when(userRepositoryMock.saveAndFlush(any())).thenReturn(user);
        when(userConfigPropertiesMock.getInitialAdminId()).thenReturn("654321@muni.cz");
        when(restTemplateMock.exchange(any(RequestEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.of(Optional.of(responseBody)));

        // tested method
        userFilter.doFilterInternal(servletRequestMock, servletResponseMock, filterChainMock);

        // verify
        verify(userRepositoryMock).findByJwtIdentifier(stringCaptor.capture());
        assertThat(stringCaptor.getValue(), equalTo(adminUser));

        verify(userConfigPropertiesMock).getInitialAdminId();

        verify(userRepositoryMock).saveAndFlush(userEntityCaptor.capture());
        User capturedUser = userEntityCaptor.getValue();
        assertThat(capturedUser.getUco(), equalTo("123456"));
        assertThat(capturedUser.getName(), equalTo("John Doe"));
        assertThat(capturedUser.getEmail(), equalTo("johndoe@mail.muni.cz"));
        assertThat(capturedUser.getJwtIdentifier(), equalTo(adminUser));
        assertThat(capturedUser.getRoles().size(), equalTo(0));

        verify(restTemplateMock).exchange(any(RequestEntity.class), eq(String.class));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        assertThat(jwtAuth.getAuthorities().size(), equalTo(0));
    }

    @Test
    void doFilterInternalAlreadyExists() throws ServletException, IOException {
        // prepare
        User user = new User();
        Role role1 = new Role();
        role1.setName(ROLE_ADMIN.name());
        Role role2 = new Role();
        role2.setName(ROLE_USER.name());
        user.setRoles(Set.of(role1, role2));
        when(userRepositoryMock.findByJwtIdentifier(adminUser)).thenReturn(Optional.of(user));

        // tested method
        userFilter.doFilterInternal(servletRequestMock, servletResponseMock, filterChainMock);

        // verify
        verify(userRepositoryMock).findByJwtIdentifier(stringCaptor.capture());
        assertThat(stringCaptor.getValue(), equalTo(adminUser));

        verify(userConfigPropertiesMock, times(0)).getInitialAdminId();
        verify(userRepositoryMock, times(0)).saveAndFlush(any());
        verify(restTemplateMock, times(0)).exchange(any(RequestEntity.class), eq(String.class));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        assertThat(jwtAuth.getAuthorities().size(), equalTo(2));
        List<String> grantedAuthorities = jwtAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(grantedAuthorities, hasItem(ROLE_ADMIN.name()));
        assertThat(grantedAuthorities, hasItem(ROLE_USER.name()));
    }

    @Test
    void doFilterInternalInvalidAuth() throws ServletException, IOException {
        // prepare
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("user123")
                .password("password")
                .roles("USER")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // tested method
        userFilter.doFilterInternal(servletRequestMock, servletResponseMock, filterChainMock);

        // verify
        verify(userRepositoryMock, times(0)).findByJwtIdentifier(any());
    }
}