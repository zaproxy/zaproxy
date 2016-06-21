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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;
import org.zaproxy.zap.utils.HashUtils;

public class Downloader extends Thread {
	private URL url;
	private Proxy proxy;
	private File targetFile;
	private Exception exception = null;
	private long size = 0;
	private boolean complete = false;
	private Date started = null;
	private Date finished = null;
	private boolean cancelDownload = false;
	private String hash = null;
	private boolean validated = false;

	private static final Logger logger = Logger.getLogger(Downloader.class);

	public Downloader(URL url, Proxy proxy, File targetFile, String hash) {
		this (url, proxy, targetFile, 0, hash);
	}

	public Downloader(URL url, Proxy proxy, File targetFile, long size, String hash) {
		super();
		this.url = url;
		this.proxy = proxy;
		this.targetFile = targetFile;
		this.size = size;
		this.hash = hash;
	}

	@Override
	public void run() {
		this.started = new Date();

		if (hash != null) {
			if (hash.indexOf(":") > 0) {
				downloadFile();
				if (!cancelDownload) {
					validateHashDownload();
				}
			} else {
				logger.debug("Not downloading file, hash field does not have valid content (\"<ALGORITHM>:<HASH>\"): " + hash);
			}
		} else {
			logger.debug("Not downloading file, does not have a hash: " + url);
		}
	    
	    this.complete = true;
		this.finished = new Date();
		if (cancelDownload) {
			this.targetFile.delete();
		}
	}

	private void downloadFile() {
    	BufferedInputStream in = null;
    	FileOutputStream out = null;
	    try {
	    	/*
	    	 * The following code may be more efficient, but doesnt give us the option
	    	 * of cancelling downloads.
	    	 *
			 * ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			 * FileOutputStream fos = new FileOutputStream(targetFile);
			 * fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	    	 */

			// XXX Change to use HttpClient instead of URL to download the file. The java.net.Authenticator is shared by all
			// the URLConnection, it may be changed by 3rd party add-ons/libraries and it can't be set on a single connection
			// (see bug 4941958 [1]) in which case the authentication will not succeed (hence the file will not be downloaded).
			//
			// [1] http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4941958
			
	    	in = new BufferedInputStream(url.openConnection(proxy).getInputStream());
	    	out = new FileOutputStream(this.targetFile);
	        byte[] data = new byte[1024];
	        int count;
	        while(! cancelDownload && (count = in.read(data,0,1024)) != -1) {
	            out.write(data, 0, count);
	        }
		} catch (Exception e) {
			this.exception = e;
	    } finally {
	    	try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				// Ignore
			}
		}
	}

	private void validateHashDownload() {
		try {
			String algorithm = hash.substring(0, hash.indexOf(":"));
			String hashValue = hash.substring(hash.indexOf(":") + 1);
			String realHash = HashUtils.getHash(targetFile, algorithm);
			if (realHash.equalsIgnoreCase(hashValue)) {
		    	validated = true;
			} else {
				logger.debug("Wrong hash - expected " + hashValue + " got " + realHash);
			}
		} catch (Exception e) {
			// Ignore - we default to unvalidated
			logger.debug("Error checking hash", e);
		}
	}

	public void cancelDownload() {
		this.cancelDownload = true;
		if (complete && this.targetFile.exists()) {
			this.targetFile.delete();
		}
	}

	public Exception getException() {
		return exception;
	}

	public URL getUrl() {
		return url;
	}

	public File getTargetFile() {
		return targetFile;
	}

	public int getProgressPercent() {
		if (complete) {
			return 100;
		}
		if (this.size == 0) {
			return 0;
		}
		return (int)(this.targetFile.length() * 100 / this.size);
	}

	public Date getStarted() {
		return started;
	}

	public Date getFinished() {
		return finished;
	}

	public String getHash() {
		return hash;
	}

	public boolean isValidated() {
		return validated;
	}

}
