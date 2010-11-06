/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.bruteforce;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.DefaultListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpBody;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.utils.SortedListModel;

import com.sittinglittleduck.DirBuster.BaseCase;

public class BruteForce extends Thread implements BruteForceListenner {

	private String site;
	private String fileName;
	private SortedListModel list;
	private boolean stopScan = false;
	private boolean pauseScan = false;
	private boolean unpauseScan = false;
	private boolean isPaused = false;
	private BruteForceListenner listenner;
	private int threads = 0;
	private DirBusterManager manager = null;
	
    private static Log log = LogFactory.getLog(BruteForce.class);

	public BruteForce (String site, String fileName, BruteForceListenner listenner, BruteForceParam bruteForceParam) {
		this.site = site;
		this.fileName = fileName;
		this.listenner = listenner;
		this.threads = bruteForceParam.getThreadPerScan();

		this.list = new SortedListModel();
		log.info("BruteForce : " + site + " threads: " + threads);

		manager = new DirBusterManager(this);
		
		manager.setDefaultNoThreads(threads);
		
		// Set up proxy?
		String hostName = site;
		if (hostName.indexOf(":") > 0) {
			hostName = site.substring(0, hostName.indexOf(":"));
		}
		ConnectionParam conParam = Model.getSingleton().getOptionsParam().getConnectionParam();
		
	    if (conParam.isUseProxy(hostName)) {
			log.debug("BruteForce : set proxy to " + manager.getProxyHost());
			manager.setProxyRealm(Model.getSingleton().getOptionsParam().getConnectionParam().getProxyChainRealm());
	    	manager.setProxyHost(conParam.getProxyChainName());
	    	manager.setProxyPort(conParam.getProxyChainPort());
	    	manager.setProxyUsername(conParam.getProxyChainUserName());
	    	manager.setProxyPassword(conParam.getProxyChainPassword());
			log.debug("BruteForce : set proxy to " + manager.getProxyHost() + ":" + manager.getProxyPort());
	    }

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
        try
        {
            URL targetURL = new URL("http://" + site + "/");
            manager.setTargetURL(targetURL);
            manager.setFileLocation(fileName);
            
            String protocol = targetURL.getProtocol();
            String host = targetURL.getHost();
            int port = targetURL.getPort();
            if (port == -1) {
                port = targetURL.getDefaultPort();
            }
			manager.setAuto(true);
			manager.setHeadLessMode(true);
            
            boolean recursive = true;
			Vector extsVector = new Vector();
			String exts = "php";
			String startPoint = "/";
			manager.setupManager(startPoint, fileName, protocol, host, port, exts, null, threads, true, true, recursive, false, extsVector);
			
			manager.start();
			
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			}

			while(manager.areWorkersAlive() || isPaused) {
				if (stopScan) {
					isPaused = false;
					manager.youAreFinished();
				}
				if (pauseScan) {
					manager.pause();
					pauseScan = false;
					isPaused = true;
				}
				if (unpauseScan) {
					manager.unPause();
					unpauseScan = false;
					isPaused = false;
				}
				//System.out.println("Done so far " +  manager.getTotalDone());
				//System.out.println("Dirs found  " +  manager.getTotalDirsFound());
				//System.out.println("Worker count " +  manager.getWorkerCount());
				
				this.listenner.scanProgress(host, manager.getTotalDone(), manager.getTotal());
				
				try {
					sleep(1000);
				} catch (InterruptedException e) {
				}
			}
        } catch(MalformedURLException ex) {
        	log.error("Failed brute forcing site " + site, ex);
        }
		
		if (this.listenner != null) {
			this.listenner.scanFinshed(site);
		}
		stopScan = true;
		log.info("BruteForce : " + site + " finished");
	}	

	public void stopScan() {
		stopScan = true;
	}

	public boolean isStopped() {
		return stopScan;
	}

	public String getSite() {
		return site;
	}
	
	public int getWorkDone () {
		return this.manager.getTotalDone();
	}

	public int getWorkTotal() {
		return (int)this.manager.getTotal();
	}

	public DefaultListModel getList() {
		return list;
	}

	@Override
	public void scanFinshed(String host) {
		// Ignore
	}

	@Override
	public void scanProgress(String host, int done, int todo) {
		if (this.listenner != null) {
			this.listenner.scanProgress(site, done, todo);
		}
	}

	@Override
	public void foundDir(URL url, int statusCode, String responce,
			String baseCase, String rawResponce, BaseCase baseCaseObj) {
		String reason = "";
		HttpResponseHeader resHeader = null;
		int historyId = -1;

		try {
			// Analyse and store the request
			HttpRequestHeader reqHeader; 
			HttpBody reqBody;
			HttpBody resBody;

			// Manually set up the header the DirBuster code uses
			reqHeader = new HttpRequestHeader(HttpRequestHeader.GET + " " + url.toString() + " " + HttpHeader.HTTP11 + 
					HttpHeader.CRLF + HttpHeader.CRLF);
			
        	reqHeader.setHeader(HttpRequestHeader.HOST, url.getHost() + (url.getPort() > 0? ":" + Integer.toString(url.getPort()):""));
	        reqHeader.setHeader(HttpHeader.USER_AGENT, "DirBuster-0.12 (http://www.owasp.org/index.php/Category:OWASP_DirBuster_Project)");
	        reqHeader.setHeader(HttpHeader.PROXY_CONNECTION,"Keep-Alive");
			reqHeader.setContentLength(0);
			
			reqBody = new HttpBody(null);

			int bodyOffset = rawResponce.indexOf(HttpHeader.CRLF + HttpHeader.CRLF);
			resHeader = new HttpResponseHeader(rawResponce.substring(0, bodyOffset));
			resBody = new HttpBody(rawResponce.substring(bodyOffset + (HttpHeader.CRLF + HttpHeader.CRLF).length()));

			HttpMessage msg = new HttpMessage(reqHeader, reqBody, resHeader, resBody);

			HistoryReference ref = new HistoryReference(Model.getSingleton().getSession(), 
					HistoryReference.TYPE_BRUTE_FORCE, msg);
			historyId = ref.getHistoryId();
			
		} catch (Exception e) {
			log.error("Failed to analyse response from " + url, e);
		}

		if (statusCode == 200) {
			// For some reason 200's dont seem to get parsed successfully
			reason = "OK";
		} else if (resHeader != null) {
			reason = resHeader.getReasonPhrase();
		}

		list.addElement(new BruteForceItem(url.toString(), statusCode, reason, historyId));

	}

	public void pauseScan() {
		this.pauseScan = true;
	}

	public void unpauseScan() {
		this.unpauseScan = true;
	}
	
	public boolean isPaused() {
		return this.isPaused;
	}

}
