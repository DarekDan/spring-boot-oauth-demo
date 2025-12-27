package dev.danvega;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Application context loading and bean verification tests.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"GOOGLE_CLIENT_ID=",
		"GOOGLE_CLIENT_SECRET=",
		"GITHUB_CLIENT_ID=",
		"GITHUB_CLIENT_SECRET="
})
class JteLoginApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertNotNull(applicationContext);
	}

	@Test
	void securityFilterChainBeanExists() {
		SecurityFilterChain securityFilterChain = applicationContext.getBean(SecurityFilterChain.class);
		assertNotNull(securityFilterChain);
	}

	@Test
	void passwordEncoderBeanExists() {
		PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
		assertNotNull(passwordEncoder);
	}

	@Test
	void passwordEncoder_encodesAndMatches() {
		PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
		String rawPassword = "testPassword123";
		String encodedPassword = passwordEncoder.encode(rawPassword);

		assertNotEquals(rawPassword, encodedPassword);
		assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
	}
}
