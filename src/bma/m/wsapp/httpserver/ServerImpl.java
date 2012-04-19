package bma.m.wsapp.httpserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import bma.m.wsapp.util.Logger;

/**
 * Provides implementation for both HTTP and HTTPS
 */
class ServerImpl implements TimeSource {

	private String protocol;
	private Executor executor;
	private ContextList contexts;
	private ServerSocketChannel schan;
	private Selector selector;
	private SelectionKey listenerKey;
	private Set<HttpConnection> idleConnections;
	private Set<HttpConnection> allConnections;
	private List<Event> events;
	private Object lolock = new Object();
	private volatile boolean finished = false;
	private volatile boolean terminating = false;
	private boolean bound = false;
	private boolean started = false;
	private volatile long time; /* current time */
	private volatile long ticks; /* number of clock ticks since server started */
	private HttpServer wrapper;

	final static int CLOCK_TICK = ServerConfig.getClockTick();
	final static long IDLE_INTERVAL = ServerConfig.getIdleInterval();
	final static int MAX_IDLE_CONNECTIONS = ServerConfig
			.getMaxIdleConnections();

	private Timer timer;
	private Logger logger;

	ServerImpl(HttpServer wrapper, String protocol) throws IOException {
		super();
		this.protocol = protocol;
		this.wrapper = wrapper;
		this.logger = Logger.getLogger(getClass());
		contexts = new ContextList();
	}
	
	ServerImpl(HttpServer wrapper, String protocol, InetSocketAddress addr,
			int backlog) throws IOException {
		this(wrapper,protocol);
		bind(addr, backlog);
		
	}

	public void bind(InetSocketAddress addr, int backlog) throws IOException {
		if (bound) {
			throw new BindException("HttpServer already bound");
		}
		if (addr == null) {
			throw new NullPointerException("null address");
		}
		schan = ServerSocketChannel.open();
		if (addr != null) {
			ServerSocket socket = schan.socket();
			socket.bind(addr, backlog);
			bound = true;
		}
		selector = Selector.open();
		schan.configureBlocking(false);
		listenerKey = schan.register(selector, SelectionKey.OP_ACCEPT);
		dispatcher = new Dispatcher();
		idleConnections = Collections
				.synchronizedSet(new HashSet<HttpConnection>());
		allConnections = Collections
				.synchronizedSet(new HashSet<HttpConnection>());
		time = System.currentTimeMillis();
		timer = new Timer("server-timer", true);
		timer.schedule(new ServerTimerTask(), CLOCK_TICK, CLOCK_TICK);
		events = new LinkedList<Event>();
		logger.debug("HttpServer bind " + protocol + " " + addr);
		bound = true;
	}

	public void start() {
		if (!bound || started || finished) {
			throw new IllegalStateException("server in wrong state");
		}
		if (executor == null) {
			executor = new DefaultExecutor();
		}
		Thread t = new Thread(dispatcher);
		started = true;
		t.start();
	}

	public void setExecutor(Executor executor) {
		if (started) {
			throw new IllegalStateException("server already started");
		}
		this.executor = executor;
	}

	private static class DefaultExecutor implements Executor {
		public void execute(Runnable task) {
			task.run();
		}
	}

	public Executor getExecutor() {
		return executor;
	}

	public void stop(int delay) {
		if (delay < 0) {
			throw new IllegalArgumentException("negative delay parameter");
		}
		terminating = true;
		try {
			schan.close();
		} catch (IOException e) {
		}
		selector.wakeup();
		long latest = System.currentTimeMillis() + delay * 1000;
		while (System.currentTimeMillis() < latest) {
			delay();
			if (finished) {
				break;
			}
		}
		finished = true;
		selector.wakeup();
		synchronized (allConnections) {
			for (HttpConnection c : allConnections) {
				c.close();
			}
		}
		allConnections.clear();
		idleConnections.clear();
		timer.cancel();
	}

	Dispatcher dispatcher;

	public synchronized HttpContextImpl createContext(String path,
			HttpHandler handler) {
		if (handler == null || path == null) {
			throw new NullPointerException("null handler, or path parameter");
		}
		HttpContextImpl context = new HttpContextImpl(protocol, path, handler,
				this);
		contexts.add(context);
		logger.debug("context created: " + path);
		return context;
	}

