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
package org.zaproxy.zap.extension.portscan;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zaproxy.zap.utils.SortedListModel;

public class PortScan extends Thread implements PortScanListenner {

	private String site;
	private SortedListModel list;
	private boolean stopScan = false;
	private PortScanListenner listenner;
	private int maxPort = 0;
	private int threads = 0;
	private int threadIndex = -1;
	private int port = 0;
	private int progress = 0;
	
    private static Log log = LogFactory.getLog(PortScan.class);

	public PortScan (String site, PortScanListenner listenner, PortScanParam portScanParam) {
		this.site = site;
		this.listenner = listenner;
		this.maxPort = portScanParam.getMaxPort();
		this.threads = portScanParam.getThreadPerScan();

		this.list = new SortedListModel();
		log.debug("PortScan : " + site + " threads: " + threads);
	}
	
	private PortScan (String site, PortScanListenner listenner, SortedListModel list, int maxPort, int threads, int threadIndex) {
		this.site = site;
		this.listenner = listenner;
		this.maxPort = maxPort;
		this.threads = threads;
		this.threadIndex = threadIndex;

		this.list = list;
		log.debug("PortScan : " + site + " threads: " + threads + " threadIndex: " + threadIndex);
	}
	
	@Override
	public void run() {
		if (threads > 1 && threadIndex == -1) {
			// Start the sub threads
			runSubThreads();
		} else {
			// This is a sub thread
			runScan();
		}
		if (this.listenner != null) {
			this.listenner.scanFinshed(site);
		}
		stopScan = true;
	}
	
	private void runScan() {
		// Do the scan
		// If there are multiple sub threads then they will start at a different point
		Date start = new Date();
		log.debug("Starting scan on " + site + " at " + start);

		stopScan = false;
		int startPort = threadIndex;
		if (startPort < 1) {
			startPort = 1;
		}

		for (port = startPort; port < maxPort; port += threads) {
			try {
				if (stopScan) {
					log.debug("Scanned stopped");
					break;
				}
				if (this.listenner != null) {
					this.listenner.scanProgress(site, port);
				}
				Socket s = new Socket(site, port);
				log.debug("Site : " + site + " open port: " + port);
				synchronized (list) {
					list.addElement(port);
				}
				s.close();
			} catch (IOException ex) {
				// The host is not listening on this port
			}
		}
		Date stop = new Date();
		log.debug("Finished scan on " + site + " at " + stop);
		log.debug("Took " + ((stop.getTime() - start.getTime())/60000) + " mins " );
	}

	private void runSubThreads() {
		List<PortScan> subThreads = new ArrayList<PortScan>();
		for (int i=0; i < threads; i++) {
			PortScan ps = new PortScan(site, this, list, maxPort, threads, i+1);
			subThreads.add(ps);
			ps.start();
		}
		boolean running = true;
		while (running) {
			running = false;
			for (PortScan st : subThreads) {
				if (stopScan) {
					st.stopScan();
				}
				if (st.isAlive()) {
					running = true;
				}
			}
			if (running) {
				try {
					sleep (500);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		}
		
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
	
	public int getProgress () {
		return port;
	}

	 int getMaxPort() {
		return this.maxPort;
	}

	public DefaultListModel getList() {
		return list;
	}

	@Override
	public void scanFinshed(String host) {
		// Ignore
	}

	@Override
	public void scanProgress(String host, int progress) {
		if (progress > this.progress) {
			this.progress = progress;
			this.listenner.scanProgress(site, progress);
		}
	}

}
