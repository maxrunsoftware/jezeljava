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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public final class LogSetup {

	private LogSetup() {}

	private static Level parseLevel(String level) {
		level = Util.trimOrNull(level);
		if (level == null) return Level.INFO;

		level = level.toLowerCase();
		if (level.equals("trace")) return Level.TRACE;
		if (level.equals("debug")) return Level.DEBUG;
		if (level.equals("info")) return Level.INFO;
		if (level.equals("warn")) return Level.WARN;
		if (level.equals("error")) return Level.ERROR;
		return Level.INFO;
	}

	public static void initialize(String level, String levelLibs) {
		initialize(parseLevel(level), parseLevel(levelLibs));
	}

	private static void initialize(Level level, Level levelLibs) {
		// This is the root logger provided by log4j
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(level);

		// Define log pattern layout
		PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");

		// Add console appender to root logger
		rootLogger.removeAllAppenders();
		rootLogger.addAppender(new ConsoleAppender(layout));

		for (var disable : Constant.LOGGING_LIBS) {
			LogManager.getLogger(disable).setLevel(levelLibs);
		}

	}

}
