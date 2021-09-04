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

import javax.inject.Inject;

import org.h2.Driver;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.H2Dialect;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.SettingService;

public class DatabaseServiceH2 implements DatabaseService {
	private final SessionFactory sessionFactory;

	@Inject
	public DatabaseServiceH2(SettingService settings) {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			throw new Error(e); // should not happen
		}

		var directory = settings.getDatabaseDir();

		String cs;
		if (directory == null || directory.equalsIgnoreCase("mem") || directory.equalsIgnoreCase("memory")) {
			cs = "jdbc:h2:mem:test";
		} else {
			// if (!directory.endsWith("/")) directory = directory + "/";
			// cs = "jdbc:h2:file:" + directory + ";USER=sa;PASSWORD=password";
			cs = "jdbc:h2:file:" + directory;
		}

		// org.h2.jdbcx.JdbcConnectionPool connectionPool =
		// JdbcConnectionPool.create(cs, "sa", "");

		org.hibernate.cfg.Configuration configuration = new Configuration();
		configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		configuration.setProperty("hibernate.connection.driver_class", Driver.class.getName());
		configuration.setProperty("hibernate.connection.url", cs);
		configuration.setProperty("hibernate.connection.username", "sa");
		configuration.setProperty("hibernate.connection.password", "");
		configuration.setProperty("hibernate.dialect", H2Dialect.class.getName());

		configuration.setProperty("connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
		configuration.setProperty("hibernate.c3p0.acquire_increment", "1");
		configuration.setProperty("hibernate.c3p0.idle_test_period", "60");
		configuration.setProperty("hibernate.c3p0.min_size", "1");
		configuration.setProperty("hibernate.c3p0.max_size", "2");
		configuration.setProperty("hibernate.c3p0.max_statements", "50");
		configuration.setProperty("hibernate.c3p0.timeout", "0");
		configuration.setProperty("hibernate.c3p0.acquireRetryAttempts", "1");
		configuration.setProperty("hibernate.c3p0.acquireRetryDelay", "250");

		configuration.setProperty("hibernate.show_sql", "true");
		configuration.setProperty("hibernate.use_sql_comments", "true");

		// configuration.setProperty("hibernate.transaction.factory_class",
		// "org.hibernate.transaction.JDBCTransactionFactory");
		configuration.setProperty("hibernate.transaction.coordinator_class", "org.hibernate.transaction.JDBCTransactionFactory");

		configuration.setProperty("hibernate.current_session_context_class", "thread");

		for (var clazz : Constant.JPA) {
			configuration.addAnnotatedClass(clazz);
		}

		sessionFactory = configuration.buildSessionFactory();

	}

	@Override
	public Session openSession() {
		return sessionFactory.openSession();
	}

	@Override
	public void close() {
		sessionFactory.close();
	}

}
