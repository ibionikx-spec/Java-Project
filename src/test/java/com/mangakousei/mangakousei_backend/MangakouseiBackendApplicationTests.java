package com.mangakousei.mangakousei_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Tests")
class MangakouseiBackendApplicationTests {

	@Test
	@DisplayName("Spring context load thành công")
	void contextLoads() {
	}
}