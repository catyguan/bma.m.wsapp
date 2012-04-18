package bma.m.wsapp.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import bma.m.wsapp.util.Logger;

public class HttpServer {

	protected ServerImpl server;

	public HttpServer() throws IOException {
		super();
		server = new ServerImpl(this, "http");
	}
	
	public HttpServer(int port) throws IOException {
		this(new InetSocketAddress(port),0);
	}

	public HttpServer(InetSocketAddress addr, int backlog) throws IOException {
		server = new ServerImpl(this, "http", addr, backlog);
	}

	public ServerImpl getServer() {
		return this.server;
	}

	public void bind(InetSocketAddress addr, int backlog) throws IOException {
		server.bind(addr, backlog);
	}

	public void start() {
		server.start();
	}

	public void setExecutor(Executor executor) {
		server.setExecutor(executor);
	}

	public Executor getExecutor() {
		return server.getExecutor();
	}

	public void stop(int delay) {
		server.stop(delay);
	}

	public HttpContextImpl createContext(String path, HttpHandler handler) {
		return server.createContext(path, handler);
	}

	public HttpContextImpl createContext(String path) {
		return server.createContext(path);
	}

	public void removeContext(String path) throws IllegalArgumentException {
		server.removeContext(path);
	}

	public void removeContext(HttpContext context)
			throws IllegalArgumentException {
		server.removeContext(context);
	}

	public InetSocketAddress getAddress() {
		return server.getAddress();
	}

	public Logger getLogger() {
		return server.getLogger();
	}

}
