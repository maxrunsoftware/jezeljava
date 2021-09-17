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
import static j2html.TagCreator.*;

import java.io.IOException;
import java.util.Collections;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.Util;
import com.maxrunsoftware.jezel.action.CommandParameter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ConfigServlet extends ServletBase {
	private static final long serialVersionUID = 7101247365121294111L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var sb = new StringBuilder();
		var configs = data.getConfigurationItems();
		sb.append("<form method=\"post\">");
		int i = 0;
		for (var configItem : configs) {
			var prefix = StringUtils.right("0000" + i, 4);
			var idName = prefix + "n";
			var idValue = prefix + "v";
			sb.append("<div>");

			sb.append(input()
					.withType("text")
					.withId(idName).withName(idName)
					.isReadonly()
					.withSize("50")
					.withStyle(Constant.STYLE_FONT_MONO)
					.withValue(configItem.name()));

			sb.append(" &#8594; ");
			var valueObj = configItem.value();
			var value = valueObj == null ? "" : valueObj.toString();

			var param = configItem.parameter();
			if (param == null || equalsAny(param.getType(), CommandParameter.TYPE_STRING, CommandParameter.TYPE_FILENAME)) {
				sb.append(input()
						.withType("text")
						.withId(idValue).withName(idValue)
						.withStyle(Constant.STYLE_FONT_MONO)
						.withValue(value));

			} else if (param.getType().equalsIgnoreCase(CommandParameter.TYPE_TEXT)) {
				sb.append(textarea()
						.withId(idValue).withName(idValue)
						.withRows("2").withCols("50")
						.withStyle(Constant.STYLE_FONT_MONO)
						.withText(value));

			} else if (param.getType().equalsIgnoreCase(CommandParameter.TYPE_INT)) {
				sb.append(input()
						.withType("number")
						.withId(idValue).withName(idValue)
						.withStyle(Constant.STYLE_FONT_MONO)
						.withMin(param.getMinValue() == null ? "0" : param.getMinValue().toString())
						.withMax(param.getMaxValue() == null ? ("" + Integer.MAX_VALUE) : param.getMaxValue().toString())
						.withValue(value));
			} else if (param.getType().equalsIgnoreCase(CommandParameter.TYPE_BOOL)) {
				sb.append(input()
						.withType("checkbox")
						.withId(idValue).withName(idValue)
						.withCondChecked(value == null ? false : parseBoolean(value)));
			} else if (param.getType().equalsIgnoreCase(CommandParameter.TYPE_OPTION)) {
				sb.append("<select id=\"" + idValue + "\" name=\"" + idValue + "\">");
				for (var optionValue : param.getOptionValues()) {
					sb.append("<option value=\"" + optionValue + "\">" + optionValue + "</option>");
				}
				sb.append("</select>");
			}

			sb.append("</div>");
			sb.append("<br>");
			i++;
		}

		sb.append(input().withType("submit").withValue("Save"));
		sb.append("</form>");

		writeResponse(response, "Configuration", sb.toString(), 200);

	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		var mapNames = new TreeMap<Integer, String>();
		var mapValues = new TreeMap<Integer, String>();

		for (var pName : Collections.list(request.getParameterNames())) {
			var pValue = trimOrNull(request.getParameter(pName));
			LOG.debug(pName + ": " + pValue);
			pName = trimOrNull(pName);
			if (pName == null) continue;
			pName = pName.toLowerCase();
			if (pName.length() != 5) continue;
			if (pName.endsWith("n") || pName.endsWith("v")) {
				// NOOP
			} else {
				continue;
			}

			var partNum = parseInt(pName.substring(0, 4));
			var partType = pName.substring(4);
			if (partType.equals("n")) {
				if (pValue == null) continue;
				mapNames.put(partNum, pValue);
			} else if (partType.equals("v")) {
				// Add to value map
				mapValues.put(partNum, pValue);
			}
		}

		var map = Util.<String>mapCaseInsensitive();

		for (var key : mapNames.keySet()) {
			var name = mapNames.get(key);
			var value = mapValues.get(key);
			map.put(name, value);
		}

		data.saveConfigurationItems(map);

		doGet(request, response);
	}

	@Override
	protected Nav getNav() {
		return Nav.CONFIG;
	}
}
