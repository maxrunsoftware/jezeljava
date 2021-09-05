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

import com.maxrunsoftware.jezel.Version;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HomeServlet extends ServletBase {

	private static final long serialVersionUID = 2114495585770348816L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String html = """
				Jezel Job Scheduling Engine  v${version}  dev@maxrunsoftware.com
				""";
		html = html.replace("${version}", Version.VALUE);
		// String html = "hello " + request.getUserPrincipal().getName();

		writeResponse(response, "Home", html, 200);
	}

	@Override
	protected Nav getNav() {
		return Nav.HOME;
	}

}
