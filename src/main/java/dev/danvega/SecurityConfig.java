package dev.danvega;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll());

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

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            // Delegate to the default implementation for loading the user
            OidcUser oidcUser = delegate.loadUser(userRequest);

            // Check if the user is the specific Google user
            if ("google".equals(userRequest.getClientRegistration().getRegistrationId())) {
                String email = oidcUser.getAttribute("email");
                if ("ddanielewski@gmail.com".equals(email)) {
                    Set<GrantedAuthority> mappedAuthorities = new HashSet<>(oidcUser.getAuthorities());
                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_POWER_USER"));

                    // Add custom claim
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
            }

            return oidcUser;
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
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails defaultUser = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(defaultUser);
    }
}
