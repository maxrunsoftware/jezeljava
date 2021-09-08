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

import com.maxrunsoftware.jezel.model.CommandLogJob;
import com.maxrunsoftware.jezel.model.SchedulerJob;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CommandLogJobServlet extends ServletBase {
	private static final long serialVersionUID = 5638548014830021753L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CommandLogJobServlet.class);

	@Override
	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var commandLogJobId = getParameterInt(request, CommandLogJob.ID);
		var schedulerJobId = getParameterInt(request, SchedulerJob.ID);

		try (var session = db.openSession()) {
			var commandLogJobs = new ArrayList<CommandLogJob>();

			if (commandLogJobId != null) {
				// return 1
				var commandLogJob = getById(CommandLogJob.class, session, commandLogJobId);
				if (commandLogJob != null) commandLogJobs.add(commandLogJob);
			} else if (schedulerJobId != null) {
				// return all logs for job
				for (var commandLogJob : getAll(CommandLogJob.class, session)) {
					if (schedulerJobId.equals(commandLogJob.getSchedulerJob().getSchedulerJobId())) { commandLogJobs.add(commandLogJob); }
				}
			} else {
				// return all logs
				for (var commandLogJob : getAll(CommandLogJob.class, session)) {
					commandLogJobs.add(commandLogJob);
				}
			}

			if (schedulerJobId == null) {
				schedulerJobs.addAll(getAll(SchedulerJob.class, session));
			} else {
				var schedulerJob = getById(SchedulerJob.class, session, schedulerJobId);
				if (schedulerJob != null) { schedulerJobs.add(schedulerJob); }
			}

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Found " + schedulerJobs.size() + " SchedulerJobs");

			json.add(SchedulerJob.NAME, createArrayBuilder(schedulerJobs));
			writeResponse(response, json);
		}
	}

	@Override
	protected void doDeleteAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var commandLogJobId = getParameterInt(request, CommandLogJob.ID);
		if (commandLogJobId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + CommandLogJob.ID + "' parameter provided to delete CommandLogJob", 400);
			return;
		}
		try (var session = db.openSession()) {
			LOG.debug("Deleting CommandLogJob[" + commandLogJobId + "]");
			var result = delete(CommandLogJob.class, session, commandLogJobId);
			if (result) {
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "CommandLogJob[" + commandLogJobId + "] successfully deleted", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "CommandLogJob[" + commandLogJobId + "] does not exist", 404);
			}
		}

	}

}
