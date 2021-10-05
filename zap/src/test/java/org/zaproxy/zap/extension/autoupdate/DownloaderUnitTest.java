/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit test for {@link Downloader}. */
class DownloaderUnitTest {

    private static final String FILE_CONTENTS = "0123456789ABCDEF";

    @TempDir static Path tempDir;

    private static URL downloadUrl;

    @BeforeAll
    static void setUp() throws IOException {
        Path file = Files.createTempFile(tempDir, "file", "");
        Files.write(file, FILE_CONTENTS.getBytes(StandardCharsets.UTF_8));
        downloadUrl = file.toUri().toURL();
    }

    @Test
    void shouldValidateDownloadWithEqualSha1Hash() throws Exception {
        // Given
        String hash = "SHA1:ce27cb141098feb00714e758646be3e99c185b71";
        Downloader downloader = noProxyDownloader(downloadUrl, hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish(downloader);
        assertThat(downloader.isValidated(), is(equalTo(true)));
    }

    @Test
    void shouldNotValidateDownloadWithDifferentSha1Hash() throws Exception {
        // Given
        String hash = "SHA1:ce27cb";
        Downloader downloader = noProxyDownloader(downloadUrl, hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish(downloader);
        assertThat(downloader.isValidated(), is(equalTo(false)));
    }

    @Test
    void shouldValidateDownloadWithEqualSha256Hash() throws Exception {
        // Given
        String hash = "SHA-256:2125b2c332b1113aae9bfc5e9f7e3b4c91d828cb942c2df1eeb02502eccae9e9";
        Downloader downloader = noProxyDownloader(downloadUrl, hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish(downloader);
        assertThat(downloader.isValidated(), is(equalTo(true)));
    }

    @Test
    void shouldNotValidateDownloadWithDifferentSha256Hash() throws Exception {
        // Given
        String hash = "SHA-256:2125bc";
        Downloader downloader = noProxyDownloader(downloadUrl, hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish(downloader);
        assertThat(downloader.isValidated(), is(equalTo(false)));
    }

    @Test
    void shouldNotValidateDownloadWithUnsupportedAlgorithm() throws Exception {
        // Given
        String hash = "~NotHashAlg~:ce27cb141098feb00714e758646be3e99c185b71";
        Downloader downloader = noProxyDownloader(downloadUrl, hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish(downloader);
        assertThat(downloader.isValidated(), is(equalTo(false)));
    }

    private static Downloader noProxyDownloader(URL url, String hash) throws IOException {
        return new Downloader(
                url, Proxy.NO_PROXY, Files.createTempFile(tempDir, "download", "").toFile(), hash);
    }

    private static void waitDownloadFinish(Downloader downloader) throws InterruptedException {
        int i = 1;
        while (downloader.getFinished() == null) {
            Thread.sleep(50);
            if (i == 200) {
                break;
            }
            i++;
        }
    }
}
