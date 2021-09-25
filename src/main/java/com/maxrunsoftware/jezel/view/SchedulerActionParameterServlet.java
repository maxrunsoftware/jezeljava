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

import com.maxrunsoftware.jezel.model.SchedulerAction;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SchedulerActionParameterServlet extends ServletBase {
	private static final long serialVersionUID = 6717387710807155560L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerActionServlet.class);

	@Override
	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerActionId = getParameterInt(request, SchedulerAction.ID);
		if (schedulerActionId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerAction.ID + "' parameter provided to update SchedulerActionParameter", 400);
			return;
		}

		try (var session = db.openSession()) {
			var schedulerAction = getById(SchedulerAction.class, session, schedulerActionId);
			if (schedulerAction == null) {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerAction[" + schedulerActionId + "] does not exist", 404);
				return;
			}

			var name = trimOrNull(getParameter(request, "name"));
			if (name == null) {
				writeResponse(response, RESPONSE_STATUS_FAILED, "No 'name' argument provided for SchedulerAction[" + schedulerActionId + "] to update SchedulerActionParameter", 404);
				return;
			}

			var value = trimOrNull(getParameter(request, "value"));
			LOG.debug("Updating SchedulerAction[" + schedulerActionId + "] " + schedulerAction.getName() + "." + name + "=" + value);

			var result = schedulerAction.setSchedulerActionParameter(session, name, value);
			if (result) {
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerAction[" + schedulerActionId + "] " + schedulerAction.getName() + "." + name + "=" + value, 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerAction[" + schedulerActionId + "] " + schedulerAction.getName() + "." + name + " parameter does not exist", 400);
			}

		}

	}

}
