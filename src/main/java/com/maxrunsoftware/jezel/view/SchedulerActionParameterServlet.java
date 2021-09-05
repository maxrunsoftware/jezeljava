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
import com.maxrunsoftware.jezel.model.SchedulerActionParameter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SchedulerActionParameterServlet extends ServletBase {
	private static final long serialVersionUID = 6717387710807155560L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerActionServlet.class);

	@Override
	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerActionParameterId = getParameterInt(request, SchedulerAction.ID);
		try (var session = db.openSession()) {

			var schedulerActionParameters = new ArrayList<SchedulerActionParameter>();
			if (schedulerActionParameterId == null) {
				schedulerActionParameters.addAll(getAll(SchedulerActionParameter.class, session));
			} else {
				schedulerActionParameters.add(getById(SchedulerActionParameter.class, session, schedulerActionParameterId));
			}

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Found " + schedulerActionParameters.size() + " SchedulerActionParameters");

			json.add(SchedulerActionParameter.NAME, createArrayBuilder(schedulerActionParameters));
			writeResponse(response, json);
		}
	}

	@Override
	protected void doPutAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try (var session = db.openSession()) {
			var schedulerActionParameter = new SchedulerActionParameter();
			schedulerActionParameter.setDisabled(false);
			var schedulerActionParameterId = save(session, schedulerActionParameter);

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Created SchedulerActionParameter[" + schedulerActionParameterId + "]")
					.add(SchedulerActionParameter.ID, schedulerActionParameterId);

			writeResponse(response, json);
		}
	}

	@Override
	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerActionParameterId = getParameterInt(request, SchedulerActionParameter.ID);
		if (schedulerActionParameterId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerActionParameter.ID + "' parameter provided to update SchedulerActionParameter", 400);
			return;
		}

		try (var session = db.openSession()) {
			var schedulerActionParameter = getById(SchedulerActionParameter.class, session, schedulerActionParameterId);
			if (schedulerActionParameter == null) {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerAction[" + schedulerActionParameterId + "] does not exist", 404);
				return;
			}

			var name = trimOrNull(request.getParameter("name"));
			if (name != null) {
				LOG.debug("Updating SchedulerActionParameter[" + schedulerActionParameterId + "] [name] from " + schedulerActionParameter.getName() + " to " + name);
				schedulerActionParameter.setName(name);
			}
			var value = trimOrNull(request.getParameter("value"));
			if (value != null) {
				LOG.debug("Updating SchedulerActionParameter[" + schedulerActionParameterId + "] [value] from " + schedulerActionParameter.getValue() + " to " + value);
				schedulerActionParameter.setValue(value);
			}
			var disabled = trimOrNull(request.getParameter("disabled"));
			if (disabled != null) {
				LOG.debug("Updating SchedulerActionParameter[" + schedulerActionParameterId + "] [disabled] from " + schedulerActionParameter.isDisabled() + " to " + disabled);
				schedulerActionParameter.setDisabled(parseBoolean(disabled));
			}

			if (name != null || value != null || disabled != null) {
				save(session, schedulerActionParameter);
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerActionParameter[" + schedulerActionParameterId + "] successfully updated", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerActionParameter[" + schedulerActionParameterId + "] nothing provided to update", 400);
			}

		}

	}

	@Override
	protected void doDeleteAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerActionParameterId = getParameterInt(request, SchedulerActionParameter.ID);
		if (schedulerActionParameterId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerActionParameter.ID + "' parameter provided to delete SchedulerActionParameter", 400);
			return;
		}
		try (var session = db.openSession()) {
			var result = delete(SchedulerActionParameter.class, session, schedulerActionParameterId);
			if (result) {
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerActionParameter[" + schedulerActionParameterId + "] successfully deleted", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerActionParameter[" + schedulerActionParameterId + "] does not exist", 404);
			}
		}

	}
}
