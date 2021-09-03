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
package com.maxrunsoftware.jezel.util;

import static com.google.common.base.Preconditions.*;
import static com.maxrunsoftware.jezel.Util.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.collect.ImmutableList;

public class Table {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Table.class);

	private final ImmutableList<String> columns;

	public ImmutableList<String> getColumns() {
		return columns;
	}

	private final ImmutableList<ImmutableList<String>> rows;

	public ImmutableList<ImmutableList<String>> getRows() {
		return rows;
	}

	public Table(ImmutableList<String> columns, ImmutableList<ImmutableList<String>> rows) {
		this.columns = checkNotNull(columns);
		this.rows = checkNotNull(rows);

	}

	public static Table parse(ResultSet resultSet) throws SQLException {
		var meta = resultSet.getMetaData();
		var len = meta.getColumnCount();
		var columnsBuilder = ImmutableList.<String>builder();
		for (int i = 1; i <= len; i++) {
			columnsBuilder.add(coalesce(meta.getColumnLabel(i), meta.getColumnName(i), "Column" + i));
		}
		var columns = columnsBuilder.build();

		var rowsBuilder = ImmutableList.<ImmutableList<String>>builder();
		while (resultSet.next()) {
			var rowBuilder = ImmutableList.<String>builder();
			for (int i = 1; i <= len; i++) {
				var val = resultSet.getString(i);
				if (trimOrNull(val) == null) val = null;
				rowBuilder.add(val);
			}
			rowsBuilder.add(rowBuilder.build());
		}

		var rows = rowsBuilder.build();

		return new Table(columns, rows);
	}

	public static ImmutableList<Table> parse(PreparedStatement statement) throws SQLException {
		boolean isResultSet = statement.execute();

		var tablesBuilder = ImmutableList.<Table>builder();

		int count = 0;
		while (true) {
			if (isResultSet) {
				try (var resultSet = statement.getResultSet()) {
					var table = parse(resultSet);
					tablesBuilder.add(table);
				}

			} else {
				if (statement.getUpdateCount() == -1) { break; }

				LOG.debug("Result " + count + " is just a count: " + statement.getUpdateCount());
			}

			count++;
			isResultSet = statement.getMoreResults();
		}
		return tablesBuilder.build();
	}
}
