/*
 * Copyright (c) 2021 Max Run Software (dev@maxrunsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.maxrunsoftware.jezel.service;

import static com.maxrunsoftware.jezel.Util.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.maxrunsoftware.jezel.SettingService;
import com.maxrunsoftware.jezel.TestBase;
import com.maxrunsoftware.jezel.model.SchedulerJob;

public class DatabaseServiceTest extends TestBase {

	@Test
	public void test() {
		var db = new DatabaseServiceH2(new SettingService() {});
		for (int i = 0; i < 3; i++) {
			try (var session = db.openSession()) {
				var schedulerJob = new SchedulerJob();
				save(session, schedulerJob);

				var schedulerJobs = getAll(SchedulerJob.class, session);
				assertEquals(i + 1, schedulerJobs.size());

			}

		}

		try (var session = db.openSession()) {
			var schedulerJob = getById(SchedulerJob.class, session, 2);
			assertNotNull(schedulerJob);
		}

		try (var session = db.openSession()) {

			var schedulerJobs = getAll(SchedulerJob.class, session);
			assertEquals(3, schedulerJobs.size());
		}
	}

}
