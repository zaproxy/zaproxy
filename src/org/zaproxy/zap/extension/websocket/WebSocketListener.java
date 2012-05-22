package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class WebSocketListener implements Runnable {

	private static Logger logger = Logger.getLogger(WebSocketListener.class);

	public enum Direction {
		INCOMING, OUTGOING
	};

//	/**
//	 * Specifies the direction of this listener. Either incoming from server
//	 * through ZAP to browser, or outgoing from browser through ZAP to server.
//	 */
//	private Direction direction;

	/**
	 * Listen from one side of this communication channel.
	 */
	private InputStream in;

	/**
	 * Write/Forward frames to the other side.
	 */
	private OutputStream out;

	/**
	 * This proxy object is used to process the read.
	 */
	private WebSocketProxy wsProxy;

	/**
	 * Create listener, that calls the WebSocketsProxy instance to process read
	 * data. It contains also the other end's writer to forward frames.
	 * 
	 * @param in Read from one side.
	 * @param out Write to the other side.
	 */
	public WebSocketListener(WebSocketProxy wsProxy, InputStream in, OutputStream out) {
		this.wsProxy = wsProxy;
		this.in = in;
		this.out = out;
	}

	@Override
	public void run() {
		try {
			byte[] buffer = new byte[1];
			while (in.read(buffer) != -1) {
				// there is something to read => process in WebSockets version specific message
				wsProxy.processRead(in, out, buffer[0]);
			}
		} catch (InterruptedIOException e) {
			// ignore this interruption, as it indicates a valid shutdown
		} catch (Exception e) {
			// if this thread has been interrupted, it was an intentional
			// shutdown -> otherwise report error
			
			if (!Thread.currentThread().isInterrupted()) {
				// shutdown was not intended from outside
				// occurred error has to be taken seriously
				logger.error("WebSocketListener quit reading due to: " + e.getMessage());
			}
		} finally {
			logger.error("shutdown listener");
			wsProxy.shutdown();
		}
	}

	/**
	 * Properly close incoming stream.
	 */
	public void closeReaderStream() {
		try {
			in.close();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * Properly close outgoing stream.
	 */
	public void closeWriterStream() {
		try {
			out.close();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}
}
