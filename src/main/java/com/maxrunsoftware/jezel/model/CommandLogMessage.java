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

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.maxrunsoftware.jezel.JsonCodable;

@Entity
public class CommandLogMessage implements JsonCodable {
	public static final String NAME = "commandLogMessage";
	public static final String ID = NAME + "Id";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int commandLogMessageId;

	public int getCommandLogMessageId() {
		return commandLogMessageId;
	}

	public void setCommandLogMessageId(int commandLogMessageId) {
		this.commandLogMessageId = commandLogMessageId;
	}

	@ManyToOne
	@JoinColumn(name = CommandLogAction.ID, nullable = false, referencedColumnName = CommandLogAction.ID)
	private CommandLogAction commandLogAction;

	public CommandLogAction getCommandLogAction() {
		return commandLogAction;
	}

	public void setCommandLogAction(CommandLogAction commandLogAction) {
		this.commandLogAction = commandLogAction;
	}

	@Column(length = 10, nullable = false, unique = false)
	private String level;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@Column(nullable = false)
	private LocalDateTime timestamp;

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	@Column(length = 1000, nullable = true, unique = false)
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = trimOrNull(message);
	}

	@Column(length = 4000, nullable = true, unique = false)
	private String exception;

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = trimOrNull(exception);
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
		json.add(ID, getCommandLogMessageId());
		json.add(CommandLogAction.ID, getCommandLogAction().getCommandLogActionId());
		json.add("level", coalesce(getLevel(), ""));
		json.add("timestamp", getTimestamp() == null ? "" : getTimestamp().toString());
		json.add("message", coalesce(getMessage(), ""));
		json.add("exception", coalesce(getException(), ""));
		json.add("index", getIndex());
		return json.build();
	}

	@Override
	public void fromJson(JsonObject o) {
		this.setCommandLogMessageId(o.getInt(ID));
		this.setLevel(o.getString("level"));
		var ts = trimOrNull(o.getString("timestamp"));
		if (ts != null) this.setTimestamp(LocalDateTime.parse(ts));
		this.setMessage(o.getString("message"));
		this.setException(o.getString("exception"));
		this.setIndex(o.getInt("index"));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getCommandLogMessageId() + "]";
	}

}
