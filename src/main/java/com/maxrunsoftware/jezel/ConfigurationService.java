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
package com.maxrunsoftware.jezel;

import static com.google.common.base.Preconditions.*;
import static com.maxrunsoftware.jezel.Util.*;

import java.util.Map;

public interface ConfigurationService {

	public Map<String, String> getConfigurationItems();

	public String getConfigurationItem(String key);

	public void setConfigurationItem(String key, Object value);

	public default void clearConfigurationItems() {
		var map = getConfigurationItems();
		for (var key : map.keySet()) {
			setConfigurationItem(key, null);
		}
	}

	public default Map<String, String> getConfigurationItemsPrefixed(String prefix) {
		var map = Util.<String>mapCaseInsensitive();
		prefix = checkNotNull(trimOrNull(prefix)).toLowerCase();
		if (!prefix.endsWith(".")) prefix += ".";
		var items = getConfigurationItems();
		for (var key : items.keySet()) {
			var val = items.get(key);
			if (val == null) continue;

			if (key.toLowerCase().startsWith(prefix)) {
				var name = trimOrNull(key.substring(prefix.length()));
				if (name == null) continue;

				map.put(name, val);
			}
		}

		return map;
	}
}
