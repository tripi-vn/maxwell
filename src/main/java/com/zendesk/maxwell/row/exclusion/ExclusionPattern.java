package com.zendesk.maxwell.row.exclusion;

import java.util.regex.Pattern;

public class ExclusionPattern {
	private final Pattern dbPattern, tablePattern, columnPattern;

	public ExclusionPattern(Pattern columnPattern) {
		this.dbPattern = null;
		this.tablePattern = null;
		this.columnPattern = columnPattern;
	}

	public ExclusionPattern(Pattern tablePattern, Pattern columnPattern) {
		this.dbPattern = null;
		this.tablePattern = tablePattern;
		this.columnPattern = columnPattern;
	}

	public ExclusionPattern(Pattern dbPattern, Pattern tablePattern, Pattern columnPattern) {
		this.dbPattern = dbPattern;
		this.tablePattern = tablePattern;
		this.columnPattern = columnPattern;
	}

	protected boolean appliesTo(String database, String table, String column) {
		return (dbPattern == null || dbPattern.matcher(database).find())
				&& (tablePattern == null || tablePattern.matcher(table).find())
				&& columnPattern.matcher(column).find();
	}

	public boolean match(String database, String table, String column) {
		return appliesTo(database, table, column);
	}
}
