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
// ZAP: 2011/04/16 i18n
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2011/11/15 Warn the user if the host is unknown
// ZAP: 2012/03/15 Changed to sort the ProxyListeners. Set the name of the proxy server thread.
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/05/04 Catch CloneNotSupportedException in excludeUrl() method, as it was introduced in HTTPClient 3.1
// ZAP: 2012/05/06 Use Java-NIO features to create sockets in createServerSocket() method

package org.parosproxy.paros.core.proxy;
 
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpUtil;
import org.parosproxy.paros.view.View;

public class ProxyServer implements Runnable {
	
	protected Thread	thread = null;

	protected final static int PORT_TIME_OUT = 0;
	protected ServerSocket proxySocket = null;
	protected boolean isProxyRunning = false;
	protected ProxyParam proxyParam = new ProxyParam();
	protected ConnectionParam connectionParam = new ConnectionParam();
	protected Vector<ProxyListener> listenerList = new Vector<ProxyListener>();
	// ZAP: Added listenersComparator.
    private static Comparator<ProxyListener> listenersComparator;
	protected boolean serialize = false;
    protected boolean enableCacheProcessing = false;
    protected Vector<CacheProcessingItem> cacheProcessingList = new Vector<CacheProcessingItem>();

    private List<Pattern> excludeUrls = null;
    private static Logger log = Logger.getLogger(ProxyServer.class);

    /**
     * @return Returns the enableCacheProcessing.
     */
    public boolean isEnableCacheProcessing() {
        return enableCacheProcessing;
    }
    /**
     * @param enableCacheProcessing The enableCacheProcessing to set.
     */
    public void setEnableCacheProcessing(boolean enableCacheProcessing) {
        this.enableCacheProcessing = enableCacheProcessing;
        if (!enableCacheProcessing) {
            cacheProcessingList.clear();
        }
    }
    
    /**
     * @return Returns the serialize.
     */
    public boolean isSerialize() {
        return serialize;
    }
	public ProxyServer() {
	}

	public void setProxyParam(ProxyParam param) {
		proxyParam = param;
	}

	public ProxyParam getProxyParam() {
		return proxyParam;
	}
	
	public void setConnectionParam(ConnectionParam connection) {
	    connectionParam = connection;
	}

	public ConnectionParam getConnectionParam() {
	    return connectionParam;
	}
	
	/**
	 * 
	 * @return	true = the server is started successfully.
	 */
	public synchronized int startServer(String ip, int port, boolean isDynamicPort) {

		if (isProxyRunning) {
			stopServer();
		}
	
		isProxyRunning	= false;

		// ZAP: Set the name of the thread.
		thread = new Thread(this, "ZAP-ProxyServer");
		thread.setDaemon(true);   
        // the priority below should be higher than normal to allow fast accept on the server socket
   	    thread.setPriority(Thread.NORM_PRIORITY+1);

   	    proxySocket = null;
   	    for (int i=0; i<20 && proxySocket == null; i++) {
   	        try {
   	            
   	            proxySocket = createServerSocket(ip, port);
   	            proxySocket.setSoTimeout(PORT_TIME_OUT);
   	            isProxyRunning = true;
   	            
   	        } catch(UnknownHostException e) {
            	// ZAP: Warn the user if the host is unknown
            	if (View.isInitialised()) {
            		View.getSingleton().showWarningDialog(Constant.messages.getString("proxy.error.host.unknow") + " " + ip);
            	} else {
            		System.out.println(Constant.messages.getString("proxy.error.host.unknow") + " " + ip);
            	}
            	return -1;
            } catch(Exception e) {
   	            if (!isDynamicPort) {
   	            	// ZAP: Warn the user if we cant listen on the static port
   	            	if (View.isInitialised()) {
   	            		View.getSingleton().showWarningDialog(Constant.messages.getString("proxy.error.port") + " " + port);
   	            	} else {
   	            		System.out.println(Constant.messages.getString("proxy.error.port") + " " + port);
   	            	}
   	                return -1;
   	            } else {
   	                if (port < 65535) {
   	                    port++;
   	                }
   	            }
   	        }
   	        
   	    }

   	    if (proxySocket == null) {
   	        return -1;
   	    }
   	    
		thread.start();

		return proxySocket.getLocalPort();
		
	}

