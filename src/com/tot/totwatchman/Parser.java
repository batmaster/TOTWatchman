package com.tot.totwatchman;

/**
 * Temporarally used instead of json_encode() from php.
 * @author batmaster
 *
 */
public class Parser {
	
	private Parser() {
		
	}
	
	public static String parse(String result) {
//		result = result.replace(" ", "");
		result = result.replace("Array!!!(!!!    [", "{\"");
		result = result.replace("] => ", "\":\"");
		result = result.replace("!!!    [", "\",\"");
		result = result.replace("!!!)!!!", "\"},");
		result = result.replace("!!!", "");
		result = result.trim();
		try {
		if (result.charAt(result.length() - 1) == ',')
			result = result.substring(0, result.length() - 1);
		} catch (StringIndexOutOfBoundsException e) {}
		return "[" + result + "]";
	}
}
