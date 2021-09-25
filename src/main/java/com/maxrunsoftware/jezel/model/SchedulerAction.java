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
package com.maxrunsoftware.jezel.model;

import static com.maxrunsoftware.jezel.Util.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;

import com.maxrunsoftware.jezel.Constant;
import com.maxrunsoftware.jezel.DatabaseService;
import com.maxrunsoftware.jezel.JsonCodable;
import com.maxrunsoftware.jezel.Util;
import com.maxrunsoftware.jezel.action.CommandParameter;

@Entity
public class SchedulerAction implements JsonCodable {
	public static final String NAME = "schedulerAction";
	public static final String ID = NAME + "Id";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerAction.class);

	public static final Comparator<SchedulerAction> SORT_INDEX = new Comparator<SchedulerAction>() {
		@Override
		public int compare(SchedulerAction o1, SchedulerAction o2) {
			if (o1 == o2) return 0;
			if (o1 == null) return -1;
			if (o2 == null) return 1;
			var c = compareTo(o1.getIndex(), o2.getIndex());
			if (c != 0) return c;
			c = compareTo(o1.getSchedulerActionId(), o2.getSchedulerActionId());
			if (c != 0) return c;

			return 0;
		}
	};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int schedulerActionId;

	public int getSchedulerActionId() {
		return schedulerActionId;
	}

	public void setSchedulerActionId(int schedulerActionId) {
		this.schedulerActionId = schedulerActionId;
	}

	@ManyToOne
	@JoinColumn(name = SchedulerJob.ID, nullable = false, referencedColumnName = SchedulerJob.ID)
	private SchedulerJob schedulerJob;

	public SchedulerJob getSchedulerJob() {
		return schedulerJob;
	}

	public void setSchedulerJob(SchedulerJob schedulerJob) {
		this.schedulerJob = schedulerJob;
	}

	@OneToMany(mappedBy = NAME)
	private Set<SchedulerActionParameter> schedulerActionParameters;

	public Set<SchedulerActionParameter> getSchedulerActionParameters() {
		if (schedulerActionParameters == null) schedulerActionParameters = new HashSet<SchedulerActionParameter>();
		return schedulerActionParameters;
	}

	public void setSchedulerActionParameters(Set<SchedulerActionParameter> schedulerActionParameters) {
		this.schedulerActionParameters = schedulerActionParameters;
	}

	@OneToMany(mappedBy = NAME)
	private Set<CommandLogAction> commandLogActions;

	public Set<CommandLogAction> getCommandLogActions() {
		if (commandLogActions == null) commandLogActions = new HashSet<CommandLogAction>();
		return commandLogActions;
	}

	public void setCommandLogActions(Set<CommandLogAction> commandLogActions) {
		this.commandLogActions = commandLogActions;
	}

	@Column(length = 200, nullable = false, unique = false)
	private String name;

	public String getName() {
		return trimOrNull(name);
	}

	public void setName(String name) {
		this.name = trimOrNull(name);
	}

	@Column(length = 1000, nullable = true, unique = false)
	private String description;

	public String getDescription() {
		return trimOrNull(description);
	}

	public void setDescription(String description) {
		this.description = trimOrNull(description);
	}

	@Column(nullable = false)
	private boolean disabled;

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Column(nullable = false)
	private int index;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public JsonObject toJson() {
		var json = createObjectBuilder();
		json.add(ID, getSchedulerActionId());
		json.add(SchedulerJob.ID, getSchedulerJob().getSchedulerJobId());
		json.add("name", coalesce(getName(), ""));
		json.add("description", coalesce(getDescription(), ""));
		json.add("disabled", isDisabled());
		json.add("index", getIndex());
		var arrayBuilder = createArrayBuilder();
		for (var schedulerActionParameter : getSchedulerActionParameters()) {
			arrayBuilder.add(schedulerActionParameter.toJson());
		}
		json.add("schedulerActionParameters", arrayBuilder);

		return json.build();
	}

	@Override
	public void fromJson(JsonObject o) {
		this.setSchedulerActionId(o.getInt(ID));
		this.setName(o.getString("name"));
		this.setDescription(o.getString("description"));
		this.setDisabled(o.getBoolean("disabled"));
		this.setIndex(getIndex());
		var array = o.getJsonArray("schedulerActionParameters");
		var h = new HashSet<SchedulerActionParameter>();
		for (var item : array) {
			var p = new SchedulerActionParameter();
			p.fromJson(item.asJsonObject());
			p.setSchedulerAction(this);
			h.add(p);
		}
		this.setSchedulerActionParameters(h);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getSchedulerActionId() + "]";
	}

	public static List<SchedulerAction> getBySchedulerJobId(Session session, int schedulerJobId) {
		var list = new ArrayList<SchedulerAction>();
		for (var o : Util.getAll(SchedulerAction.class, session)) {
			if (Integer.valueOf(schedulerJobId).equals(o.getSchedulerJob().getSchedulerJobId())) list.add(o);
		}
		return list;
	}

	public static List<String> getSchedulerActionNames() {
		var list = new ArrayList<String>();
		for (var clazz : Constant.COMMANDS) {
			list.add(clazz.getSimpleName());
		}
		return list;
	}

	public static boolean isValidSchedulerActionName(String name) {
		name = trimOrNull(name);
		if (name == null) return false;
		for (var n : getSchedulerActionNames()) {
			if (name.equalsIgnoreCase(n)) return true;
		}
		return false;
	}

	public SchedulerActionParameter getSchedulerActionParameter(String name) {
		for (var p : getSchedulerActionParameters()) {
			if (p.getName().equalsIgnoreCase(name)) return p;
		}
		return null;
	}

	public void syncParametersToCommand(Session session) {
		var namesPar = new HashSet<String>();
		var namesCmd = new HashSet<String>();

		var namesToRemove = new HashSet<String>();
		var namesToAdd = new HashSet<String>();

		for (var par : getSchedulerActionParameters()) {
			namesPar.add(par.getName());
		}

		for (var cmd : CommandParameter.getForCommand(getName())) {
			namesCmd.add(cmd.getName());
		}

		for (var namePar : namesPar) {
			if (!namesCmd.contains(namePar)) { namesToRemove.add(namePar); }
		}

		for (var nameCmd : namesCmd) {
			if (!namesPar.contains(nameCmd)) { namesToAdd.add(nameCmd); }
		}

		for (var nameToRemove : namesToRemove) {
			LOG.debug("Removing parameter [" + nameToRemove + "] from SchedulerAction[" + getSchedulerActionId() + "]");
			var p = getSchedulerActionParameter(nameToRemove);
			delete(session, p);
		}

		for (var nameToAdd : namesToAdd) {
			LOG.debug("Adding parameter [" + nameToAdd + "] to SchedulerAction[" + getSchedulerActionId() + "]");

			var p = new SchedulerActionParameter();
			p.setName(nameToAdd);
			p.setSchedulerAction(this);
			save(session, p);
		}

	}

	public boolean setSchedulerActionParameter(Session session, String name, String value) {
		var p = getSchedulerActionParameter(name);
		if (p == null) return false;
		p.setValue(value);
		save(session, p);
		return true;
	}

	public static void syncAllParametersToCommand(Session session) {
		var schedulerActions = getAll(SchedulerAction.class, session);
		for (var schedulerAction : schedulerActions) {
			schedulerAction.syncParametersToCommand(session);
		}
	}

	public static void syncAllParametersToCommand() {
		try (var session = Constant.getInstance(DatabaseService.class).openSession()) {
			syncAllParametersToCommand(session);
		}
	}

	public static int create(Session session, SchedulerJob schedulerJob, String name) {
		// Create
		var schedulerAction = new SchedulerAction();
		schedulerAction.setDisabled(false);
		schedulerAction.setSchedulerJob(schedulerJob);
		schedulerAction.setIndex(Integer.MAX_VALUE);
		schedulerAction.setName(name);
		var schedulerActionId = save(session, schedulerAction);

		// Reindex
		schedulerJob = getById(SchedulerJob.class, session, schedulerJob.getSchedulerJobId());
		schedulerJob.reindexSchedulerActions(session);

		// Create Parameters
		schedulerAction = getById(SchedulerAction.class, session, schedulerActionId);
		schedulerAction.syncParametersToCommand(session);

		return schedulerActionId;
	}

}
