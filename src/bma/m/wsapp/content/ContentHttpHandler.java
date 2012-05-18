package bma.m.wsapp.content;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;
import bma.m.wsapp.httpserver.Headers;
import bma.m.wsapp.httpserver.HttpExchange;
import bma.m.wsapp.httpserver.HttpHandler;
import bma.m.wsapp.util.HttpExchangeUtil;

public class ContentHttpHandler implements HttpHandler {

	private static final String HTTP_HEADER_IF_MODIFIED = "If-Modified-Since:";

	// Date Format pattern for HTTP headers
	private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

	private static final String TAG = "ContentHandler";

	private ContentFileProvider provider;
	private String page404 = "404.html";
	private HttpHandler rootHandler;

	public ContentHttpHandler(ContentFileProvider p) {
		super();
		this.provider = p;
	}

	public HttpHandler getRootHandler() {
		return rootHandler;
	}

	public void setRootHandler(HttpHandler rootHandler) {
		this.rootHandler = rootHandler;
	}

	public String getPage404() {
		return page404;
	}

	public void setPage404(String page404) {
		this.page404 = page404;
	}

	/**
	 * Function to process the request
	 * 
	 * @throws Exception
	 */
	protected void processRequest(HttpExchange exchange) throws IOException {

		Headers headers = exchange.getRequestHeaders();
		Headers rheaders = exchange.getResponseHeaders();

		try {
			String httpCommand = exchange.getRequestMethod();
			String fileName = exchange.getRequestURI().getPath();

			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "content: " + httpCommand + ", " + fileName);
			}

			// Perform a GET or HEAD-Request
			if (httpCommand.equalsIgnoreCase("GET")
					|| httpCommand.equalsIgnoreCase("HEAD")) {
				ContentFile file = provider.getContent(fileName, exchange);

				boolean fileExists = true;

				if (file == null || !file.exists()) {
					fileExists = false;
				}

				SimpleDateFormat df = new SimpleDateFormat(HTTP_DATE_FORMAT);

				rheaders.add("Date", df.format(System.currentTimeMillis()));

				if (!fileExists) {
					Log.w(TAG, "404: " + fileName);
				}

				if (file != null) {
					Date ld = file.lastModified();
					if (ld != null) {
						rheaders.add("Last-Modified",
								df.format(file.lastModified()));
						if (headers.containsKey(HTTP_HEADER_IF_MODIFIED)) {
							try {
								long ifModifiedSince = df
										.parse(headers
												.getFirst(HTTP_HEADER_IF_MODIFIED))
										.getTime();
								if (ifModifiedSince >= ld.getTime()) {
									HttpExchangeUtil.reply(exchange, 304,
											"Not Modified");
									return;
								}
							} catch (ParseException e) {
								// if-modified-since-header has a defective
								// value.
								// we continue as the header is not present.
							}
						}
					}
				}

				if (file == null) {
					HttpExchangeUtil.reply(exchange, 404, "File not found");
					return;
				}

				rheaders.add("Content-type", file.getContentType());
				if (fileExists) {
					long ct = file.getCacheTime();
					if (ct > 0) {
						rheaders.add("Expires",
								df.format(System.currentTimeMillis() + ct));
					}
				}

				int len = 0;
				len = file.getContentLength();
				if (httpCommand.equalsIgnoreCase("HEAD")) {
					if (len > 0) {
						rheaders.add("Content-Length", Integer.toString(len));
						exchange.sendResponseHeaders(200, -1);
						return;
					}
				}

				exchange.sendResponseHeaders(200, len);
				OutputStream out = exchange.getResponseBody();
				try {
					file.writeTo(out);
				} finally {
					out.close();
				}
				return;

			} else if (httpCommand.equalsIgnoreCase("POST")) {
				Log.d(TAG, "POST method not allowd");
				rheaders.add("Allow", "HEAD, GET");

				String error = "Method Not Allowed";
				exchange.sendResponseHeaders(405, error.length());
				OutputStream out = exchange.getResponseBody();
				try {
					out.write(error.getBytes());
				} finally {
					out.close();
				}
				return;

			} else {
				// HTTP 1.0 only defines HEAD, GET, POST.
				Log.d(TAG, "bad request: " + httpCommand);
				HttpExchangeUtil.reply(exchange, 400, "Bad Request");
				return;
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			// Something happened while creating the response.
			// Ok, now HTTP 500 is the right way to inform the client.
			Log.w(TAG, "Internal Server error", e);

			rheaders.add("Content-type", "text/html");
			HttpExchangeUtil.reply(exchange, 500, "Internal Server Error");
		}
	}

	public void handle(HttpExchange exchange) throws IOException {
		processRequest(exchange);
	}

}