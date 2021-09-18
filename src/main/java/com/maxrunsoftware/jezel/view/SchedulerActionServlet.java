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
package com.maxrunsoftware.jezel.view;

import static com.maxrunsoftware.jezel.Util.*;

import java.io.IOException;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.model.SchedulerAction;
import com.maxrunsoftware.jezel.model.SchedulerJob;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SchedulerActionServlet extends ServletBase {
	private static final long serialVersionUID = 7724770478012107058L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerActionServlet.class);

	@Override
	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterInt(request, SchedulerJob.ID);
		if (schedulerJobId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerJob.ID + "' parameter provided to get SchedulerActions", 400);
			return;
		}

		try (var session = db.openSession()) {

			var schedulerActionsArray = createArrayBuilder();
			var schedulerActions = SchedulerAction.getBySchedulerJobId(session, schedulerJobId);
			for (var schedulerAction : schedulerActions) {
				schedulerActionsArray.add(schedulerAction.toJson());
			}

			var actionNamesArray = createArrayBuilder();
			for (var clazz : Constant.COMMANDS) {
				actionNamesArray.add(clazz.getSimpleName());
			}

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Found " + schedulerActions.size() + " SchedulerActions")
					.add("actionNames", actionNamesArray)
					.add(SchedulerAction.NAME, schedulerActionsArray);

			writeResponse(response, json);
		}
	}

	@Override
	protected void doPutAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterInt(request, SchedulerJob.ID);
		if (schedulerJobId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerJob.ID + "' parameter provided to create SchedulerActions", 400);
			return;
		}

		try (var session = db.openSession()) {
			var schedulerJob = getById(SchedulerJob.class, session, schedulerJobId);
			if (schedulerJob == null) {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerJobId of '" + SchedulerJob.ID + "' does not exist to create SchedulerActions", 400);
				return;
			}

			var schedulerAction = new SchedulerAction();
			schedulerAction.setDisabled(false);
			schedulerAction.setSchedulerJob(schedulerJob);
			schedulerAction.setIndex(Integer.MAX_VALUE);
			var schedulerActionId = save(session, schedulerAction);

			schedulerJob = getById(SchedulerJob.class, session, schedulerJobId);
			schedulerJob.reindexSchedulerActions(session);

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Created SchedulerAction[" + schedulerActionId + "]")
					.add(SchedulerAction.ID, schedulerActionId);

			writeResponse(response, json);
		}
	}

	@Override
	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerActionId = getParameterInt(request, SchedulerAction.ID);
		if (schedulerActionId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerAction.ID + "' parameter provided to update SchedulerAction", 400);
			return;
		}

		try (var session = db.openSession()) {
			var schedulerAction = getById(SchedulerAction.class, session, schedulerActionId);
			if (schedulerAction == null) {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerAction[" + schedulerActionId + "] does not exist", 404);
				return;
			}

			var name = trimOrNull(request.getParameter("name"));
			if (name != null) {
				LOG.debug("Updating SchedulerAction[" + schedulerActionId + "] [name] from " + schedulerAction.getName() + " to " + name);
				schedulerAction.setName(name);
			}
			var description = trimOrNull(request.getParameter("description"));
			if (description != null) {
				LOG.debug("Updating SchedulerAction[" + schedulerActionId + "] [description] from " + schedulerAction.getDescription() + " to " + description);
				schedulerAction.setDescription(description);
			}
			var disabled = trimOrNull(request.getParameter("disabled"));
			if (disabled != null) {
				LOG.debug("Updating SchedulerAction[" + schedulerActionId + "] [disabled] from " + schedulerAction.isDisabled() + " to " + disabled);
				schedulerAction.setDisabled(parseBoolean(disabled));
			}

			if (name != null || description != null || disabled != null) {
				save(session, schedulerAction);
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerAction[" + schedulerActionId + "] successfully updated", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerAction[" + schedulerActionId + "] nothing provided to update", 400);
			}

		}

	}

	@Override
	protected void doDeleteAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerActionId = getParameterInt(request, SchedulerAction.ID);
		if (schedulerActionId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerAction.ID + "' parameter provided to delete SchedulerAction", 400);
			return;
		}
		try (var session = db.openSession()) {
			var result = delete(SchedulerAction.class, session, schedulerActionId);
			if (result) {
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerAction[" + schedulerActionId + "] successfully deleted", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerAction[" + schedulerActionId + "] does not exist", 404);
			}
		}

	}

}
