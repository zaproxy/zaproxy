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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.ConnectionParam;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.utils.SortedListModel;

public class PortScan extends ScanThread implements ScanListenner {

	private String site;
	private SortedListModel<Integer> list = new SortedListModel<>();
	private boolean stopScan = false;
	private boolean pauseScan = false;
	private boolean unpauseScan = false;
	private boolean isPaused = false;
	private ScanListenner listenner;
	private int maxPort = 0;
	private int threads = 0;
	private int threadIndex = -1;
	private int port = 0;
	private int progress = 0;
	private int timeout = 0;
	private boolean useProxy = true;
	private List<PortScan> subThreads = new ArrayList<>();
	
    private static Logger log = Logger.getLogger(PortScan.class);

	public PortScan (String site, ScanListenner listenner, PortScanParam portScanParam) {
		super(site, listenner);
		this.site = site;
		this.listenner = listenner;
		this.maxPort = portScanParam.getMaxPort();
		this.threads = portScanParam.getThreadPerScan();
		this.timeout = portScanParam.getTimeoutInMs();
		this.useProxy = portScanParam.isUseProxy();

		log.debug("PortScan : " + site + " threads: " + threads);
	}
	
	private PortScan (String site, ScanListenner listenner, SortedListModel<Integer> list, int maxPort, int threads, int threadIndex) {
		super(site, listenner);
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
		list.clear();

		stopScan = false;
		int startPort = threadIndex;
		if (startPort < 1) {
			startPort = 1;
		}

		ConnectionParam connParams = Model.getSingleton().getOptionsParam().getConnectionParam();
		SocketAddress sa = new InetSocketAddress(connParams.getProxyChainName(), connParams.getProxyChainPort());
		final java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.SOCKS, sa);

		for (port = startPort; port < maxPort; port += threads) {
			try {
				if (pauseScan) {
					pauseScan = false;
					isPaused = true;
					for (PortScan ps : subThreads) {
						ps.pauseScan();
					}
					while (! stopScan && ! unpauseScan) {
						try {
							sleep (500);
						} catch (InterruptedException e) {
							// Ignore
						}
					}
					isPaused = false;
					for (PortScan ps : subThreads) {
						ps.resumeScan();
					}
				}
				if (stopScan) {
					log.debug("Scanned stopped");
					break;
				}
				if (this.listenner != null) {
					this.listenner.scanProgress(site, port, maxPort);
				}

				Socket s = null;
				if (useProxy && Model.getSingleton().getOptionsParam().getConnectionParam().isUseProxy(site)) {
					
					FutureTask<Integer> ft = new FutureTask<>(new Callable<Integer>() {
						@Override
						public Integer call() {
							Socket s = new Socket(proxy);
							SocketAddress endpoint = new InetSocketAddress(site, port);
							try {
								s.connect(endpoint, timeout);
								s.close();
							} catch (IOException e) {
								return null;
							}
							return port;
							
						}});
					new Thread(ft).start();
					try {
						ft.get(2, TimeUnit.SECONDS);
					} catch (Exception e) {
						ft.cancel(true);
						throw new IOException();
					}
					
				} else {
					// Not using a proxy
					s = new Socket();
					s.connect(new InetSocketAddress(site, port), timeout);
					s.close();
				}
				log.debug("Site : " + site + " open port: " + port);
				synchronized (list) {
					list.addElement(port);
				}
			} catch (IOException ex) {
				// The host is not listening on this port
			}
		}
		Date stop = new Date();
		log.debug("Finished scan on " + site + " at " + stop);
		log.debug("Took " + ((stop.getTime() - start.getTime())/60000) + " mins " );
	}

	private void runSubThreads() {
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
				if (pauseScan) {
					unpauseScan = false;
					st.pauseScan();
				}
				if (unpauseScan) {
					pauseScan = false;
					st.resumeScan();
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

	@Override
	public void stopScan() {
		stopScan = true;
	}

	@Override
	public boolean isStopped() {
		return stopScan;
	}
	
	@Override
	public boolean isRunning() {
		return this.isAlive();
	}

	@Override
	public String getSite() {
		return site;
	}
	
	@Override
	public int getProgress () {
		return progress;
	}

	 int getMaxPort() {
		return this.maxPort;
	}

	@Override
	public DefaultListModel<Integer> getList() {
		return list;
	}

	@Override
	public void scanFinshed(String host) {
		// Ignore
	}

	@Override
	public void scanProgress(String host, int progress, int maximum) {
		if (progress > this.progress) {
			this.progress = progress;
			this.listenner.scanProgress(site, progress, maximum);
		}
	}

	@Override
	public void pauseScan() {
		this.pauseScan = true;
		this.unpauseScan = false;
		this.isPaused = true;
	}

	@Override
	public void resumeScan() {
		this.unpauseScan = true;
		this.pauseScan = false;
		this.isPaused = false;
	}
	
	@Override
	public boolean isPaused() {
		return this.isPaused;
	}

	@Override
	public int getMaximum() {
		return maxPort;
	}

	@Override
	public void reset() {
		this.list = new SortedListModel<>();
	}

	@Override
	public void setJustScanInScope(boolean scanInScope) {
		// Dont support
	}

	@Override
	public boolean getJustScanInScope() {
		// Dont support
		return false;
	}

	@Override
	public void setScanChildren(boolean scanChildren) {
		// Dont support
	}

	@Override
	public void setScanContext(Context context) {
		// Don't support			
	}

}
