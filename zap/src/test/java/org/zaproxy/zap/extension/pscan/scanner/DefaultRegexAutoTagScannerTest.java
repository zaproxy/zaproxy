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
package org.zaproxy.zap.extension.pscan.scanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.InputStream;
import java.util.stream.Stream;
import net.htmlparser.jericho.Source;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanParam;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

@Timeout(6)
class DefaultRegexAutoTagScannerTest {

    private static final String BASE_STRING = "lorem ipsum < type= href= ";

    private static Source source;
    private static HttpMessage message;
    private static PassiveScanParam passiveScanParam;

    @BeforeAll
    static void beforeAll() throws Exception {
        passiveScanParam = new PassiveScanParam();
        try (InputStream is =
                DefaultRegexAutoTagScannerTest.class.getResourceAsStream(
                        "/org/zaproxy/zap/resources/config.xml")) {
            passiveScanParam.load(new ZapXmlConfiguration(is));
        }

        StringBuilder strBuilder = new StringBuilder(16_000_000);
        int count = strBuilder.capacity() / BASE_STRING.length();
        for (int i = 0; i < count; i++) {
            strBuilder.append(BASE_STRING);
        }
        String body = strBuilder.toString();
        source = new Source(body);
        message = new HttpMessage();
        message.setResponseBody(body);
    }

    static Stream<Arguments> defaultRegexes() {
        return passiveScanParam.getAutoTagScanners().stream()
                .map(e -> Arguments.of(e.getName(), e));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("defaultRegexes")
    void shouldNotBeSlowWhenScanningBigBody(String name, RegexAutoTagScanner scanner) {
        assertDoesNotThrow(() -> scanner.scanHttpResponseReceive(message, -1, source));
    }
}
