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
import java.util.Map;

import com.maxrunsoftware.jezel.action.CommandParameter;
import com.maxrunsoftware.jezel.model.ConfigurationItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ConfigurationItemServlet extends ServletBase {
	private static final long serialVersionUID = 6084447000052305725L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationItemServlet.class);

	@Override
	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Map<String, String> map;
		try (var session = db.openSession()) {
			map = ConfigurationItem.getValues(session);
		}

		var arrayBuilder = createArrayBuilder();
		for (var key : map.keySet()) {
			var mapValue = map.get(key);
			var o = createObjectBuilder();
			o.add("name", key);
			o.add("value", coalesce(mapValue, ""));
			var cp = CommandParameter.get(key);
			if (cp != null) { o.add(CommandParameter.NAME, cp.toJson()); }
			arrayBuilder.add(o);
		}

		var json = createObjectBuilder()
				.add(RESPONSE_STATUS, RESPONSE_STATUS_SUCCESS)
				.add(RESPONSE_MESSAGE, "Found " + map.size() + " " + ConfigurationItem.class.getSimpleName() + "s")
				.add(ConfigurationItem.NAME, arrayBuilder.build());

		writeResponse(response, json);
	}

	@Override
	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try (var session = db.openSession()) {

			for (var pName : Collections.list(request.getParameterNames())) {
				var pValue = trimOrNull(request.getParameter(pName));
				pName = trimOrNull(pName);
				if (pName == null) continue;
				ConfigurationItem.setValueExisting(session, pName, pValue);

			}
		}

		writeResponse(response, RESPONSE_STATUS_SUCCESS, ConfigurationItem.class.getSimpleName() + " successfully saved", 200);
	}

}
