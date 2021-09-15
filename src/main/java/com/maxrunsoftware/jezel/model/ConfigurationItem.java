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

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.maxrunsoftware.jezel.JsonCodable;

@Entity
public class ConfigurationItem implements JsonCodable {
	public static final String TYPE_STRING = "string";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_INT = "integer";
	public static final String TYPE_BOOL = "boolean";
	public static final String TYPE_FILENAME = "filename";
	public static final String TYPE_OPTION = "option";

	public static final String NAME = "configurationItem";
	public static final String ID = NAME + "Id";

	public ConfigurationItem() {}

	public ConfigurationItem(String name, String description, String type) {
		this.name = name;
		this.description = description;
		this.type = type;
	}

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
		return trimOrNullLower(name);
	}

	public void setName(String name) {
		this.name = trimOrNullLower(name);
	}

	@Column(length = 2000, nullable = false, unique = false)
	private String value;

	public String getValue() {
		return trimOrNull(value);
	}

	public void setValue(String value) {
		this.value = trimOrNull(value);
	}

	@Transient
	private String description;

	public String getDescription() {
		return trimOrNull(description);
	}

	public void setDescription(String description) {
		this.description = trimOrNull(description);
	}

	@Transient
	private String type;

	public String getType() {
		return trimOrNullLower(type);
	}

	public void setType(String type) {
		type = trimOrNull(type);
		if (type == null) {
			this.type = type;
		} else {
			type = type.toLowerCase();
			if (equalsAny(type, TYPE_STRING, TYPE_TEXT, TYPE_INT, TYPE_BOOL, TYPE_FILENAME, TYPE_OPTION)) {
				this.type = type;
			} else {
				throw new IllegalArgumentException("Invalid type: " + type);
			}
		}
	}

	@Transient
	private Integer minValue;

	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	@Transient
	private Integer maxValue;

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	@Transient
	private String defaultValue;

	public String getDefaultValue() {
		return trimOrNull(defaultValue);
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = trimOrNull(defaultValue);
	}

	@Transient
	private List<String> optionValues;

	public List<String> getOptionValues() {
		if (optionValues == null) optionValues = new ArrayList<String>();
		return optionValues;
	}

	public void setOptionValues(List<String> optionValues) {
		this.optionValues = optionValues;
	}

	@Override
	public JsonObject toJson() {
		var json = createObjectBuilder();
		json.add(ID, getConfigurationItemId());
		json.add("name", coalesce(getName(), ""));
		json.add("value", coalesce(getValue(), ""));
		json.add("description", coalesce(getDescription(), ""));
		json.add("type", coalesce(getType(), ""));
		json.add("minValue", getMinValue() == null ? "" : getMinValue().toString());
		json.add("maxValue", getMaxValue() == null ? "" : getMaxValue().toString());
		json.add("defaultValue", coalesce(getDefaultValue(), ""));

		var ab = createArrayBuilder();
		for (var optionValue : getOptionValues()) {
			ab.add(optionValue);
		}
		json.add("optionValues", ab.build());

		return json.build();
	}

	@Override
	public void fromJson(JsonObject o) {
		this.setConfigurationItemId(o.getInt(ID));
		this.setName(o.getString("name"));
		this.setValue(o.getString("value"));
		this.setDescription(o.getString("description"));
		this.setType(o.getString("type"));
		var minV = trimOrNull(o.getString("minValue"));
		setMinValue(minV == null ? null : parseInt(minV));

		var maxV = trimOrNull(o.getString("maxValue"));
		this.setMaxValue(maxV == null ? null : parseInt(maxV));

		this.setDefaultValue(o.getString("defaultValue"));

		var ar = o.getJsonArray("optionValues");
		var ovs = new ArrayList<String>();
		for (int i = 0; i < ar.size(); i++) {
			ovs.add(ar.getString(i));
		}
		this.setOptionValues(ovs);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getConfigurationItemId() + "]";
	}

	public static ConfigurationItem createString(String name, String description) {
		return new ConfigurationItem(name, description, TYPE_STRING);
	}

	public static ConfigurationItem createText(String name, String description) {
		return new ConfigurationItem(name, description, TYPE_TEXT);
	}

	public static ConfigurationItem createInteger(String name, String description) {
		return new ConfigurationItem(name, description, TYPE_INT);
	}

	public static ConfigurationItem createInteger(String name, String description, int minValue, int maxValue) {
		var p = new ConfigurationItem(name, description, TYPE_INT);
		p.minValue = minValue;
		p.maxValue = maxValue;
		return p;
	}

	public static ConfigurationItem createInteger(String name, String description, int minValue, int maxValue, int defaultValue) {
		var p = new ConfigurationItem(name, description, TYPE_INT);
		p.minValue = minValue;
		p.maxValue = maxValue;
		p.defaultValue = "" + defaultValue;
		return p;
	}

	public static ConfigurationItem createBoolean(String name, String description) {
		var p = new ConfigurationItem(name, description, TYPE_BOOL);
		p.defaultValue = "false";
		return p;
	}

	public static ConfigurationItem createFilename(String name, String description) {
		return new ConfigurationItem(name, description, TYPE_FILENAME);
	}

	public static ConfigurationItem createOption(String name, String description, String defaultValue, String... options) {
		var p = new ConfigurationItem(name, description, TYPE_OPTION);
		p.defaultValue = defaultValue;
		for (var s : options) {
			p.getOptionValues().add(s);
		}
		return p;
	}

	public Object getValueOrDefault() {
		if (getType() == null || equalsAny(getType(), TYPE_STRING, TYPE_TEXT, TYPE_FILENAME)) {
			var value = getValue();
			if (value != null) return value;
			return getDefaultValue();
		}

		if (equalsAny(getType(), TYPE_BOOL)) {
			var value = getValue();
			if (value != null) return parseBoolean(value);
			value = getDefaultValue();
			if (value != null) return parseBoolean(value);
			return Boolean.FALSE.toString();
		}

		if (equalsAny(getType(), TYPE_INT)) {
			var value = getValue();
			if (value != null) return parseInt(value);
			value = getDefaultValue();
			if (value != null) return parseInt(value);
			return Integer.valueOf(0).toString();
		}

		if (equalsAny(getType(), TYPE_OPTION)) {
			var value = getValue();
			if (value != null) return value;
			value = getDefaultValue();
			if (value != null) return value;
			return null;
		}

		var value = getValue();
		if (value != null) return value;
		value = getDefaultValue();
		if (value != null) return value;
		return null;
	}

}
