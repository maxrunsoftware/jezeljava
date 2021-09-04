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

import com.maxrunsoftware.jezel.Constant;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

public class ServletBase extends HttpServlet {
	private static final long serialVersionUID = 7162466372715656028L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletBase.class);

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
						</style>
					</head>
					<body>
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
