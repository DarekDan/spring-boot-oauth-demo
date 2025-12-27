package dev.danvega;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoginController.
 */
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private OAuth2ClientConditionService oauthService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        loginController = new LoginController(oauthService);
    }

    @Test
    void login_withNoError_setsOAuthFlags() {
        when(oauthService.isGoogleEnabled()).thenReturn(true);
        when(oauthService.isGithubEnabled()).thenReturn(false);

        String viewName = loginController.login(request, model, null, null);

        assertEquals("pages/login", viewName);
        verify(model).addAttribute("googleEnabled", true);
        verify(model).addAttribute("githubEnabled", false);
        verify(model, never()).addAttribute(eq("error"), any());
    }

    @Test
    void login_withError_setsErrorAttributes() {
        when(oauthService.isGoogleEnabled()).thenReturn(false);
        when(oauthService.isGithubEnabled()).thenReturn(true);

        String viewName = loginController.login(request, model, "error", null);

        assertEquals("pages/login", viewName);
        verify(model).addAttribute("error", true);
        verify(model).addAttribute("errorMessage", "Invalid username or password");
        verify(model).addAttribute("googleEnabled", false);
        verify(model).addAttribute("githubEnabled", true);
    }

    @Test
    void login_withLogout_setsOAuthFlags() {
        when(oauthService.isGoogleEnabled()).thenReturn(true);
        when(oauthService.isGithubEnabled()).thenReturn(true);

        String viewName = loginController.login(request, model, null, "true");

        assertEquals("pages/login", viewName);
        verify(model).addAttribute("googleEnabled", true);
        verify(model).addAttribute("githubEnabled", true);
    }

    @Test
    void login_withBothProvidersDisabled_setsDisabledFlags() {
        when(oauthService.isGoogleEnabled()).thenReturn(false);
        when(oauthService.isGithubEnabled()).thenReturn(false);

        String viewName = loginController.login(request, model, null, null);

        assertEquals("pages/login", viewName);
        verify(model).addAttribute("googleEnabled", false);
        verify(model).addAttribute("githubEnabled", false);
    }

    @Test
    void home_returnsHomeView() {
        String viewName = loginController.home();

        assertEquals("pages/home", viewName);
    }
}
