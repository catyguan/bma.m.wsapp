package bma.m.wsapp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import bma.m.wsapp.httpserver.Filter;
import bma.m.wsapp.httpserver.Headers;
import bma.m.wsapp.httpserver.HttpContext;
import bma.m.wsapp.httpserver.HttpExchange;
import bma.m.wsapp.httpserver.HttpHandler;
import bma.m.wsapp.httpserver.HttpServer;
import bma.m.wsapp.util.HttpExchangeUtil;

public class WSAppServer extends HttpServer {

	private String token;

	public WSAppServer() throws IOException {
		super();
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");
		this.bind(new InetSocketAddress("127.0.0.1", 0), 0);
	}

	public String getUrl(String uri) {
		StringBuffer buf = new StringBuffer(128);
		buf.append("http://127.0.0.1:");
		buf.append(getAddress().getPort());
		if (uri == null || !uri.startsWith("/")) {
			buf.append("/");
		}
		if (uri != null) {
			buf.append(uri);
		}
		return buf.toString();
	}

	public String createServer() {
		this.token = java.util.UUID.randomUUID().toString();

		String r = getUrl(this.token);
		getLogger().debug("WSAppServer(" + r + ")");
		super.start();

		super.createContext("/"+this.token, new HttpHandler() {

			public void handle(HttpExchange exchange) throws IOException {

				String home = "index.html";
				Map<String, String> reqs = HttpExchangeUtil
						.parseQueryString(exchange);
				if (reqs.containsKey("go")) {
					home = reqs.get("go");
				}
				getLogger().debug("auth done,home = " + home);
				Headers h = exchange.getResponseHeaders();
				h.add("Location", getUrl(home));
				h.add("Set-Cookie",
						"token=" + HttpExchangeUtil.urlencode(token));
				HttpExchangeUtil.reply(exchange, 302, "loading");
			}

		});

		return r;
	}

	@Override
	public HttpContext createContext(String path, HttpHandler handler) {
		HttpContext ctx = super.createContext(path, handler);
		ctx.getFilters().add(new Filter() {
			@Override
			public String description() {
				return "authFilter";
			}

			@Override
			public void doFilter(HttpExchange exchange, Chain chain)
					throws IOException {
				String ck = exchange.getRequestHeaders().getFirst("Cookie");
				if (ck != null) {
					String[] cklist = ck.split(";");
					for (String ckitem : cklist) {
						String[] ckp = ckitem.split("=");
						if (ckp.length > 1 && ckp[0].trim().equals("token")
								&& ckp[1].trim().equals(token)) {
							chain.doFilter(exchange);
							return;
						}
					}
				}
				HttpExchangeUtil.reply(exchange, 403, "Access Denied");
			}
		});
		return ctx;
	}
}
