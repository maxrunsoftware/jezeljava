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

		String html = """
				<table>
				  <tr>
				    <th>JobId</th>
				    <th>Name</th>
				    <th>Path</th>
				    <th>Schedules</th>
				    <th>Actions</th>
				    <th>Logs</th>
				  </tr>
				  ${tableRows}
				</table>
				""";

		List<SchedulerJob> jobs;
		try {
			jobs = client.getSchedulerJobs();
		} catch (Exception e) {
			throw new IOException(e);
		}
		var sb = new StringBuilder();
		for (var job : jobs) {
			sb.append("<tr>");
			sb.append("  <td>" + job.getSchedulerJobId() + "</td>");
			sb.append("  <td>" + job.getName() + "</td>");
			sb.append("  <td>" + job.getPath() + "</td>");
			sb.append("  <td>" + job.getSchedulerSchedules().size() + "</td>");
			sb.append("  <td>" + job.getSchedulerActions().size() + "</td>");
			sb.append("  <td>" + job.getCommandLogJobs().size() + "</td>");
			sb.append("</tr>");
		}
		html = html.replace("${tableRows}", sb.toString());
		// String html = "hello " + request.getUserPrincipal().getName();

		writeResponse(response, html);
	}
}
