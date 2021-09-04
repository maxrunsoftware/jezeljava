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

import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

public interface SettingService {

	public default String getDir() {
		return coalesce(getEnvironmentVariable("JEZEL_Dir", trimOrNull(Paths.get(".").toAbsolutePath().normalize().toString())), System.getProperty("java.io.tmpdir"));
	}

	public default String getDirTemp() {
		return coalesce(getEnvironmentVariable("JEZEL_DirTemp", System.getProperty("java.io.tmpdir")), trimOrNull(Paths.get(".").toAbsolutePath().normalize().toString()));
	}

	public default String getLoggingLevel() {
		return getEnvironmentVariable("JEZEL_LoggingLevel", "debug");
	}

	public default String getLoggingLevelLibs() {
		return getEnvironmentVariable("JEZEL_LoggingLevelLibs", "warn");
	}

	public default int getRestPort() {
		return getEnvironmentVariable("JEZEL_RestPort", 8080);
	}

	public default int getRestMaxThreads() {
		return getEnvironmentVariable("JEZEL_RestMaxThreads", 100);
	}

	public default int getRestMinThreads() {
		return getEnvironmentVariable("JEZEL_RestMinThreads", 10);
	}

	public default int getRestIdleTimeout() {
		return getEnvironmentVariable("JEZEL_RestIdleTimeout", 120);
	}

	public default boolean getRestJoinThread() {
		return getEnvironmentVariable("JEZEL_RestJoinThread", true);
	}

	public default boolean getRestIgnoreCredentials() {
		return getEnvironmentVariable("JEZEL_RestIgnoreCredentials", true);
	}

	public default int getSchedulerThreads() {
		return getEnvironmentVariable("JEZEL_SchedulerThreads", 10);
	}

	public default String getDatabaseDir() {
		return getEnvironmentVariable("JEZEL_DatabaseDir", "mem");
	}

	public default int getWebMaxThreads() {
		return getEnvironmentVariable("JEZEL_WebMaxThreads", 100);
	}

	public default int getWebMinThreads() {
		return getEnvironmentVariable("JEZEL_WebMinThreads", 10);
	}

	public default int getWebIdleTimeout() {
		return getEnvironmentVariable("JEZEL_WebIdleTimeout", 120);
	}

	public default boolean getWebJoinThread() {
		return getEnvironmentVariable("JEZEL_WebJoinThread", true);
	}

	public default boolean getWebIgnoreCredentials() {
		return getEnvironmentVariable("JEZEL_WebIgnoreCredentials", true);
	}

	public default Map<String, Object> toMap() {
		var map = new CaseInsensitiveMap<String, Object>();

		for (var method : getClass().getDeclaredMethods()) {
			var methodName = method.getName();
			if (!methodName.startsWith("get")) continue;
			int modifiers = method.getModifiers();
			if (!Modifier.isPublic(modifiers)) continue;
			Object o;
			try {
				o = method.invoke(this);
			} catch (Exception e) {
				throw new Error(e);
			}

			map.put(methodName.substring("get".length()), o);

		}
		return map;
	}
}
