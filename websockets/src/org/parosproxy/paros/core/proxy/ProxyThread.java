/*
 * Created on May 25, 2004
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/05/09 Support for API
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2012/03/15 Removed unnecessary castings from methods notifyListenerRequestSend,
// notifyListenerResponseReceive and isProcessCache. Set the name of the proxy thread.
// Replaced the class HttpBody with the new class HttpRequestBody and replaced the method 
// call from readBody to readRequestBody of the class HttpInputStream. 
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/05/06 Handle over socket connection to WebSockets extension in processHttp.
package org.parosproxy.paros.core.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpInputStream;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpOutputStream;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpUtil;
import org.parosproxy.paros.security.MissingRootCertificateException;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.spider.ExtensionSpider;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.network.HttpRequestBody;


class ProxyThread implements Runnable {

//	private static final int		BUFFEREDSTREAM_SIZE = 4096;
	private static final String		CONNECT_HTTP_200 = "HTTP/1.1 200 Connection established\r\nProxy-connection: Keep-alive\r\n\r\n";
//	private static ArrayList 		processForwardList = new ArrayList();
    
	private static Logger log = Logger.getLogger(ProxyThread.class);
    
	// change httpSender to static to be shared among proxies to reuse keep-alive connections

	protected ProxyServer parentServer = null;
	protected ProxyParam proxyParam = null;
	protected ConnectionParam connectionParam = null;
	protected Thread thread = null;
	protected Socket inSocket	= null;
	protected Socket outSocket = null;
	protected HttpInputStream httpIn = null;
	protected HttpOutputStream httpOut = null;
	protected ProxyThread originProcess = this;
	
	private HttpSender 		httpSender = null;
//	private BufferedOutputStream forwardOut = null;
//	private BufferedInputStream forwardIn = null;
//	private boolean disconnect = false;
	private Object semaphore = this;
	private boolean keepSocketAfterDisconnect = false;
	private static Object semaphoreSingleton = new Object();
//	private Thread forwardThread = null;
    
    private static Vector<Thread> proxyThreadList = new Vector<Thread>();
    
	ProxyThread(ProxyServer server, Socket socket) {
		parentServer = server;
		proxyParam = parentServer.getProxyParam();
		connectionParam = parentServer.getConnectionParam();

		inSocket = socket;
    	try {
			inSocket.setTcpNoDelay(true);
			// ZAP: Set timeout
    		inSocket.setSoTimeout(connectionParam.getTimeoutInSecs() * 1000);
		} catch (SocketException e) {
			// ZAP: Log exceptions
			log.warn(e.getMessage(), e);
		}

		thread = new Thread(this, "ZAP-ProxyThread"); // ZAP: Set the name of the thread.
		thread.setDaemon(true);
		thread.setPriority(Thread.NORM_PRIORITY-1);
	}

	public void start() {
		thread.start();
        
	}
	
	/**
	 * @param targethost the host where you want to connect to
	 * @throws IOException
	 */
	private void beginSSL(String targethost) throws IOException {

        boolean isSecure = true;
        HttpRequestHeader firstHeader = null;
		// ZAP: added parameter 'targethost'
        try {
			inSocket = HttpSender.getSSLConnector().createTunnelServerSocket(targethost, inSocket);
        } catch (MissingRootCertificateException e) {
        	throw new MissingRootCertificateException(e); // throw again, cause will be catched later.
		} catch (Exception e) {
			// ZAP: transform for further processing 
			throw new IOException("Error while establishing SSL connection!", e);
		}
        
        httpIn = new HttpInputStream(inSocket);
        httpOut = new HttpOutputStream(inSocket.getOutputStream());
        
        firstHeader = httpIn.readRequestHeader(isSecure);
        processHttp(firstHeader, isSecure);
    }
	
	@Override
	public void run() {
        proxyThreadList.add(thread);
		boolean isSecure = this instanceof ProxyThreadSSL;
		HttpRequestHeader firstHeader = null;
		
		try {
			httpIn = new HttpInputStream(inSocket);
			httpOut = new HttpOutputStream(inSocket.getOutputStream());
			
			firstHeader = httpIn.readRequestHeader(isSecure);
            
			if (firstHeader.getMethod().equalsIgnoreCase(HttpRequestHeader.CONNECT)) {
				
				// ZAP: added host name variable
                String hostName = firstHeader.getHostName();
				try {
					httpOut.write(CONNECT_HTTP_200);
					httpOut.flush();
					beginSSL(hostName);
				} catch (MissingRootCertificateException e) {
					// Unluckily Firefox and Internet Explorer will not show this message.
					// We should find a way to let the browsers display this error message.
					// May we can redirect to some kind of ZAP custom error page.
					final HttpMessage errmsg = new HttpMessage();
					errmsg.setRequestHeader(firstHeader);
					errmsg.setResponseBody("ZAP SSL Error: " + e.getLocalizedMessage());
			    	int len = errmsg.getResponseBody().length();
			    	errmsg.setResponseHeader("HTTP/1.1 504 Gateway Timeout\r\nContent-Length: " + len + "\r\nContent-Type: text/plain;");
			        httpOut.write(errmsg.getResponseHeader());
		            httpOut.flush();
			        if (errmsg.getResponseBody().length() > 0) {
			            httpOut.write(errmsg.getResponseBody().getBytes());
			            httpOut.flush();
			        }
			        throw new IOException(e);
				}
			} else {
				processHttp(firstHeader, isSecure);
			}
	    } catch (SocketTimeoutException e) {
        	// ZAP: Log the exception
	    	if (firstHeader != null) {
	    		log.warn("Timeout accessing " + firstHeader.getURI());
	    	} else {
	    		log.warn("Timeout", e);
	    	}
	    } catch (HttpMalformedHeaderException e) {
	    	log.warn("Malformed Header: ", e);
		} catch (IOException e) {
		    log.debug("IOException: ", e);
		} finally {
            proxyThreadList.remove(thread);
			disconnect();
		}
	}
	
	protected void processHttp(HttpRequestHeader requestHeader, boolean isSecure) throws IOException {

		HttpRequestBody reqBody = null; // ZAP: Replaced the class HttpBody with the class HttpRequestBody.
		boolean isFirstRequest = true;
		HttpMessage msg = null;
        
        if (API.getInstance().handleApiRequest(requestHeader, httpIn, httpOut)) {
        	// It was an API request
        	return;
        }

        if (isRecursive(requestHeader)) {
            throw new IOException("Recursive request to proxy itself stopped.");
        }
        
        // reduce socket timeout after first read
        inSocket.setSoTimeout(2500);
        
		do {

			if (isFirstRequest) {
				isFirstRequest = false;
			} else {
			    try {
			        requestHeader = httpIn.readRequestHeader(isSecure);

			    } catch (SocketTimeoutException e) {
		        	// ZAP: Log the exception
		        	log.warn("Timeout reading " + requestHeader.getURI().toString());
		        	return;
			    }
			}

			msg = new HttpMessage();
			msg.setRequestHeader(requestHeader);
			
			if (msg.getRequestHeader().getContentLength() > 0) {
				reqBody		= httpIn.readRequestBody(requestHeader); // ZAP: Changed to call the method readRequestBody.
				msg.setRequestBody(reqBody);
			}
            
			modifyHeader(msg);

            if (isProcessCache(msg)) {
                continue;
            }
          
//            System.out.println("send required: " + msg.getRequestHeader().getURI().toString());
            
			if (parentServer.isSerialize()) {
			    semaphore = semaphoreSingleton;
			} else {
			    semaphore = this;
			}
			
			synchronized (semaphore) {
			    
			    if (! notifyListenerRequestSend(msg)) {
		        	// One of the listeners has told us to drop the request
			    	return;
			    }
			    
			    
			    try {
//					bug occur where response cannot be processed by various listener
//			        first so streaming feature was disabled		        
//					getHttpSender().sendAndReceive(msg, httpOut, buffer);
					getHttpSender().sendAndReceive(msg);
					
			        if (! notifyListenerResponseReceive(msg)) {
			        	// One of the listeners has told us to drop the response
   				    	return;
			        }
		        
			        // write out response header and body
			        httpOut.write(msg.getResponseHeader());
		            httpOut.flush();
			        
			        if (msg.getResponseBody().length() > 0) {
			            httpOut.write(msg.getResponseBody().getBytes());
			            httpOut.flush();
			        }
			        
//			        notifyWrittenToForwardProxy();
			    } catch (HttpException e) {
//			    	System.out.println("HttpException");
			    	throw e;
			    } catch (IOException e) {
			    	msg.setResponseBody("ZAP Error: " + e.getLocalizedMessage());
			    	int len = msg.getResponseBody().length();
			    	msg.setResponseHeader("HTTP/1.1 504 Gateway Timeout\r\nContent-Length: " + len + "\r\nContent-Type: text/plain;");
			    	
			        notifyListenerResponseReceive(msg);

			        httpOut.write(msg.getResponseHeader());
		            httpOut.flush();

			        if (msg.getResponseBody().length() > 0) {
			            httpOut.write(msg.getResponseBody().getBytes());
			            httpOut.flush();
			        }

			        //throw e;
			    }
			}	// release semaphore
			
			// ZAP: Add check for connection upgrade and stop if one arrived
			if (isWebSocketUpgrade(msg)) {
				keepSocketAfterDisconnect = true;
				log.debug("Got WebSockets upgrade request. Handle socket connection over to WebSockets extension.");
				ExtensionWebSocket extWebSocket = (ExtensionWebSocket) Control.getSingleton().getExtensionLoader().getExtension("ExtensionWebSocket");
				extWebSocket.addWebSocketsChannel(inSocket.getChannel());
				break;
			}
	    } while (!isConnectionClose(msg) && !inSocket.isClosed());
		
    }
	
	private boolean isConnectionClose(HttpMessage msg) {
		
		if (msg == null || msg.getResponseHeader().isEmpty()) {
		    return true;
		}
		
		if (msg.getRequestHeader().isConnectionClose()) {
		    return true;
		}
				
		if (msg.getResponseHeader().isConnectionClose()) {
		    return true;
		}
        
        if (msg.getResponseHeader().getContentLength() == -1 && msg.getResponseBody().length() > 0) {
            // no length and body > 0 must terminate otherwise cannot there is no way for client browser to know the length.
            // terminate early can give better response by client.
            return true;
        }
		
		return false;
	}
	
	/*
	 * ZAP: New method checking for connection upgrade
	 */
	private boolean isWebSocketUpgrade(HttpMessage msg) {
		if (msg != null && !msg.getResponseHeader().isEmpty()) {
			String connectionHeader = msg.getResponseHeader().getHeader("connection");
			String upgradeHeader = msg.getResponseHeader().getHeader("upgrade");
			
			if (connectionHeader != null && connectionHeader.equalsIgnoreCase("upgrade")) {
				if (upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket")) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected void disconnect() {
		try {
            if (httpIn != null) {
                httpIn.close();
            }
        } catch (Exception e) {
			// ZAP: Log exceptions
			log.warn(e.getMessage(), e);
        }
        
        try {
            if (httpOut != null) {
                httpOut.close();
            }
        } catch (Exception e) {
			// ZAP: Log exceptions
			log.warn(e.getMessage(), e);
        }
        
        if (keepSocketAfterDisconnect == false) {
        	HttpUtil.closeSocket(inSocket);
		}
        
		if (httpSender != null) {
            httpSender.shutdown();
        }

	}
	
//	private void processForwardPort() {
//	    StreamForwarder forwarder = null;
//	    
//	    setDisconnect(false);
//	    
//	    try {
//	        
//	        synchronized (processForwardList) {
//	            outSocket = new Socket(proxyParam.getProxyIp(), parentServer.getForwardPort());
//	            outSocket.setTcpNoDelay(true);
//	            processForwardList.add(this);
//	            forwardOut = new BufferedOutputStream(outSocket.getOutputStream());
//	            forwardIn = new BufferedInputStream(outSocket.getInputStream());
//	        }
//	        
//	        forwarder = new StreamForwarder(inSocket, httpIn, forwardOut);
//	        forwarder.start();
//	        
//	        byte[] buffer = new byte[BUFFEREDSTREAM_SIZE*2];
//	        int len = -1;
//	        long startTime = System.currentTimeMillis();
//	        int continuousCount = 0;
//	        
//            outSocket.setSoTimeout(150);
//
//	        do {
//	            try {
//	                len = forwardIn.read(buffer);
//	                
//	                if (len > 0) {
//	                    httpOut.write(buffer, 0, len);
//	                    httpOut.flush();
//	                    startTime = System.currentTimeMillis();
//	                    continuousCount++;
//	                    if (continuousCount % 10 == 9) Thread.yield(); // time slice to avoid same thread occupy all CPU time.
//	                }
//	            } catch (SocketTimeoutException ex) {
//		            len = 0;
//		            continuousCount = 0;
//	            }
//
//	            if (len > 0) {
//	                continue;
//	            } else {
//	                
//	                if (forwardIn.available() == 0) {
//	                    setForwardInputBufferEmpty(true);
//	                    if (System.currentTimeMillis() - startTime > TIME_OUT) {
//	                        break;
//	                    }
//	                }
//	            }
//
//	        } while (!isDisconnect());
//	        
//	    } catch (Exception e) {
//	        //showErrMessage("Error connecting to internal SSL proxy.");
//	    } finally {
//	        
//	        if (forwardThread != null) {
//	            forwardThread.interrupt();
//	        }
//	        if (forwarder != null) {
//	            forwarder.setStop(true);
//	        }
//	        removeFromList();	// end forward port processing
//	        
//	        HttpUtil.closeInputStream(forwardIn);
//	        HttpUtil.closeOutputStream(forwardOut);
//	        HttpUtil.closeSocket(outSocket);
//	        
//	    }
//	    
//	}
	


//	protected void removeFromList() {
//		synchronized (processForwardList) {
//			processForwardList.remove(this);
//		}
//	}
//
//	protected synchronized boolean isDisconnect() {
//		return disconnect;
//	}
//
//	protected synchronized void setDisconnect(boolean flag) {
//		disconnect = flag;
//	}
//	
//	private boolean isForwardInputBufferEmpty = true;	
//
//	/**
//	Set if tunnel buffer empty.
//	@param	bufferEmpty true if tunnel buffer is empty
//	*/
//	protected synchronized void setForwardInputBufferEmpty(boolean bufferEmpty) {
//		this.isForwardInputBufferEmpty = bufferEmpty;
//	}
//
//	/**
//	Check if tunnel buffer empty here
//	@return	true = tunnel buffer is empty
//	*/
//	protected synchronized boolean isForwardInputBufferEmpty() {
//		try {
//			if (forwardIn.available() > 0) {
//				setForwardInputBufferEmpty(false);
//			}
//		} catch (Exception e) {
//		}
//
//		return isForwardInputBufferEmpty;
//	}

//	protected static ProxyThread getOriginatingProcess (int remotePortUsing) {
//		ProxyThread process = null;
//		synchronized (processForwardList) {
//			for (int i=0;i<processForwardList.size();i++) {
//				process = (ProxyThread) processForwardList.get(i);
//				if (process.outSocket.getLocalPort() == remotePortUsing) {
//					return process;
//				}
//			}
//		}
//		return null;
//	}
//	
//	public Thread getThread() {
//		return thread;
//	}
//	
//	private void notifyWrittenToForwardProxy() {
//	    if (originProcess == null) {
//	        return;
//	    }
//	    
//		synchronized (originProcess) {
//			originProcess.setForwardInputBufferEmpty(false);
//			originProcess.notify();
//		}
//	}

	/**
	 * Go through each observers to process a request in each observers.
	 * The method can be modified in each observers.
	 * @param httpMessage
	 */
	private boolean notifyListenerRequestSend(HttpMessage httpMessage) {
		if (parentServer.excludeUrl(httpMessage.getRequestHeader().getURI())) {
			return true;
		}
		ProxyListener listener = null;
		List<ProxyListener> listenerList = parentServer.getListenerList();
		for (int i=0;i<listenerList.size();i++) {
			listener = listenerList.get(i);
			try {
			    if (! listener.onHttpRequestSend(httpMessage)) {
			    	return false;
			    }
			} catch (Exception e) {
				// ZAP: Log exceptions
				log.warn(e.getMessage(), e);
			}
		}
		return true;
	}

	/**
	 * Go thru each observers and process the http message in each observers.
	 * The msg can be changed by each observers.
	 * @param msg
	 */
	private boolean notifyListenerResponseReceive(HttpMessage httpMessage) {
		if (parentServer.excludeUrl(httpMessage.getRequestHeader().getURI())) {
			return true;
		}
		ProxyListener listener = null;
		List<ProxyListener> listenerList = parentServer.getListenerList();
		for (int i=0;i<listenerList.size();i++) {
			listener = listenerList.get(i);
			try {
			    if (!listener.onHttpResponseReceive(httpMessage)) {
			    	return false;
			    }
			} catch (Exception e) {
				// ZAP: Log exceptions
				log.warn(e.getMessage(), e);
			}
		}
		return true;
	}
	
	private boolean isRecursive(HttpRequestHeader header) {
        boolean isRecursive = false;
        try {
            URI uri = header.getURI();
            if (uri.getHost().equals(proxyParam.getProxyIp())) {
                if (uri.getPort() == proxyParam.getProxyPort()) {
                    isRecursive = true;
                }
            }
        } catch (Exception e) {
			// ZAP: Log exceptions
			log.warn(e.getMessage(), e);
        }
        return isRecursive;
    }
	    
    private static final Pattern remove_gzip1 = Pattern.compile("(gzip|deflate|compress|x-gzip|x-compress)[^,]*,?\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern remove_gzip2 = Pattern.compile("[,]\\z", Pattern.CASE_INSENSITIVE);
    
    private void modifyHeader(HttpMessage msg) {
        String encoding = msg.getRequestHeader().getHeader(HttpHeader.ACCEPT_ENCODING);
        if (encoding == null) {
            return;
        }
        
        encoding = remove_gzip1.matcher(encoding).replaceAll("");
        encoding = remove_gzip2.matcher(encoding).replaceAll("");
        // avoid returning gzip encoding
        
        if (encoding.length() == 0) {
            encoding = null;
        }
        msg.getRequestHeader().setHeader(HttpHeader.ACCEPT_ENCODING,encoding);
        
//        msg.getRequestHeader().setHeader("TE", "chunked;q=0");

    }
    
	protected HttpSender getHttpSender() {

	    if (httpSender == null) {
		    httpSender = new HttpSender(connectionParam, true);
		}

	    return httpSender;
	}

    static boolean isAnyProxyThreadRunning() {
        return !proxyThreadList.isEmpty();
    }
    
    protected boolean isProcessCache(HttpMessage msg) throws IOException {
        if (!parentServer.isEnableCacheProcessing()) {
            return false;
        }
        
        if (parentServer.getCacheProcessingList().isEmpty()) {
            return false;
        }
        
        CacheProcessingItem item = parentServer.getCacheProcessingList().get(0);
        if (msg.equals(item.message)) {
            HttpMessage newMsg = item.message.cloneAll();
            msg.setResponseHeader(newMsg.getResponseHeader());
            msg.setResponseBody(newMsg.getResponseBody());

            httpOut.write(msg.getResponseHeader());
            httpOut.flush();

            if (msg.getResponseBody().length() > 0) {
                httpOut.write(msg.getResponseBody().getBytes());
                httpOut.flush();

            }
            
            return true;
            
        } else {

            try {
                RecordHistory history = Model.getSingleton().getDb().getTableHistory().getHistoryCache(item.reference, msg);
                if (history == null) {
                    return false;
                }
                
                msg.setResponseHeader(history.getHttpMessage().getResponseHeader());
                msg.setResponseBody(history.getHttpMessage().getResponseBody());

                httpOut.write(msg.getResponseHeader());
                httpOut.flush();

                if (msg.getResponseBody().length() > 0) {
                    httpOut.write(msg.getResponseBody().getBytes());                    
                    httpOut.flush();

                }
//                System.out.println("cached:" + msg.getRequestHeader().getURI().toString());
                
                return true;
                
            } catch (Exception e) {
                return true;
            }
            
        }
        
        
//        return false;
        
    }
    
//	protected void setForwardThread(Thread forwardThread) {
//	    this.forwardThread = forwardThread;
//	}

}
