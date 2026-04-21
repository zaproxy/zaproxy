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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.apache.commons.configuration.FileConfiguration;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link OptionsParamCheckForUpdates}. */
class OptionsParamCheckForUpdatesUnitTest {

    @Test
    void shouldNotHaveConfigByDefault() {
        // Given / When
        OptionsParamCheckForUpdates param = new OptionsParamCheckForUpdates();
        // Then
        assertThat(param.getConfig()).isEqualTo(null);
    }

    @Test
    void shouldParseEmptyConfig() {
        // Given
        OptionsParamCheckForUpdates param = new OptionsParamCheckForUpdates();
        FileConfiguration config = new ZapXmlConfiguration();
        // When
        param.load(config);
        // Then
        assertThat(param.isCheckOnStart()).isTrue();
        assertThat(param.getDayLastChecked()).isNull();
        assertThat(param.getDayLastInstallWarned()).isNull();
        assertThat(param.getDayLastUpdateWarned()).isNull();
        assertThat(param.isDownloadNewRelease()).isFalse();
        assertThat(param.isCheckAddonUpdates()).isTrue();
        assertThat(param.isInstallAddonUpdates()).isFalse();
        assertThat(param.isInstallScannerRules()).isFalse();
        assertThat(param.isReportReleaseAddons()).isFalse();
        assertThat(param.isReportBetaAddons()).isFalse();
        assertThat(param.isReportAlphaAddons()).isFalse();
        assertThat(param.getAddonDirectories()).isEmpty();
        assertThat(param.getDownloadDirectory()).isEqualTo(new File(Constant.FOLDER_LOCAL_PLUGIN));
    }
}
