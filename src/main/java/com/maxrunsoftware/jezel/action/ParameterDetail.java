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
package com.maxrunsoftware.jezel.action;

import java.util.ArrayList;
import java.util.List;

public class ParameterDetail {
	private static final String TYPE_STRING = "string";
	private static final String TYPE_TEXT = "text";
	private static final String TYPE_INT = "integer";
	private static final String TYPE_BOOL = "boolean";
	private static final String TYPE_FILENAME = "filename";
	private static final String TYPE_OPTION = "option";

	private final String name;
	private final String description;
	private final String type;
	private int minValue;
	private int maxValue;
	private String defaultValue;
	private final List<String> optionValues = new ArrayList<String>();

	public ParameterDetail(String name, String description, String type) {
		this.name = name;
		this.description = description;
		this.type = type;
	}

	public int getMinValue() {
		return minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public List<String> getOptionValues() {
		return optionValues;
	}

	public static ParameterDetail createString(String name, String description) {
		return new ParameterDetail(name, description, TYPE_STRING);
	}

	public static ParameterDetail createText(String name, String description) {
		return new ParameterDetail(name, description, TYPE_TEXT);
	}

	public static ParameterDetail createInteger(String name, String description) {
		return new ParameterDetail(name, description, TYPE_INT);
	}

	public static ParameterDetail createInteger(String name, String description, int minValue, int maxValue) {
		var p = new ParameterDetail(name, description, TYPE_INT);
		p.minValue = minValue;
		p.maxValue = maxValue;
		return p;
	}

	public static ParameterDetail createInteger(String name, String description, int minValue, int maxValue, int defaultValue) {
		var p = new ParameterDetail(name, description, TYPE_INT);
		p.minValue = minValue;
		p.maxValue = maxValue;
		p.defaultValue = "" + defaultValue;
		return p;
	}

	public static ParameterDetail createBoolean(String name, String description) {
		var p = new ParameterDetail(name, description, TYPE_BOOL);
		p.defaultValue = "false";
		return p;
	}

	public static ParameterDetail createFilename(String name, String description) {
		return new ParameterDetail(name, description, TYPE_FILENAME);
	}

	public static ParameterDetail createOption(String name, String description, String defaultValue, String... options) {
		var p = new ParameterDetail(name, description, TYPE_OPTION);
		p.defaultValue = defaultValue;
		for (var s : options) {
			p.optionValues.add(s);
		}
		return p;
	}

}
