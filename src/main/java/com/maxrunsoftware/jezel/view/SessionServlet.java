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
import java.util.UUID;

import javax.json.Json;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SessionServlet extends ServletBase {
	private static final long serialVersionUID = 5705514443865496459L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var authUserPass = httpAuthorizationDecode(request.getHeader(HEADER_AUTHORIZATION));
		if (authUserPass == null) {
			LOG.debug("No credentials provided");
			unauthorized(response);
			return;
		}

		// TODO: Auth credentials

		authorized(response);
	}

	private void authorized(HttpServletResponse response) throws IOException {

		var bearer = UUID.randomUUID().toString().replace("-", "");
		LOG.debug("Authorized: " + bearer);
		this.bearer.addBearer(bearer);

		var json = Json.createObjectBuilder()
				.add("status", RESPONSE_STATUS_AUTHORIZED)
				.add("bearer", bearer);

		writeResponse(response, json);
	}

	private void unauthorized(HttpServletResponse response) throws IOException {
		LOG.debug("Unauthorized");

		var json = createObjectBuilder()
				.add("status", RESPONSE_STATUS_UNAUTHORIZED)
				.add("bearer", "");

		writeResponse(response, json, HttpServletResponse.SC_UNAUTHORIZED);

	}

}
