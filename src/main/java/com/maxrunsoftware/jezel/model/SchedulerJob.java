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
package com.maxrunsoftware.jezel.model;

import static com.maxrunsoftware.jezel.Util.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.maxrunsoftware.jezel.JsonCodable;

@Entity
public class SchedulerJob implements JsonCodable {

	public static final String NAME = "schedulerJob";
	public static final String ID = NAME + "Id";

	public static final Comparator<SchedulerJob> SORT_ID = new Comparator<SchedulerJob>() {
		@Override
		public int compare(SchedulerJob o1, SchedulerJob o2) {
			if (o1 == o2) return 0;
			if (o1 == null) return -1;
			if (o2 == null) return 1;
			return Integer.valueOf(o1.getSchedulerJobId()).compareTo(o2.getSchedulerJobId());
		}
	};

	public static final Comparator<SchedulerJob> SORT_GROUP = new Comparator<SchedulerJob>() {
		@Override
		public int compare(SchedulerJob o1, SchedulerJob o2) {
			if (o1 == o2) return 0;
			if (o1 == null) return -1;
			if (o2 == null) return 1;
			return coalesce(o1.getGroup(), "").toLowerCase().compareTo(coalesce(o2.getGroup(), "").toLowerCase());
		}
	};

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int schedulerJobId;

	public int getSchedulerJobId() {
		return schedulerJobId;
	}

	public void setSchedulerJobId(int schedulerJobId) {
		this.schedulerJobId = schedulerJobId;
	}

	@OneToMany(mappedBy = NAME)
	private Set<SchedulerAction> schedulerActions;

	public Set<SchedulerAction> getSchedulerActions() {
		return schedulerActions;
	}

	public void setSchedulerActions(Set<SchedulerAction> schedulerActions) {
		if (schedulerActions == null) schedulerActions = new HashSet<SchedulerAction>();
		this.schedulerActions = schedulerActions;
	}

	@OneToMany(mappedBy = NAME)
	private Set<SchedulerSchedule> schedulerSchedules;

	public Set<SchedulerSchedule> getSchedulerSchedules() {
		return schedulerSchedules;
	}

	public void setSchedulerSchedules(Set<SchedulerSchedule> schedulerSchedules) {
		if (schedulerSchedules == null) schedulerSchedules = new HashSet<SchedulerSchedule>();
		this.schedulerSchedules = schedulerSchedules;
	}

	@OneToMany(mappedBy = NAME)
	private Set<CommandLogJob> commandLogJobs;

	public Set<CommandLogJob> getCommandLogJobs() {
		if (commandLogJobs == null) commandLogJobs = new HashSet<CommandLogJob>();
		return commandLogJobs;
	}

	public void setCommandLogJobs(Set<CommandLogJob> commandLogJobs) {
		this.commandLogJobs = commandLogJobs;
	}

	@Column(length = 200, nullable = true, unique = false)
	private String name;

	public String getName() {
		return trimOrNull(name);
	}

	public void setName(String name) {
		this.name = trimOrNull(name);
	}

	@Column(name = "grouping", length = 200, nullable = true, unique = false)
	private String group;

	public String getGroup() {
		return trimOrNull(group);
	}

	public void setGroup(String group) {
		this.group = trimOrNull(group);
	}

	@Column(nullable = false)
	private boolean disabled;

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public JsonObject toJson() {
		var json = createObjectBuilder();
		json.add(ID, getSchedulerJobId());
		json.add("name", coalesce(getName(), ""));
		json.add("group", coalesce(getGroup(), ""));
		json.add("disabled", isDisabled());

		var arrayBuilder = createArrayBuilder();
		for (var schedulerSchedule : getSchedulerSchedules()) {
			arrayBuilder.add(schedulerSchedule.toJson());
		}
		json.add("schedulerSchedules", arrayBuilder);

		arrayBuilder = createArrayBuilder();
		for (var schedulerAction : getSchedulerActions()) {
			arrayBuilder.add(schedulerAction.toJson());
		}
		json.add("schedulerActions", arrayBuilder);

		return json.build();
	}

	@Override
	public void fromJson(JsonObject o) {
		this.setSchedulerJobId(o.getInt(ID));
		this.setName(o.getString("name"));
		this.setGroup(o.getString("group"));
		this.setDisabled(o.getBoolean("disabled"));

		var array = o.getJsonArray("schedulerSchedules");
		var hss = new HashSet<SchedulerSchedule>();
		for (var item : array) {
			var p = new SchedulerSchedule();
			p.fromJson(item.asJsonObject());
			p.setSchedulerJob(this);
			hss.add(p);
		}
		this.setSchedulerSchedules(hss);

		array = o.getJsonArray("schedulerActions");
		var hsa = new HashSet<SchedulerAction>();
		for (var item : array) {
			var p = new SchedulerAction();
			p.fromJson(item.asJsonObject());
			p.setSchedulerJob(this);
			hsa.add(p);
		}
		this.setSchedulerActions(hsa);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getSchedulerJobId() + "]";
	}

}
