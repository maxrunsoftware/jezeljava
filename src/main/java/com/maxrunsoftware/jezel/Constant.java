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
package com.maxrunsoftware.jezel;

import java.util.List;

import com.google.inject.Injector;
import com.maxrunsoftware.jezel.action.Command;
import com.maxrunsoftware.jezel.action.SqlQuery;
import com.maxrunsoftware.jezel.model.CommandLogAction;
import com.maxrunsoftware.jezel.model.CommandLogJob;
import com.maxrunsoftware.jezel.model.CommandLogMessage;
import com.maxrunsoftware.jezel.model.SchedulerAction;
import com.maxrunsoftware.jezel.model.SchedulerActionParameter;
import com.maxrunsoftware.jezel.model.SchedulerJob;
import com.maxrunsoftware.jezel.model.SchedulerSchedule;
import com.maxrunsoftware.jezel.service.DatabaseServiceH2;
import com.maxrunsoftware.jezel.service.SchedulerServiceQuartz;
import com.maxrunsoftware.jezel.service.SettingServiceEnvironment;
import com.maxrunsoftware.jezel.service.WebServiceJetty;
import com.maxrunsoftware.jezel.service.WebServiceJettyBearerMemory;
import com.maxrunsoftware.jezel.view.HomeServlet;
import com.maxrunsoftware.jezel.view.SchedulerActionParameterServlet;
import com.maxrunsoftware.jezel.view.SchedulerActionServlet;
import com.maxrunsoftware.jezel.view.SchedulerJobServlet;
import com.maxrunsoftware.jezel.view.SchedulerScheduleServlet;
import com.maxrunsoftware.jezel.view.SessionServlet;

import jakarta.servlet.Servlet;

public class Constant {
	private Constant() {}

	public static record Page(Class<? extends Servlet> servlet, String path) {}

	public static final List<String> LOGGING_LIBS = List.of(
			"org.apache.hc.client5.http",
			"org.apache.hc.client5.http.wire",
			"org.eclipse.jetty",
			"org.quartz",
			"org.hibernate",
			"com.mchange",
			"org.jboss"

	);

	public static final List<Page> REST_SERVLETS = List.of(
			new Page(HomeServlet.class, "/*"),
			new Page(SessionServlet.class, "/session"),
			new Page(SchedulerJobServlet.class, "/job"),
			new Page(SchedulerScheduleServlet.class, "/job/schedule"),
			new Page(SchedulerActionServlet.class, "/job/action"),
			new Page(SchedulerActionParameterServlet.class, "/job/action/parameter")

	);

	public static final List<Page> WEB_SERVLETS = List.of(
			new Page(com.maxrunsoftware.jezel.web.LoginServlet.class, "/login"),
			new Page(com.maxrunsoftware.jezel.web.LogoutServlet.class, "/logout"),
			new Page(com.maxrunsoftware.jezel.web.HomeServlet.class, "/*"),
			new Page(com.maxrunsoftware.jezel.web.JobServlet.class, "/jobs"),
			new Page(com.maxrunsoftware.jezel.web.ScheduleServlet.class, "/schedules")

	);

	public static final List<Class<?>> JPA = List.of(
			SchedulerJob.class,
			SchedulerAction.class,
			SchedulerActionParameter.class,
			SchedulerSchedule.class,
			CommandLogJob.class,
			CommandLogAction.class,
			CommandLogMessage.class

	);

	@SuppressWarnings("rawtypes")
	public static record InjectorBind(Class classInterface, Class classImplementation, boolean singleton) {}

	public static final List<InjectorBind> BINDS = List.of(
			new InjectorBind(SettingService.class, SettingServiceEnvironment.class, true),
			new InjectorBind(WebService.class, WebServiceJetty.class, true),
			new InjectorBind(DatabaseService.class, DatabaseServiceH2.class, true),
			new InjectorBind(SchedulerService.class, SchedulerServiceQuartz.class, true),
			new InjectorBind(BearerService.class, WebServiceJettyBearerMemory.class, true));

	public static final List<Class<? extends Command>> COMMANDS = List.of(
			SqlQuery.class

	);

	private static Injector injector;

	public static void setInjector(Injector injector) {
		Constant.injector = injector;
	}

	public static <T> T getInstance(Class<T> clazz) {
		return injector.getInstance(clazz);
	}

	public static final List<String> TXT_EXTS = List.of(
			"txt",
			"html",
			"xml",
			"json",
			"js"

	);

	public static final List<String> MIME_TEXT = List.of(
			"text/plain",
			"text/css",
			"text/csv",
			"text/html",
			"text/calendar",
			"text/javascript",
			"application/xhtml+xml",
			"application/xml",
			"text/xml"

	);

	public static final List<String> NOUNS = List.of(
			"ball", "bat", "bed", "book", "boy", "bun",
			"can", "cake", "cap", "car", "cat", "cow", "cub", "cup",
			"dad", "day", "dog", "doll", "dust",
			"fan", "feet", "girl", "gun",
			"hall", "hat", "hen",
			"jar",
			"kite",
			"man", "map", "men", "mom",
			"pan", "pet", "pie", "pig", "pot",
			"rat",
			"son", "sun",
			"toe", "tub",
			"van"

	);

	public static final String CONTENTTYPE_TEXT = "text/plain; charset=UTF-8";
	public static final String CONTENTTYPE_JSON = "application/json; charset=UTF-8";
	public static final String CONTENTTYPE_BINARY = "application/octet-stream";
	public static final String CONTENTTYPE_HTML = "text/html; charset=UTF-8";
	public static final String ENCODING_UTF8 = "UTF-8";
}
