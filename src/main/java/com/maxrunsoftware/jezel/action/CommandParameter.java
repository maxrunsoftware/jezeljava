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
package com.maxrunsoftware.jezel.action;

import static com.maxrunsoftware.jezel.Util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.json.JsonObject;

import org.hibernate.Session;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.JsonCodable;
import com.maxrunsoftware.jezel.model.ConfigurationItem;

public class CommandParameter implements JsonCodable {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CommandParameter.class);

	public static final String NAME = "commandParameter";

	public static final String TYPE_STRING = "string";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_INT = "integer";
	public static final String TYPE_BOOL = "boolean";
	public static final String TYPE_FILENAME = "filename";
	public static final String TYPE_OPTION = "option";

	public CommandParameter() {}

	public CommandParameter(String name, String description, String type) {
		setName(name);
		setDescription(description);
		setType(type);
	}

	private String clazz;

	public String getClazz() {
		return trimOrNull(clazz);
	}

	public void setClazz(String clazz) {
		this.clazz = trimOrNull(clazz);
	}

	private String name;

	public String getName() {
		return trimOrNull(name);
	}

	public void setName(String name) {
		this.name = trimOrNull(name);
	}

	public String getNameFull() {
		return getClazz() + "." + getName();
	}

	private String description;

	public String getDescription() {
		return trimOrNull(description);
	}

	public void setDescription(String description) {
		this.description = trimOrNull(description);
	}

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

	private Integer minValue;

	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	private Integer maxValue;

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	private String defaultValue;

	public String getDefaultValue() {
		return trimOrNull(defaultValue);
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = trimOrNull(defaultValue);
	}

	private List<String> optionValues;

	public List<String> getOptionValues() {
		if (optionValues == null) optionValues = new ArrayList<String>();
		return optionValues;
	}

	public void setOptionValues(List<String> optionValues) {
		this.optionValues = optionValues;
	}

	public static CommandParameter createString(String name, String description) {
		return new CommandParameter(name, description, TYPE_STRING);
	}

	public static CommandParameter createText(String name, String description) {
		return new CommandParameter(name, description, TYPE_TEXT);
	}

	public static CommandParameter createInteger(String name, String description) {
		return new CommandParameter(name, description, TYPE_INT);
	}

	public static CommandParameter createInteger(String name, String description, int minValue, int maxValue) {
		var p = new CommandParameter(name, description, TYPE_INT);
		p.minValue = minValue;
		p.maxValue = maxValue;
		return p;
	}

	public static CommandParameter createInteger(String name, String description, int minValue, int maxValue, int defaultValue) {
		var p = new CommandParameter(name, description, TYPE_INT);
		p.minValue = minValue;
		p.maxValue = maxValue;
		p.defaultValue = "" + defaultValue;
		return p;
	}

	public static CommandParameter createBoolean(String name, String description) {
		var p = new CommandParameter(name, description, TYPE_BOOL);
		p.defaultValue = "false";
		return p;
	}

	public static CommandParameter createFilename(String name, String description) {
		return new CommandParameter(name, description, TYPE_FILENAME);
	}

	public static CommandParameter createOption(String name, String description, String defaultValue, String... options) {
		var p = new CommandParameter(name, description, TYPE_OPTION);
		p.defaultValue = defaultValue;
		for (var s : options) {
			p.getOptionValues().add(s);
		}
		return p;
	}

	@Override
	public JsonObject toJson() {
		var json = createObjectBuilder();
		json.add("name", coalesce(getName(), ""));
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
		this.setName(o.getString("name"));
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

	public static List<CommandParameter> getAll() {
		var list = new ArrayList<CommandParameter>();
		for (var c : Constant.COMMANDS) {
			try {
				var o = c.getDeclaredConstructor().newInstance();
				list.addAll(o.getParameterDetails());
			} catch (Exception e) {
				throw new Error(e);
			}
		}
		return list;
	}

	public static List<CommandParameter> getForCommand(String commandName) {
		var list = new ArrayList<CommandParameter>();
		commandName = trimOrNull(commandName);
		if (commandName == null) return list;
		for (var cp : getAll()) {
			if (cp.getClazz().equalsIgnoreCase(commandName)) { list.add(cp); }
		}

		return list;
	}

	public static CommandParameter get(String nameFull) {
		for (var cp : getAll()) {
			if (cp.getNameFull().equalsIgnoreCase(nameFull)) return cp;
		}
		return null;
	}

	public static List<CommandParameter> getWithPrefix(String prefix) {
		var list = new ArrayList<CommandParameter>();
		for (var cp : getAll()) {
			if (cp.getClazz().equalsIgnoreCase(prefix)) list.add(cp);
		}
		return list;
	}

	public static void initializeConfigurationItems(Session session) {

		LOG.debug("initializeConfigurationItems");
		var cpsHash = new HashMap<String, CommandParameter>();
		for (var cp : getAll()) {
			cpsHash.put(cp.getNameFull(), cp);
		}

		var ciHash = new HashSet<String>();
		for (var ci : ConfigurationItem.getValues(session).keySet()) {
			ciHash.add(ci);
		}

		for (var cpName : cpsHash.keySet()) {
			if (!ciHash.contains(cpName)) {
				var cp = cpsHash.get(cpName);
				var value = cp.getDefaultValue();
				LOG.debug("Adding ConfigurationItem [" + cpName + "]: " + value);
				ConfigurationItem.setValue(session, cpName, cp.getDefaultValue());

			}
		}

	}

	public static void initializeConfigurationItems() {
		try (var session = Constant.getInstance(DatabaseService.class).openSession()) {
			initializeConfigurationItems(session);
		}
	}

}
