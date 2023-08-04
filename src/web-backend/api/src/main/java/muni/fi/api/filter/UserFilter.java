package muni.fi.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import muni.fi.api.config.UserConfigProperties;
import muni.fi.bl.config.ApiConfigProperties;
import muni.fi.dal.entity.Role;
import muni.fi.dal.entity.User;
import muni.fi.dal.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static muni.fi.enums.Role.ROLE_ADMIN;
import static muni.fi.enums.Role.ROLE_USER;

/**
 * Used as post-JWT-token-validation filter which manages application internal roles.
 * Adds user's saved roles as additional authorities in order to authorize his request
 */
@Component
public class UserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final UserConfigProperties userConfigProperties;
    private final RestTemplate restTemplate;
    private final ApiConfigProperties apiConfigProperties;

    public UserFilter(UserRepository userRepository,
                      UserConfigProperties userConfigProperties,
                      RestTemplate restTemplate,
                      ApiConfigProperties apiConfigProperties) {
        this.userRepository = userRepository;
        this.userConfigProperties = userConfigProperties;
        this.restTemplate = restTemplate;
        this.apiConfigProperties = apiConfigProperties;
    }

    @Override
    protected void doFilterInternal(@Nullable HttpServletRequest request,
                                    @Nullable HttpServletResponse response,
                                    @Nullable FilterChain chain)
            throws ServletException, IOException {

        Authentication oldAuth = SecurityContextHolder.getContext().getAuthentication();
        if (!(oldAuth instanceof JwtAuthenticationToken oldJwtAuth)) { // checks for null as well
            if (chain != null) {
                chain.doFilter(request, response);
            }
            return;
        }
        String accessToken = oldJwtAuth.getToken().getTokenValue();
        User user = getOrCreateUser(oldJwtAuth.getName(), accessToken);

        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            GrantedAuthority authRole = new SimpleGrantedAuthority(role.getName());
            updatedAuthorities.add(authRole);
        }
        updatedAuthorities.addAll(oldJwtAuth.getAuthorities());
        Authentication authentication = new JwtAuthenticationToken((Jwt) oldJwtAuth.getPrincipal(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (chain != null) {
            chain.doFilter(request, response);
        }
    }

    private synchronized User getOrCreateUser(String uniqueId, String accessToken) {
        Optional<User> user = userRepository.findByJwtIdentifier(uniqueId);
        if (user.isEmpty()) {

            JSONObject userInfo = geUserInfoJson(accessToken);
            User newUser = new User();
            newUser.setJwtIdentifier(uniqueId);
            newUser.setUco((String) userInfo.get("preferred_username"));
            newUser.setName((String) userInfo.get("name"));
            newUser.setEmail((String) userInfo.get("email"));

            // initial admin creation
            if (uniqueId.equals(userConfigProperties.getInitialAdminId())) {
                Role adminRole = new Role(ROLE_ADMIN.name());
                Role userRole = new Role(ROLE_USER.name());
                newUser.setRoles(Set.of(adminRole, userRole));
            }
            return userRepository.saveAndFlush(newUser);
        }
        return user.get();
    }

    @SneakyThrows(URISyntaxException.class)
    private JSONObject geUserInfoJson(String accessToken) {
        String url = apiConfigProperties.getIssuerUserinfoUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        String json = restTemplate
                .exchange(RequestEntity.get(new URI(url)).headers(headers).build(), String.class)
                .getBody();
        return new JSONObject(json);
    }

}