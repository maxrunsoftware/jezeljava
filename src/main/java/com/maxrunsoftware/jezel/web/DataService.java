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
		Collections.sort(list, SchedulerSchedule.SORT_ID);
		return list;
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
				"job",
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

}