package com.zendesk.maxwell.row.exclusion;

import com.amazonaws.util.StringInputStream;
import com.zendesk.maxwell.filtering.InvalidFilterException;
import com.zendesk.maxwell.util.ConfigParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.io.StreamTokenizer.TT_EOF;

public class ExclusionParser {
	private StreamTokenizer tokenizer;
	private InputStreamReader inputStream;
	protected final String input;
	ExclusionParser(String input) {
		this.input = input;
	}

	public List<ExclusionPattern> parse() throws InvalidFilterException {
		try {
			inputStream = new InputStreamReader(new StringInputStream(input));
		} catch ( UnsupportedEncodingException e ) {
			throw new InvalidFilterException(e.getMessage());
		}

		tokenizer =  ConfigParser.initTokenizer(inputStream);

		try {
			return doParse();
		} catch ( IOException e ) {
			throw new InvalidFilterException(e.getMessage());
		}
	}

	private void skipToken(char token) throws IOException {
		if ( tokenizer.ttype != token ) {
			throw new IOException("Expected '" + token + "', saw: " + tokenizer.toString());
		}
		tokenizer.nextToken();
	}

	private List<ExclusionPattern> doParse() throws IOException {
		ArrayList<ExclusionPattern> patterns = new ArrayList<>();

		tokenizer.nextToken();

		ExclusionPattern p;
		while ( (p = parseFilterPattern()) != null )
			patterns.add(p);

		return patterns;
	}

	private ExclusionPattern parseFilterPattern() throws IOException {
		ExclusionPattern ret;

		if ( tokenizer.ttype == TT_EOF )
			return null;

		Pattern p1 = ConfigParser.parsePattern(inputStream, tokenizer);
		if ( tokenizer.ttype == TT_EOF ) {
			ret = new ExclusionPattern(p1);
		} else {
			skipToken('.');
			Pattern p2 = ConfigParser.parsePattern(inputStream, tokenizer);
			if ( tokenizer.ttype == TT_EOF ) {
				ret = new ExclusionPattern(p1, p2);
			} else {
				skipToken('.');
				Pattern columnPattern = ConfigParser.parsePattern(inputStream, tokenizer);
				ret = new ExclusionPattern(p1, p2, columnPattern);
			}
		}

		if ( tokenizer.ttype == ',' ) {
			tokenizer.nextToken();
		}

		return ret;
	}

}
