package com.zendesk.maxwell.util;

import java.io.*;
import java.util.regex.Pattern;

import static java.io.StreamTokenizer.TT_WORD;

public class ConfigParser {

	public static StreamTokenizer initTokenizer(InputStreamReader inputStream) {
		StreamTokenizer tokenizer = new StreamTokenizer(inputStream);
		tokenizer.ordinaryChar('.');
		tokenizer.ordinaryChar('/');
		tokenizer.wordChars('_', '_');

		tokenizer.ordinaryChars('0', '9');
		tokenizer.wordChars('0', '9');

		tokenizer.quoteChar('`');
		tokenizer.quoteChar('\'');
		tokenizer.quoteChar('"');
		return tokenizer;
	}

	public static Pattern parsePattern(InputStreamReader inputStream, StreamTokenizer tokenizer) throws IOException {
		Pattern pattern;
		switch ( tokenizer.ttype ) {
			case '/':
				pattern = Pattern.compile(parseRegexp(inputStream));
				break;
			case '*':
				pattern = Pattern.compile("");
				break;
			case TT_WORD:
			case '`':
			case '\'':
			case '"':
				pattern = Pattern.compile("^" + tokenizer.sval + "$");
				break;
			default:
				throw new IOException("Expected string or regexp, saw '" + Character.toString((char) tokenizer.ttype));
		}
		tokenizer.nextToken();
		return pattern;
	}

	private static String parseRegexp(InputStreamReader inputStream, StreamTokenizer tokenizer) throws IOException {
		char ch, lastChar = 0;
		String s = "";
		while ( true ) {
			ch = (char) inputStream.read();
			if ( (ch == '.' || ch == '$') && lastChar != '\\' )
				break;

			s += ch;
			lastChar = ch;
		}
		return s;
	}

	private static String parseRegexp(InputStreamReader inputStream) throws IOException {
		char ch, lastChar = 0;
		String s = "";
		while ( true ) {
			ch = (char) inputStream.read();
			if ( ch == '/' && lastChar != '\\' )
				break;

			s += ch;
			lastChar = ch;
		}
		return s;
	}
}
