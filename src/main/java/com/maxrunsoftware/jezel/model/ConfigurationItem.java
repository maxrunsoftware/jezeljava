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

import com.maxrunsoftware.jezel.JsonCodable;

@Entity
public class ConfigurationItem implements JsonCodable {
	public static final String NAME = "configurationItem";
	public static final String ID = NAME + "Id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int configurationItemId;

	public int getConfigurationItemId() {
		return configurationItemId;
	}

	public void setConfigurationItemId(int configurationItemId) {
		this.configurationItemId = configurationItemId;
	}

	@Column(length = 500, nullable = false, unique = true)
	private String name;

	public String getName() {
		return trimOrNull(name);
	}

	public void setName(String name) {
		this.name = trimOrNull(name);
	}

	@Column(length = 2000, nullable = false, unique = false)
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public JsonObject toJson() {
		var json = createObjectBuilder();
		json.add(ID, getConfigurationItemId());
		json.add("name", coalesce(getName(), ""));
		json.add("value", coalesce(getValue(), ""));
		return json.build();
	}

	@Override
	public void fromJson(JsonObject o) {
		this.setConfigurationItemId(o.getInt(ID));
		this.setName(o.getString("name"));
		this.setValue(o.getString("value"));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getConfigurationItemId() + "]";
	}

}
