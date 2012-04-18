package bma.m.wsapp.content;

import bma.m.wsapp.httpserver.HttpExchange;

public interface ContentFileProvider {

	public ContentFile getContent(String path, HttpExchange exchange);

}
