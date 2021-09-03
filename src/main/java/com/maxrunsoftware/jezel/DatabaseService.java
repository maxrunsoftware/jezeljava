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

import java.io.Closeable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public interface DatabaseService extends Closeable {
	public Session openSession();

	public static class Impl implements DatabaseService {
		private StandardServiceRegistry registry;
		private SessionFactory sessionFactory;

		public SessionFactory getSessionFactory() {
			if (sessionFactory == null) {
				try {
					// Create registry
					registry = new StandardServiceRegistryBuilder().configure().build();

					var sources = new MetadataSources(registry);

					var metadata = sources.getMetadataBuilder().build();

					sessionFactory = metadata.getSessionFactoryBuilder().build();

				} catch (Exception e) {
					e.printStackTrace();
					if (registry != null) { StandardServiceRegistryBuilder.destroy(registry); }
				}
			}
			return sessionFactory;
		}

		@Override
		public Session openSession() {
			return getSessionFactory().openSession();
		}

		@Override
		public void close() {
			if (registry != null) { StandardServiceRegistryBuilder.destroy(registry); }
		}

	}
}
