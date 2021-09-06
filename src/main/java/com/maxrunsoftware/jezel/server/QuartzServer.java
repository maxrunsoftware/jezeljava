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
package com.maxrunsoftware.jezel.server;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class QuartzServer {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QuartzServer.class);

	private QuartzServerExecutor executor;
	private Scheduler scheduler;

	private static JobKey createJobKey(int schedulerJobId) {
		return new JobKey("" + schedulerJobId);
	}

	private static TriggerKey createTriggerKey(int schedulerScheduleId) {
		return new TriggerKey("" + schedulerScheduleId);
	}

	public Boolean existsJob(int jobId) {
		try {
			return scheduler.checkExists(createJobKey(jobId));
		} catch (SchedulerException e) {
			LOG.error("Error checking if Job[" + jobId + "] exists in scheduler", e);
			return null;
		}
	}

	public Boolean existsTrigger(int triggerId) {
		try {
			return scheduler.checkExists(createTriggerKey(triggerId));
		} catch (SchedulerException e) {
			LOG.error("Error checking if Trigger[" + triggerId + "] exists in scheduler", e);
			return null;
		}
	}

	public boolean removeJob(int jobId) {

		var exists = existsJob(jobId);
		if (exists == null) return false;

		if (exists) {
			try {
				scheduler.deleteJob(createJobKey(jobId));
			} catch (SchedulerException e) {
				LOG.error("Error deleting existing Job[" + jobId + "] in scheduler", e);
				return false;
			}
		}

		return true;
	}

	public boolean removeTrigger(int triggerId) {

		var exists = existsTrigger(triggerId);
		if (exists == null) return false;

		if (exists) {
			try {
				scheduler.unscheduleJob(createTriggerKey(triggerId));
			} catch (SchedulerException e) {
				LOG.error("Error deleting existing Trigger[" + triggerId + "] in scheduler", e);
				return false;
			}
		}

		return true;
	}

	public boolean addTrigger(
			int jobId,
			int triggerId,
			boolean sunday,
			boolean monday,
			boolean tuesday,
			boolean wednesday,
			boolean thursday,
			boolean friday,
			boolean saturday,
			int hour,
			int minute) {

		var daysList = new ArrayList<Integer>();
		if (sunday) daysList.add(1);
		if (monday) daysList.add(2);
		if (tuesday) daysList.add(3);
		if (wednesday) daysList.add(4);
		if (thursday) daysList.add(5);
		if (friday) daysList.add(6);
		if (saturday) daysList.add(7);
		var days = daysList.toArray(Integer[]::new);

		if (hour > 23) hour = 23;
		if (hour < 0) hour = 0;
		if (minute > 59) minute = 59;
		if (minute < 0) minute = 0;

		var desc = new StringBuilder();
		desc.append(StringUtils.right("000" + hour, 2));
		desc.append(StringUtils.right("000" + minute, 2));
		desc.append(sunday ? "Y" : "N");
		desc.append(monday ? "Y" : "N");
		desc.append(tuesday ? "Y" : "N");
		desc.append(wednesday ? "Y" : "N");
		desc.append(thursday ? "Y" : "N");
		desc.append(friday ? "Y" : "N");
		desc.append(saturday ? "Y" : "N");

		var trigger = TriggerBuilder.newTrigger()
				.forJob(createJobKey(jobId))
				.withIdentity(createTriggerKey(triggerId))
				.withDescription(desc.toString())
				.withSchedule(CronScheduleBuilder.atHourAndMinuteOnGivenDaysOfWeek(hour, minute, days))
				.build();

		return addTrigger(jobId, triggerId, trigger);
	}

	public boolean addTrigger(int jobId, int triggerId, Trigger trigger) {
		var removeResult = removeTrigger(triggerId);
		if (!removeResult) return false; // Should already be logged in removeTrigger

		try {
			scheduler.scheduleJob(trigger);
			return true;
		} catch (SchedulerException e) {
			LOG.error("Error scheduling Job[" + jobId + "] with SchedulerSchedule[" + triggerId + "]");
			return false;
		}
	}

	private static class QuartzJob implements Job {

		private final QuartzServer server;

		public QuartzJob(QuartzServer server) {
			this.server = checkNotNull(server);
		}

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			var jobId = Integer.parseInt(context.getJobDetail().getKey().getName());
			var triggerId = Integer.parseInt(context.getTrigger().getKey().getName());
			var executor = server.getExecutor();
			executor.execute(jobId, triggerId);
		}

	}

	public boolean addJob(int jobId) {

		var removeResult = removeJob(jobId);
		if (!removeResult) return false; // Should already be logged in removeJob

		var jobDetail = JobBuilder.newJob()
				.withIdentity(createJobKey(jobId))
				.ofType(QuartzJob.class)
				.storeDurably()
				.build();

		try {
			scheduler.addJob(jobDetail, true);
			return true;
		} catch (SchedulerException e) {
			LOG.error("Error adding Job[" + jobId + "] to scheduler", e);
			return false;
		}
	}

	public QuartzServerExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(QuartzServerExecutor executor) {
		this.executor = executor;
	}

	private int threadCount;

	public void start() throws SchedulerException {
		if (scheduler != null) { stop(); }
		LOG.debug("Starting: " + getClass().getSimpleName());

		// http://www.quartz-scheduler.org/documentation/quartz-2.0.2/configuration/
		var props = new Properties();
		props.put("org.quartz.threadPool.threadCount", "" + getThreadCount());
		props.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
		props.put("org.quartz.plugin.shutdownhook.class", "org.quartz.plugins.management.ShutdownHookPlugin");
		props.put("org.quartz.plugin.shutdownhook.cleanShutdown", "true");
		props.put("org.quartz.scheduler.skipUpdateCheck", "true");
		var schedulerFactory = new StdSchedulerFactory(props);
		scheduler = schedulerFactory.getScheduler();
		var t = this;
		scheduler.setJobFactory(new JobFactory() {
			@Override
			public org.quartz.Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
				return new QuartzJob(t);
			}
		});
		LOG.debug("Starting Quartz");
		scheduler.start();
		LOG.debug("Started: " + getClass().getSimpleName());
	}

	public void stop() throws SchedulerException {
		var s = scheduler;
		scheduler = null;
		if (s == null) return;
		LOG.debug("Stopping Quartz");
		s.shutdown();
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
}
