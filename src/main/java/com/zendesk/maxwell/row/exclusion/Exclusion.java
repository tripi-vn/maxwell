package com.zendesk.maxwell.row.exclusion;

import com.zendesk.maxwell.filtering.InvalidFilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Exclusion {
	static final Logger LOGGER = LoggerFactory.getLogger(Exclusion.class);
	private final List<ExclusionPattern> patterns;
	public Exclusion(String excludeString) throws InvalidFilterException {
		this.patterns = new ArrayList<>();
		patterns.addAll(new ExclusionParser(excludeString).parse());
	}

	public boolean includes(String database, String table, String column) {
		for ( ExclusionPattern p : patterns ) {
			if( p.match(database, table, column) ) {
				return true;
			}
		}

		return false;
	}
}
