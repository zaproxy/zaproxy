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
package org.zaproxy.zap.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

/**
 * Task that handles a weekly release.
 *
 * <p>Sends a repository dispatch to update the marketplace.
 */
public abstract class HandleWeeklyRelease extends SendRepositoryDispatch {

    private static final String HTTPS_SCHEME = "HTTPS";

    private Map<String, String> payloadData;

    public HandleWeeklyRelease() {
        getClientPayload()
                .set(
                        getProject()
                                .provider(
                                        () -> {
                                            if (payloadData == null) {
                                                createPayloadData();
                                            }
                                            return payloadData;
                                        }));
    }

    @Input
    public abstract Property<String> getDownloadUrl();

    @Input
    public abstract Property<String> getChecksumAlgorithm();

    private void createPayloadData() {
        String checksum;
        try {
            checksum = createChecksum(getChecksumAlgorithm().get(), downloadRelease());
        } catch (IOException e) {
            throw new BuildException(e);
        }

        payloadData = new HashMap<>();
        payloadData.put("url", getDownloadUrl().get());
        payloadData.put("checksum", checksum);
    }

    private Path downloadRelease() throws IOException {
        String urlString = getDownloadUrl().get();
        URL url = new URL(urlString);
        if (!HTTPS_SCHEME.equalsIgnoreCase(url.getProtocol())) {
            throw new IllegalArgumentException(
                    "The provided URL does not use HTTPS scheme: " + url.getProtocol());
        }

        Path release = getTemporaryDir().toPath().resolve(extractFileName(urlString));
        if (Files.exists(release)) {
            getLogger().info("File already exists, skipping download.");
            return release;
        }

        try (InputStream in = url.openStream()) {
            Files.copy(in, release);
        } catch (IOException e) {
            throw new IOException("Failed to download the file: " + e.getMessage(), e);
        }
        getLogger().info("File downloaded to: " + release);
        return release;
    }

    private static String extractFileName(String url) {
        int idx = url.lastIndexOf("/");
        if (idx == -1) {
            throw new IllegalArgumentException(
                    "The provided URL does not have a file name: " + url);
        }
        return url.substring(idx + 1);
    }

    private static String createChecksum(String algorithm, Path file) throws IOException {
        return new DigestUtils(algorithm).digestAsHex(file.toFile());
    }
}
