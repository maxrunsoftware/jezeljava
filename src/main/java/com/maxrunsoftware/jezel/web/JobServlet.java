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
import java.util.List;

import com.maxrunsoftware.jezel.model.SchedulerJob;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JobServlet extends ServletBase {
	private static final long serialVersionUID = 6343839739720974399L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		var headers = List.of("", "JobId", "Name", "Path", "Schedules", "Actions", "Logs", "Enabled");
		List<SchedulerJob> jobs;
		try {
			jobs = client.getSchedulerJobs();
		} catch (Exception e) {
			throw new IOException(e);
		}

		var html = table(attrs("#table-example"),
				thead(each(headers, h -> th(h))),
				tbody(each(jobs, i -> tr(
						td(a("Edit").withHref("/jobs?schedulerJobId=" + i.getSchedulerJobId())),
						td("" + i.getSchedulerJobId()),
						td("" + i.getName()),
						td("" + i.getPath()),
						td("" + i.getSchedulerSchedules().size()),
						td("" + i.getSchedulerActions().size()),
						td("" + i.getCommandLogJobs().size()),
						td(input().attr("type", "checkbox").attr("disabled", "disabled").withCondChecked(!i.isDisabled())
						// End of row
						)))));

		writeResponse(response, html.renderFormatted());
	}

	@Override
	protected Nav getNav() {
		return Nav.JOBS;
	}
}
