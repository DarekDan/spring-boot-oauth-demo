package dev.danvega;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardController.
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    @Mock
    private CsrfToken csrfToken;

    @Mock
    private OAuth2User oauth2User;

    private final DashboardController dashboardController = new DashboardController();

    @Test
    void dashboard_withUserDetails_setsUserAttributes() {
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .authorities(authorities)
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

        String viewName = dashboardController.dashboard(authentication, request, model);

        assertEquals("pages/dashboard", viewName);
        verify(model).addAttribute("username", "admin");
        verify(model).addAttribute("authorities", userDetails.getAuthorities());
        verify(model).addAttribute(eq("csrfHiddenInput"), any(CsrfHiddenInput.class));
    }

    @Test
    void dashboard_withOAuth2User_setsOAuth2Attributes() {
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Map<String, Object> attributes = Map.of(
                "name", "John Doe",
                "email", "john@example.com",
                "sub", "12345");

        when(oauth2User.getAttribute("name")).thenReturn("John Doe");
        when(oauth2User.getAttribute("email")).thenReturn("john@example.com");
        when(oauth2User.getAuthorities()).thenReturn(authorities);
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

        String viewName = dashboardController.dashboard(authentication, request, model);

        assertEquals("pages/dashboard", viewName);
        verify(model).addAttribute("username", "John Doe");
        verify(model).addAttribute("email", "john@example.com");
        verify(model).addAttribute("authorities", authorities);
        verify(model).addAttribute("attributes", attributes);
        verify(model).addAttribute(eq("csrfHiddenInput"), any(CsrfHiddenInput.class));
    }

    @Test
    void dashboard_withNullCsrfToken_doesNotSetCsrfAttribute() {
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = User.builder()
                .username("user")
                .password("password")
                .authorities(authorities)
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        String viewName = dashboardController.dashboard(authentication, request, model);

        assertEquals("pages/dashboard", viewName);
        verify(model).addAttribute("username", "user");
        verify(model, never()).addAttribute(eq("csrfHiddenInput"), any(CsrfHiddenInput.class));
    }

    @Test
    void dashboard_withUnknownPrincipalType_handlesCsrfOnly() {
        Object unknownPrincipal = new Object();

        when(authentication.getPrincipal()).thenReturn(unknownPrincipal);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

        String viewName = dashboardController.dashboard(authentication, request, model);

        assertEquals("pages/dashboard", viewName);
        verify(model, never()).addAttribute(eq("username"), anyString());
        verify(model).addAttribute(eq("csrfHiddenInput"), any(CsrfHiddenInput.class));
    }
}
