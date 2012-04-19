package bma.m.wsapp.util;

import java.io.IOException;
import java.io.OutputStream;

import bma.m.wsapp.httpserver.HttpExchange;

public class HttpExchangeUtil {

	public static void reply(HttpExchange exchange, int code, String content)
			throws IOException {
		int len = content == null ? 0 : content.length();
		if (len == 0)
			len = -1;
		exchange.sendResponseHeaders(code, len);
		OutputStream out = exchange.getResponseBody();
		try {
			out.write(content.getBytes());
		} finally {
			out.close();
		}
	}
}
