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
package com.maxrunsoftware.jezel.service;

import static com.google.common.base.Preconditions.*;
import static com.maxrunsoftware.jezel.Util.*;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.Session;

import com.maxrunsoftware.jezel.ConfigurationService;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.Util;
import com.maxrunsoftware.jezel.model.ConfigurationItem;

public class ConfigurationServiceDatabase implements ConfigurationService {
	private DatabaseService db;

	@Inject
	public ConfigurationServiceDatabase(DatabaseService db) {
		this.db = checkNotNull(db);
	}

	@Override
	public String getConfigurationItem(String key) {
		try (var session = db.openSession()) {
			var item = getConfigurationItem(session, key);
			if (item == null) return null;
			return item.getValue();
		}
	}

	private List<ConfigurationItem> getAllConfigurationItems(Session session) {
		return getAll(ConfigurationItem.class, session);
	}

	private ConfigurationItem getConfigurationItem(Session session, String key) {
		key = trimOrNull(key);
		if (key == null) return null;
		key = key.toLowerCase();

		// TODO: Hibernate WHERE clause
		for (var item : getAllConfigurationItems(session)) {
			var keyOther = item.getName().toLowerCase();
			if (key.equals(keyOther)) return item;
		}
		return null;

	}

	@Override
	public void setConfigurationItem(String key, Object value) {
		key = trimOrNull(key);
		checkNotNull(key);
		key = key.toLowerCase();

		var valueString = value == null ? null : value.toString();
		if (trimOrNull(valueString) == null) valueString = null;

		try (var session = db.openSession()) {
			var item = getConfigurationItem(session, key);
			if (item == null && valueString == null) {
				// NOOP
			} else if (item == null && valueString != null) {
				// ADD
				item = new ConfigurationItem();
				item.setName(key);
				item.setValue(valueString);
				save(session, item);
			} else if (item != null && valueString == null) {
				// DELETE
				delete(ConfigurationItem.class, session, item.getConfigurationItemId());
			} else if (item != null && valueString != null) {
				// UPDATE
				item.setValue(key);
				save(session, item);
			}
		}
	}

	@Override
	public Map<String, String> getConfigurationItems() {
		var map = Util.<String>mapCaseInsensitive();

		try (var session = db.openSession()) {
			var list = getAllConfigurationItems(session);

			for (var item : list) {
				map.put(item.getName().toLowerCase(), item.getValue());
			}
		}

		return map;
	}

}
