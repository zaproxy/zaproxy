package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class WebSocketsThread implements Runnable {
	private static Logger logger = Logger.getLogger(WebSocketProxy.class);

	private Selector selector;

	public WebSocketsThread(Selector selector) {
		this.selector = selector;
	}

	@Override
	public void run() {
		while (true) {			
			int readyChannels = 0;
			
			try {
				// timeout after x milliseconds
				readyChannels = selector.select(1000);
			} catch (IOException e) {
				logger.error("NIO selector failed. Abort WebSocketsThread.");
				break;
			}

			if (readyChannels == -1) {
				logger.debug("Thread was interrupted, as Selector.select() delivered '-1'.");
				break;
			} else if (readyChannels == 0) {
				// TODO: Should I call "Thread.yield()" here or not?
				// Thread.yield();
				continue;
			}

			Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();

				if (key.isReadable()) {
					// a channel is ready for reading
					WebSocketProxy webSocket = (WebSocketProxy) key.attachment();
					try {
						webSocket.processRead(key);
					} catch (IOException e) {
						logger.error("Read failed due to: " + e);
					}
				}

				keyIterator.remove(); // indicates that this event has been processed
			}
		}
		logger.debug("WebSockets-Thread terminated.");
	}
}
