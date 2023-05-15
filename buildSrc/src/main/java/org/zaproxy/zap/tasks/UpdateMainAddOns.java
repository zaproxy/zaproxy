/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
import java.nio.file.Path;
import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.zaproxy.zap.tasks.internal.MainAddOn;
import org.zaproxy.zap.tasks.internal.MainAddOnsData;
import org.zaproxy.zap.tasks.internal.MarketplaceAddOn;
import org.zaproxy.zap.tasks.internal.Utils;

public abstract class UpdateMainAddOns extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getZapVersions();

    @Option(
            option = "zap-versions",
            description = "The file system path to a ZapVersions.xml file.")
    public void setZapVersionsPath(String path) {
        getZapVersions().set(getProject().getParent().file(path));
    }

    @InputFile
    public abstract RegularFileProperty getAddOnsData();

    @OutputFile
    public abstract RegularFileProperty getAddOnsDataUpdated();

    @TaskAction
    public void update() throws IOException {
        Path zapVersions = getZapVersions().getAsFile().get().toPath();
        Map<String, MarketplaceAddOn> marketplace = Utils.getZapVersionsAddOns(zapVersions);
        MainAddOnsData data = Utils.parseData(getAddOnsData().get().getAsFile().toPath());
        updateAddOns(data, marketplace);

        Utils.updateYaml(
                data,
                getAddOnsData().get().getAsFile().toPath(),
                getAddOnsDataUpdated().get().getAsFile().toPath());
    }

    private void updateAddOns(MainAddOnsData data, Map<String, MarketplaceAddOn> marketplace)
            throws IOException {
        for (MainAddOn mainAddOn : data.getAddOns()) {
            MarketplaceAddOn marketplaceAddOn = marketplace.get(mainAddOn.getId());
            if (marketplaceAddOn == null) {
                throw new IOException(
                        "Add-on with ID " + mainAddOn.getId() + " not found in marketplace.");
            }

            mainAddOn.setUrl(marketplaceAddOn.getUrl());
            mainAddOn.setHash(marketplaceAddOn.getHash());
        }
    }
}
