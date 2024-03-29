package bma.m.wsapp.httpserver;

import java.util.List;
import java.util.Map;

/**
 * HttpContext represents a mapping between the root URI path of an application
 * to a {@link HttpHandler} which is invoked to handle requests destined for
 * that path on the associated HttpServer or HttpsServer.
 * <p>
 * HttpContext instances are created by the create methods in HttpServer and
 * HttpsServer
 * <p>
 * A chain of {@link Filter} objects can be added to a HttpContext. All
 * exchanges processed by the context can be pre- and post-processed by each
 * Filter in the chain.
 * 
 * @since 1.6
 */
public abstract class HttpContext {

	protected HttpContext() {
	}

	/**
	 * returns the handler for this context
	 * 
	 * @return the HttpHandler for this context
	 */
	public abstract HttpHandler getHandler();

	/**
	 * Sets the handler for this context, if not already set.
	 * 
	 * @param h
	 *            the handler to set for this context
	 * @throws IllegalArgumentException
	 *             if this context's handler is already set.
	 * @throws NullPointerException
	 *             if handler is <code>null</code>
	 */
	public abstract void setHandler(HttpHandler h);

	/**
	 * returns the path this context was created with
	 * 
	 * @return this context's path
	 */
	public abstract String getPath();

	/**
	 * returns the server this context was created with
	 * 
	 * @return this context's server
	 */
	public abstract HttpServer getServer();

	/**
	 * returns a mutable Map, which can be used to pass configuration and other
	 * data to Filter modules and to the context's exchange handler.
	 * <p>
	 * Every attribute stored in this Map will be visible to every HttpExchange
	 * processed by this context
	 */
	public abstract Map<String, Object> getAttributes();

	/**
	 * returns this context's list of Filters. This is the actual list used by
	 * the server when dispatching requests so modifications to this list
	 * immediately affect the the handling of exchanges.
	 */
	public abstract List<Filter> getFilters();

}
