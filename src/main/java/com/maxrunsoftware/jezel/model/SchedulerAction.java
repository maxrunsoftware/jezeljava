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

import java.util.HashSet;
import java.util.Set;

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.maxrunsoftware.jezel.JsonCodable;

@Entity
public class SchedulerAction implements JsonCodable {
	public static final String NAME = "schedulerAction";
	public static final String ID = NAME + "Id";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int schedulerActionId;

	public int getSchedulerActionId() {
		return schedulerActionId;
	}

	public void setSchedulerActionId(int schedulerActionId) {
		this.schedulerActionId = schedulerActionId;
	}

	@ManyToOne
	@JoinColumn(name = SchedulerJob.ID, nullable = false, referencedColumnName = SchedulerJob.ID)
	private SchedulerJob schedulerJob;

	public SchedulerJob getSchedulerJob() {
		return schedulerJob;
	}

	public void setSchedulerJob(SchedulerJob schedulerJob) {
		this.schedulerJob = schedulerJob;
	}

	@OneToMany(mappedBy = NAME)
	private Set<SchedulerActionParameter> schedulerActionParameters;

	public Set<SchedulerActionParameter> getSchedulerActionParameters() {
		if (schedulerActionParameters == null) schedulerActionParameters = new HashSet<SchedulerActionParameter>();
		return schedulerActionParameters;
	}

	public void setSchedulerActionParameters(Set<SchedulerActionParameter> schedulerActionParameters) {
		this.schedulerActionParameters = schedulerActionParameters;
	}

	@OneToMany(mappedBy = NAME)
	private Set<CommandLogAction> commandLogActions;

	public Set<CommandLogAction> getCommandLogActions() {
		if (commandLogActions == null) commandLogActions = new HashSet<CommandLogAction>();
		return commandLogActions;
	}

	public void setCommandLogActions(Set<CommandLogAction> commandLogActions) {
		this.commandLogActions = commandLogActions;
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
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(nullable = false)
	private boolean disabled;

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Column(nullable = false)
	private int index;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public JsonObject toJson() {
		var json = createObjectBuilder();
		json.add(ID, getSchedulerActionId());
		json.add(SchedulerJob.ID, getSchedulerJob().getSchedulerJobId());
		json.add("name", coalesce(getName(), ""));
		json.add("description", coalesce(getDescription(), ""));
		json.add("disabled", isDisabled());
		json.add("index", getIndex());
		var arrayBuilder = createArrayBuilder();
		for (var schedulerActionParameter : getSchedulerActionParameters()) {
			arrayBuilder.add(schedulerActionParameter.toJson());
		}
		json.add("schedulerActionParameters", arrayBuilder);

		return json.build();
	}

	@Override
	public void fromJson(JsonObject o) {
		this.setSchedulerActionId(o.getInt(ID));
		this.setName(o.getString("name"));
		this.setDescription(o.getString("description"));
		this.setDisabled(o.getBoolean("disabled"));
		this.setIndex(getIndex());
		var array = o.getJsonArray("schedulerActionParameters");
		var h = new HashSet<SchedulerActionParameter>();
		for (var item : array) {
			var p = new SchedulerActionParameter();
			p.fromJson(item.asJsonObject());
			p.setSchedulerAction(this);
			h.add(p);
		}
		this.setSchedulerActionParameters(h);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getSchedulerActionId() + "]";
	}

}
