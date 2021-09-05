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
import java.util.List;
import java.util.TreeMap;

import com.maxrunsoftware.jezel.model.SchedulerJob;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JobServlet extends ServletBase {
	private static final long serialVersionUID = 6343839739720974399L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameterInt(request, SchedulerJob.ID);
		if (schedulerJobId == null) {
			doGetShowJobs(request, response);
		} else {
			doGetShowJob(request, response, schedulerJobId);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var schedulerJobId = getParameter(request, "schedulerJobId");
		var name = getParameter(request, "schedulerJobName");
		var group = getParameter(request, "schedulerJobGroup");
		var enabled = getParameter(request, "schedulerJobEnabled");

		var title = "Job[" + schedulerJobId + "]";
		var html = p(
				text("SchedulerJobId: " + schedulerJobId),
				br(),
				text("Name: " + name),
				br(),
				text("Group: " + group),
				br(),
				text("Enabled: " + enabled)

		);

		writeResponse(response, title, html.renderFormatted(), 200);

	}

	private void doGetShowJob(HttpServletRequest request, HttpServletResponse response, int schedulerJobId) throws ServletException, IOException {

		var title = "Job[" + schedulerJobId + "]";

		SchedulerJob job;
		try {
			job = client.getSchedulerJob(schedulerJobId);
		} catch (Exception e) {
			throw new IOException(e);
		}

		if (job == null) {
			var html = "<h2>" + title + " not found<h2>";
			writeResponse(response, title, html, 404);
			return;
		}

		var html = form(
				text("Job[" + schedulerJobId + "]"),
				input().withId("schedulerJobId").withName("schedulerJobId").withType("hidden").withValue("" + schedulerJobId),
				br(),
				label("Name: ").withFor("schedulerJobName"),
				input().withId("schedulerJobName").withName("schedulerJobName").withType("text").withValue(job.getName()),
				br(),
				label("Group: ").withFor("schedulerJobGroup"),
				input().withId("schedulerJobGroup").withName("schedulerJobGroup").withType("text").withValue(job.getGroup()),
				br(),
				label("Enabled: ").withFor("schedulerJobEnabled"),
				input().withId("schedulerJobEnabled").withName("schedulerJobEnabled").withType("checkbox").withCondChecked(!job.isDisabled()),
				br(),
				input().withType("submit").withValue("Save"))
						.withMethod("POST")

		;

		writeResponse(response, title, html.renderFormatted(), 200);

	}

	private void doGetShowJobs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var headers = List.of("", "JobId", "Name", "Group", "Schedules", "Actions", "Logs", "Enabled");
		List<SchedulerJob> jobsAll;
		try {
			jobsAll = client.getSchedulerJobs();
		} catch (Exception e) {
			throw new IOException(e);
		}

		var map = new TreeMap<String, ArrayList<SchedulerJob>>();
		for (var job : jobsAll) {
			var key = job.getGroup();
			if (key == null) key = "";
			key = key.toUpperCase();
			var list = map.get(key);
			if (list == null) {
				list = new ArrayList<SchedulerJob>();
				map.put(key, list);
			}
			list.add(job);
		}

		var sb = new StringBuilder();
		for (var key : map.keySet()) {
			var jobs = map.get(key);
			sb.append("<p>");
			sb.append(h2(key));
			var table = table(attrs("#table-example"),
					thead(each(headers, h -> th(h))),
					tbody(each(jobs, i -> tr(
							td(a("Edit").withHref("/jobs?schedulerJobId=" + i.getSchedulerJobId())),
							td("" + i.getSchedulerJobId()),
							td("" + i.getName()),
							td("" + i.getGroup()),
							td("" + i.getSchedulerSchedules().size()),
							td("" + i.getSchedulerActions().size()),
							td("" + i.getCommandLogJobs().size()),
							td(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(!i.isDisabled())
							// End of row
							)))));
			sb.append(table);
			sb.append("</p><br><br>");
		}
		writeResponse(response, "Jobs", sb.toString(), 200);
	}

	@Override
	protected Nav getNav() {
		return Nav.JOBS;
	}
}
