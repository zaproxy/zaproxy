package org.zaproxy.zap.extension.websocket;

/**
 * Simple class that represents a tuple.
 * 
 * @param <X>
 * @param <Y>
 */
public class Pair<X, Y> {
	public final X x;
	public final Y y;

	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
}
