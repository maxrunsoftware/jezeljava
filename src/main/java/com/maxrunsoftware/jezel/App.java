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

import static com.google.common.base.Preconditions.*;
import static com.maxrunsoftware.jezel.Util.*;

import java.time.LocalDateTime;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.maxrunsoftware.jezel.model.SchedulerAction;
import com.maxrunsoftware.jezel.model.SchedulerActionParameter;
import com.maxrunsoftware.jezel.model.SchedulerJob;
import com.maxrunsoftware.jezel.model.SchedulerSchedule;
import com.maxrunsoftware.jezel.service.SettingServiceEnvironment;
import com.maxrunsoftware.jezel.web.DataService;
import com.maxrunsoftware.jezel.web.RestClient;
import com.maxrunsoftware.jezel.web.WebServer;

public class App {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(App.class);
	private final WebService webServer;
	private final SettingService settings;
	private final SchedulerService scheduler;
	private final DatabaseService db;

	@Inject
	public App(WebService webServer, SettingService settings, SchedulerService scheduler, DatabaseService db) {
		this.webServer = checkNotNull(webServer);
		this.settings = checkNotNull(settings);
		this.scheduler = checkNotNull(scheduler);
		this.db = checkNotNull(db);

		var map = settings.toMap();
		for (var key : map.keySet()) {
			LOG.debug(key + ": " + map.get(key));
		}
	}

	public static void main(String[] args) {

		var settingService = new SettingService() {};
		LogSetup.initialize(settingService.getLoggingLevel(), settingService.getLoggingLevelLibs());
		LOG.info("Jezel Job Scheduling Engine  v" + Version.VALUE + "  dev@maxrunsoftware.com");

		String serverType = null;
		if (args == null || args.length == 0) serverType = "Not Specified";
		else if (args.length > 1) serverType = "Too Many Args Specified";
		else serverType = trimOrNull(args[0]);
		if (serverType == null) serverType = "Not Specified";

		if (serverType.equalsIgnoreCase("web")) {
			var settings = new SettingServiceEnvironment();
			var webServer = new WebServer(settings, new DataService(new RestClient(settings)));
			try {
				webServer.start(true);
			} catch (Exception e) {
				LOG.error("Error in Web server", e);
			}
		} else if (serverType.equalsIgnoreCase("rest")) {
			var module = new AbstractModule() {
				@SuppressWarnings("unchecked")
				@Override
				protected void configure() {
					for (var bnd : Constant.BINDS) {
						if (bnd.singleton()) {
							bind(bnd.classInterface()).to(bnd.classImplementation()).in(Singleton.class);
						} else {
							bind(bnd.classInterface()).to(bnd.classImplementation());
						}
					}
				}
			};
			Constant.setInjector(Guice.createInjector(module));

			var app = Constant.getInstance(App.class);

			app.run(args);

		} else {
			LOG.error("Valid server types are WEB or REST");
		}

	}

	private void run(String[] args) {
		try {
			populate();
			var webjoinThread = settings.getRestJoinThread();

			scheduler.start(webjoinThread);
			scheduler.syncAll();

			webServer.start(webjoinThread);

			if (!webjoinThread) {
				while (true) {
					System.out.println("Type 'q', 'quit', 'exit' to exit");
					var input = Util.trimOrNull(Util.readLine());
					if (Util.equalsAnyIgnoreCase(input, "q", "quit", "exit")) break;
				}
			}
			webServer.stop();
			scheduler.stop();
		} catch (Exception e) {
			LOG.error("Error in REST server", e);
		}

	}

	private void populate() {
		var db = Constant.getInstance(DatabaseService.class);
		try (var session = db.openSession()) {

			for (int i = 0; i < randomInt(8, 10); i++) {
				var j = new SchedulerJob();
				j.setName(randomPick(Constant.NOUNS));
				j.setGroup(randomPick("group1", "group2", "group3"));
				j.setDisabled(randomBoolean());
				j = getById(SchedulerJob.class, session, save(session, j));

				for (int ii = 0; ii < randomInt(3, 5); ii++) {
					var s = new SchedulerSchedule();
					s.setDays(true, randomBoolean(), randomBoolean(), randomBoolean(), randomBoolean(), randomBoolean(), randomBoolean());
					s.setTime(randomInt(0, 23), randomInt(0, 59));
					s.setDisabled(randomBoolean());
					s.setSchedulerJob(j);
					save(session, s);
				}
				for (int ii = 0; ii < 3; ii++) {
					var s = new SchedulerSchedule();
					s.setDays(true, true, true, true, true, true, true);
					s.setTime(LocalDateTime.now().getHour(), LocalDateTime.now().getMinute() + ii);
					s.setDisabled(false);
					s.setSchedulerJob(j);
					save(session, s);
				}

				for (int ii = 0; ii < randomInt(3, 5); ii++) {
					var a = new SchedulerAction();
					a.setName("SqlQuery");
					a.setDescription(randomPick(Constant.NOUNS));
					a.setDisabled(randomBoolean());
					a.setSchedulerJob(j);
					a.setIndex(ii);
					a = getById(SchedulerAction.class, session, save(session, a));

					for (int iii = 0; iii < randomInt(5, 8); iii++) {
						var ap = new SchedulerActionParameter();
						ap.setName(randomPick(Constant.NOUNS));
						ap.setValue(randomPick(Constant.NOUNS));
						ap.setSchedulerAction(a);
						save(session, a);
					}

				}

			}

		}
	}

}
