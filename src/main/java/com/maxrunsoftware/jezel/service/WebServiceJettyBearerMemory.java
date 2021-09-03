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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.maxrunsoftware.jezel.BearerService;
import com.maxrunsoftware.jezel.Util;

public class WebServiceJettyBearerMemory implements BearerService {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebServiceJettyBearerMemory.class);

	private final Map<String, LocalDateTime> map = new CaseInsensitiveMap<String, LocalDateTime>();
	private final Object locker = new Object();

	@Override
	public void addBearer(String bearer) {
		synchronized (locker) {
			bearer = Util.trimOrNull(bearer);
			if (bearer == null) return;
			map.remove(bearer);
			var now = LocalDateTime.now();
			LOG.debug("Adding Bearer token " + bearer);
			map.put(bearer, now);
		}
	}

	@Override
	public boolean authBearer(String bearer) {
		synchronized (locker) {
			bearer = Util.trimOrNull(bearer);
			if (bearer == null) {
				LOG.debug("Bearer auth failed, NULL bearer token");
				return false;
			}
			var dt = map.get(bearer);
			if (dt == null) {
				LOG.debug("Bearer auth failed for token " + bearer + " because token does not exist");
				return false;
			}
			var now = LocalDateTime.now();
			var distance = dt.until(now, ChronoUnit.MILLIS);
			if (distance > sessionTime) {
				map.remove(bearer);
				LOG.debug("Bearer auth failed for token " + bearer + " because token is expired");
				return false;
			}

			map.put(bearer, now); // renew session
			LOG.debug("Bearer auth success for token " + bearer);
			return true;
		}
	}

	private int sessionTime = 1000 * 60 * 5; // 5 minutes

	@Override
	public int getSessionTime() {
		synchronized (locker) {
			return sessionTime;
		}
	}

	@Override
	public void setSessionTime(int milliseconds) {
		if (milliseconds < 1) milliseconds = Integer.MAX_VALUE;
		synchronized (locker) {
			sessionTime = milliseconds;
		}
	}
}
