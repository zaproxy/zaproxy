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

import java.util.HashMap;
import java.util.Map;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

/**
 * Task that handles a main release.
 *
 * <p>Sends a repository dispatch to update the marketplace.
 */
public abstract class HandleMainRelease extends SendRepositoryDispatch {

    private Map<String, String> payloadData;

    public HandleMainRelease() {
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
    public abstract Property<String> getVersion();

    private void createPayloadData() {
        payloadData = new HashMap<>();
        payloadData.put("version", getVersion().get());
    }
}
