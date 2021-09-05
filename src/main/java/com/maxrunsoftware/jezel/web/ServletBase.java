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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.SettingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

public class ServletBase extends HttpServlet {
	private static final long serialVersionUID = 7162466372715656028L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletBase.class);

	protected SettingService settings;
	protected RestClient client;
	private final Object locker = new Object();
	private Map<String, Object> services;

	@Override
	public void init() throws ServletException {
		settings = getService(SettingService.class);
		client = getService(RestClient.class);
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

	protected void writeResponse(HttpServletResponse response, String html) {
		writeResponse(response, trimOrNull(getClass().getSimpleName().replace("Servlet", "")), html);
	}

	protected void writeResponse(HttpServletResponse response, String title, String html) {
		writeResponse(response, title, html, 200);
	}

	protected void writeResponse(HttpServletResponse response, String title, String html, int statusCode) {
		html = coalesce(trimOrNull(html), "Missing HTML");

		var str = """
				<html dir="ltr" lang="en">
					<head>
						<meta charset="utf-8">
						<title>${title}</title>
						<style>
						.topnav {
						  background-color: #333;
						  overflow: hidden;
						}

						/* Style the links inside the navigation bar */
						.topnav a {
						  float: left;
						  color: #f2f2f2;
						  text-align: center;
						  padding: 14px 16px;
						  text-decoration: none;
						  font-size: 17px;
						}

						/* Change the color of links on hover */
						.topnav a:hover {
						  background-color: #ddd;
						  color: black;
						}

						/* Add a color to the active/current link */
						.topnav a.active {
						  background-color: #04AA6D;
						  color: white;
						}
						</style>
					</head>
					<body>
						<div class="topnav">
							<a class="active" href="/">Home</a>
							<a href="/jobs">Jobs</a>
							<a href="/logs">Logs</a>
							<a href="/logout">Logout</a>
						</div>
						<br>

						${body}
					</body>
				</html>
				""";

		str = str.replace("${title}", title);
		str = str.replace("${body}", html);
		str = trimOrNull(str);
		html = str;

		LOG.trace("Writing response [" + statusCode + "]: " + html);
		response.setContentType(Constant.CONTENTTYPE_HTML);
		response.setCharacterEncoding(Constant.ENCODING_UTF8);
		response.setStatus(statusCode);
		response.addHeader("Cache-Control", "no-cache");
		response.addHeader("Content-Language", "en-US");
		try {
			response.getWriter().print(html);
		} catch (IOException ioe) {
			LOG.error("Error writing response", ioe);
		}
	}

}