	public synchronized HttpContextImpl createContext(String path) {
		if (path == null) {
			throw new NullPointerException("null path parameter");
		}
		HttpContextImpl context = new HttpContextImpl(protocol, path, null,
				this);
		contexts.add(context);
		logger.debug("context created: " + path);
		return context;
	}

	public synchronized void removeContext(String path)
			throws IllegalArgumentException {
		if (path == null) {
			throw new NullPointerException("null path parameter");
		}
		contexts.remove(protocol, path);
		logger.debug("context removed: " + path);
	}

	public synchronized void removeContext(HttpContext context)
			throws IllegalArgumentException {
		if (!(context instanceof HttpContextImpl)) {
			throw new IllegalArgumentException("wrong HttpContext type");
		}
		contexts.remove((HttpContextImpl) context);
		logger.debug("context removed: " + context.getPath());
	}

	public InetSocketAddress getAddress() {
		return (InetSocketAddress) schan.socket().getLocalSocketAddress();
	}

	Selector getSelector() {
		return selector;
	}

	void addEvent(Event r) {
		synchronized (lolock) {
			events.add(r);
			selector.wakeup();
		}
	}

	int resultSize() {
		synchronized (lolock) {
			return events.size();
		}
	}

	/* main server listener task */

	class Dispatcher implements Runnable {

		private void handleEvent(Event r) {
			ExchangeImpl t = r.exchange;
			HttpConnection c = t.getConnection();
			try {
				if (r instanceof WriteFinishedEvent) {

					int exchanges = endExchange();
					if (terminating && exchanges == 0) {
						finished = true;
					}
					LeftOverInputStream is = t.getOriginalInputStream();
					if (!is.isEOF()) {
						t.close = true;
					}
					if (t.close
							|| idleConnections.size() >= MAX_IDLE_CONNECTIONS) {
						c.close();
						allConnections.remove(c);
					} else {
						if (is.isDataBuffered()) {
							/* don't re-enable the interestops, just handle it */
							handle(c.getChannel(), c);
						} else {
							/* re-enable interestops */
							SelectionKey key = c.getSelectionKey();
							if (key.isValid()) {
								key.interestOps(key.interestOps()
										| SelectionKey.OP_READ);
							}
							c.time = getTime() + IDLE_INTERVAL;
							idleConnections.add(c);
						}
					}
				}
			} catch (IOException e) {
				logger.debug("Dispatcher (1)", e);
				c.close();
			}
		}

		public void run() {
			try {
				run1();
			} catch (Exception e) {
				logger.debug("Dispatcher (7)", e);
			}

		}

		public void run1() {
			while (!finished) {
				try {

					/* process the events list first */

					while (resultSize() > 0) {
						Event r;
						synchronized (lolock) {
							r = events.remove(0);
							handleEvent(r);
						}
					}

					selector.select(1000);

					/* process the selected list now */
					Set<SelectionKey> selected = selector.selectedKeys();
					// getLogger().debug("selectedKeys:"+selected.size());
					Iterator<SelectionKey> iter = selected.iterator();
					while (iter.hasNext()) {
						SelectionKey key = iter.next();
						iter.remove();
						if (key.equals(listenerKey)) {
							if (terminating) {
								continue;
							}
							SocketChannel chan = schan.accept();
							if (chan == null) {
								continue; /* cancel something ? */
							}
							chan.configureBlocking(false);
							SelectionKey newkey = chan.register(selector,
									SelectionKey.OP_READ);
							HttpConnection c = new HttpConnection();
							c.selectionKey = newkey;
							c.setChannel(chan);
							newkey.attach(c);
							allConnections.add(c);
						} else {
							try {
								if (key.isReadable()) {
									SocketChannel chan = (SocketChannel) key
											.channel();
									HttpConnection conn = (HttpConnection) key
											.attachment();
									// interestOps will be restored at end of
									// read
									key.interestOps(0);
									handle(chan, conn);
								} else {
									assert false;
								}
							} catch (IOException e) {
								HttpConnection conn = (HttpConnection) key
										.attachment();
								logger.debug("Dispatcher (2)", e);
								conn.close();
							}
						}
					}
				} catch (CancelledKeyException e) {
					logger.debug("Dispatcher (3)", e);
				} catch (IOException e) {
					logger.debug("Dispatcher (4)", e);
				}
			}
		}

