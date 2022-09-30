/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.autoupdate;

import java.io.File;
import java.net.Proxy;
import java.net.URL;
import java.util.Date;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.utils.HashUtils;

public class Downloader extends Thread {
    private URL url;
    private File targetFile;
    private Exception exception = null;
    private long size = 0;
    private boolean complete = false;
    private Date started = null;
    private Date finished = null;
    private boolean cancelDownload = false;
    private String hash = null;
    private boolean validated = false;
    private final int initiator;

    private static final Logger logger = LogManager.getLogger(Downloader.class);

    /** @deprecated (2.12.0) */
    @Deprecated
    public Downloader(URL url, Proxy proxy, File targetFile, String hash) {
        this(url, proxy, targetFile, 0, hash);
    }

    /** @deprecated (2.12.0) */
    @Deprecated
    public Downloader(URL url, Proxy proxy, File targetFile, long size, String hash) {
        this(url, targetFile, 0, hash, HttpSender.CHECK_FOR_UPDATES_INITIATOR);
    }

    Downloader(URL url, File targetFile, long size, String hash, int initiator) {
        super();
        this.url = url;
        this.targetFile = targetFile;
        this.size = size;
        this.hash = hash;
        this.initiator = initiator;
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
                logger.debug(
                        "Not downloading file, hash field does not have valid content (\"<ALGORITHM>:<HASH>\"): {}",
                        hash);
            }
        } else {
            logger.debug("Not downloading file, does not have a hash: {}", url);
        }

        this.complete = true;
        this.finished = new Date();
        if (cancelDownload) {
            this.targetFile.delete();
        }
    }

    private void downloadFile() {
        try {
            HttpSender sender = new HttpSender(initiator);
            sender.setFollowRedirect(true);
            HttpMessage message = new HttpMessage(new URI(url.toString(), true));
            sender.sendAndReceive(message, targetFile.toPath());
        } catch (Exception e) {
            this.exception = e;
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
                logger.debug("Wrong hash - expected {} got {}", hashValue, realHash);
            }
        } catch (Exception e) {
            // Ignore - we default to unvalidated
            logger.debug("Error checking hash", e);
        }
    }

    boolean isCancelled() {
        return cancelDownload;
    }

    public void cancelDownload() {
        this.cancelDownload = true;
        interrupt();
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
        return (int) (this.targetFile.length() * 100 / this.size);
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