	/**
	 * Stop this server
	 * @return true if server can be stopped.
	 */
	public synchronized boolean stopServer(){

		if (!isProxyRunning) {
			return false;
		}

		isProxyRunning = false;
        HttpUtil.closeServerSocket(proxySocket);

		try {
			thread.join();   //(PORT_TIME_OUT);
		} catch (Exception e) {
		}

		proxySocket = null;

		return true;
	}

	@Override
	public void run() {

		Socket clientSocket = null;
		ProxyThread process = null;

		while (isProxyRunning) {
			try {
				clientSocket = proxySocket.accept();
				process = createProxyProcess(clientSocket);
				process.start();
			} catch (SocketTimeoutException e) {
			    // nothing, socket time reached only.
			} catch (IOException e) {
			    // unknown IO exception - continue but with delay to avoid eating up CPU time if continue
			    try {
                    Thread.sleep(100);
                    //e.printStackTrace();
                } catch (InterruptedException e1) {
                }
			}
			
		}

	}

	protected ServerSocket createServerSocket(String ip, int port) throws UnknownHostException, IOException {
//		ServerSocket socket = new ServerSocket(port, 300, InetAddress.getByName(ip)getProxyParam().getProxyIp()));
		
		// ZAP: use Java-NIO features to create sockets: would allow for more performance with WebSockets
		// ZAP: leaves blocking mode on per default
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.socket().bind(new InetSocketAddress(ip, port), 400);

		return ssc.socket();
	}
	
	protected ProxyThread createProxyProcess(Socket clientSocket) {
		ProxyThread process = new ProxyThread(this, clientSocket);
		return process;
	}
	
	protected void writeOutput(String s) {
	}
	
	public void addProxyListener(ProxyListener listener) {
		listenerList.add(listener);
		// ZAP: Added to sort the listeners.
		Collections.sort(listenerList, getListenersComparator());
	}
	
	public void removeProxyListener(ProxyListener listener) {
		listenerList.remove(listener);
	}
	
	synchronized List<ProxyListener> getListenerList() {
		return listenerList;
	}

    public boolean isAnyProxyThreadRunning() {
        return ProxyThread.isAnyProxyThreadRunning();
    }

    /**
     * @param serialize The serialize to set.
     */
    public void setSerialize(boolean serialize) {
        this.serialize = serialize;
    }

    public void addCacheProcessingList(CacheProcessingItem item) {
        cacheProcessingList.add(item);
    }
    
    Vector<CacheProcessingItem> getCacheProcessingList() {
        return cacheProcessingList;
    }
    
	public void setExcludeList(List<String> urls) {
		excludeUrls = new ArrayList<Pattern>(urls.size());
	    for (String url : urls) {
			Pattern p = Pattern.compile(url, Pattern.CASE_INSENSITIVE);
			excludeUrls.add(p);
	    }
	}
	
	public boolean excludeUrl(URI uri) {
		boolean ignore = false;
		if (excludeUrls != null) {
			String uriString = uri.toString();
			for (Pattern p : excludeUrls) {
				if (p.matcher(uriString).matches()) {
					ignore = true;
					if (log.isDebugEnabled()) {
						log.debug("URL excluded: " + uriString + " Regex: " + p.pattern());
					}
					break;
				}
			}
		}
		return ignore;
	}
    
	// ZAP: Added the method.
	private Comparator<ProxyListener> getListenersComparator() {
		if(listenersComparator == null) {
			createListenersComparator();
		}
		
		return listenersComparator;
	}
	
	// ZAP: Added the method.
	synchronized private void createListenersComparator() {
		if (listenersComparator == null) {
			listenersComparator = new Comparator<ProxyListener>() {
				
				@Override
				public int compare(ProxyListener o1, ProxyListener o2) {
					int order1 = o1.getProxyListenerOrder();
					int order2 = o2.getProxyListenerOrder();
					
					if (order1 < order2) {
						return -1;
					} else if (order1 > order2) {
						return 1;
					}
					
					return 0;
				}
			};
		}
	}
}
