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
		this.schedulerActions = schedulerActions;
	}

	@OneToMany(mappedBy = NAME)
	private Set<SchedulerSchedule> schedulerSchedules;

	public Set<SchedulerSchedule> getSchedulerSchedules() {
		return schedulerSchedules;
	}

	public void setSchedulerSchedules(Set<SchedulerSchedule> schedulerSchedules) {
		this.schedulerSchedules = schedulerSchedules;
	}

	@OneToMany(mappedBy = NAME)
	private Set<CommandLogJob> commandLogJobs;

	public Set<CommandLogJob> getCommandLogJobs() {
		return commandLogJobs;
	}

	public void setCommandLogJobs(Set<CommandLogJob> commandLogJobs) {
		this.commandLogJobs = commandLogJobs;
	}

	@Column(length = 200, nullable = true, unique = false)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 1000, nullable = true, unique = false)
	private String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
		json.add("path", coalesce(getPath(), ""));
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
	public String toString() {
		return getClass().getSimpleName() + "[" + getSchedulerJobId() + "]";
	}

}
