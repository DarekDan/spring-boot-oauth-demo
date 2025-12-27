package dev.danvega;

import dev.danvega.domain.Role;
import dev.danvega.domain.RoleAssignment;
import dev.danvega.repository.RoleAssignmentRepository;
import dev.danvega.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SecurityConfig's OIDC user service lambda execution.
 * Uses Mockito's MockedConstruction to mock the OidcUserService delegate.
 */
class SecurityConfigOidcLambdaTest {

    private RoleAssignmentRepository roleAssignmentRepository;
    private ClientRegistrationRepository clientRegistrationRepository;
    private RoleService roleService;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        roleAssignmentRepository = mock(RoleAssignmentRepository.class);
        clientRegistrationRepository = mock(ClientRegistrationRepository.class);
        roleService = new RoleService(roleAssignmentRepository);
        securityConfig = new SecurityConfig(clientRegistrationRepository, roleService);
    }

    @Test
    void oidcUserService_powerUserWithUserInfo_addsCustomClaimAndReturnsUser() throws Exception {
        // Setup: Power user role assignment
        Role powerUserRole = new Role("ROLE_POWER_USER");
        powerUserRole.setId(1L);
        RoleAssignment assignment = new RoleAssignment("google:poweruser@example.com", powerUserRole);
        when(roleAssignmentRepository.findByUserIdentifier("google:poweruser@example.com"))
                .thenReturn(List.of(assignment));

        // Create mock OIDC user with userInfo
        OidcUser mockOidcUser = createMockOidcUser("poweruser@example.com", true);
        OidcUserRequest userRequest = createOidcUserRequest("google");

        // Use MockedConstruction to mock the delegate OidcUserService
        try (MockedConstruction<OidcUserService> mocked = mockConstruction(OidcUserService.class,
                (mock, context) -> when(mock.loadUser(any())).thenReturn(mockOidcUser))) {

            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = getOidcUserService();
            OidcUser result = oidcUserService.loadUser(userRequest);

            assertNotNull(result);
            // Should have the custom claim
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_POWER_USER")));
        }
    }

    @Test
    void oidcUserService_powerUserWithNullUserInfo_addsCustomClaimWithEmptyBaseClaims() throws Exception {
        // Setup: Power user role assignment
        Role powerUserRole = new Role("ROLE_POWER_USER");
        powerUserRole.setId(1L);
        RoleAssignment assignment = new RoleAssignment("google:poweruser@example.com", powerUserRole);
        when(roleAssignmentRepository.findByUserIdentifier("google:poweruser@example.com"))
                .thenReturn(List.of(assignment));

        // Create mock OIDC user WITHOUT userInfo (null)
        OidcUser mockOidcUser = createMockOidcUser("poweruser@example.com", false);
        OidcUserRequest userRequest = createOidcUserRequest("google");

        try (MockedConstruction<OidcUserService> mocked = mockConstruction(OidcUserService.class,
                (mock, context) -> when(mock.loadUser(any())).thenReturn(mockOidcUser))) {

            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = getOidcUserService();
            OidcUser result = oidcUserService.loadUser(userRequest);

            assertNotNull(result);
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_POWER_USER")));
        }
    }

    @Test
    void oidcUserService_regularUser_returnsUserWithoutCustomClaim() throws Exception {
        // Setup: Regular user role assignment (not power user)
        Role userRole = new Role("ROLE_USER");
        userRole.setId(1L);
        RoleAssignment assignment = new RoleAssignment("google:regularuser@example.com", userRole);
        when(roleAssignmentRepository.findByUserIdentifier("google:regularuser@example.com"))
                .thenReturn(List.of(assignment));

        // Create mock OIDC user
        OidcUser mockOidcUser = createMockOidcUser("regularuser@example.com", true);
        OidcUserRequest userRequest = createOidcUserRequest("google");

        try (MockedConstruction<OidcUserService> mocked = mockConstruction(OidcUserService.class,
                (mock, context) -> when(mock.loadUser(any())).thenReturn(mockOidcUser))) {

            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = getOidcUserService();
            OidcUser result = oidcUserService.loadUser(userRequest);

            assertNotNull(result);
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
            // Should NOT have ROLE_POWER_USER
            assertFalse(result.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_POWER_USER")));
        }
    }

    @Test
    void oidcUserService_noRolesInDatabase_returnsUserWithDefaultAuthorities() throws Exception {
        // Setup: No roles in database
        when(roleAssignmentRepository.findByUserIdentifier("google:newuser@example.com"))
                .thenReturn(Collections.emptyList());

        // Create mock OIDC user
        OidcUser mockOidcUser = createMockOidcUser("newuser@example.com", true);
        OidcUserRequest userRequest = createOidcUserRequest("google");

        try (MockedConstruction<OidcUserService> mocked = mockConstruction(OidcUserService.class,
                (mock, context) -> when(mock.loadUser(any())).thenReturn(mockOidcUser))) {

            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = getOidcUserService();
            OidcUser result = oidcUserService.loadUser(userRequest);

            assertNotNull(result);
            // Should still have the OIDC default authority
            assertTrue(result.getAuthorities().size() >= 1);
        }
    }

    @Test
    void oidcUserService_githubProvider_usesCorrectIdentifier() throws Exception {
        // Setup: GitHub user
        Role userRole = new Role("ROLE_USER");
        userRole.setId(1L);
        RoleAssignment assignment = new RoleAssignment("github:githubuser@example.com", userRole);
        when(roleAssignmentRepository.findByUserIdentifier("github:githubuser@example.com"))
                .thenReturn(List.of(assignment));

        // Create mock OIDC user
        OidcUser mockOidcUser = createMockOidcUser("githubuser@example.com", true);
        OidcUserRequest userRequest = createOidcUserRequest("github");

        try (MockedConstruction<OidcUserService> mocked = mockConstruction(OidcUserService.class,
                (mock, context) -> when(mock.loadUser(any())).thenReturn(mockOidcUser))) {

            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = getOidcUserService();
            OidcUser result = oidcUserService.loadUser(userRequest);

            assertNotNull(result);
            verify(roleAssignmentRepository).findByUserIdentifier("github:githubuser@example.com");
        }
    }

    // Helper method to invoke private oidcUserService method
    @SuppressWarnings("unchecked")
    private OAuth2UserService<OidcUserRequest, OidcUser> getOidcUserService() throws Exception {
        Method method = SecurityConfig.class.getDeclaredMethod("oidcUserService");
        method.setAccessible(true);
        return (OAuth2UserService<OidcUserRequest, OidcUser>) method.invoke(securityConfig);
    }

    // Helper to create a mock OidcUser
    private OidcUser createMockOidcUser(String email, boolean includeUserInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "12345");
        claims.put("email", email);
        claims.put("name", "Test User");

        OidcIdToken idToken = new OidcIdToken(
                "test-id-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                claims);

        OidcUserInfo userInfo = null;
        if (includeUserInfo) {
            userInfo = new OidcUserInfo(claims);
        }

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("OIDC_USER"));

        return new DefaultOidcUser(authorities, idToken, userInfo);
    }

    // Helper to create an OidcUserRequest
    private OidcUserRequest createOidcUserRequest(String provider) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(provider)
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://example.com/auth")
                .tokenUri("https://example.com/token")
                .userInfoUri("https://example.com/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://example.com/jwks")
                .clientName(provider)
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600));

        Map<String, Object> claims = Map.of(
                "sub", "12345",
                "iss", "https://example.com");

        OidcIdToken idToken = new OidcIdToken(
                "test-id-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                claims);

        return new OidcUserRequest(clientRegistration, accessToken, idToken);
    }
}
