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
public class SchedulerActionParameter implements JsonCodable {
	public static final String NAME = "schedulerActionParameter";
	public static final String ID = NAME + "Id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int schedulerActionParameterId;

	public int getSchedulerActionParameterId() {
		return schedulerActionParameterId;
	}

	public void setSchedulerActionParameterId(int schedulerActionParameterId) {
		this.schedulerActionParameterId = schedulerActionParameterId;
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

	@Column(length = 200, nullable = false, unique = false)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = trimOrNull(name);
	}

	@Column(length = 1000, nullable = false, unique = false)
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
		json.add(ID, getSchedulerActionParameterId());
		json.add(SchedulerAction.ID, getSchedulerAction().getSchedulerActionId());
		json.add("name", coalesce(getName(), ""));
		json.add("value", coalesce(getValue(), ""));
		json.add("disabled", isDisabled());
		return json.build();
	}

	@Override
	public void fromJson(JsonObject o) {
		this.setSchedulerActionParameterId(o.getInt(ID));
		this.setName(o.getString("name"));
		this.setValue(o.getString("value"));
		this.setDisabled(o.getBoolean("disabled"));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getSchedulerActionParameterId() + "]";
	}

}
