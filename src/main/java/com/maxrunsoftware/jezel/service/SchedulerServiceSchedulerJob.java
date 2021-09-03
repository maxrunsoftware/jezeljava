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
import java.util.ArrayList;
import java.util.Map;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.Util;
import com.maxrunsoftware.jezel.action.Command;
import com.maxrunsoftware.jezel.model.CommandLogAction;
import com.maxrunsoftware.jezel.model.CommandLogJob;
import com.maxrunsoftware.jezel.model.SchedulerAction;
import com.maxrunsoftware.jezel.model.SchedulerJob;

public class SchedulerServiceSchedulerJob {

	private static class ActionItem {
		private final int schedulerActionId;
		private final String schedulerActionName;
		private final Map<String, String> parameters;

		public ActionItem(SchedulerAction schedulerAction) {
			this.schedulerActionId = schedulerAction.getSchedulerActionId();
			this.schedulerActionName = schedulerAction.getName();
			parameters = Util.mapCaseInsensitive();
			for (var schedulerActionParameter : schedulerAction.getSchedulerActionParameters()) {
				var key = schedulerActionParameter.getName();
				var val = schedulerActionParameter.getValue();
				getParameters().put(key, val);
			}

		}

		public int getSchedulerActionId() {
			return schedulerActionId;
		}

		public Map<String, String> getParameters() {
			return parameters;
		}

		public String getSchedulerActionName() {
			return schedulerActionName;
		}
	}

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerServiceSchedulerJob.class);

	private final DatabaseService db;

	public SchedulerServiceSchedulerJob(DatabaseService db) {
		this.db = checkNotNull(db);
	}

	private boolean execute(ActionItem action, int actionIndex, int commandLogJobId) {
		int commandLogActionId;
		try (var session = db.openSession()) {
			var commandLogJob = getById(CommandLogJob.class, session, commandLogJobId);
			var schedulerAction = getById(SchedulerAction.class, session, action.getSchedulerActionId());

			var commandLogAction = new CommandLogAction();
			commandLogAction.setCommandLogJob(commandLogJob);
			commandLogAction.setSchedulerAction(schedulerAction);
			commandLogAction.setIndex(actionIndex);
			commandLogAction.setStart(LocalDateTime.now());
			commandLogActionId = save(session, commandLogAction);
		}

		var schedulerServiceSchedulerJobLog = new SchedulerServiceSchedulerJobLog(db, commandLogActionId);
		var command = createCommand(action);
		var successfulExection = true;
		try {
			if (command == null) throw new Exception("Could not find Action named " + action.getSchedulerActionName());
			command.setLog(schedulerServiceSchedulerJobLog);
			command.setParameters(action.getParameters());
			command.execute();
		} catch (Throwable t) {
			schedulerServiceSchedulerJobLog.error(t);
			successfulExection = false;
		}

		try (var session = db.openSession()) {
			var commandLogAction = getById(CommandLogAction.class, session, commandLogActionId);
			commandLogAction.setEnd(LocalDateTime.now());
			save(session, commandLogAction);
		}

		return successfulExection;
	}

	public void execute(final int schedulerJobId) {
		int commandLogJobId;
		var actions = new ArrayList<ActionItem>();
		String schedulerJobName;
		try (var session = db.openSession()) {
			var schedulerJob = getById(SchedulerJob.class, session, schedulerJobId);
			if (schedulerJob == null) {
				LOG.warn("Cannot execute non-existant SchedulerJob[" + schedulerJobId + "]");
				return;
			}

			schedulerJobName = schedulerJob.getName();
			if (schedulerJob.isDisabled()) {
				LOG.info("Skipping execution of disabled SchedulerJob[ " + schedulerJobId + "] " + schedulerJobName);
				return;
			}

			LOG.info("Starting execution of SchedulerJob[" + schedulerJobId + "] " + schedulerJobName);
			var commandLogJob = new CommandLogJob();
			commandLogJob.setSchedulerJob(schedulerJob);
			commandLogJob.setStart(LocalDateTime.now());
			commandLogJobId = save(session, commandLogJob);

			for (var schedulerAction : schedulerJob.getSchedulerActions()) {
				actions.add(new ActionItem(schedulerAction));
			}
		}

		var actionIndex = 0;
		for (var action : actions) {
			var successfulExection = execute(action, actionIndex, commandLogJobId);
			if (!successfulExection) break;

			actionIndex++;
		}

		try (var session = db.openSession()) {
			var commandLogJob = getById(CommandLogJob.class, session, commandLogJobId);
			commandLogJob.setEnd(LocalDateTime.now());
			save(session, commandLogJob);
		}

		LOG.info("Completed execution of SchedulerJob[" + schedulerJobId + "] " + schedulerJobName);
	}

	private Command createCommand(ActionItem action) {
		var name = trimOrNull(action.getSchedulerActionName());
		if (name == null) return null;

		for (var clazz : Constant.COMMANDS) {
			if (name.equalsIgnoreCase(clazz.getSimpleName())) {
				var command = Constant.getInstance(clazz);
				return command;
			}
		}
		return null;

	}

}
