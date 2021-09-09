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

import static j2html.TagCreator.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.maxrunsoftware.jezel.model.CommandLogAction;
import com.maxrunsoftware.jezel.model.CommandLogJob;
import com.maxrunsoftware.jezel.model.CommandLogMessage;
import com.maxrunsoftware.jezel.model.SchedulerJob;
import com.maxrunsoftware.jezel.model.SchedulerSchedule;
import com.maxrunsoftware.jezel.util.Table;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LogJobServlet extends ServletBase {
	private static final long serialVersionUID = 5343838486663771389L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LogJobServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterInt(request, SchedulerJob.ID);
		var commandLogJobId = getParameterInt(request, CommandLogJob.ID);
		if (commandLogJobId != null) {
			doGetShowLogSingle(request, response, commandLogJobId);
		} else {
			doGetShowLogAll(request, response, schedulerJobId);
		}
	}

	private void doGetShowLogSingle(HttpServletRequest request, HttpServletResponse response, int commandLogJobId) throws ServletException, IOException {
		var title = "CommandLogJob[" + commandLogJobId + "]";

		var commandLogJobs = data.getCommandLogJob(commandLogJobId, null);

		if (commandLogJobs.size() == 0) {
			var html = "<h2>" + title + " not found<h2>";
			writeResponse(response, title, html, 404);
			return;
		}

		var columns = List.of(
				"ID",
				"Type",
				"Index",
				"Timestamp",
				"Level",
				"Message",
				"Error"

		);

		var rows = new ArrayList<ArrayList<Object>>();

		var commandLogJob = commandLogJobs.get(0);
		var commandLogActions = new ArrayList<CommandLogAction>(commandLogJob.getCommandLogActions());
		Collections.sort(commandLogActions, CommandLogAction.SORT_INDEX);
		for (var commandLogAction : commandLogActions) {
			var list = new ArrayList<Object>();
			list.add(CommandLogAction.ID + "[" + commandLogAction.getCommandLogActionId() + "]");
			list.add("Action");
			list.add(commandLogAction.getIndex());
			list.add(commandLogAction.getStart());
			list.add("");
			list.add("");
			list.add("");
			rows.add(list);

			var commandLogMessages = new ArrayList<CommandLogMessage>(commandLogAction.getCommandLogMessages());
			Collections.sort(commandLogMessages, CommandLogMessage.SORT_INDEX);
			for (var commandLogMessage : commandLogMessages) {
				list = new ArrayList<Object>();
				list.add(CommandLogMessage.ID + "[" + commandLogMessage.getCommandLogMessageId() + "]");
				list.add("Message");
				list.add(commandLogMessage.getIndex());
				list.add(commandLogMessage.getTimestamp());
				list.add(commandLogMessage.getLevel());
				list.add(commandLogMessage.getMessage());
				list.add(commandLogMessage.getException());
				rows.add(list);
			}

			list = new ArrayList<Object>();
			list.add(CommandLogAction.ID + "[" + commandLogAction.getCommandLogActionId() + "]");
			list.add("Action");
			list.add(commandLogAction.getIndex());
			list.add(commandLogAction.getEnd());
			list.add("");
			list.add("");
			list.add("");
			rows.add(list);
		}

		var table = Table.parse(columns, rows);

		var cljhtml = p(
				text(CommandLogAction.ID + "[" + commandLogJobId + "]"),
				br(),
				text("Start: " + commandLogJob.getStart()),
				br(),
				text("End: " + commandLogJob.getEnd()),
				br(),
				text("Error: " + commandLogJob.isError()),
				br());

		var sb = new StringBuilder();
		sb.append(cljhtml);
		sb.append(table.toHtml());

		writeResponse(response, CommandLogAction.ID + "[" + commandLogJobId + "]", sb.toString(), 200);

	}

	private void doGetShowLogAll(HttpServletRequest request, HttpServletResponse response, Integer schedulerJobId) throws ServletException, IOException {
		var commandLogJobs = data.getCommandLogJob(null, schedulerJobId);

		var sb = new StringBuilder();

		int currentSchedulerJobId = -1;
		
		var map = new TreeMap<Integer, ArrayList<CommandLogJob>>();
		for (var commandLogJob : commandLogJobs) {
			schedulerJobId = commandLogJob.getSchedulerJob().getSchedulerJobId();
			var list = map.get(schedulerJobId);
			if (list == null) {
				list = new ArrayList<CommandLogJob>();
				map.put(schedulerJobId, list);
			}
			list.add(commandLogJob);
		}
		
		for(var schedulerJobId2 : map.keySet()) {
			var commandLogJobs2 = map.get(schedulerJobId2);
			Collections.sort(commandLogJobs2, CommandLogJob.SORT_JOB);
			commandLogJobs2.
		}
		
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

	@Override
	protected Nav getNav() {
		return Nav.LOGS;
	}
}
