package bma.m.wsapp;

import java.io.IOException;
import java.net.InetSocketAddress;

import bma.m.wsapp.httpserver.HttpServer;

public class WSAppServer extends HttpServer {
	
	private String token;

	public WSAppServer() throws IOException {
		super(new InetSocketAddress("127.0.0.1", 0), 0);
	}

	public String createServer() {
		this.token = java.util.UUID.randomUUID().toString();

		StringBuffer buf = new StringBuffer(128);
		buf.append("http://127.0.0.1:");
		buf.append(getAddress().getPort());
		buf.append("/");
		buf.append(this.token);
		getLogger().debug("WSAppServer(" + buf + ")");
		super.start();
		return buf.toString();
	}
}
