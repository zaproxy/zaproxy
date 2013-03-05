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
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;
import org.zaproxy.zap.utils.SortedListModel;

import com.sittinglittleduck.DirBuster.BaseCase;
import com.sittinglittleduck.DirBuster.ExtToCheck;

public class BruteForce extends Thread implements BruteForceListenner {

	private String site;
	private String fileName;
	private int port = 80;
	private String directory;
	private SortedListModel<BruteForceItem> list;
	private boolean stopScan = false;
	private boolean pauseScan = false;
	private boolean unpauseScan = false;
	private boolean isPaused = false;
	private BruteForceListenner listenner;
	private int threads = 0;
	private boolean recursive = BruteForceParam.DEFAULT_RECURSIVE;
	private DirBusterManager manager = null;
	private List<String> extensions = null;
	
	private boolean onlyUnderDirectory;
	
    private static Logger log = Logger.getLogger(BruteForce.class);

	public BruteForce (String site, String fileName, BruteForceListenner listenner, BruteForceParam bruteForceParam) {
		this.site = site;
		this.fileName = fileName;
		this.directory = null;
		this.listenner = listenner;
		this.threads = bruteForceParam.getThreadPerScan();
		this.recursive = bruteForceParam.getRecursive();
		
		this.onlyUnderDirectory = false;

		this.list = new SortedListModel<>();
		log.info("BruteForce : " + site + "/" + directory + " threads: " + threads);

		manager = new DirBusterManager(this);
		
		manager.setDefaultNoThreads(threads);
		
		// Set up proxy?
		String hostName = site;
		if (hostName.indexOf(":") > 0) {
			hostName = site.substring(0, hostName.indexOf(":"));
			String portStr = site.substring(site.indexOf(":")+1);
			if (portStr.indexOf(" ") > 0) {
				portStr = portStr.substring(0, portStr.indexOf(" "));
			}
			port = Integer.parseInt(portStr);
		}
		ConnectionParam conParam = Model.getSingleton().getOptionsParam().getConnectionParam();
		
	    if (conParam.isUseProxy(hostName)) {
			log.debug("BruteForce : set proxy to " + manager.getProxyHost());
			manager.setProxyRealm(Model.getSingleton().getOptionsParam().getConnectionParam().getProxyChainRealm());
	    	manager.setProxyHost(conParam.getProxyChainName());
	    	manager.setProxyPort(conParam.getProxyChainPort());
	    	manager.setProxyUsername(conParam.getProxyChainUserName());
	    	manager.setProxyPassword(conParam.getProxyChainPassword());
	    	manager.setUseProxy(true);
	    	manager.setUseProxyAuth(true);
			log.debug("BruteForce : set proxy to " + manager.getProxyHost() + ":" + manager.getProxyPort());
	    }
	    
	    if (bruteForceParam.isBrowseFiles()) {
	    	extensions = bruteForceParam.getFileExtensionsList();
	    } else {
	    	extensions = Collections.emptyList();
	    }
	}

    public BruteForce (String site, String fileName, BruteForceListenner listenner, BruteForceParam bruteForceParam, String directory) {
        this(site, fileName, listenner, bruteForceParam);
        this.directory = directory;
        
        if (this.directory != null) {
            this.recursive = false;
            this.onlyUnderDirectory = true;
            
            if (!this.directory.endsWith("/")) {
                this.directory += "/";
            }
        }
    }
    
