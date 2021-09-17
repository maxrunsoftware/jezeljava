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

import static com.maxrunsoftware.jezel.Util.*;

import java.time.LocalDateTime;

import org.hibernate.Session;

import com.maxrunsoftware.jezel.model.SchedulerAction;
import com.maxrunsoftware.jezel.model.SchedulerActionParameter;
import com.maxrunsoftware.jezel.model.SchedulerJob;
import com.maxrunsoftware.jezel.model.SchedulerSchedule;

public class RandomData {
	public static void populateDb(Session session) {
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

	public static void populateDb(DatabaseService db) {
		try (var session = db.openSession()) {
			populateDb(session);
		}
	}

	public static void populateDb() {
		populateDb(Constant.getInstance(DatabaseService.class));
	}
}
