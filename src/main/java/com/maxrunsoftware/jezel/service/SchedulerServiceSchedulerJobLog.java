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

import java.time.LocalDateTime;

import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.LogLevel;
import com.maxrunsoftware.jezel.action.CommandLog;
import com.maxrunsoftware.jezel.model.CommandLogAction;
import com.maxrunsoftware.jezel.model.CommandLogMessage;

public class SchedulerServiceSchedulerJobLog implements CommandLog {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerServiceSchedulerJobLog.class);

	private final int commandLogActionId;
	private final DatabaseService db;
	private int index = 0;

	public SchedulerServiceSchedulerJobLog(DatabaseService db, int commandLogActionId) {
		this.db = checkNotNull(db);
		this.commandLogActionId = commandLogActionId;
	}

	@Override
	public void log(LogLevel level, Object message, Throwable exception) {
		try (var session = db.openSession()) {
			var commandLogAction = getById(CommandLogAction.class, session, commandLogActionId);
			if (commandLogAction == null) {
				LOG.warn("Could not find CommandLogAction[" + commandLogActionId + "]");
			} else {
				var commandLogMessage = new CommandLogMessage();
				commandLogMessage.setCommandLogAction(commandLogAction);
				commandLogMessage.setTimestamp(LocalDateTime.now());
				commandLogMessage.setLevel(level.toString());
				commandLogMessage.setIndex(index);
				commandLogMessage.setMessage(message == null ? null : message.toString());
				commandLogMessage.setException(exception == null ? null : exception.toString());
				save(session, commandLogMessage);

				index++;
			}
		}
	}
}
