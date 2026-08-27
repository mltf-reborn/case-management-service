package com.bagusxmahendra.mltf.case_management_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestSpannerConfig.class)
class CaseManagementServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
