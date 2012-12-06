/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 ZAP development team
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

package org.zaproxy.zap.extension.autoupdate;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DownloadManager extends Thread {
	private Logger logger = Logger.getLogger(DownloadManager.class);
	private List<Downloader> currentDownloads = new ArrayList<Downloader>();
	private List<Downloader> completedDownloads = new ArrayList<Downloader>();
	private boolean shutdown = false;
	private boolean cancelDownloads = false;

	public DownloadManager () {
	}
	
	public void downloadFile (URL url, File targetFile, long size) {
		logger.debug("Download file " + url + " to " + targetFile.getAbsolutePath());
		Downloader dl = new Downloader(url, targetFile, size);
		dl.start();
		this.currentDownloads.add(dl);
	}
	
	public void run () {
		while (getCurrentDownlodCount() > 0 || !shutdown) {
			//logger.debug("# downloads " + this.currentDownloads.size() + " shutdown " + shutdown);
			List<Downloader> finishedDownloads = new ArrayList<Downloader>();
			for (Downloader dl : this.currentDownloads) {
				if (!dl.isAlive()) {
					logger.debug("Download finished " + dl.getTargetFile().getAbsolutePath());
					finishedDownloads.add(dl);
				} else if (this.cancelDownloads){
					logger.debug("Cancelling download " + dl.getTargetFile().getAbsolutePath());
					dl.cancelDownload();
				} else {
					logger.debug("Still downloading " + dl.getTargetFile().getAbsolutePath() + " progress % " + dl.getProgressPercent());
				}
			}
			for (Downloader dl : finishedDownloads) {
				this.completedDownloads.add(dl);
				this.currentDownloads.remove(dl);
			}
			try {
				if (getCurrentDownlodCount() > 0) {
					sleep(200);
				} else {
					sleep(1000);
				}
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		logger.debug("Shutdown");
	}
	
	public int getCurrentDownlodCount() {
		return this.currentDownloads.size();
	}
	
	public void shutdown(boolean cancelDownloads) {
		this.shutdown = true;
		this.cancelDownloads = cancelDownloads;
	}
	
	public int getProgressPercent(URL url) throws Exception {
		for (Downloader dl : this.currentDownloads) {
			if (dl.getUrl().equals(url)) {
				if (dl.getException() != null) {
					throw dl.getException();
				}
				return dl.getProgressPercent();
			}
		}
		for (Downloader dl : this.completedDownloads) {
			if (dl.getUrl().equals(url)) {
				if (dl.getException() != null) {
					throw dl.getException();
				}
				return 100;
			}
		}
		return -1;
	}
	
	public List<Downloader> getProgress() {
		List<Downloader> allDownloads = new ArrayList<Downloader>();
		for (Downloader d : this.currentDownloads) {
			allDownloads.add(d);
		}
		for (Downloader d : this.completedDownloads) {
			allDownloads.add(d);
		}
		return allDownloads;
	}

}
