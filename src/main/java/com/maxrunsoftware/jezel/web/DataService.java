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
package com.maxrunsoftware.jezel.web;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.maxrunsoftware.jezel.Util;
import com.maxrunsoftware.jezel.model.CommandLogJob;
import com.maxrunsoftware.jezel.model.ConfigurationItem;
import com.maxrunsoftware.jezel.model.SchedulerJob;
import com.maxrunsoftware.jezel.model.SchedulerSchedule;
import com.maxrunsoftware.jezel.web.RestClient.ParamNameValue;
import com.maxrunsoftware.jezel.web.RestClient.Verb;

public class DataService {

	private final RestClient client;

	public DataService(RestClient client) {
		this.client = checkNotNull(client);
	}

	public List<SchedulerJob> getSchedulerJob(Integer schedulerJobId) throws IOException {
		var response = client.get(
				Verb.GET,
				"job",
				par(SchedulerJob.ID, schedulerJobId));
		var o = response.jsonObject();
		var array = o.getJsonArray(SchedulerJob.NAME);
		var list = new ArrayList<SchedulerJob>();
		for (var val : array) {
			var oo = val.asJsonObject();
			var ooo = new SchedulerJob();
			ooo.fromJson(oo);
			list.add(ooo);
		}
		Collections.sort(list, SchedulerJob.SORT_ID);
		return list;
	}

	public List<SchedulerSchedule> getSchedulerSchedule(Integer schedulerJobId, Integer schedulerScheduleId) throws IOException {

		//@formatter:off
		var response = client.get(
				Verb.GET,
				"job/schedule",
				par(SchedulerJob.ID, schedulerJobId),
				par(SchedulerSchedule.ID, schedulerScheduleId));
		var o = response.jsonObject();
		var array = o.getJsonArray(SchedulerSchedule.NAME);
		var list = new ArrayList<SchedulerSchedule>();
		for (var val : array) {
			var oo = val.asJsonObject();
			var ooo = new SchedulerSchedule();
			ooo.fromJson(oo);
			list.add(ooo);
		}
		
		/*
		var list = new ArrayList<SchedulerSchedule>();
		var jobs = getSchedulerJob(null);
		for(var job : jobs) {
			if (schedulerJobId != null) {
				// Check if we were provided a schedulerJobId
				if (!schedulerJobId.equals(job.getSchedulerJobId())) {
					continue;
				}
			}
			for(var schedule : job.getSchedulerSchedules()) {
				if (schedulerScheduleId != null) {
					// Check if we were provided a schedulerScheduleId
					if (!schedulerScheduleId.equals(schedule.getSchedulerScheduleId())) {
						continue;
					}
				}
				list.add(schedule);
			}
		}
		*/
		Collections.sort(list, SchedulerSchedule.SORT_ID);
		return list;
		//@formatter:on
	}

	private static ParamNameValue par(String key, Object value) {
		return new ParamNameValue(key, value);
	}

	public void updateSchedulerJob(int schedulerJobId, String name, String group, boolean disabled) throws IOException {
		client.get(
				Verb.POST,
				"job",
				par(SchedulerJob.ID, schedulerJobId),
				par("name", name),
				par("group", group),
				par("disabled", disabled));

	}

	public void updateSchedulerSchedule(
			int schedulerScheduleId,
			boolean sunday,
			boolean monday,
			boolean tuesday,
			boolean wednesday,
			boolean thursday,
			boolean friday,
			boolean saturday,
			int hour,
			int minute,
			boolean disabled) throws IOException {
		client.get(
				Verb.POST,
				"job/schedule",
				par(SchedulerSchedule.ID, schedulerScheduleId),
				par("sunday", sunday),
				par("monday", monday),
				par("tuesday", tuesday),
				par("wednesday", wednesday),
				par("thursday", thursday),
				par("friday", friday),
				par("saturday", saturday),
				par("hour", hour),
				par("minute", minute),
				par("disabled", disabled));

	}

	public int addSchedulerSchedule(
			int schedulerJobId,
			boolean sunday,
			boolean monday,
			boolean tuesday,
			boolean wednesday,
			boolean thursday,
			boolean friday,
			boolean saturday,
			int hour,
			int minute,
			boolean disabled) throws IOException {
		var response = client.get(
				Verb.PUT,
				"job/schedule",
				par(SchedulerJob.ID, schedulerJobId));
		var schedulerScheduleId = response.jsonObject().getInt(SchedulerSchedule.ID);
		updateSchedulerSchedule(
				schedulerScheduleId,
				sunday,
				monday,
				tuesday,
				wednesday,
				thursday,
				friday,
				saturday,
				hour,
				minute,
				disabled);
		return schedulerScheduleId;
	}

	public void deleteSchedulerSchedule(int schedulerScheduleId) throws IOException {
		client.get(
				Verb.DELETE,
				"job/schedule",
				par(SchedulerSchedule.ID, schedulerScheduleId));

	}

	public List<CommandLogJob> getCommandLogJob(Integer commandLogJob, Integer schedulerJobId) throws IOException {
		var response = client.get(Verb.GET,
				"log/job",
				par(CommandLogJob.ID, commandLogJob),
				par(SchedulerJob.ID, schedulerJobId));

		var o = response.jsonObject();
		var array = o.getJsonArray(CommandLogJob.NAME);
		var list = new ArrayList<CommandLogJob>();
		for (var val : array) {
			var oo = val.asJsonObject();
			var ooo = new CommandLogJob();
			ooo.fromJson(oo);
			list.add(ooo);
		}

		Collections.sort(list, CommandLogJob.SORT_JOB);
		return list;
	}

	public Map<String, String> getConfigurationItems() throws IOException {
		var response = client.get(Verb.GET,
				"config");

		var o = response.jsonObject();
		var array = o.getJsonArray(ConfigurationItem.NAME);
		var map = Util.<String>mapCaseInsensitive();
		for (var val : array) {
			var oo = val.asJsonObject();
			var name = oo.getString("name");
			var value = oo.getString("value");
			map.put(name, value);
		}
		return map;
	}

	public void saveConfigurationItems(Map<String, String> map) throws IOException {

		var response = client.get(Verb.GET,
				"log/job",
				par(CommandLogJob.ID, commandLogJob),
				par(SchedulerJob.ID, schedulerJobId));

	}
}
