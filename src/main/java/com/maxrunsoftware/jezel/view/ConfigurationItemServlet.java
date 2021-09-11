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
import java.util.Collections;

import com.maxrunsoftware.jezel.model.ConfigurationItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ConfigurationItemServlet extends ServletBase {
	private static final long serialVersionUID = 6084447000052305725L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationItemServlet.class);

	@Override
	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var map = config.getConfigurationItems();

		var arrayBuilder = createArrayBuilder();
		for (var key : map.keySet()) {
			var name = trimOrNull(key);
			if (name == null) continue;
			name = name.toLowerCase();
			var val = map.get(key);
			if (val == null) continue;

			var objectBuilder = createObjectBuilder();
			objectBuilder.add("name", name);
			objectBuilder.add("value", val);
			arrayBuilder.add(objectBuilder.build());
		}

		var json = createObjectBuilder()
				.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
				.add(RESPONSE_MESSAGE, "Found " + map.size() + " ConfigurationItems");

		json.add(ConfigurationItem.NAME, arrayBuilder.build());

		writeResponse(response, json);
	}

	@Override
	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		for (var pName : Collections.list(request.getParameterNames())) {
			var pValue = trimOrNull(request.getParameter(pName));
			pName = trimOrNull(pName);
			if (pName == null) continue;
			pName = pName.toLowerCase();

			config.setConfigurationItem(pName, pValue);
		}

		writeResponse(response, RESPONSE_STATUS_SUCCESS, "ConfigurationItem successfully saved", 200);
	}

	@Override
	protected void doDeleteAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		config.clearConfigurationItems();
		writeResponse(response, RESPONSE_STATUS_SUCCESS, "ConfigurationItems successfully deleted", 200);
	}

}
