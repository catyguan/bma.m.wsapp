package bma.m.wsapp.httpserver;

class Event {

	ExchangeImpl exchange;

	protected Event(ExchangeImpl t) {
		this.exchange = t;
	}
}

class WriteFinishedEvent extends Event {
	WriteFinishedEvent(ExchangeImpl t) {
		super(t);
	}
}