		public void handle(SocketChannel chan, HttpConnection conn)
				throws IOException {
			try {
				Exchange t = new Exchange(chan, protocol, conn);
				executor.execute(t);
			} catch (HttpError e1) {
				logger.debug("Dispatcher (5)", e1);
				conn.close();
			} catch (IOException e) {
				logger.debug("Dispatcher (6)", e);
				conn.close();
			}
		}
	}

	static boolean debug = ServerConfig.debugEnabled();

	static synchronized void dprint(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	static synchronized void dprint(Exception e) {
		if (debug) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public Logger getLogger() {
		return logger;
	}

	/* per exchange task */

	class Exchange implements Runnable {
		SocketChannel chan;
		HttpConnection connection;
		HttpContextImpl context;
		InputStream rawin;
		OutputStream rawout;
		String protocol;
		ExchangeImpl tx;
		HttpContextImpl ctx;
		boolean rejected = false;

		Exchange(SocketChannel chan, String protocol, HttpConnection conn)
				throws IOException {
			this.chan = chan;
			this.connection = conn;
			this.protocol = protocol;
		}

		public void run() {
			/* context will be null for new connections */
			context = connection.getHttpContext();
			boolean newconnection;
			String requestLine = null;
			try {
				if (context != null) {
					this.rawin = connection.getInputStream();
					this.rawout = connection.getRawOutputStream();
					newconnection = false;
				} else {
					/* figure out what kind of connection this is */
					newconnection = true;
					rawin = new BufferedInputStream(new Request.ReadStream(
							ServerImpl.this, chan),4*1024);
					rawout = new Request.WriteStream(ServerImpl.this, chan);
				}
				Request req = new Request(rawin, rawout);
				requestLine = req.requestLine();
				if (requestLine == null) {
					/* connection closed */
					connection.close();
					return;
				}
				int space = requestLine.indexOf(' ');
				if (space == -1) {
					reject(Code.HTTP_BAD_REQUEST, requestLine,
							"Bad request line");
					return;
				}
				String method = requestLine.substring(0, space);
				int start = space + 1;
				space = requestLine.indexOf(' ', start);
				if (space == -1) {
					reject(Code.HTTP_BAD_REQUEST, requestLine,
							"Bad request line");
					return;
				}
				String uriStr = requestLine.substring(start, space);
				URI uri = new URI(uriStr);
				start = space + 1;
				String version = requestLine.substring(start);
				Headers headers = req.headers();
				String s = headers.getFirst("Transfer-encoding");
				int clen = 0;
				if (s != null && s.equalsIgnoreCase("chunked")) {
					clen = -1;
				} else {
					s = headers.getFirst("Content-Length");
					if (s != null) {
						clen = Integer.parseInt(s);
					}
				}
				ctx = contexts.findContext(protocol, uri.getPath());
				if (ctx == null) {
					reject(Code.HTTP_NOT_FOUND, requestLine,
							"No context found for request");
					return;
				}
				connection.setContext(ctx);
				if (ctx.getHandler() == null) {
					reject(Code.HTTP_INTERNAL_ERROR, requestLine,
							"No handler for context");
					return;
				}
				tx = new ExchangeImpl(method, uri, req, clen, connection);
				String chdr = headers.getFirst("Connection");
				Headers rheaders = tx.getResponseHeaders();

				if (chdr != null && chdr.equalsIgnoreCase("close")) {
					tx.close = true;
				}
				if (version.equalsIgnoreCase("http/1.0")) {
					tx.http10 = true;
					if (chdr == null) {
						tx.close = true;
						rheaders.set("Connection", "close");
					} else if (chdr.equalsIgnoreCase("keep-alive")) {
						rheaders.set("Connection", "keep-alive");
						int idle = (int) ServerConfig.getIdleInterval() / 1000;
						int max = (int) ServerConfig.getMaxIdleConnections();
						String val = "timeout=" + idle + ", max=" + max;
						rheaders.set("Keep-Alive", val);
					}
				}

				if (newconnection) {
					connection.setParameters(rawin, rawout, chan, protocol,
							ctx, rawin);
				}
				/*
				 * check if client sent an Expect 100 Continue. In that case,
				 * need to send an interim response. In future API may be
				 * modified to allow app to be involved in this process.
				 */
				String exp = headers.getFirst("Expect");
				if (exp != null && exp.equalsIgnoreCase("100-continue")) {
					logReply(100, requestLine, null);
					sendReply(Code.HTTP_CONTINUE, false, null);
				}
				/*
				 * uf is the list of filters seen/set by the user. sf is the
				 * list of filters established internally and which are not
				 * visible to the user. uc and sc are the corresponding
				 * Filter.Chains. They are linked together by a LinkHandler so
				 * that they can both be invoked in one call.
				 */
				List<Filter> sf = ctx.getSystemFilters();
				List<Filter> uf = ctx.getFilters();

				Filter.Chain sc = new Filter.Chain(sf, ctx.getHandler());
				Filter.Chain uc = new Filter.Chain(uf, new LinkHandler(sc));

				/* set up the two stream references */
				tx.getRequestBody();
				tx.getResponseBody();
				uc.doFilter(new HttpExchangeImpl(tx));

			} catch (IOException e1) {
				logger.debug("ServerImpl.Exchange (1)", e1);
				connection.close();
			} catch (NumberFormatException e3) {
				reject(Code.HTTP_BAD_REQUEST, requestLine,
						"NumberFormatException thrown");
			} catch (URISyntaxException e) {
				reject(Code.HTTP_BAD_REQUEST, requestLine,
						"URISyntaxException thrown");
			} catch (Exception e4) {
				logger.debug("ServerImpl.Exchange (2)", e4);
				connection.close();
			}
		}

		/* used to link to 2 or more Filter.Chains together */

		class LinkHandler implements HttpHandler {
			Filter.Chain nextChain;

			LinkHandler(Filter.Chain nextChain) {
				this.nextChain = nextChain;
			}

			public void handle(HttpExchange exchange) throws IOException {
				nextChain.doFilter(exchange);
			}
		}

		void reject(int code, String requestStr, String message) {
			rejected = true;
			logReply(code, requestStr, message);
			sendReply(code, true, "<h1>" + code + Code.msg(code) + "</h1>"
					+ message);
		}

		void sendReply(int code, boolean closeNow, String text) {
			try {
				String s = "HTTP/1.1 " + code + Code.msg(code) + "\r\n";
				if (text != null && text.length() != 0) {
					s = s + "Content-Length: " + text.length() + "\r\n";
					s = s + "Content-Type: text/html\r\n";
				} else {
					s = s + "Content-Length: 0\r\n";
					text = "";
				}
				if (closeNow) {
					s = s + "Connection: close\r\n";
				}
				s = s + "\r\n" + text;
				byte[] b = s.getBytes("ISO8859_1");
				rawout.write(b);
				rawout.flush();
				if (closeNow) {
					connection.close();
				}
			} catch (IOException e) {
				logger.debug("ServerImpl.sendReply", e);
				connection.close();
			}
		}

	}

	void logReply(int code, String requestStr, String text) {
		if (text == null) {
			text = "";
		}
		String message = requestStr + " [" + code + " " + Code.msg(code)
				+ "] (" + text + ")";
		logger.debug(message);
	}

	long getTicks() {
		return ticks;
	}

	public long getTime() {
		return time;
	}

	void delay() {
		Thread.yield();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
	}

	private int exchangeCount = 0;

	synchronized void startExchange() {
		exchangeCount++;
	}

	synchronized int endExchange() {
		exchangeCount--;
		assert exchangeCount >= 0;
		return exchangeCount;
	}

	HttpServer getWrapper() {
		return wrapper;
	}

	/**
	 * TimerTask run every CLOCK_TICK ms
	 */
	class ServerTimerTask extends TimerTask {
		public void run() {
			LinkedList<HttpConnection> toClose = new LinkedList<HttpConnection>();
			time = System.currentTimeMillis();
			ticks++;
			synchronized (idleConnections) {
				for (HttpConnection c : idleConnections) {
					if (c.time <= time) {
						toClose.add(c);
					}
				}
				for (HttpConnection c : toClose) {
					idleConnections.remove(c);
					allConnections.remove(c);
					c.close();
				}
			}
		}
	}
}
