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
package com.maxrunsoftware.jezel.service;

import static com.google.common.base.Preconditions.*;

import javax.inject.Inject;

import com.maxrunsoftware.jezel.BearerService;
import com.maxrunsoftware.jezel.ConfigurationService;
import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.SchedulerService;
import com.maxrunsoftware.jezel.SettingService;
import com.maxrunsoftware.jezel.WebService;
import com.maxrunsoftware.jezel.server.JettyServer;

public class WebServiceJetty implements WebService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebServiceJetty.class);

	private JettyServer server;

	private final SettingService settings;
	private final DatabaseService db;
	private final BearerService bearer;
	private final SchedulerService scheduler;
	private final ConfigurationService config;

	@Inject
	public WebServiceJetty(SettingService settings, DatabaseService db, BearerService bearer, SchedulerService scheduler, ConfigurationService config) {
		this.settings = checkNotNull(settings);
		this.db = checkNotNull(db);
		this.bearer = checkNotNull(bearer);
		this.scheduler = checkNotNull(scheduler);
		this.config = checkNotNull(config);
	}

	@Override
	public void start(boolean joinThread) throws Exception {
		LOG.debug("Starting");
		server = new JettyServer();
		server.setMaxThreads(settings.getRestMaxThreads());
		server.setMinThreads(settings.getRestMinThreads());
		server.setIdleTimeout(settings.getRestIdleTimeout());
		server.setPort(settings.getRestPort());
		server.setTempDirectory(settings.getDirTemp());

		server.addResource(SettingService.class.getName(), settings);
		server.addResource(DatabaseService.class.getName(), db);
		server.addResource(BearerService.class.getName(), bearer);
		server.addResource(SchedulerService.class.getName(), scheduler);
		server.addResource(ConfigurationService.class.getName(), config);

		for (var page : Constant.REST_SERVLETS) {
			server.addPage(page.servlet(), page.path());
		}

		server.start(joinThread);
	}

	@Override
	public void stop() throws Exception {
		server.stop();
	}
}
