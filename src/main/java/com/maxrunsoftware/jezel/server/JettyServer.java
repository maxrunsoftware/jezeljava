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
package com.maxrunsoftware.jezel.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import jakarta.servlet.Servlet;

public class JettyServer {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JettyServer.class);

	private Server server;

	private int maxThreads;
	private int minThreads;
	private int idleTimeout;
	private int port;
	private String tempDirectory;

	private static record Page(Class<? extends Servlet> servlet, String path) {}

	private final List<Page> pages = new ArrayList<Page>();

	public void addPage(Class<? extends Servlet> servlet, String path) {
		pages.add(new Page(servlet, path));
	}

	private static record Resource(String name, Object resource) {}

	private final List<Resource> resources = new ArrayList<Resource>();

	public void addResource(String name, Object resource) {
		resources.add(new Resource(name, resource));
	}

	public void start(boolean joinThread) throws Exception {

		var maxThreads = getMaxThreads();
		LOG.debug("maxThreads: " + maxThreads);

		var minThreads = getMinThreads();
		LOG.debug("minThreads: " + minThreads);

		var idleTimeout = getIdleTimeout();
		LOG.debug("idleTimeout: " + idleTimeout);

		var port = getPort();
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

		ServletContextHandler context;
		if (credentials.size() > 0) {
			context = new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
		} else {
			context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		}
		context.setContextPath("/");
		context.setResourceBase(getTempDirectory());

		for (var resource : resources) {
			LOG.debug("Adding Resource [" + resource.name + "]: " + resource.getClass().getName());
			context.setAttribute(resource.name, resource.resource);
		}

		for (var page : pages) {
			LOG.debug("Adding Page [" + page.path + "]: " + page.servlet.getClass().getName());
			context.addServlet(page.servlet(), page.path());
		}

		server.setHandler(context);

		// ServletHandler servletHandler = new ServletHandler();
		// server.setHandler(servletHandler);
		// servletHandler.addServletWithMapping(Servlet.class, "/*");

		if (credentials.size() > 0) {

			Constraint constraint = new Constraint();
			constraint.setName(Constraint.__FORM_AUTH);
			constraint.setRoles(new String[] { "user", "admin" });
			constraint.setAuthenticate(true);

			ConstraintMapping constraintMapping = new ConstraintMapping();
			constraintMapping.setConstraint(constraint);
			constraintMapping.setPathSpec("/*");

			var userStore = new UserStore();
			for (var username : credentials.keySet()) {
				var password = credentials.get(username);
				userStore.addUser(username, new Password(password), new String[] { "user" });
				LOG.debug("Adding credential [" + username + "]: " + password);
			}
			var loginService = new HashLoginService();
			loginService.setUserStore(userStore);

			ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
			securityHandler.addConstraintMapping(constraintMapping);
			securityHandler.setLoginService(loginService);

			FormAuthenticator authenticator = new FormAuthenticator("/login", "/login?error=true", false);
			securityHandler.setAuthenticator(authenticator);

			context.setSecurityHandler(securityHandler);
		}

		LOG.info("Starting server on port " + port);
		server.start();

		if (joinThread) {
			LOG.debug("Joining <main> thread");
			server.join();
		}

	}

	public void stop() throws Exception {
		LOG.info("Stopping server");
		server.stop();

	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public int getMinThreads() {
		return minThreads;
	}

	public void setMinThreads(int minThreads) {
		this.minThreads = minThreads;
	}

	public int getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	private final Map<String, String> credentials = new HashMap<String, String>();

	public void addCredential(String username, String password) {
		credentials.put(username, password);
	}
}
