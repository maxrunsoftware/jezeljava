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

import com.maxrunsoftware.jezel.LogLevel;

public interface CommandLog {
	public default void trace(Object message) {
		log(LogLevel.TRACE, message, null);
	}

	public default void trace(Throwable exception) {
		log(LogLevel.TRACE, null, exception);
	}

	public default void trace(Object message, Throwable exception) {
		log(LogLevel.TRACE, message, exception);
	}

	public default void debug(Object message) {
		log(LogLevel.DEBUG, message, null);
	}

	public default void debug(Throwable exception) {
		log(LogLevel.DEBUG, null, exception);
	}

	public default void debug(Object message, Throwable exception) {
		log(LogLevel.DEBUG, message, exception);
	}

	public default void info(Object message) {
		log(LogLevel.INFO, message, null);
	}

	public default void info(Throwable exception) {
		log(LogLevel.INFO, null, exception);
	}

	public default void info(Object message, Throwable exception) {
		log(LogLevel.INFO, message, exception);
	}

	public default void warn(Object message) {
		log(LogLevel.WARN, message, null);
	}

	public default void warn(Throwable exception) {
		log(LogLevel.WARN, null, exception);
	}

	public default void warn(Object message, Throwable exception) {
		log(LogLevel.WARN, message, exception);
	}

	public default void error(Object message) {
		log(LogLevel.ERROR, message, null);
	}

	public default void error(Throwable exception) {
		log(LogLevel.ERROR, null, exception);
	}

	public default void error(Object message, Throwable exception) {
		log(LogLevel.ERROR, message, exception);
	}

	public void log(LogLevel level, Object message, Throwable exception);

}
