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

import static com.maxrunsoftware.jezel.Util.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Table.class);

	private final List<String> columns;

	public List<String> getColumns() {
		return columns;
	}

	private final List<List<String>> rows;

	public List<List<String>> getRows() {
		return rows;
	}

	public <T extends List<String>> Table(List<String> columns, List<T> rows) {
		int maxRowLength = columns.size();
		for (var row : rows) {
			maxRowLength = Math.max(maxRowLength, row.size());
		}

		var newColumns = new ArrayList<String>();
		for (int i = 0; i < maxRowLength; i++) {
			if (i < columns.size()) {
				newColumns.add(columns.get(i));
			} else {
				newColumns.add("Column" + (i + 1));
			}
		}
		this.columns = Collections.unmodifiableList(newColumns);

		var newRows = new ArrayList<List<String>>();
		for (var row : rows) {
			var newRow = new ArrayList<String>();
			for (int i = 0; i < maxRowLength; i++) {
				if (i < row.size()) {
					newRow.add(row.get(i));
				} else {
					String s = null;
					newRow.add(s);
				}
			}
			newRows.add(Collections.unmodifiableList(newRow));
		}
		this.rows = Collections.unmodifiableList(newRows);
	}

	public static <T extends Iterable<? extends Object>> Table parse(Iterable<String> columns, Iterable<T> rows) {
		var cols = new ArrayList<String>();
		for (var c : columns) {
			cols.add(c);
		}

		var rs = new ArrayList<ArrayList<String>>();
		for (var row : rows) {
			var list = new ArrayList<String>();
			for (var cell : row) {
				list.add(cell == null ? null : cell.toString());
			}
			rs.add(list);
		}
		return new Table(cols, rs);
	}

	public static Table parse(ResultSet resultSet) throws SQLException {
		var meta = resultSet.getMetaData();
		var len = meta.getColumnCount();
		var columns = new ArrayList<String>();
		for (int i = 1; i <= len; i++) {
			columns.add(coalesce(meta.getColumnLabel(i), meta.getColumnName(i), "Column" + i));
		}

		var rows = new ArrayList<List<String>>();
		while (resultSet.next()) {
			var row = new ArrayList<String>();
			for (int i = 1; i <= len; i++) {
				var val = resultSet.getString(i);
				if (trimOrNull(val) == null) val = null;
				row.add(val);
			}
			rows.add(row);
		}

		return new Table(columns, rows);
	}

	public static List<Table> parse(PreparedStatement statement) throws SQLException {
		boolean isResultSet = statement.execute();

		var tables = new ArrayList<Table>();

		int count = 0;
		while (true) {
			if (isResultSet) {
				try (var resultSet = statement.getResultSet()) {
					var table = parse(resultSet);
					tables.add(table);
				}

			} else {
				if (statement.getUpdateCount() == -1) { break; }

				LOG.debug("Result " + count + " is just a count: " + statement.getUpdateCount());
			}

			count++;
			isResultSet = statement.getMoreResults();
		}
		return tables;
	}

	public String toHtml() {
		return toHtml(new HtmlFormatter());
	}

	public String toHtml(HtmlFormatter formatter) {
		var sb = new StringBuilder();
		sb.append("<table>");
		formatter.colgroup(sb, columns);
		sb.append("<thead>");
		sb.append("<tr>");
		int colIndex = 0;
		for (var col : getColumns()) {
			formatter.th(sb, colIndex, col);
			colIndex++;
		}
		sb.append("</tr>");
		sb.append("</thead>");
		sb.append("<tbody>");
		var rowIndex = 0;
		for (var row : getRows()) {
			sb.append("<tr>");
			colIndex = 0;
			for (var cell : row) {
				formatter.td(sb, rowIndex, colIndex, cell);
				colIndex++;
			}
			sb.append("</tr>");
			rowIndex++;
		}
		sb.append("</tbody>");
		sb.append("</table>");
		return sb.toString();
	}

	public static class HtmlFormatter {
		public void th(StringBuilder sb, int columnIndex, String columnName) {
			sb.append("<th>" + coalesce(columnName, "") + "</th>");
		}

		public void td(StringBuilder sb, int rowIndex, int columnIndex, String content) {
			sb.append("<td>" + coalesce(content, "") + "</td>");
		}

		public void colgroup(StringBuilder sb, List<String> columns) {

		}
	}
}
