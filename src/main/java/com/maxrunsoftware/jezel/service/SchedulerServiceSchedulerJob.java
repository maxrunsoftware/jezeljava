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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.action.Command;
import com.maxrunsoftware.jezel.model.CommandLogAction;
import com.maxrunsoftware.jezel.model.CommandLogJob;
import com.maxrunsoftware.jezel.model.ConfigurationItem;
import com.maxrunsoftware.jezel.model.SchedulerAction;
import com.maxrunsoftware.jezel.model.SchedulerJob;

public class SchedulerServiceSchedulerJob {

	private static class ActionItem {
		private final int schedulerActionId;
		private final String schedulerActionName;
		private final Map<String, String> parameters;

		public ActionItem(SchedulerAction schedulerAction, DatabaseService db) {
			this.schedulerActionId = schedulerAction.getSchedulerActionId();
			this.schedulerActionName = schedulerAction.getName();

			parameters = new HashMap<String, String>();

			try (var session = db.openSession()) {
				var parametersDefault = ConfigurationItem.getValuesWithPrefix(session, schedulerActionName);
				for (var key : parametersDefault.keySet()) {
					parameters.put(key, parametersDefault.get(key));
				}
			}

			for (var schedulerActionParameter : schedulerAction.getSchedulerActionParameters()) {
				var key = schedulerActionParameter.getName();
				var val = schedulerActionParameter.getValue();
				parameters.put(key, val);
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

	@Inject
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
			commandLogAction.setName(schedulerAction.getName());
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
			LOG.warn("Encountered error: " + t);
			schedulerServiceSchedulerJobLog.error(ExceptionUtils.getStackTrace(t));

			ExceptionUtils.getStackTrace(t);
			successfulExection = false;
		}

		try (var session = db.openSession()) {
			var commandLogAction = getById(CommandLogAction.class, session, commandLogActionId);
			commandLogAction.setEnd(LocalDateTime.now());
			save(session, commandLogAction);
		}

		return successfulExection;
	}

	private static final Object locker = new Object();
	private static final Set<Integer> executingJobs = new HashSet<Integer>();

	public void execute(final int schedulerJobId) {
		synchronized (locker) {
			if (!executingJobs.add(schedulerJobId)) {
				LOG.warn("SchedulerJob[" + schedulerJobId + "] already executing so skipping execution");
				return;
			}
		}
		try {
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
					actions.add(new ActionItem(schedulerAction, db));
				}
			}

			var actionIndex = 0;
			boolean successfulExecution = true;
			for (var action : actions) {
				successfulExecution = execute(action, actionIndex, commandLogJobId);
				LOG.debug("Received successful execution: " + successfulExecution);
				if (!successfulExecution) break;

				actionIndex++;
			}

			try (var session = db.openSession()) {
				var commandLogJob = getById(CommandLogJob.class, session, commandLogJobId);
				commandLogJob.setEnd(LocalDateTime.now());
				commandLogJob.setError(!successfulExecution);
				save(session, commandLogJob);
			}

			LOG.info("Completed execution of SchedulerJob[" + schedulerJobId + "] " + schedulerJobName);
		} finally {
			synchronized (locker) {
				executingJobs.remove(schedulerJobId);
			}
		}
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
