package dev.danvega;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CsrfTokenAdvice.
 */
@ExtendWith(MockitoExtension.class)
class CsrfTokenAdviceTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private CsrfToken csrfToken;

    @Test
    void csrf_returnsCsrfTokenFromRequest() {
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

        CsrfTokenAdvice advice = new CsrfTokenAdvice();
        CsrfToken result = advice.csrf(request);

        assertEquals(csrfToken, result);
        verify(request).getAttribute(CsrfToken.class.getName());
    }

    @Test
    void csrf_returnsNullWhenNoTokenInRequest() {
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        CsrfTokenAdvice advice = new CsrfTokenAdvice();
        CsrfToken result = advice.csrf(request);

        assertNull(result);
    }

    @Test
    void csrfHiddenInput_returnsCsrfHiddenInputWithToken() {
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

        CsrfTokenAdvice advice = new CsrfTokenAdvice();
        CsrfHiddenInput result = advice.csrfHiddenInput(request);

        assertNotNull(result);
    }

    @Test
    void csrfHiddenInput_returnsCsrfHiddenInputWithNullToken() {
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        CsrfTokenAdvice advice = new CsrfTokenAdvice();
        CsrfHiddenInput result = advice.csrfHiddenInput(request);

        assertNotNull(result);
    }
}
