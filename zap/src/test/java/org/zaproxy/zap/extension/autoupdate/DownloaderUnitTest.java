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

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.testutils.NanoServerHandler;

/** Unit test for {@link Downloader}. */
class DownloaderUnitTest extends WithConfigsTest {

    private static final String FILE_CONTENTS = "0123456789ABCDEF";
    private static final int FILE_LENGTH = FILE_CONTENTS.length();

    private static final String FILE_PATH = "/file.txt";
    private NanoServerHandler defaultHandler;

    private URL downloadUrl;
    private Downloader downloader;

    @BeforeEach
    void setUp() throws IOException {
        startServer();

        defaultHandler =
                new NanoServerHandler(FILE_PATH) {
                    @Override
                    protected Response serve(IHTTPSession session) {
                        return newFixedLengthResponse(FILE_CONTENTS);
                    }
                };
        nano.addHandler(defaultHandler);

        downloadUrl = new URL("http://127.0.0.1:" + nano.getListeningPort() + FILE_PATH);
    }

    @AfterEach
    void tearDown() {
        stopServer();
    }

    void createDowloader(String hash) throws IOException {
        createDowloader(hash, FILE_LENGTH);
    }

    void createDowloader(String hash, long size) throws IOException {
        downloader =
                new Downloader(
                        downloadUrl,
                        Files.createTempFile(tempDir, "download", "").toFile(),
                        size,
                        hash,
                        -1);
    }

    @Test
    void shouldValidateDownloadWithEqualSha1Hash() throws Exception {
        // Given
        String hash = "SHA1:ce27cb141098feb00714e758646be3e99c185b71";
        createDowloader(hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish();
        assertThat(downloader.isValidated(), is(equalTo(true)));
        assertFileContents();
    }

    @Test
    void shouldNotValidateDownloadWithDifferentSha1Hash() throws Exception {
        // Given
        String hash = "SHA1:ce27cb";
        createDowloader(hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish();
        assertThat(downloader.isValidated(), is(equalTo(false)));
    }

    @Test
    void shouldValidateDownloadWithEqualSha256Hash() throws Exception {
        // Given
        String hash = "SHA-256:2125b2c332b1113aae9bfc5e9f7e3b4c91d828cb942c2df1eeb02502eccae9e9";
        createDowloader(hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish();
        assertThat(downloader.isValidated(), is(equalTo(true)));
        assertFileContents();
    }

    @Test
    void shouldNotValidateDownloadWithDifferentSha256Hash() throws Exception {
        // Given
        String hash = "SHA-256:2125bc";
        createDowloader(hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish();
        assertThat(downloader.isValidated(), is(equalTo(false)));
    }

    @Test
    void shouldNotValidateDownloadWithUnsupportedAlgorithm() throws Exception {
        // Given
        String hash = "~NotHashAlg~:ce27cb141098feb00714e758646be3e99c185b71";
        createDowloader(hash);
        // When
        downloader.start();
        // Then
        waitDownloadFinish();
        assertThat(downloader.isValidated(), is(equalTo(false)));
    }

    @Test
    void shouldDownloadBigFile() throws Exception {
        // Given
        long size = 10485760L;
        nano.removeHandler(defaultHandler);
        nano.addHandler(
                new NanoServerHandler(FILE_PATH) {
                    @Override
                    protected Response serve(IHTTPSession session) {
                        return newFixedLengthResponse(
                                Status.OK,
                                NanoHTTPD.MIME_PLAINTEXT,
                                new InputStream() {
                                    @Override
                                    public int read() throws IOException {
                                        return 'A';
                                    }
                                },
                                size);
                    }
                });
        createDowloader(
                "SHA-256:eb6183addde05c2196ce25e6fa34a4baf20f9bf30d33892f452a9a1e88c9a472", size);
        // When
        downloader.start();
        // Then
        waitDownloadFinish();
        assertThat(downloader.getException(), is(nullValue()));
        assertThat(downloader.getFinished(), is(not(nullValue())));
        assertThat(downloader.isValidated(), is(equalTo(true)));
        assertThat(downloader.getTargetFile().length(), is(equalTo(size)));
    }

    @Test
    @Timeout(25)
    void shouldCancelDownload() throws Exception {
        // Given
        nano.removeHandler(defaultHandler);
        nano.addHandler(
                new NanoServerHandler(FILE_PATH) {
                    @Override
                    protected Response serve(IHTTPSession session) {
                        return newFixedLengthResponse(
                                Status.OK,
                                NanoHTTPD.MIME_PLAINTEXT,
                                new InputStream() {
                                    @Override
                                    public int read() throws IOException {
                                        return 'A';
                                    }
                                },
                                Integer.MAX_VALUE);
                    }
                });
        createDowloader(
                "SHA-256:eb6183addde05c2196ce25e6fa34a4baf20f9bf30d33892f452a9a1e88c9a472",
                Integer.MAX_VALUE);
        // When
        downloader.start();
        downloader.cancelDownload();
        // Then
        waitDownloadFinish();
        assertThat(downloader.getException(), is(instanceOf(ClosedByInterruptException.class)));
        assertThat(downloader.getFinished(), is(not(nullValue())));
        assertThat(downloader.isValidated(), is(equalTo(false)));
        assertThat(Files.notExists(downloader.getTargetFile().toPath()), is(equalTo(true)));
    }

    void assertFileContents() throws IOException {
        Path file = downloader.getTargetFile().toPath();
        String downloadedContents = new String(Files.readAllBytes(file), StandardCharsets.US_ASCII);
        assertThat(downloadedContents, is(equalTo(FILE_CONTENTS)));
    }

    private void waitDownloadFinish() throws InterruptedException {
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
