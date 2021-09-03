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
import static com.maxrunsoftware.jezel.Util.*;

import java.util.ArrayList;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.SchedulerService;
import com.maxrunsoftware.jezel.SettingService;
import com.maxrunsoftware.jezel.model.SchedulerJob;
import com.maxrunsoftware.jezel.model.SchedulerSchedule;

public class SchedulerServiceQuartz implements SchedulerService {

	private static JobKey createJobKey(int schedulerJobId) {
		return new JobKey("" + schedulerJobId);
	}

	private static TriggerKey createTriggerKey(int schedulerScheduleId) {
		return new TriggerKey("" + schedulerScheduleId);
	}

	private static Trigger asTrigger(SchedulerSchedule schedulerSchedule) {
		var isSun = schedulerSchedule.isSunday();
		var isMon = schedulerSchedule.isMonday();
		var isTue = schedulerSchedule.isTuesday();
		var isWed = schedulerSchedule.isWednesday();
		var isThu = schedulerSchedule.isThursday();
		var isFri = schedulerSchedule.isFriday();
		var isSat = schedulerSchedule.isSaturday();
		var hour = schedulerSchedule.getHour();
		var minute = schedulerSchedule.getMinute();

		var daysList = new ArrayList<Integer>();
		if (isSun) daysList.add(1);
		if (isMon) daysList.add(2);
		if (isTue) daysList.add(3);
		if (isWed) daysList.add(4);
		if (isThu) daysList.add(5);
		if (isFri) daysList.add(6);
		if (isSat) daysList.add(7);
		var days = daysList.toArray(Integer[]::new);

		if (hour > 23) hour = 23;
		if (hour < 0) hour = 0;
		if (minute > 59) minute = 59;
		if (minute < 0) minute = 0;

		var desc = new StringBuilder();
		desc.append(StringUtils.right("000" + hour, 2));
		desc.append(StringUtils.right("000" + minute, 2));
		desc.append(isSun ? "Y" : "N");
		desc.append(isMon ? "Y" : "N");
		desc.append(isTue ? "Y" : "N");
		desc.append(isWed ? "Y" : "N");
		desc.append(isThu ? "Y" : "N");
		desc.append(isFri ? "Y" : "N");
		desc.append(isSat ? "Y" : "N");

		var trigger = TriggerBuilder.newTrigger()
				.forJob(createJobKey(schedulerSchedule.getSchedulerJob().getSchedulerJobId()))
				.withIdentity(createTriggerKey(schedulerSchedule.getSchedulerScheduleId()))
				.withDescription(desc.toString())
				.withSchedule(CronScheduleBuilder.atHourAndMinuteOnGivenDaysOfWeek(hour, minute, days))
				.build();

		return trigger;
	}

	private static SchedulerSchedule asSchedulerSchedule(Trigger trigger) {
		var desc = trigger.getDescription();
		if (desc.length() != 11) throw new IllegalArgumentException("Trigger description '" + desc + "' not in correct format");
		var hour = Integer.parseInt(desc.subSequence(0, 2).toString());
		var minute = Integer.parseInt(desc.subSequence(2, 4).toString());
		var isSun = parseBoolean(desc.subSequence(4, 5).toString());
		var isMon = parseBoolean(desc.subSequence(5, 6).toString());
		var isTue = parseBoolean(desc.subSequence(6, 7).toString());
		var isWed = parseBoolean(desc.subSequence(7, 8).toString());
		var isThu = parseBoolean(desc.subSequence(8, 9).toString());
		var isFri = parseBoolean(desc.subSequence(9, 10).toString());
		var isSat = parseBoolean(desc.subSequence(10, 11).toString());

		var schedulerSchedule = new SchedulerSchedule();
		schedulerSchedule.setSchedulerScheduleId(Integer.parseInt(trigger.getKey().getName()));
		schedulerSchedule.setDays(isSun, isMon, isTue, isWed, isThu, isFri, isSat);
		schedulerSchedule.setTime(hour, minute);
		return schedulerSchedule;
	}

	private static class QuartzWrapper {
		private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QuartzWrapper.class);

		private final Scheduler scheduler;

		public QuartzWrapper(Scheduler scheduler) {
			this.scheduler = checkNotNull(scheduler);
		}

		public Boolean isJobExist(int schedulerJobId) {

			try {
				return scheduler.checkExists(createJobKey(schedulerJobId));
			} catch (SchedulerException e) {
				LOG.error("Error checking if SchedulerJob[" + schedulerJobId + "] exists in scheduler", e);
				return null;
			}
		}

		public boolean removeJob(int schedulerJobId) {

			var exists = isJobExist(schedulerJobId);
			if (exists == null) return false;

			if (exists) {
				try {
					scheduler.deleteJob(createJobKey(schedulerJobId));
				} catch (SchedulerException e) {
					LOG.error("Error deleting existing SchedulerJob[" + schedulerJobId + "] in scheduler", e);
					return false;
				}
			}

			return true;
		}

		public boolean addJob(int schedulerJobId) {

			var removeResult = removeJob(schedulerJobId);
			if (!removeResult) return false; // Should already be logged in removeJob

			var jobDetail = JobBuilder.newJob()
					.withIdentity(createJobKey(schedulerJobId))
					.ofType(SchedulerServiceQuartzJob.class)
					.storeDurably()
					.build();

			try {
				scheduler.addJob(jobDetail, true);
				return true;
			} catch (SchedulerException e) {
				LOG.error("Error adding SchedulerJob[" + schedulerJobId + "] to scheduler", e);
				return false;
			}
		}

