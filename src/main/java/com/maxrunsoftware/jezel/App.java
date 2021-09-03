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

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;

public class App {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(App.class);
	private final WebService webServer;
	private final SettingService settings;
	private final SchedulerService scheduler;

	@Inject
	public App(WebService webServer, SettingService settings, SchedulerService scheduler) {
		this.webServer = checkNotNull(webServer);
		this.settings = checkNotNull(settings);
		this.scheduler = checkNotNull(scheduler);

		var map = settings.toMap();
		for (var key : map.keySet()) {
			LOG.debug(key + ": " + map.get(key));
		}
	}

	public static void main(String[] args) {
		var settingService = new SettingService() {};
		LogSetup.initialize(settingService.getLoggingLevel(), settingService.getLoggingLevelLibs());
		LOG.info("Jezel Job Scheduling Engine  v" + Version.VALUE + "  dev@maxrunsoftware.com");

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
	}

	private void run(String[] args) {
		try {
			var webjoinThread = settings.getWebJoinThread();

			scheduler.start(webjoinThread);
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
			LOG.error("Error in web server", e);
		}

	}

}
