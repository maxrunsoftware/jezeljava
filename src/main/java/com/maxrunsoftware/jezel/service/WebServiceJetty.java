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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.maxrunsoftware.jezel.BearerService;
import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.SettingService;
import com.maxrunsoftware.jezel.WebService;

public class WebServiceJetty implements WebService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebServiceJetty.class);

	private Server server;

	private final SettingService settings;
	private final DatabaseService db;
	private final BearerService bearer;

	@Inject
	public WebServiceJetty(SettingService settings, DatabaseService db, BearerService bearer) {
		this.settings = checkNotNull(settings);
		this.db = checkNotNull(db);
		this.bearer = checkNotNull(bearer);
	}

	@Override
	public void start(boolean joinThread) throws Exception {

		var maxThreads = settings.getWebMaxThreads();
		LOG.debug("maxThreads: " + maxThreads);

		var minThreads = settings.getWebMinThreads();
		LOG.debug("minThreads: " + minThreads);

		var idleTimeout = settings.getWebIdleTimeout();
		LOG.debug("idleTimeout: " + idleTimeout);

		var port = settings.getWebPort();
		LOG.debug("port: " + port);

		QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

		server = new Server(threadPool);

		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setSendServerVersion(false);
		HttpConnectionFactory httpFactory = new HttpConnectionFactory(httpConfig);
		ServerConnector connector = new ServerConnector(server, httpFactory);

		connector.setPort(port);

		server.setConnectors(new Connector[] { connector });
		server.setStopAtShutdown(true);
		server.setStopTimeout(5000);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase(settings.getTempDirectory());

		LOG.debug("Adding service [" + SettingService.class.getSimpleName() + "]: " + settings.getClass().getName());
		context.setAttribute(SettingService.class.getName(), settings);
		LOG.debug("Adding service [" + DatabaseService.class.getSimpleName() + "]: " + db.getClass().getName());
		context.setAttribute(DatabaseService.class.getName(), db);
		LOG.debug("Adding service [" + BearerService.class.getSimpleName() + "]: " + bearer.getClass().getName());
		context.setAttribute(BearerService.class.getName(), bearer);

		for (var page : Constant.PAGES) {
			context.addServlet(page.servlet(), page.path());
		}

		server.setHandler(context);

		// ServletHandler servletHandler = new ServletHandler();
		// server.setHandler(servletHandler);
		// servletHandler.addServletWithMapping(Servlet.class, "/*");

		LOG.info("Starting server on port " + port);
		server.start();

		if (joinThread) {
			LOG.debug("Joining <main> thread");
			server.join();
		}

	}

	@Override
	public void stop() throws Exception {
		LOG.info("Stopping server");
		server.stop();

	}
}
