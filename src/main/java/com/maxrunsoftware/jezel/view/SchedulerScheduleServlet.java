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
import com.maxrunsoftware.jezel.model.SchedulerSchedule;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SchedulerScheduleServlet extends ServletBase {
	private static final long serialVersionUID = 2081409135482199513L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerScheduleServlet.class);

	@Override
	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerScheduleId = getParameterId(request, SchedulerSchedule.ID);
		var schedulerJobId = getParameterId(request, SchedulerJob.ID);

		try (var session = db.openSession()) {

			var schedulerSchedules = new ArrayList<SchedulerSchedule>();
			for (var schedulerSchedule : getAll(SchedulerSchedule.class, session)) {
				boolean shouldAdd = true;
				if (schedulerScheduleId != null) {

					if (((int) schedulerScheduleId) != schedulerSchedule.getSchedulerScheduleId()) {

						shouldAdd = false;
					}
				}

				if (schedulerJobId != null) {

					if (((int) schedulerJobId) != schedulerSchedule.getSchedulerJob().getSchedulerJobId()) {

						shouldAdd = false;
					}
				}

				if (shouldAdd) schedulerSchedules.add(schedulerSchedule);

			}

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Found " + schedulerSchedules.size() + " " + SchedulerSchedule.class.getSimpleName() + "s");

			json.add(SchedulerSchedule.NAME, createArrayBuilder(schedulerSchedules));
			writeResponse(response, json);
		}
	}

	@Override
	protected void doPutAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterId(request, SchedulerJob.ID);
		if (schedulerJobId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerJob.ID + "' parameter provided to create SchedulerSchedule", 400);
			return;
		}
		try (var session = db.openSession()) {

			var schedulerJob = getById(SchedulerJob.class, session, schedulerJobId);
			if (schedulerJob == null) {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerJob[" + schedulerJobId + "] does not exist", 404);
				return;
			}

			var schedulerSchedule = new SchedulerSchedule();
			schedulerSchedule.setSchedulerJob(schedulerJob);
			schedulerSchedule.setDisabled(false);
			var schedulerScheduleId = save(session, schedulerSchedule);

			var json = createObjectBuilder()
					.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
					.add(RESPONSE_MESSAGE, "Created SchedulerSchedule[" + schedulerScheduleId + "]")
					.add(SchedulerSchedule.ID, schedulerScheduleId);

			writeResponse(response, json);
		}
	}

	@Override
	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerScheduleId = getParameterId(request, SchedulerSchedule.ID);
		if (schedulerScheduleId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerSchedule.ID + "' parameter provided to update SchedulerSchedule", 400);
			return;
		}

		try (var session = db.openSession()) {
			var schedulerSchedule = getById(SchedulerSchedule.class, session, schedulerScheduleId);
			if (schedulerSchedule == null) {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerSchedule[" + schedulerScheduleId + "] does not exist", 404);
				return;
			}

			var sunday = trimOrNull(request.getParameter("sunday"));
			if (sunday != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [sunday] from " + schedulerSchedule.isSunday() + " to " + sunday);
				schedulerSchedule.setSunday(parseBoolean(sunday));
			}

			var monday = trimOrNull(request.getParameter("monday"));
			if (monday != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [monday] from " + schedulerSchedule.isMonday() + " to " + monday);
				schedulerSchedule.setMonday(parseBoolean(monday));
			}

			var tuesday = trimOrNull(request.getParameter("tuesday"));
			if (tuesday != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [tuesday] from " + schedulerSchedule.isTuesday() + " to " + tuesday);
				schedulerSchedule.setTuesday(parseBoolean(tuesday));
			}

			var wednesday = trimOrNull(request.getParameter("wednesday"));
			if (wednesday != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [wednesday] from " + schedulerSchedule.isWednesday() + " to " + wednesday);
				schedulerSchedule.setWednesday(parseBoolean(wednesday));
			}

			var thursday = trimOrNull(request.getParameter("thursday"));
			if (thursday != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [thursday] from " + schedulerSchedule.isThursday() + " to " + thursday);
				schedulerSchedule.setThursday(parseBoolean(thursday));
			}

			var friday = trimOrNull(request.getParameter("friday"));
			if (friday != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [friday] from " + schedulerSchedule.isFriday() + " to " + friday);
				schedulerSchedule.setFriday(parseBoolean(friday));
			}

			var saturday = trimOrNull(request.getParameter("saturday"));
			if (saturday != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [saturday] from " + schedulerSchedule.isSaturday() + " to " + saturday);
				schedulerSchedule.setSaturday(parseBoolean(saturday));
			}

			var hour = trimOrNull(request.getParameter("hour"));
			if (hour != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [hour] from " + schedulerSchedule.getHour() + " to " + hour);
				schedulerSchedule.setHour(parseInt(hour));
			}

			var minute = trimOrNull(request.getParameter("minute"));
			if (minute != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [minute] from " + schedulerSchedule.getMinute() + " to " + minute);
				schedulerSchedule.setMinute(parseInt(minute));
			}

			var disabled = trimOrNull(request.getParameter("disabled"));
			if (disabled != null) {
				LOG.debug("Updating SchedulerSchedule[" + schedulerScheduleId + "] [disabled] from " + schedulerSchedule.isDisabled() + " to " + disabled);
				schedulerSchedule.setDisabled(parseBoolean(disabled));
			}

			if (sunday != null || monday != null || tuesday != null || wednesday != null || thursday != null || friday != null || saturday != null || hour != null || minute != null
					|| disabled != null) {
				save(session, schedulerSchedule);
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerSchedule[" + schedulerScheduleId + "] successfully updated", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerSchedule[" + schedulerScheduleId + "] nothing provided to update", 400);
			}

		}

	}

	@Override
	protected void doDeleteAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerScheduleId = getParameterId(request, SchedulerSchedule.ID);
		if (schedulerScheduleId == null) {
			writeResponse(response, RESPONSE_STATUS_FAILED, "No '" + SchedulerSchedule.ID + "' parameter provided to delete SchedulerSchedule", 400);
			return;
		}
		try (var session = db.openSession()) {
			var result = delete(SchedulerSchedule.class, session, schedulerScheduleId);
			if (result) {
				writeResponse(response, RESPONSE_STATUS_SUCCESS, "SchedulerSchedule[" + schedulerScheduleId + "] successfully deleted", 200);
			} else {
				writeResponse(response, RESPONSE_STATUS_FAILED, "SchedulerSchedule[" + schedulerScheduleId + "] does not exist", 404);
			}
		}

	}

}
