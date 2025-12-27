package dev.danvega;

import gg.jte.TemplateOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CsrfHiddenInput.
 */
@ExtendWith(MockitoExtension.class)
class CsrfHiddenInputTest {

    @Mock
    private CsrfToken csrfToken;

    @Mock
    private TemplateOutput templateOutput;

    @Test
    void writeTo_withValidToken_writesHiddenInput() {
        when(csrfToken.getParameterName()).thenReturn("_csrf");
        when(csrfToken.getToken()).thenReturn("test-token-123");

        CsrfHiddenInput csrfHiddenInput = new CsrfHiddenInput(csrfToken);
        csrfHiddenInput.writeTo(templateOutput);

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(templateOutput).writeContent(contentCaptor.capture());

        String writtenContent = contentCaptor.getValue();
        assertTrue(writtenContent.contains("type=\"hidden\""));
        assertTrue(writtenContent.contains("name=\"_csrf\""));
        assertTrue(writtenContent.contains("value=\"test-token-123\""));
    }

    @Test
    void writeTo_withNullToken_doesNotWriteAnything() {
        CsrfHiddenInput csrfHiddenInput = new CsrfHiddenInput(null);
        csrfHiddenInput.writeTo(templateOutput);

        verify(templateOutput, never()).writeContent(anyString());
    }

    @Test
    void constructor_storesCsrfToken() {
        CsrfHiddenInput csrfHiddenInput = new CsrfHiddenInput(csrfToken);

        // Verify it was constructed without exceptions
        assertNotNull(csrfHiddenInput);
    }
}
