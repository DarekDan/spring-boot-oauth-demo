package dev.danvega.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for H2DatabaseConfig.
 */
class H2DatabaseConfigTest {

    @Test
    void h2PasswordInitializer_returnsApplicationRunner() throws Exception {
        H2DatabaseConfig config = new H2DatabaseConfig();
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.execute(anyString())).thenReturn(true);

        ApplicationRunner runner = config.h2PasswordInitializer(mockDataSource);

        assertNotNull(runner);
    }

    @Test
    void h2PasswordInitializer_executesAlterUser() throws Exception {
        H2DatabaseConfig config = new H2DatabaseConfig();
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.execute(anyString())).thenReturn(true);

        ApplicationRunner runner = config.h2PasswordInitializer(mockDataSource);
        runner.run(null);

        // Verify ALTER USER was executed
        verify(mockStatement).execute(argThat(sql -> sql.startsWith("ALTER USER SA SET PASSWORD")));
    }

    @Test
    void h2PasswordInitializer_setsGeneratedPassword() throws Exception {
        H2DatabaseConfig config = new H2DatabaseConfig();
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.execute(anyString())).thenReturn(true);

        ApplicationRunner runner = config.h2PasswordInitializer(mockDataSource);
        runner.run(null);

        assertNotNull(config.getGeneratedPassword());
        assertFalse(config.getGeneratedPassword().isBlank());
    }

    @Test
    void h2PasswordInitializer_handlesException() throws Exception {
        H2DatabaseConfig config = new H2DatabaseConfig();
        DataSource mockDataSource = mock(DataSource.class);

        when(mockDataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        ApplicationRunner runner = config.h2PasswordInitializer(mockDataSource);

        // Should not throw - just logs warning
        assertDoesNotThrow(() -> runner.run(null));
        assertNull(config.getGeneratedPassword());
    }

    @Test
    void getGeneratedPassword_isNullBeforeRunnerExecutes() {
        H2DatabaseConfig config = new H2DatabaseConfig();

        assertNull(config.getGeneratedPassword());
    }
}
