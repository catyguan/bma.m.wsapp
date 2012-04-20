package bma.m.wsapp.content;

import java.util.List;

import bma.m.wsapp.httpserver.HttpExchange;

public class ChainContentFileProvider implements ContentFileProvider {

	private List<ContentFileProvider> providers;

	public ChainContentFileProvider() {
		super();
	}

	public ChainContentFileProvider(List<ContentFileProvider> providers) {
		super();
		this.providers = providers;
	}

	public List<ContentFileProvider> getProviders() {
		return providers;
	}

	public void setProviders(List<ContentFileProvider> providers) {
		this.providers = providers;
	}

	public ContentFile getContent(String path, HttpExchange exchange) {
		if(this.providers!=null) {
			for(ContentFileProvider p : this.providers) {
				ContentFile f = p.getContent(path, exchange);
				if(f!=null && f.exists()) {
					return f;
				}
			}
		}
		return null;
	}

}
