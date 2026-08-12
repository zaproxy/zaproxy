/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.testutils.TestUtils;

class MessagesUnitTest extends TestUtils {

    @BeforeEach
    void setUp() {
        Constant.messages = new I18N(Locale.ENGLISH);
    }

    @AfterEach
    void cleanUp() {
        Constant.messages = null;
    }

    @ParameterizedTest
    @CsvSource({
        "view.options.label.advancedview, Activate Advanced View",
        "view.options.label.showSplashScreen, Show Splash Screen"
    })
    void shouldUseTitleCapitalizationForDisplayOptionLabels(String key, String expected) {
        assertThat(Constant.messages.getString(key), equalTo(expected));
    }
}
