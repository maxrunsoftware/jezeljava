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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.maxrunsoftware.jezel.model.CommandLogAction;
import com.maxrunsoftware.jezel.model.CommandLogJob;
import com.maxrunsoftware.jezel.model.CommandLogMessage;
import com.maxrunsoftware.jezel.model.SchedulerJob;
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
				"Action",
				"Type",
				"Timestamp",
				"Level",
				"Message"

		);

		var rows = new ArrayList<ArrayList<Object>>();

		var commandLogJob = commandLogJobs.get(0);
		var commandLogActions = new ArrayList<CommandLogAction>(commandLogJob.getCommandLogActions());
		Collections.sort(commandLogActions, CommandLogAction.SORT_INDEX);
		for (var commandLogAction : commandLogActions) {
			var list = new ArrayList<Object>();
			list.add(commandLogAction.getName());
			list.add("Start");
			list.add(format(commandLogAction.getStart()));
			list.add("");
			list.add("");
			rows.add(list);

			var commandLogMessages = new ArrayList<CommandLogMessage>(commandLogAction.getCommandLogMessages());
			Collections.sort(commandLogMessages, CommandLogMessage.SORT_INDEX);
			for (var commandLogMessage : commandLogMessages) {
				list = new ArrayList<Object>();
				list.add(commandLogAction.getName());
				list.add("Message");
				list.add(format(commandLogMessage.getTimestamp()));
				list.add(commandLogMessage.getLevel());
				list.add("<pre>" + coalesce(commandLogMessage.getMessage(), commandLogMessage.getException(), "") + "</pre>");
				rows.add(list);
			}

			if (commandLogAction.getEnd() != null) {
				list = new ArrayList<Object>();
				list.add(commandLogAction.getName());
				list.add("End");
				list.add(format(commandLogAction.getEnd()));
				list.add("");
				list.add("");
				rows.add(list);
			}

		}

		var table = Table.parse(columns, rows);

		var cljhtml = p(
				text(CommandLogAction.ID + "[" + commandLogJobId + "]"),
				br(),
				text("Start: " + format(commandLogJob.getStart())),
				br(),
				text("End: " + format(commandLogJob.getEnd())),
				br(),
				text("Error: " + commandLogJob.isError()),
				br());

		var sb = new StringBuilder();
		sb.append(cljhtml);
		sb.append("<p>");
		var htmlFormatter = new Table.HtmlFormatter() {
			@Override
			public void colgroup(StringBuilder sb, List<String> columns) {
				sb.append("<colgroup>");
				sb.append("<col style=\"width: 15%;\">");
				sb.append("<col style=\"width: 10%;\">");
				sb.append("<col style=\"width: 15%;\">");
				sb.append("<col style=\"width: 10%;\">");
				sb.append("<col style=\"width: 50%;\">");
				sb.append("</colgroup>");
			}
		};
		sb.append(table.toHtml(htmlFormatter));
		sb.append("</p>");

		writeResponse(response, CommandLogAction.ID + "[" + commandLogJobId + "]", sb.toString(), 200);

	}

	private static String format(LocalDateTime datetime) {
		if (datetime == null) return "";
		var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return datetime.format(formatter);
	}

	private void doGetShowLogAll(HttpServletRequest request, HttpServletResponse response, Integer schedulerJobId) throws ServletException, IOException {
		var commandLogJobs = data.getCommandLogJob(null, schedulerJobId);

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

		var sb = new StringBuilder();
		for (var schedulerJobId2 : map.keySet()) {
			var commandLogJobs2 = map.get(schedulerJobId2);
			Collections.sort(commandLogJobs2, CommandLogJob.SORT_JOB);
			LOG.debug("Displaying for Job[" + schedulerJobId2 + "]");
			sb.append("<p>");
			sb.append(SchedulerJob.NAME + "[" + schedulerJobId2 + "] " + commandLogJobs2.get(0).getSchedulerJob().getName());
			sb.append("</p>");

			sb.append("<p>");
			var tableColumns = List.of("", "Start", "End", "Is Error");
			var tableList = new ArrayList<ArrayList<String>>();
			for (var commandLogJob2 : commandLogJobs2) {
				var list = new ArrayList<String>();
				var link = a("View").withHref("/logs" + parameters(CommandLogJob.ID, commandLogJob2.getCommandLogJobId()));
				list.add(link.renderFormatted());
				list.add(commandLogJob2.getStart() != null ? format(commandLogJob2.getStart()) : "");
				list.add(commandLogJob2.getEnd() != null ? format(commandLogJob2.getEnd()) : "");
				list.add("" + commandLogJob2.isError());
				tableList.add(list);
			}

			var table = Table.parse(tableColumns, tableList);
			sb.append(table.toHtml());
			sb.append("</p>");
		}

		writeResponse(response, "CommandLogJobs", sb.toString(), 200);

	}

	@Override
	protected Nav getNav() {
		return Nav.LOGS;
	}
}
