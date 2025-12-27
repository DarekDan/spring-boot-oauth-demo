package dev.danvega;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests SecurityConfig when OAuth2 providers are configured.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "GOOGLE_CLIENT_ID=test-google-id",
        "GOOGLE_CLIENT_SECRET=test-google-secret",
        "GITHUB_CLIENT_ID=test-github-id",
        "GITHUB_CLIENT_SECRET=test-github-secret"
})
class SecurityConfigWithOAuth2Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPage_withOAuth2Configured_isAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void oauth2AuthorizationEndpoint_redirectsToProvider() throws Exception {
        // This should redirect to the OAuth2 provider
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void oauth2AuthorizationEndpoint_github_redirectsToProvider() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/github"))
                .andExpect(status().is3xxRedirection());
    }
}
