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

import com.maxrunsoftware.jezel.model.SchedulerJob;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SchedulerJobServlet extends ServletBase {
	private static final long serialVersionUID = 5390613315846662858L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerJobServlet.class);

	@Override
	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterId(request, SchedulerJob.ID);
		try (var session = db.openSession()) {

			var schedulerJobs = new ArrayList<SchedulerJob>();
			if (schedulerJobId == null) {
				schedulerJobs.addAll(getAll(SchedulerJob.class, session));
			} else {
				var schedulerJob = getById(SchedulerJob.class, session, schedulerJobId);
				if (schedulerJob != null) { schedulerJobs.add(schedulerJob); }
			}

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Found " + schedulerJobs.size() + " SchedulerJobs");

			json.add(SchedulerJob.NAME + "s", createArrayBuilder(schedulerJobs));
			writeResponse(response, json);
		}
	}

	@Override
	protected void doPutAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try (var session = db.openSession()) {
			var schedulerJob = new SchedulerJob();
			schedulerJob.setDisabled(false);
			var schedulerJobId = save(session, schedulerJob);

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Created SchedulerJob[" + schedulerJobId + "]")
					.add(SchedulerJob.ID, schedulerJobId);

			writeResponse(response, json);

		}
	}

	@Override
	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterId(request, SchedulerJob.ID);
		if (schedulerJobId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerJob.ID + "' parameter provided to update SchedulerJob", 400);
			return;
		}

		try (var session = db.openSession()) {
			var schedulerJob = getById(SchedulerJob.class, session, schedulerJobId);
			if (schedulerJob == null) {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerJob[" + schedulerJobId + "] does not exist", 404);
				return;
			}

			var name = trimOrNull(request.getParameter("name"));
			if (name != null) {
				LOG.debug("Updating SchedulerJob[" + schedulerJobId + "] [name] from " + schedulerJob.getName() + " to " + name);
				schedulerJob.setName(name);
			}
			var path = trimOrNull(request.getParameter("path"));
			if (path != null) {
				LOG.debug("Updating SchedulerJob[" + schedulerJobId + "] [path] from " + schedulerJob.getPath() + " to " + path);
				schedulerJob.setPath(path);
			}
			var disabled = trimOrNull(request.getParameter("disabled"));
			if (disabled != null) {
				LOG.debug("Updating SchedulerJob[" + schedulerJobId + "] [disabled] from " + schedulerJob.isDisabled() + " to " + disabled);
				schedulerJob.setDisabled(parseBoolean(disabled));
			}

			if (name != null || path != null || disabled != null) {
				save(session, schedulerJob);
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerJob[" + schedulerJobId + "] successfully updated", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerJob[" + schedulerJobId + "] nothing provided to update", 400);
			}

		}

	}

	@Override
	protected void doDeleteAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterId(request, SchedulerJob.ID);
		if (schedulerJobId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerJob.ID + "' parameter provided to delete SchedulerJob", 400);
			return;
		}
		try (var session = db.openSession()) {
			var result = delete(SchedulerJob.class, session, schedulerJobId);
			if (result) {
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerJob[" + schedulerJobId + "] successfully deleted", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerJob[" + schedulerJobId + "] does not exist", 404);
			}
		}

	}

}
