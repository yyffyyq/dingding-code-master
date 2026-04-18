package com.example.backend;

import com.example.backend.job.AttendanceSyncJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {


	@Autowired
	private AttendanceSyncJob attendanceSyncJob;

	@Test
	void contextLoads() {
	}

	/**
	 * 测试定时获取方法是否可以实现
	 */
	@Test
	void testSyncJob() {
			attendanceSyncJob.syncAllGroupAttendanceRecords();
	}

}
