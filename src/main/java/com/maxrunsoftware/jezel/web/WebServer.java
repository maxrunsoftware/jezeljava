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

import static com.google.common.base.Preconditions.*;

import javax.inject.Inject;

import org.eclipse.jetty.server.Server;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.SettingService;
import com.maxrunsoftware.jezel.WebService;
import com.maxrunsoftware.jezel.server.JettyServer;
import com.maxrunsoftware.jezel.service.WebServiceJetty;

public class WebServer implements WebService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebServiceJetty.class);

	private Server server;

	private final SettingService settings;

	@Inject
	public WebServer(SettingService settings) {
		this.settings = checkNotNull(settings);
	}

	@Override
	public void start(boolean joinThread) throws Exception {
		LOG.debug("Starting");
		var server = new JettyServer();
		server.setMaxThreads(settings.getRestMaxThreads());
		server.setMinThreads(settings.getRestMinThreads());
		server.setIdleTimeout(settings.getRestIdleTimeout());
		server.setPort(settings.getRestPort());
		server.setTempDirectory(settings.getDirTemp());
		server.addResource(SettingService.class.getName(), settings);

		for (var page : Constant.WEB_SERVLETS) {
			server.addPage(page.servlet(), page.path());
		}

		server.addCredential("user", "pass");

		server.start(joinThread);
	}

	@Override
	public void stop() throws Exception {
		server.stop();
	}
}
