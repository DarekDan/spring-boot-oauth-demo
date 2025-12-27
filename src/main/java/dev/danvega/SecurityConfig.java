package dev.danvega;

import dev.danvega.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository, RoleService roleService) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.roleService = roleService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/error").permitAll()
                        // H2 console access: ADMIN has full access, POWER_USER can access (read-only
                        // enforced via connection)
                        .requestMatchers("/h2-console/**").hasAnyRole("ADMIN", "POWER_USER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll())
                // Allow H2 console to use frames
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                // Disable CSRF for H2 console
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

        // Only enable OAuth2 login if at least one provider is configured
        if (hasOAuth2Providers()) {
            http.oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .userInfoEndpoint(userInfo -> userInfo
                            .oidcUserService(oidcUserService())));
        }

        return http.build();
    }

    /**
     * Custom OIDC user service that loads roles from the database.
     */
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            // Delegate to the default implementation for loading the user
            OidcUser oidcUser = delegate.loadUser(userRequest);

            String provider = userRequest.getClientRegistration().getRegistrationId();
            String email = oidcUser.getAttribute("email");

            // Build user identifier and load roles from database
            String userIdentifier = RoleService.buildOAuth2UserIdentifier(provider, email);
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(oidcUser.getAuthorities());
            Set<GrantedAuthority> dbRoles = roleService.getRolesForUser(userIdentifier);
            mappedAuthorities.addAll(dbRoles);

            logger.info("✓ OAuth2 user '{}' authenticated with roles: {}", email, mappedAuthorities);

            // Check if user has ROLE_POWER_USER to add custom claim
            boolean isPowerUser = dbRoles.stream()
                    .anyMatch(auth -> "ROLE_POWER_USER".equals(auth.getAuthority()));

            if (isPowerUser) {
                // Add custom claim for power users
                Map<String, Object> claims = new HashMap<>();
                if (oidcUser.getUserInfo() != null) {
                    claims.putAll(oidcUser.getUserInfo().getClaims());
                }
                claims.put("custom_claim", "Power User Active");
                OidcUserInfo userInfo = new OidcUserInfo(claims);

                String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                        .getUserInfoEndpoint().getUserNameAttributeName();

                return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), userInfo, userNameAttributeName);
            }

            // Return user with database-loaded authorities
            String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                    .getUserInfoEndpoint().getUserNameAttributeName();

            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo(),
                    userNameAttributeName);
        };
    }

    private boolean hasOAuth2Providers() {
        // Check if repository can find any registration (google or github)
        try {
            return clientRegistrationRepository.findByRegistrationId("google") != null
                    || clientRegistrationRepository.findByRegistrationId("github") != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    /**
     * Custom UserDetailsService that loads roles from the database.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // For demo purposes, only "admin" user is supported
            // In a real app, you would load user credentials from a database too
            if ("admin".equals(username)) {
                String userIdentifier = RoleService.buildFormUserIdentifier(username);
                Set<GrantedAuthority> authorities = roleService.getRolesForUser(userIdentifier);

                logger.info("✓ Form user '{}' authenticated with roles: {}", username, authorities);

                return User.builder()
                        .username(username)
                        .password(passwordEncoder.encode("admin123"))
                        .authorities(authorities)
                        .build();
            }

            throw new UsernameNotFoundException("User not found: " + username);
        };
    }
}
