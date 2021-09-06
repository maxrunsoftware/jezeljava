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

import static com.google.common.base.Preconditions.*;

import javax.inject.Inject;

import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.SchedulerService;
import com.maxrunsoftware.jezel.SettingService;
import com.maxrunsoftware.jezel.model.SchedulerJob;
import com.maxrunsoftware.jezel.server.QuartzServer;
import com.maxrunsoftware.jezel.server.QuartzServerExecutor;

public class SchedulerServiceQuartz implements SchedulerService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerServiceQuartz.class);

	private final SettingService settings;
	private final DatabaseService db;
	private QuartzServer server;

	@Inject
	public SchedulerServiceQuartz(SettingService settings, DatabaseService db) {
		this.settings = checkNotNull(settings);
		this.db = checkNotNull(db);
	}

	@Override
	public void sync(int schedulerJobId) {
		try (var session = db.openSession()) {
			var schedulerJob = session.get(SchedulerJob.class, schedulerJobId);
			var jobRemove = false;

			if (schedulerJob == null) {
				LOG.debug("SchedulerJob[" + schedulerJobId + "] does not exist in database so removing from Scheduler");
				jobRemove = true;
			}
			if (schedulerJob.isDisabled()) {
				LOG.debug("SchedulerJob[" + schedulerJobId + "] is disabled so removing from Scheduler");
				jobRemove = true;
			}

			var allSchedulesDisabled = true;
			for (var schedulerSchedule : schedulerJob.getSchedulerSchedules()) {
				if (!schedulerSchedule.isDisabled()) allSchedulesDisabled = false;
			}
			if (allSchedulesDisabled) {
				LOG.debug("SchedulerJob[" + schedulerJobId + "] does not contain any schedules or all schedules are disabled so removing from Scheduler");
				jobRemove = true;
			}

			var jobExistsInScheduler = server.existsJob(schedulerJobId);
			if (jobRemove) {
				if (jobExistsInScheduler) {
					// REMOVE
					server.removeJob(schedulerJobId);
					LOG.debug("SchedulerJob[" + schedulerJobId + "] no longer exists, is disabled, or has no schedules but exists in Scheduler so removing from Scheduler");
				} else {
					// NOOP
					LOG.debug("SchedulerJob[" + schedulerJobId + "] does not exist, is disabled, or has no schedules and does not exist in Scheduler so noop");
				}
			} else {
				if (jobExistsInScheduler) {
					// UPDATE
					LOG.debug("SchedulerJob[" + schedulerJobId + "] exists and exists in Scheduler so updating in Scheduler");
				} else {
					// ADD
					LOG.debug("SchedulerJob[" + schedulerJobId + "] exists but does not exist in Scheduler so adding to Scheduler");
				}

				var result = server.addJob(schedulerJobId);
				if (!result) return;
				for (var schedulerSchedule : schedulerJob.getSchedulerSchedules()) {
					LOG.debug("For SchedulerJob[" + schedulerJobId + "] adding SchedulerSchedule[" + schedulerSchedule.getSchedulerScheduleId() + "]");
					result = server.addTrigger(
							schedulerJobId,
							schedulerSchedule.getSchedulerScheduleId(),
							schedulerSchedule.isSunday(),
							schedulerSchedule.isMonday(),
							schedulerSchedule.isTuesday(),
							schedulerSchedule.isWednesday(),
							schedulerSchedule.isThursday(),
							schedulerSchedule.isFriday(),
							schedulerSchedule.isSaturday(),
							schedulerSchedule.getHour(),
							schedulerSchedule.getMinute());
					if (!result) {
						server.removeJob(schedulerJobId);
						return;
					}
				}
				LOG.debug("Completed adding SchedulerJob[" + schedulerJobId + "] to Scheduler with " + schedulerJob.getSchedulerSchedules().size() + " SchedulerSchedules");

			}

		}

	}

	private static class Executor implements QuartzServerExecutor {

		@Override
		public void execute(int jobId, int triggerId) {
			var ssj = com.maxrunsoftware.jezel.Constant.getInstance(SchedulerServiceSchedulerJob.class);
			ssj.execute(jobId);
		}

	}

	@Override
	public void start(boolean joinThread) throws Exception {
		stop();
		server = new QuartzServer();
		server.setThreadCount(settings.getSchedulerThreads());
		server.setExecutor(new Executor());
		server.start();
	}

	@Override
	public void stop() throws Exception {
		var s = server;
		server = null;
		if (s == null) return;
		s.stop();
	}

}
