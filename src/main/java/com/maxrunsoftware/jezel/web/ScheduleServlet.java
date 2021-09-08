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
package com.maxrunsoftware.jezel.web;

import static com.maxrunsoftware.jezel.Util.*;
import static j2html.TagCreator.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.maxrunsoftware.jezel.model.SchedulerJob;
import com.maxrunsoftware.jezel.model.SchedulerSchedule;
import com.maxrunsoftware.jezel.util.Table;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ScheduleServlet extends ServletBase {
	private static final long serialVersionUID = 1285903727709923745L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ScheduleServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterInt(request, SchedulerJob.ID);
		var schedulerScheduleId = getParameterInt(request, SchedulerSchedule.ID);
		var action = coalesce(getParameter(request, "action"), " ");

		if (schedulerJobId != null && action.equalsIgnoreCase("add")) {
			// Add
			doGetShowScheduleSingle(request, response, schedulerJobId, null, null);
		} else if (schedulerJobId != null && schedulerScheduleId != null && action.equalsIgnoreCase("edit")) {
			// Edit
			doGetShowScheduleSingle(request, response, schedulerJobId, schedulerScheduleId, null);
		} else if (schedulerJobId != null) {
			// Show for Job
			doGetShowScheduleAll(request, response, schedulerJobId);
		} else {
			// Show everything
			doGetShowScheduleAll(request, response, null);
		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterInt(request, SchedulerJob.ID);
		LOG.debug(SchedulerJob.ID + ": " + schedulerJobId);
		var schedulerScheduleId = getParameterInt(request, SchedulerSchedule.ID);
		LOG.debug(SchedulerSchedule.ID + ": " + schedulerScheduleId);
		var sunday = getParameterBool(request, "schedulerScheduleSunday");
		LOG.debug("sunday: " + sunday);
		var monday = getParameterBool(request, "schedulerScheduleMonday");
		LOG.debug("monday: " + monday);
		var tuesday = getParameterBool(request, "schedulerScheduleTuesday");
		LOG.debug("tuesday: " + tuesday);
		var wednesday = getParameterBool(request, "schedulerScheduleWednesday");
		LOG.debug("wednesday: " + wednesday);
		var thursday = getParameterBool(request, "schedulerScheduleThursday");
		LOG.debug("thursday: " + thursday);
		var friday = getParameterBool(request, "schedulerScheduleFriday");
		LOG.debug("friday: " + friday);
		var saturday = getParameterBool(request, "schedulerScheduleSaturday");
		LOG.debug("saturday: " + saturday);

		var hour = getParameterInt(request, "schedulerScheduleHour");
		LOG.debug("hour: " + hour);
		var minute = getParameterInt(request, "schedulerScheduleMinute");
		LOG.debug("minute: " + minute);

		var enabled = getParameterBool(request, "schedulerScheduleEnabled");
		LOG.debug("enabled: " + enabled);

		if (!sunday && !monday && !tuesday && !wednesday && !thursday && !friday && !saturday) {
			doGetShowScheduleSingle(request, response, schedulerJobId, schedulerScheduleId, "Must select at least 1 day of the week");
		}

		else if (schedulerScheduleId == null) {
			data.addSchedulerSchedule(
					schedulerJobId,
					sunday,
					monday,
					tuesday,
					wednesday,
					thursday,
					friday,
					saturday,
					hour,
					minute,
					!enabled);
			doGetShowScheduleAll(request, response, schedulerJobId);
		} else {
			data.updateSchedulerSchedule(
					schedulerScheduleId,
					sunday,
					monday,
					tuesday,
					wednesday,
					thursday,
					friday,
					saturday,
					hour,
					minute,
					!enabled);
			doGetShowScheduleAll(request, response, schedulerJobId);
		}

	}

	private void doGetShowScheduleSingle(HttpServletRequest request, HttpServletResponse response, int schedulerJobId, Integer schedulerScheduleId, String errorMessage)
			throws ServletException, IOException {

		var title = "Schedule[" + schedulerScheduleId + "]";

		List<SchedulerSchedule> schedules = new ArrayList<SchedulerSchedule>();
		if (schedulerScheduleId == null) {
			schedules.add(new SchedulerSchedule());
		} else {
			schedules = data.getSchedulerSchedule(null, schedulerScheduleId);
		}

		if (schedules.size() == 0) {
			var html = "<h2>" + title + " not found<h2>";
			writeResponse(response, title, html, 404);
			return;
		}

		var schedule = schedules.get(0);
		var html = form(
				input().withId(SchedulerJob.ID).withName(SchedulerJob.ID).withType("hidden").withValue("" + schedulerJobId),
				input().withId(SchedulerSchedule.ID).withName(SchedulerSchedule.ID).withType("hidden").withValue((schedulerScheduleId == null ? "" : "" + schedulerScheduleId)),
				text("Schedule[" + schedulerScheduleId + "]"),
				br(),
				label("Sunday: ").withFor("schedulerScheduleSunday"),
				input().withId("schedulerScheduleSunday").withName("schedulerScheduleSunday").withType("checkbox").withCondChecked(schedule.isSunday()),
				br(),
				label("Monday: ").withFor("schedulerScheduleMonday"),
				input().withId("schedulerScheduleMonday").withName("schedulerScheduleMonday").withType("checkbox").withCondChecked(schedule.isMonday()),
				br(),
				label("Tuesday: ").withFor("schedulerScheduleTuesday"),
				input().withId("schedulerScheduleTuesday").withName("schedulerScheduleTuesday").withType("checkbox").withCondChecked(schedule.isTuesday()),
				br(),
				label("Wednesday: ").withFor("schedulerScheduleWednesday"),
				input().withId("schedulerScheduleWednesday").withName("schedulerScheduleWednesday").withType("checkbox").withCondChecked(schedule.isWednesday()),
				br(),
				label("Thursday: ").withFor("schedulerScheduleThursday"),
				input().withId("schedulerScheduleThursday").withName("schedulerScheduleThursday").withType("checkbox").withCondChecked(schedule.isThursday()),
				br(),
				label("Friday: ").withFor("schedulerScheduleFriday"),
				input().withId("schedulerScheduleFriday").withName("schedulerScheduleFriday").withType("checkbox").withCondChecked(schedule.isFriday()),
				br(),
				label("Saturday: ").withFor("schedulerScheduleSaturday"),
				input().withId("schedulerScheduleSaturday").withName("schedulerScheduleSaturday").withType("checkbox").withCondChecked(schedule.isSaturday()),
				br(),
				label("Hour: ").withFor("schedulerScheduleHour"),
				input().withId("schedulerScheduleHour").withName("schedulerScheduleHour").withType("text").withValue("" + schedule.getHour()),
				br(),
				label("Minute: ").withFor("schedulerScheduleMinute"),
				input().withId("schedulerScheduleMinute").withName("schedulerScheduleMinute").withType("text").withValue("" + schedule.getMinute()),
				br(),
				label("Enabled: ").withFor("schedulerScheduleEnabled"),
				input().withId("schedulerScheduleEnabled").withName("schedulerScheduleEnabled").withType("checkbox").withCondChecked(!schedule.isDisabled()),
				br(),

				input().withType("submit").withValue(schedulerScheduleId == null ? "Add" : "Save"))
						.withMethod("POST")

		;

		if (errorMessage == null) {
			errorMessage = "";
		} else {
			errorMessage = "<p class=\"errorMessage\">ERROR: " + errorMessage + "</p>";
		}

		writeResponse(response, title, errorMessage + html.renderFormatted(), 200);

	}

	private void doGetShowScheduleAll(HttpServletRequest request, HttpServletResponse response, Integer schedulerJobId) throws ServletException, IOException {
		var jobsAll = data.getSchedulerJob(schedulerJobId);

		var sb = new StringBuilder();

		for (var job : jobsAll) {
			var schedules = new ArrayList<SchedulerSchedule>(job.getSchedulerSchedules());
			Collections.sort(schedules, SchedulerSchedule.SORT_ID);
			var table = toTable(schedules);
			sb.append("<p>");
			var link = a("Add").withHref("/schedules" + parameters(SchedulerJob.ID, job.getSchedulerJobId(), "action", "add"));
			// var link = <a href="https://www.w3schools.com/">Visit W3Schools.com!</a>
			sb.append(h2("Job[" + job.getSchedulerJobId() + "] " + job.getName()));
			sb.append(link);
			sb.append(table.toHtml());
			sb.append("</p><br><br>");
		}

		writeResponse(response, "Schedules", sb.toString(), 200);
	}

	private Table toTable(Iterable<SchedulerSchedule> schedules) {
		var cols = List.of("", "SchedulerScheduleId", "SchedulerJobId", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Hour", "Minute", "Enabled");
		var rows = new ArrayList<List<Object>>();

		for (var schedule : schedules) {
			var schedulerScheduleId = schedule.getSchedulerScheduleId();
			var schedulerJobId = schedule.getSchedulerJob().getSchedulerJobId();
			var parsEdit = parameters(SchedulerJob.ID, schedulerJobId, SchedulerSchedule.ID, schedulerScheduleId, "action", "edit");
			var list = new ArrayList<Object>();
			list.add(a("Edit").withHref("/schedules" + parsEdit));
			list.add(a("Schedule[" + schedulerScheduleId + "]").withHref("/schedules" + parsEdit));
			list.add(a("Job[" + schedulerJobId + "]").withHref("/jobs" + parameters(SchedulerJob.ID, schedulerJobId)));
			list.add(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(schedule.isSunday()));
			list.add(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(schedule.isMonday()));
			list.add(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(schedule.isTuesday()));
			list.add(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(schedule.isWednesday()));
			list.add(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(schedule.isThursday()));
			list.add(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(schedule.isFriday()));
			list.add(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(schedule.isSaturday()));
			list.add(schedule.getHour());
			list.add(schedule.getMinute());
			list.add(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(!schedule.isDisabled()));
			rows.add(list);
		}

		return Table.parse(cols, rows);
	}

	@Override
	protected Nav getNav() {
		return Nav.SCHEDULES;
	}
}
