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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.zaproxy.zap.WithConfigsTest;

/** Unit test for {@link DownloadManager}. */
class DownloadManagerUnitTest extends WithConfigsTest {

    private static final String HASH =
            "SHA-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private DownloadManager downloadManager;

    @BeforeEach
    void setUp() throws Exception {
        downloadManager = new DownloadManager(-1);
    }

    @AfterEach
    void tearDown() {
        downloadManager.shutdown(true);
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
    @Timeout(5)
    void shouldDownloadAllFiles() throws Exception {
        // Given
        setFileHandler((msg, file) -> Files.write(file, new byte[0]));
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
                    assertThat(download.getException(), is(nullValue()));
                    assertThat(download.getFinished(), is(not(nullValue())));
                });
    }

    private static URL createDownloadUrl(int i) throws MalformedURLException {
        return new URL("http://127.0.0.1:42/" + i);
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