		public boolean addTrigger(int schedulerJobId, int schedulerScheduleId, Trigger trigger) {
			var removeResult = removeTrigger(schedulerScheduleId);
			if (!removeResult) return false; // Should already be logged in removeJob

			try {
				scheduler.scheduleJob(trigger);
				return true;
			} catch (SchedulerException e) {
				LOG.error("Error scheduling SchedulerJob[" + schedulerJobId + "] with SchedulerSchedule[" + schedulerScheduleId + "]");
				return false;
			}
		}

		public Boolean isTriggerExist(int schedulerScheduleId) {
			try {
				return scheduler.checkExists(createTriggerKey(schedulerScheduleId));
			} catch (SchedulerException e) {
				LOG.error("Error checking if SchedulerSchedule[" + schedulerScheduleId + "] exists in scheduler", e);
				return null;
			}
		}

		public boolean removeTrigger(int schedulerScheduleId) {

			var exists = isTriggerExist(schedulerScheduleId);
			if (exists == null) return false;

			if (exists) {
				try {
					scheduler.unscheduleJob(createTriggerKey(schedulerScheduleId));
				} catch (SchedulerException e) {
					LOG.error("Error deleting existing SchedulerSchedule[" + schedulerScheduleId + "] in scheduler", e);
					return false;
				}
			}

			return true;
		}

	}

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerServiceQuartz.class);

	private final SettingService settings;
	private final DatabaseService db;
	private Scheduler scheduler;
	private QuartzWrapper wrapper;

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

			var jobKey = createJobKey(schedulerJobId);
			JobDetail jobDetail = null;
			try {
				jobDetail = scheduler.getJobDetail(jobKey);
			} catch (SchedulerException e) {
				LOG.warn("Scheduler exception retrieving JobDetail for SchedulerJob[" + schedulerJobId + "]", e);
			}

			if (jobRemove) {
				if (jobDetail == null) {
					// NOOP
					LOG.debug("SchedulerJob[" + schedulerJobId + "] does not exist, is disabled, or has no schedules and does not exist in Scheduler so noop");
				} else {
					// REMOVE
					LOG.debug("SchedulerJob[" + schedulerJobId + "] no longer exists, is disabled, or has no schedules but exists in Scheduler so removing from Scheduler");
					try {
						scheduler.deleteJob(jobKey);
					} catch (SchedulerException e) {
						LOG.error("Error encountered removing SchedulerJob[" + schedulerJobId + "] from scheduler");
					}
				}
			} else {
				if (jobDetail == null) {
					// ADD
					LOG.debug("SchedulerJob[" + schedulerJobId + "] exists but does not exist in Scheduler so adding to Scheduler");
				} else {
					// UPDATE
					LOG.debug("SchedulerJob[" + schedulerJobId + "] exists and exists in Scheduler so updating in Scheduler");
				}

				var result = wrapper.addJob(schedulerJob.getSchedulerJobId());
				if (!result) return;
				for (var schedulerSchedule : schedulerJob.getSchedulerSchedules()) {
					var t = asTrigger(schedulerSchedule);
					LOG.debug("For SchedulerJob[" + schedulerJobId + "] adding SchedulerSchedule[" + schedulerSchedule.getSchedulerScheduleId() + "]  " + t.getDescription());
					result = wrapper.addTrigger(schedulerJobId, schedulerSchedule.getSchedulerScheduleId(), t);
					if (!result) {
						wrapper.removeJob(schedulerJobId);
						return;
					}
				}
				LOG.debug("Completed adding SchedulerJob[" + schedulerJobId + "] to Scheduler with " + schedulerJob.getSchedulerSchedules().size() + " SchedulerSchedules");

			}

		}

	}

	@Override
	public void start(boolean joinThread) throws Exception {
		var s = scheduler;
		if (s != null) { stop(); }

		// http://www.quartz-scheduler.org/documentation/quartz-2.0.2/configuration/
		var props = new Properties();
		props.put("org.quartz.threadPool.threadCount", "" + settings.getSchedulerThreads());
		props.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
		props.put("org.quartz.plugin.shutdownhook.class", "org.quartz.plugins.management.ShutdownHookPlugin");
		props.put("org.quartz.plugin.shutdownhook.cleanShutdown", "true");
		props.put("org.quartz.scheduler.skipUpdateCheck", "true");
		var schedulerFactory = new StdSchedulerFactory(props);
		scheduler = schedulerFactory.getScheduler();
		scheduler.setJobFactory(new JobFactory() {
			@Override
			public org.quartz.Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
				return com.maxrunsoftware.jezel.Constant.getInstance(SchedulerServiceQuartzJob.class);
			}
		});
		LOG.debug("Starting Quartz");
		scheduler.start();
		wrapper = new QuartzWrapper(scheduler);
	}

	@Override
	public void stop() throws Exception {
		var s = scheduler;
		scheduler = null;
		if (s == null) return;
		LOG.debug("Stopping Quartz");
		s.shutdown();
	}

}
