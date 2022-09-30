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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;

public class DownloadManager extends Thread {
    private static final Logger logger = LogManager.getLogger(DownloadManager.class);
    private final int initiator;
    private Collection<Downloader> currentDownloads = new ConcurrentLinkedQueue<>();
    private Collection<Downloader> completedDownloads = new ConcurrentLinkedQueue<>();
    private boolean shutdown = false;
    private boolean cancelDownloads = false;

    /** @deprecated (2.12.0) */
    @Deprecated
    public DownloadManager(org.parosproxy.paros.network.ConnectionParam connectionParam) {
        this(HttpSender.CHECK_FOR_UPDATES_INITIATOR);
    }

    DownloadManager(int initiator) {
        super("ZAP-DownloadManager");
        setDaemon(true);
        this.initiator = initiator;
    }

    public Downloader downloadFile(URL url, File targetFile, long size, String hash) {
        logger.debug("Download file {} to {}", url, targetFile.getAbsolutePath());

        Downloader dl = new Downloader(url, targetFile, size, hash, initiator);
        dl.start();
        this.currentDownloads.add(dl);
        return dl;
    }

    @Override
    public void run() {
        while (getCurrentDownloadCount() > 0 || !shutdown) {
            // logger.debug("# downloads " + this.currentDownloads.size() + " shutdown " +
            // shutdown);
            List<Downloader> finishedDownloads = new ArrayList<>();
            for (Downloader dl : this.currentDownloads) {
                if (!dl.isAlive()) {
                    if (dl.getException() != null) {
                        logger.debug("Download failed {}", dl.getTargetFile().getAbsolutePath());
                    } else if (dl.isValidated()) {
                        logger.debug("Download finished {}", dl.getTargetFile().getAbsolutePath());
                    } else if (dl.isCancelled()) {
                        logger.debug("Download cancelled {}", dl.getTargetFile().getAbsolutePath());
                    } else {
                        // Corrupt or corrupted file? Pretty bad anyway
                        logger.error("Validation failed {}", dl.getTargetFile().getAbsolutePath());
                        dl.cancelDownload();
                        if (View.isInitialised()) {
                            View.getSingleton()
                                    .showWarningDialog(
                                            Constant.messages.getString(
                                                    "cfu.warn.badhash",
                                                    dl.getTargetFile().getName()));
                        }
                    }
                    finishedDownloads.add(dl);
                } else if (this.cancelDownloads) {
                    logger.debug("Cancelling download {}", dl.getTargetFile().getAbsolutePath());
                    dl.cancelDownload();
                } else {
                    logger.debug(
                            "Still downloading {} progress % {}",
                            dl.getTargetFile().getAbsolutePath(), dl.getProgressPercent());
                }
            }
            for (Downloader dl : finishedDownloads) {
                this.completedDownloads.add(dl);
                this.currentDownloads.remove(dl);
            }
            try {
                if (getCurrentDownloadCount() > 0) {
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

    public int getCurrentDownloadCount() {
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
        List<Downloader> allDownloads = new ArrayList<>();
        for (Downloader d : this.currentDownloads) {
            allDownloads.add(d);
        }
        for (Downloader d : this.completedDownloads) {
            allDownloads.add(d);
        }
        return allDownloads;
    }
}
