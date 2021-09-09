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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.json.JsonObject;
import javax.persistence.CascadeType;
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
public class CommandLogJob implements JsonCodable {
	public static final String NAME = "commandLogJob";
	public static final String ID = NAME + "Id";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int commandLogJobId;

	public int getCommandLogJobId() {
		return commandLogJobId;
	}

	public void setCommandLogJobId(int commandLogJobId) {
		this.commandLogJobId = commandLogJobId;
	}

	@OneToMany(mappedBy = NAME, cascade = CascadeType.ALL)
	private Set<CommandLogAction> commandLogActions;

	public Set<CommandLogAction> getCommandLogActions() {
		if (commandLogActions == null) commandLogActions = new HashSet<CommandLogAction>();
		return commandLogActions;
	}

	public void setCommandLogActions(Set<CommandLogAction> commandLogActions) {
		this.commandLogActions = commandLogActions;
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

	@Column(nullable = false)
	private LocalDateTime start;

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	@Column(nullable = true)
	private LocalDateTime end;

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	@Column(nullable = false)
	private boolean error;

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	@Override
	public JsonObject toJson() {
		var json = createObjectBuilder();
		json.add(ID, getCommandLogJobId());
		json.add(SchedulerJob.ID, getSchedulerJob().getSchedulerJobId());
		json.add("start", getStart() == null ? "" : getStart().toString());
		json.add("end", getEnd() == null ? "" : getEnd().toString());
		json.add(SchedulerJob.NAME, getSchedulerJob().toJson());
		var arrayBuilder = createArrayBuilder();
		for (var commandLogAction : getCommandLogActions()) {
			arrayBuilder.add(commandLogAction.toJson());
		}
		json.add("commandLogActions", arrayBuilder);

		return json.build();
	}

	@Override
	public void fromJson(JsonObject o) {
		this.setCommandLogJobId(o.getInt(ID));
		var st = trimOrNull(o.getString("start"));
		if (st != null) this.setStart(LocalDateTime.parse(st));
		var en = trimOrNull(o.getString("end"));
		if (en != null) this.setEnd(LocalDateTime.parse(en));

		var schedulerJob = new SchedulerJob();
		schedulerJob.fromJson(o.getJsonObject(SchedulerJob.NAME));
		this.setSchedulerJob(schedulerJob);

		var array = o.getJsonArray("commandLogActions");
		var hss = new HashSet<CommandLogAction>();
		for (var item : array) {
			var p = new CommandLogAction();
			p.fromJson(item.asJsonObject());
			p.setCommandLogJob(this);
			hss.add(p);
		}
		this.setCommandLogActions(hss);

	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getCommandLogJobId() + "]";
	}

}
