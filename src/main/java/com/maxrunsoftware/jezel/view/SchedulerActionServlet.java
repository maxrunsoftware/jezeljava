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
import java.util.ArrayList;

import com.maxrunsoftware.jezel.model.SchedulerAction;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SchedulerActionServlet extends ServletBase {
	private static final long serialVersionUID = 7724770478012107058L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerActionServlet.class);

	@Override
	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerActionId = getParameterId(request, SchedulerAction.ID);
		try (var session = db.openSession()) {

			var schedulerActions = new ArrayList<SchedulerAction>();
			if (schedulerActionId == null) {
				schedulerActions.addAll(getAll(SchedulerAction.class, session));
			} else {
				schedulerActions.add(getById(SchedulerAction.class, session, schedulerActionId));
			}

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Found " + schedulerActions.size() + " SchedulerActions");

			json.add(SchedulerAction.NAME, createArrayBuilder(schedulerActions));
			writeResponse(response, json);
		}
	}

	@Override
	protected void doPutAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try (var session = db.openSession()) {
			var schedulerAction = new SchedulerAction();
			schedulerAction.setDisabled(false);
			var schedulerActionId = save(session, schedulerAction);

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Created SchedulerAction[" + schedulerActionId + "]")
					.add(SchedulerAction.ID, schedulerActionId);

			writeResponse(response, json);
		}
	}

	@Override
	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerActionId = getParameterId(request, SchedulerAction.ID);
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
		var schedulerActionId = getParameterId(request, SchedulerAction.ID);
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
