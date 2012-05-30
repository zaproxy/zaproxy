package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class WebSocketListener implements Runnable {

	private static Logger logger = Logger.getLogger(WebSocketListener.class);

	/**
	 * Listener operates either on
	 *  - incoming channel: server -> ZAP -> browser
	 *  - outgoing channel: browser -> ZAP -> server
	 */
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
	 * Name of this thread (in-, or outgoing)
	 */
	private String name;

	private boolean isFinished;

	/**
	 * Create listener, that calls the WebSocketsProxy instance to process read
	 * data. It contains also the other end's writer to forward frames.
	 * 
	 * @param wsProxy Calls this proxy object when a read has to be processed.
	 * @param in Read from one side.
	 * @param out Write to the other side.
	 * @param name Name of this thread, used for logging also.
	 */
	public WebSocketListener(WebSocketProxy wsProxy, InputStream in, OutputStream out, String name) {
		this.wsProxy = wsProxy;
		this.in = in;
		this.out = out;
		this.name = name;
	}

	@Override
	public void run() {
		Thread.currentThread().setName(name);
		
		try {
			byte[] buffer = new byte[1];
			while (in.read(buffer) != -1) {
				// there is something to read => process in WebSockets version specific message
				wsProxy.processRead(in, out, buffer[0]);
			}
		} catch (InterruptedIOException e) {
			// ignore this interruption, as it indicates a valid shutdown
		} catch (Exception e) {
			if (Thread.currentThread().isInterrupted()) {
				// this thread has been interrupted -> it was an intentional shutdown
			} else {
				// shutdown was not intended
				// error has to be taken seriously
				logger.error("WebSocketListener quit reading due to: " + e.getClass() + ": " + e.getMessage());
			}
		} finally {
			isFinished = true;
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

	/**
	 * Interrupts current thread,
	 * stopping its execution.
	 */
	public void stop() {
		Thread.currentThread().interrupt();
	}

	/**
	 * Returns true if this listener stopped listening.
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return isFinished;
	}
}
