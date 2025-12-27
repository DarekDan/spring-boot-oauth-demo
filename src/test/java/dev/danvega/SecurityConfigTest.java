package dev.danvega;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SecurityConfig authentication and authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "GOOGLE_CLIENT_ID=",
        "GOOGLE_CLIENT_SECRET=",
        "GITHUB_CLIENT_ID=",
        "GITHUB_CLIENT_SECRET="
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // === PUBLIC ENDPOINTS ===

    @Test
    void homeEndpoint_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void loginEndpoint_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void errorEndpoint_isAccessibleWithoutAuth() throws Exception {
        // /error without an actual error returns 500 (no error to display)
        // We're testing that it's accessible without authentication (not redirected to
        // login)
        mockMvc.perform(get("/error"))
                .andExpect(status().is5xxServerError());
    }

    // === PROTECTED ENDPOINTS ===

    @Test
    void dashboardEndpoint_requiresAuth_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void dashboardEndpoint_withUser_returnsOk() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    // === H2 CONSOLE ACCESS CONTROL ===

    @Test
    void h2Console_requiresAuth_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void h2Console_withAdminRole_isAccessible() throws Exception {
        // H2 console may redirect or return different status codes
        // The key is that it's not 401/403 (authorization passed)
        mockMvc.perform(get("/h2-console"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Expected authorized access but got " + status);
                    }
                });
    }

    @Test
    @WithMockUser(username = "poweruser", roles = { "POWER_USER" })
    void h2Console_withPowerUserRole_isAccessible() throws Exception {
        // H2 console may redirect or return different status codes
        // The key is that it's not 401/403 (authorization passed)
        mockMvc.perform(get("/h2-console"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Expected authorized access but got " + status);
                    }
                });
    }

    @Test
    @WithMockUser(username = "regularuser", roles = { "USER" })
    void h2Console_withRegularUserRole_isForbidden() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isForbidden());
    }

    // === FORM LOGIN ===

    @Test
    void formLogin_withValidCredentials_redirectsToDashboard() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void formLogin_withInvalidCredentials_redirectsToLoginWithError() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "wrongpassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void formLogin_withUnknownUser_redirectsToLoginWithError() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "unknownuser")
                .param("password", "password")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    // === LOGOUT ===

    @Test
    @WithMockUser(username = "admin")
    void logout_redirectsToHome() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // === CSRF ===

    @Test
    void formLogin_withoutCsrf_isForbidden() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123"))
                .andExpect(status().isForbidden());
    }
}
