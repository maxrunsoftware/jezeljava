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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginServlet extends ServletBase {

	private static final long serialVersionUID = 6377692026473347039L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String html = """
				<form method=POST action="/j_security_check">
					<label for="j_username">Username: </label>
				   	<input id="j_username" type="text" name="j_username" />
				   	<br><br>
					<label for="j_password">Password: </label>
				   	<input id="j_password" type="password" name="j_password" />
				   	<br><br>
				   	<input type="submit" value="Login" />
				</form>
					""";

		writeResponse(response, html);
	}

	@Override
	protected Nav getNav() {
		return Nav.NONE;
	}

}
