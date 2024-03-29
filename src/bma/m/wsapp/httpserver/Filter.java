package bma.m.wsapp.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ListIterator;

/**
 * A filter used to pre- and post-process incoming requests. Pre-processing
 * occurs before the application's exchange handler is invoked, and
 * post-processing occurs after the exchange handler returns. Filters are
 * organised in chains, and are associated with HttpContext instances.
 * <p>
 * Each Filter in the chain, invokes the next filter within its own doFilter()
 * implementation. The final Filter in the chain invokes the applications
 * exchange handler.
 * 
 * @since 1.6
 */
public abstract class Filter {

	protected Filter() {
	}

	/**
	 * a chain of filters associated with a HttpServer. Each filter in the chain
	 * is given one of these so it can invoke the next filter in the chain
	 */
	public static class Chain {
		/*
		 * the last element in the chain must invoke the users handler
		 */
		protected List<Filter> filters;
		private ListIterator<Filter> iter;
		private HttpHandler handler;

		public Chain(List<Filter> filters, HttpHandler handler) {
			this.filters = filters;
			iter = filters.listIterator();
			this.handler = handler;
		}

		/**
		 * calls the next filter in the chain, or else the users exchange
		 * handler, if this is the final filter in the chain. The Filter may
		 * decide to terminate the chain, by not calling this method. In this
		 * case, the filter <b>must</b> send the response to the request,
		 * because the application's exchange handler will not be invoked.
		 * 
		 * @param exchange
		 *            the HttpExchange
		 * @throws IOException
		 *             let exceptions pass up the stack
		 * @throws NullPointerException
		 *             if exchange is <code>null</code>
		 */
		public void doFilter(HttpExchange exchange) throws IOException {
			if (!iter.hasNext()) {
				handler.handle(exchange);
			} else {
				Filter f = iter.next();
				f.doFilter(exchange, this);
			}
		}
	}

	/**
	 * Asks this filter to pre/post-process the given exchange. The filter can
	 * :-
	 * <ul>
	 * <li>examine or modify the request headers</li>
	 * <li>filter the request body or the response body, by creating suitable
	 * filter streams and calling
	 * {@link HttpExchange#setStreams(InputStream,OutputStream)}</li>
	 * <li>set attribute Objects in the exchange, which other filters or the
	 * exchange handler can access.</li>
	 * <li>decide to either :-
	 * <ol>
	 * <li>invoke the next filter in the chain, by calling
	 * {@link Filter.Chain#doFilter(HttpExchange)}</li>
	 * <li>terminate the chain of invocation, by <b>not</b> calling
	 * {@link Filter.Chain#doFilter(HttpExchange)}</li>
	 * </ol>
	 * <li>if option 1. above taken, then when doFilter() returns all subsequent
	 * filters in the Chain have been called, and the response headers can be
	 * examined or modified.</li>
	 * <li>if option 2. above taken, then this Filter must use the HttpExchange
	 * to send back an appropriate response</li>
	 * </ul>
	 * <p>
	 * 
	 * @param exchange
	 *            the <code>HttpExchange</code> to be filtered.
	 * @param chain
	 *            the Chain which allows the next filter to be invoked.
	 * @throws IOException
	 *             may be thrown by any filter module, and if caught, must be
	 *             rethrown again.
	 * @throws NullPointerException
	 *             if either exchange or chain are <code>null</code>
	 */
	public abstract void doFilter(HttpExchange exchange, Chain chain)
			throws IOException;

	/**
	 * returns a short description of this Filter
	 * 
	 * @return a string describing the Filter
	 */
	public abstract String description();

}