	@Override
	public void run() {
        try {
        	list.clear();
        	
        	String protocol = "http";
        	if (port == 443 || port == 8443) {
        		protocol = "https";
        	}
            URL targetURL = new URL(protocol + "://" + site + "/");
            manager.setTargetURL(targetURL);
            manager.setFileLocation(fileName);
            
            String host = targetURL.getHost();
			manager.setAuto(true);
			manager.setHeadLessMode(true);
            
			manager.setOnlyUnderStartPoint(onlyUnderDirectory);
			
			
			Vector<ExtToCheck> extsVector = new Vector<>(extensions.size());
			for (String ext: extensions) {
				extsVector.add(new ExtToCheck(ext, true));
			}
			
			String exts = "php";
			String startPoint = "/";
			if (directory != null) {
				startPoint = directory;
			}
			log.debug("BruteForce : starting on http://" + site + startPoint);
			log.debug("BruteForce : file: " + fileName + " recursive=" + recursive);
			manager.setupManager(startPoint, fileName, protocol, host, port, exts, null, threads, true, true, recursive, false, extsVector);
			
			manager.start();
			
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			}

			while( ! manager.hasFinished()) {
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
				//System.out.println("Done " +  manager.getTotalDone() + "/" + manager.getTotal());
				
				this.listenner.scanProgress(host, port, manager.getTotalDone(), manager.getTotal());
				
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
		return this.manager.getTotal();
	}

	public DefaultListModel<BruteForceItem> getList() {
		return list;
	}
	
	public void clearList() {
		if (this.list != null) {
			this.list.clear();
		}
	}
		

	@Override
	public void scanFinshed(String host) {
		// Ignore
	}

	@Override
	public void scanProgress(String host, int port, int done, int todo) {
		if (this.listenner != null) {
			this.listenner.scanProgress(site, port, done, todo);
		}
	}

	@Override
	public void foundDir(URL url, int statusCode, String response,
			String baseCase, String rawResponse, BaseCase baseCaseObj) {
		String reason = "";
		HttpResponseHeader resHeader = null;
		int historyId = -1;

		try {
			// Analyse and store the request
			HttpRequestHeader reqHeader; 
			HttpRequestBody reqBody;
			HttpResponseBody resBody;

			// Manually set up the header the DirBuster code uses
			reqHeader = new HttpRequestHeader(HttpRequestHeader.GET + " " + url.toString() + " " + HttpHeader.HTTP11 + 
					HttpHeader.CRLF + HttpHeader.CRLF);
			
        	reqHeader.setHeader(HttpRequestHeader.HOST, url.getHost() + (url.getPort() > 0? ":" + Integer.toString(url.getPort()):""));
	        reqHeader.setHeader(HttpHeader.USER_AGENT, "DirBuster-0.12 (http://www.owasp.org/index.php/Category:OWASP_DirBuster_Project)");
	        reqHeader.setHeader(HttpHeader.PROXY_CONNECTION,"Keep-Alive");
			reqHeader.setContentLength(0);
			
			reqBody = new HttpRequestBody();

			int bodyOffset = rawResponse.indexOf(HttpHeader.CRLF + HttpHeader.CRLF);
			resHeader = new HttpResponseHeader(rawResponse.substring(0, bodyOffset));
			resBody = new HttpResponseBody(rawResponse.substring(bodyOffset + (HttpHeader.CRLF + HttpHeader.CRLF).length()));

			HttpMessage msg = new HttpMessage(reqHeader, reqBody, resHeader, resBody);

			HistoryReference ref = new HistoryReference(Model.getSingleton().getSession(), 
					HistoryReference.TYPE_BRUTE_FORCE, msg);
			historyId = ref.getHistoryId();
			
			// Add to the sites tree
			SiteNode sn = Model.getSingleton().getSession().getSiteTree().addPath(ref);
			sn.addCustomIcon(ExtensionBruteForce.HAMMER_ICON_RESOURCE, true);
			
			
		} catch (Exception e) {
			log.error("Failed to analyse response from " + url, e);
		}

		if (statusCode == 200) {
			// For some reason 200's dont seem to get parsed successfully
			reason = "OK";
		} else if (resHeader != null) {
			reason = resHeader.getReasonPhrase();
		}

		BruteForceItem bfi = new BruteForceItem(url.toString(), statusCode, reason, historyId);
		if (! list.contains(bfi)) {
			list.addElement(bfi);
		}

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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setOnlyUnderDirectory(boolean onlyUnderDirectory) {
	    this.onlyUnderDirectory = onlyUnderDirectory;
	    
	    if (onlyUnderDirectory) {
	        this.recursive = true;
	    }
	}

}
