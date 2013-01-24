/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * Wrap it in a thread to listen for one end of a WebSockets connection. It does
 * so by using blocking reads and passes read bytes to {@link WebSocketProxy} for
 * processing. If you want to listen (a.k.a. observe) for WebSocket messages, see
 * the {@link WebSocketObserver} class.
 */
public class WebSocketListener implements Runnable {

	private static final Logger logger = Logger.getLogger(WebSocketListener.class);

	/**
	 * Listen from one side of this communication channel.
	 */
	private final InputStream in;

	/**
	 * Write/Forward frames to the other side.
	 */
	private final OutputStream out;

	/**
	 * This proxy object is used to process the read.
	 */
	private final WebSocketProxy wsProxy;

	/**
	 * Name of this thread (in-, or outgoing)
	 */
	private final String name;

	/**
	 * Indicates if it still listens.
	 */
	private boolean isFinished = false;

	/**
	 * Create listener, that calls the WebSocketsProxy instance to process read
	 * data. It contains also the other end's writer to forward frames.
	 * 
	 * @param wsProxy When a read has to be processed, it delegates it to this object.
	 * @param in Read from one side.
	 * @param out Write to the other side.
	 * @param name Name of this thread (used for logging too).
	 */
	public WebSocketListener(WebSocketProxy wsProxy, InputStream in, OutputStream out, String name) {
		this.wsProxy = wsProxy;
		this.in = in;
		this.out = out;
		this.name = name;
	}

	/**
	 * Start listening and process messages as long as the Socket is open.
	 */
	@Override
	public void run() {
		Thread.currentThread().setName(name);
		
		try {
			if (in != null) {
				byte[] buffer = new byte[1];
				while (in.read(buffer) != -1) {
					// there is something to read => process in WebSockets version specific message
					wsProxy.processRead(in, out, buffer[0]);
				}
			}
		} catch (IOException e) {
			// includes SocketException
			// no more reading possible
			stop();
		} finally {				
			// mark as finished
			isFinished = true;
			
			// close the other listener too
			wsProxy.shutdown();
		}
	}

	/**
	 * Properly close incoming stream.
	 */
	private void closeReaderStream() {
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * Properly close outgoing stream.
	 */
	private void closeWriterStream() {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * Interrupts current thread, stopping its execution.
	 */
	public void stop() {
		// no more bytes can be read
		closeReaderStream();
		
		// no more bytes can be written
		closeWriterStream();
	}

	/**
	 * Has this listener already stopped working?
	 * 
	 * @return True if listener stopped listening.
	 */
	public boolean isFinished() {
		return isFinished;
	}
	
	/**
	 * Use this stream to send custom messages.
	 * 
	 * @return outgoing stream
	 */
	public OutputStream getOutputStream() {
		return out;
	}
}
