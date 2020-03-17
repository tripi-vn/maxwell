package com.zendesk.maxwell.filtering;

import com.amazonaws.util.StringInputStream;
import com.zendesk.maxwell.util.ConfigParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.io.StreamTokenizer.*;

public class FilterParser {
	private StreamTokenizer tokenizer;
	private InputStreamReader inputStream;
	protected final String input;
	public FilterParser(String input) {
		this.input = input;
	}

	public List<FilterPattern> parse() throws InvalidFilterException {
		try {
			inputStream = new InputStreamReader(new StringInputStream(input));
		} catch ( UnsupportedEncodingException e ) {
			throw new InvalidFilterException(e.getMessage());
		}

		this.tokenizer = ConfigParser.initTokenizer(inputStream);

		try {
			return doParse();
		} catch ( IOException e ) {
			throw new InvalidFilterException(e.getMessage());
		}
	}

	private List<FilterPattern> doParse() throws IOException {
		ArrayList<FilterPattern> patterns = new ArrayList<>();

		tokenizer.nextToken();

		FilterPattern p;
		while ( (p = parseFilterPattern()) != null )
			patterns.add(p);

		return patterns;
	}

	private void skipToken(char token) throws IOException {
		if ( tokenizer.ttype != token ) {
			throw new IOException("Expected '" + token + "', saw: " + tokenizer.toString());
		}
		tokenizer.nextToken();
	}

	private FilterPattern parseFilterPattern() throws IOException {
		FilterPatternType type;
		FilterPattern ret;

		if ( tokenizer.ttype == TT_EOF )
			return null;

		if ( tokenizer.ttype != TT_WORD )
			throw new IOException("expected [include, exclude, blacklist] in filter definition.");

		switch(tokenizer.sval.toLowerCase()) {
			case "include":
				type = FilterPatternType.INCLUDE;
				break;
			case "exclude":
				type = FilterPatternType.EXCLUDE;
				break;
			case "blacklist":
				type = FilterPatternType.BLACKLIST;
				break;
			default:
				throw new IOException("Unknown filter keyword: " + tokenizer.sval);
		}
		tokenizer.nextToken();


		skipToken(':');
		Pattern dbPattern = ConfigParser.parsePattern(inputStream, tokenizer);
		skipToken('.');
		Pattern tablePattern = ConfigParser.parsePattern(inputStream, tokenizer);

		if ( tokenizer.ttype == '.' ) {
			// column-value filter
			tokenizer.nextToken();

			if ( tokenizer.ttype != TT_WORD )
				throw new IOException("expected column name, got" + tokenizer.nextToken());

			String columnName = tokenizer.sval;
			tokenizer.nextToken();

			skipToken('=');
			Pattern valuePattern = ConfigParser.parsePattern(inputStream, tokenizer);
			ret = new FilterColumnPattern(type, dbPattern, tablePattern, columnName, valuePattern);
		} else {
			ret = new FilterPattern(type, dbPattern, tablePattern);
		}

		if ( tokenizer.ttype == ',' ) {
			tokenizer.nextToken();
		}

		return ret;
	}


}
