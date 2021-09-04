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

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.maxrunsoftware.jezel.BearerService;
import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.SettingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public abstract class ServletBase extends HttpServlet {
	private static final long serialVersionUID = 6575422778781011121L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletBase.class);
	private Object locker = new Object();
	private Map<String, Object> services = null;
	protected HttpSession session;
	protected SettingService settings;
	protected DatabaseService db;
	protected BearerService bearer;

	protected static final String HEADER_AUTHORIZATION = "AUTHORIZATION";
	protected static final String RESPONSE_STATUS = "status";
	protected static final String RESPONSE_STATUS_SUCCESS = "success";
	protected static final String RESPONSE_STATUS_FAILED = "failed";
	protected static final String RESPONSE_STATUS_AUTHORIZED = "authorized";
	protected static final String RESPONSE_STATUS_UNAUTHORIZED = "unauthorized";
	protected static final String RESPONSE_MESSAGE = "message";

	@Override
	public void init() throws ServletException {
		settings = getService(SettingService.class);
		db = getService(DatabaseService.class);
		bearer = getService(BearerService.class);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getService(Class<T> clazz) {
		synchronized (locker) {
			if (services == null) {
				services = new CaseInsensitiveMap<String, Object>();

				var ctx = getServletContext();
				for (var attrName : Collections.list(ctx.getAttributeNames())) {
					var attrVal = ctx.getAttribute(attrName);
					if (attrVal != null) {
						LOG.trace("Found attribute [" + attrName + "]: " + attrVal.getClass().getName());
						services.put(attrName, attrVal);
					}
				}
			}

			var o = services.get(clazz.getName());
			if (o == null) throw new IllegalArgumentException("No service found named " + clazz.getName());
			return (T) o;
		}
	}

	protected static void writeResponse(HttpServletResponse response, String json) {
		writeResponse(response, json, HttpServletResponse.SC_OK);
	}

	protected static void writeResponse(HttpServletResponse response, JsonObject json) {
		writeResponse(response, toJsonString(json, true));
	}

	protected static void writeResponse(HttpServletResponse response, JsonObjectBuilder json) {
		writeResponse(response, toJsonString(json, true));
	}

	protected static void writeResponse(HttpServletResponse response, JsonObject json, int statusCode) {
		writeResponse(response, toJsonString(json, true), statusCode);
	}

	protected static void writeResponse(HttpServletResponse response, JsonObjectBuilder json, int statusCode) {
		writeResponse(response, toJsonString(json, true), statusCode);
	}

	protected static void writeResponse(HttpServletResponse response, String json, int statusCode) {
		LOG.trace("Writing response [" + statusCode + "]: " + json);
		response.setContentType(Constant.CONTENTTYPE_JSON);
		response.setCharacterEncoding(Constant.ENCODING_UTF8);
		response.setStatus(statusCode);
		response.addHeader("Cache-Control", "no-cache");
		response.addHeader("Content-Language", "en-US");
		try {
			response.getWriter().print(json);
		} catch (IOException ioe) {
			LOG.error("Error writing response", ioe);
		}
	}

	protected static void writeResponse(HttpServletResponse response, String status, String message, int statusCode) {
		var json = createObjectBuilder()
				.add(RESPONSE_STATUS, status)
				.add(RESPONSE_MESSAGE, message);
		writeResponse(response, json.build(), statusCode);
	}

	protected static Integer getParameterId(HttpServletRequest request, String idName) {
		String val = trimOrNull(getParameter(request, idName));
		if (val == null) return null;
		try {
			return Integer.parseInt(val);
		} catch (Throwable t) {
			return null;
		}
	}

	protected static String getParameter(HttpServletRequest request, String name) {
		name = trimOrNull(name);
		if (name == null) return null;
		name = name.toLowerCase();
		for (var pName : Collections.list(request.getParameterNames())) {
			if (pName.toLowerCase().equals(name)) return request.getParameter(pName);
		}
		return null;
	}

	private boolean authorize(HttpServletRequest request, HttpServletResponse response) {
		var authHeader = request.getHeader(HEADER_AUTHORIZATION);
		LOG.debug(HEADER_AUTHORIZATION + ": " + authHeader);

		var authBearer = httpAuthorizationDecodeBearer(authHeader);
		String errorMessage = null;
		if (authBearer == null) {
			errorMessage = "Please provide AUTHORIZATION Bearer token";
		} else if (!this.bearer.authBearer(authBearer)) {

			errorMessage = "Invalid AUTHORIZATION Bearer token " + authBearer;
		}

		if (errorMessage == null) return true;

		LOG.debug(errorMessage);

		if (settings.getWebIgnoreCredentials()) return true;

		var json = createObjectBuilder()
				.add(RESPONSE_STATUS, RESPONSE_STATUS_UNAUTHORIZED)
				.add(RESPONSE_MESSAGE, errorMessage);

		writeResponse(response, json);
		return false;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (authorize(request, response)) { doGetAuthorized(request, response); }
	}

	protected void doGetAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (authorize(request, response)) { doPutAuthorized(request, response); }
	}

	protected void doPutAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doPut(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (authorize(request, response)) { doPostAuthorized(request, response); }
	}

	protected void doPostAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doPost(request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (authorize(request, response)) { doDeleteAuthorized(request, response); }
	}

	protected void doDeleteAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doDelete(request, response);
	}

}
