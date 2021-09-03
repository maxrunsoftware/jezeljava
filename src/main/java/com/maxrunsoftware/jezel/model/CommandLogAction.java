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
public class CommandLogAction implements JsonCodable {
	public static final String NAME = "commandLogAction";
	public static final String ID = NAME + "Id";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int commandLogActionId;

	public int getCommandLogActionId() {
		return commandLogActionId;
	}

	public void setCommandLogActionId(int commandLogActionId) {
		this.commandLogActionId = commandLogActionId;
	}

	@ManyToOne
	@JoinColumn(name = CommandLogJob.ID, nullable = false, referencedColumnName = CommandLogJob.ID)
	private CommandLogJob commandLogJob;

	public CommandLogJob getCommandLogJob() {
		return commandLogJob;
	}

	public void setCommandLogJob(CommandLogJob commandLogJob) {
		this.commandLogJob = commandLogJob;
	}

	@ManyToOne
	@JoinColumn(name = SchedulerAction.ID, nullable = false, referencedColumnName = SchedulerAction.ID)
	private SchedulerAction schedulerAction;

	public SchedulerAction getSchedulerAction() {
		return schedulerAction;
	}

	public void setSchedulerAction(SchedulerAction schedulerAction) {
		this.schedulerAction = schedulerAction;
	}

	@OneToMany(mappedBy = NAME, cascade = CascadeType.ALL)
	private Set<CommandLogMessage> commandLogMessages;

	public Set<CommandLogMessage> getCommandLogMessages() {
		return commandLogMessages;
	}

	public void setCommandLogMessages(Set<CommandLogMessage> commandLogMessages) {
		this.commandLogMessages = commandLogMessages;
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
		json.add(ID, getCommandLogActionId());
		json.add(CommandLogJob.ID, getCommandLogJob().getCommandLogJobId());
		json.add(SchedulerAction.ID, getSchedulerAction().getSchedulerActionId());
		json.add("start", getStart() == null ? "" : getStart().toString());
		json.add("end", getEnd() == null ? "" : getEnd().toString());
		json.add("index", getIndex());

		var arrayBuilder = createArrayBuilder();
		for (var commandLogMessage : getCommandLogMessages()) {
			arrayBuilder.add(commandLogMessage.toJson());
		}
		json.add("commandLogActions", arrayBuilder);

		return json.build();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getCommandLogActionId() + "]";
	}

}
