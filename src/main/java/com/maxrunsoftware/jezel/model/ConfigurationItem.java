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

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.Session;

import com.maxrunsoftware.jezel.JsonCodable;

@Entity
public class ConfigurationItem implements JsonCodable {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationItem.class);

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

	@Lob
	@Column(nullable = true, unique = false)
	private String value;

	public String getValue() {
		return trimOrNull(value);
	}

	public void setValue(String value) {
		this.value = trimOrNull(value);
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

	public static Map<String, String> getValues(Session session) {
		var map = new HashMap<String, String>();
		for (var item : getAll(ConfigurationItem.class, session)) {
			map.put(item.getName(), item.getValue());
		}
		return map;
	}

	public static Map<String, String> getValuesWithPrefix(Session session, String prefix) {
		var map = new HashMap<String, String>();
		prefix = prefix.toLowerCase();
		if (!prefix.endsWith(".")) prefix += ".";
		var cis = getValues(session);
		for (var name : cis.keySet()) {
			var value = cis.get(name);
			if (name.toLowerCase().startsWith(prefix)) {
				name = name.substring(prefix.length());
				map.put(name, value);
			}

		}
		return map;

	}

	public static void setValue(Session session, String name, String value) {
		name = trimOrNull(name);
		if (name == null) return;
		value = trimOrNull(value);

		ConfigurationItem o = get(session, name);
		if (o == null) {
			// create
			o = new ConfigurationItem();
			o.setName(name);
			o.setValue(value);
			LOG.debug("Creating " + ConfigurationItem.class.getSimpleName() + " [" + name + "]: " + value);
			save(session, o);
		} else {
			// update
			o.setValue(value);
			LOG.debug("Saving " + ConfigurationItem.class.getSimpleName() + " [" + o.getName() + "]: " + value);
			save(session, o);
		}

	}

	public static ConfigurationItem get(Session session, String name) {
		name = trimOrNull(name);
		if (name == null) return null;
		for (var item : getAll(ConfigurationItem.class, session)) {
			if (name.equalsIgnoreCase(item.getName())) { return item; }
		}
		return null;
	}

	public static boolean setValueExisting(Session session, String name, String value) {
		name = trimOrNull(name);
		if (name == null) return false;
		value = trimOrNull(value);

		ConfigurationItem o = get(session, name);
		if (o == null) return false;
		o.setValue(value);
		save(session, o);
		return true;

	}

	public static boolean remove(Session session, String name) {
		name = trimOrNull(name);
		if (name == null) return false;
		boolean foundOne = false;
		for (var item : getAll(ConfigurationItem.class, session)) {
			if (name.equalsIgnoreCase(item.getName())) {
				delete(session, item);
				foundOne = true;
			}
		}
		return foundOne;
	}

	public static String getValue(Session session, String name) {
		name = trimOrNull(name);
		if (name == null) return null;
		for (var item : getAll(ConfigurationItem.class, session)) {
			if (name.equalsIgnoreCase(item.getName())) return item.getValue();
		}
		return null;
	}

}
