package bma.m.wsapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import bma.m.wsapp.httpserver.Code;
import bma.m.wsapp.httpserver.HttpExchange;

public class HttpExchangeUtil {

	public static final String JSON_CONTENT_TYPE = "application/json";

	public static void reply(HttpExchange exchange, int code, String content)
			throws IOException {
		if (content == null)
			content = Code.msg(code);
		int len = content.length();
		if (len == 0) {
			len = -1;
		}
		exchange.sendResponseHeaders(code, len);
		if (len > 0) {
			OutputStream out = exchange.getResponseBody();
			try {
				out.write(content.getBytes());
			} finally {
				out.close();
			}
		}
	}

	public static void replyJson(HttpExchange exchange, int code, String json)
			throws IOException {
		exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
		reply(exchange, code, json);
	}

	public static void replyJson(HttpExchange exchange, String json)
			throws IOException {
		replyJson(exchange, 200, json);
	}

	public static void replyJson(HttpExchange exchange, Map<String, ?> json)
			throws IOException {
		JSONObject jo = new JSONObject(json);
		reply(exchange, 200, jo.toString());
	}

	public static void replyJson(HttpExchange exchange, List<?> json)
			throws IOException {
		JSONArray jo = new JSONArray(json);
		reply(exchange, 200, jo.toString());
	}

	public static int getContentLength(HttpExchange exchange) {
		String s = exchange.getRequestHeaders().getFirst("Content-Length");
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch (Exception e) {
			}
		}
		return 0;
	}

	public static String urldecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public static String urlencode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public static Map<String, String> parseQueryString(HttpExchange exchange) {
		URI uri = exchange.getRequestURI();
		String queryString = uri.getQuery();
		return parseQueryString(queryString);
	}

	public static Map<String, String> parseQueryString(String queryString) {
		if (queryString == null) {
			return Collections.emptyMap();
		}

		Map<String, String> result = new HashMap<String, String>();

		int parameterBegin = 0;
		int parameterSeparator = queryString.indexOf('=', parameterBegin);
		int parameterEnd;

		while (parameterBegin < queryString.length()) {
			parameterEnd = queryString.indexOf('&', parameterBegin);
			if (parameterEnd == -1) {
				parameterEnd = queryString.length();
			}
			if ((parameterSeparator > parameterEnd) || (parameterSeparator < 0)) {
				String key = urldecode(queryString.substring(parameterBegin,
						parameterEnd));
				result.put(key, "");
			} else {
				String key = urldecode(queryString.substring(parameterBegin,
						parameterSeparator));
				String value = urldecode(queryString.substring(
						parameterSeparator + 1, parameterEnd));
				result.put(key, value);
				parameterSeparator = queryString.indexOf('=', parameterEnd + 1);
			}
			parameterBegin = parameterEnd + 1;
		}
		return result;
	}

	public static Map<String, String> parsePostData(HttpExchange exchange)
			throws IOException {
		int contentLength = getContentLength(exchange);
		InputStream in = exchange.getRequestBody();
		byte[] buffer = new byte[contentLength];
		in.read(buffer, 0, buffer.length);
		in.close();
		return parseQueryString(new String(buffer, "UTF-8"));
	}
}
