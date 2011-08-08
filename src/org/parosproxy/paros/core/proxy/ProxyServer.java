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

package org.parosproxy.paros.core.proxy;
 
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

		thread = new Thread(this);
		thread.setDaemon(true);   
        // the priority below should be higher than normal to allow fast accept on the server socket
   	    thread.setPriority(Thread.NORM_PRIORITY+1);

   	    proxySocket = null;
   	    for (int i=0; i<20 && proxySocket == null; i++) {
   	        try {
   	            
   	            proxySocket = createServerSocket(ip, port);
   	            proxySocket.setSoTimeout(PORT_TIME_OUT);
   	            isProxyRunning = true;
   	            
   	        } catch (Exception e) {
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
		ServerSocket socket = new ServerSocket(port, 400, InetAddress.getByName(ip));

		return socket;
	}
	
	protected ProxyThread createProxyProcess(Socket clientSocket) {
		ProxyThread process = new ProxyThread(this, clientSocket);
		return process;
	}
	
	protected void writeOutput(String s) {
	}
	
	public void addProxyListener(ProxyListener listener) {
		listenerList.add(listener);		
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
		excludeUrls = new ArrayList<Pattern>();
	    for (String url : urls) {
	    	url = url.replaceAll("\\.", "\\\\.");
	    	url = url.replaceAll("\\*",".*?").replaceAll("(;+$)|(^;+)", "");
	    	url = "(" + url.replaceAll(";+", "|") + ")$";
			Pattern p = Pattern.compile(url, Pattern.CASE_INSENSITIVE);
			excludeUrls.add(p);
	    }
	}
	
	public boolean excludeUrl(URI uri) {
		boolean ignore = false;
		if (excludeUrls != null) {
			URI uri2 = (URI)uri.clone();
		    try {
				uri2.setQuery(null);
			} catch (URIException e) {
				log.error(e.getMessage(), e);
			}
			for (Pattern p : excludeUrls) {
				if (p.matcher(uri2.toString()).find()) {
					ignore = true;
					break;
				}
			}
		}
		return ignore;
	}
    
}
