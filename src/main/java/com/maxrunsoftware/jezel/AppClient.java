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

import static com.maxrunsoftware.jezel.Util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppClient {
	private final Map<String, String> properties;
	private final List<String> arguments;

	public AppClient(String[] args) {
		if (args == null || args.length == 0) { throw new IllegalArgumentException("No args specified"); }

		var props = Util.<String>mapCaseInsensitive();
		var argsList = new ArrayList<String>();
		for (var arg : args) {
			if (trimOrNull(arg) == null) continue;
			if (arg.startsWith("-")) {
				while (arg.startsWith("-"))
					arg = arg.substring(1);
				if (trimOrNull(arg) == null) continue;

				var sbKey = new StringBuilder();
				var sbVal = new StringBuilder();
				var workingOnVal = false;

				for (char c : arg.toCharArray()) {
					if (workingOnVal) {
						sbVal.append(c);
					} else {
						if (c == '=') {
							workingOnVal = true;
						} else {
							sbKey.append(c);
						}
					}
				}

				var key = trimOrNull(sbKey.toString());
				if (key == null) continue;
				var val = sbVal.toString();
				if (trimOrNull(val) == null) val = "true";
				props.put(key, val);
			} else {
				argsList.add(trimOrNull(arg));
			}
		}

		properties = props;
		arguments = argsList;

	}

	private String getProp(String... propNames) {
		for (var propName : propNames) {
			var val = properties.get(propName);
			if (val != null) return val;
		}
		return null;
	}

	public void execute() {
		var h = getProp("h");
		if (h == null) throw new IllegalArgumentException("No '-h=<host>' specified");
		if (!h.toLowerCase().startsWith("http")) h = "http://" + h;
		if (!h.endsWith("/")) h = h + "/";

		var u = getProp("u");
		if (u == null) throw new IllegalArgumentException("No '-u=<username>' specified");
		var p = getProp("p");
		if (p == null) throw new IllegalArgumentException("No '-p=<password>' specified");

		if (arguments.size() < 1) throw new IllegalArgumentException("No action specified");
		var action = arguments.remove(0).toLowerCase();

	}
}
