/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.parosproxy.paros.network.ConnectionParam;
import org.zaproxy.zap.testutils.NanoServerHandler;
import org.zaproxy.zap.testutils.TestUtils;

/** Unit test for {@link DownloadManager}. */
class DownloadManagerUnitTest extends TestUtils {

    private static final String HASH =
            "SHA-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @TempDir static Path tempDir;

    private DownloadManager downloadManager;

    @BeforeEach
    void setUp() throws Exception {
        downloadManager = new DownloadManager(mock(ConnectionParam.class));

        startServer();
    }

    @AfterEach
    void cleanUp() {
        downloadManager.shutdown(true);

        stopServer();
    }

    @Test
    @Timeout(5)
    void shouldShutdown() throws Exception {
        // Given
        downloadManager.start();
        // When
        downloadManager.shutdown(false);
        // Then
        waitDownloadManagerShutdown();
        assertThat(downloadManager.isAlive(), is(equalTo(false)));
    }

    @Test
    @Timeout(30)
    void shouldDownloadAllFiles() throws Exception {
        // Given
        nano.addHandler(
                new NanoServerHandler("/") {
                    @Override
                    protected Response serve(IHTTPSession session) {
                        return newFixedLengthResponse("");
                    }
                });
        downloadManager.start();
        int numberOfDownloads = 1000;
        // When
        for (int i = 0; i < numberOfDownloads; i++) {
            downloadManager.downloadFile(createDownloadUrl(i), createTargetFile(i), 0L, HASH);
        }
        // Then
        waitDownloadManagerFinished();
        List<Downloader> progress = downloadManager.getProgress();
        assertThat(progress, hasSize(numberOfDownloads));
        progress.forEach(
                download -> {
                    assertThat(download.getFinished(), is(not(nullValue())));
                });
    }

    private URL createDownloadUrl(int i) throws MalformedURLException {
        return new URL("http://127.0.0.1:" + nano.getListeningPort() + "/" + i);
    }

    private static File createTargetFile(int i) throws IOException {
        return Files.createTempFile(tempDir, "file" + i, "").toFile();
    }

    private void waitDownloadManagerFinished() throws InterruptedException {
        while (downloadManager.getCurrentDownloadCount() != 0) {
            Thread.sleep(50);
        }
    }

    private void waitDownloadManagerShutdown() throws InterruptedException {
        while (downloadManager.isAlive()) {
            Thread.sleep(50);
        }
    }
}
