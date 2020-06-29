/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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

import edu.umass.cs.benchlab.har.HarEntries;
import edu.umass.cs.benchlab.har.HarLog;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

public class HarUtilsUnitTest {

    @Test
    public void serializedAndDerserializedShouldMatch() throws Exception {

        // given
        byte requestBody[] = {0x01, 0x02};
        byte responseBody[] = {0x30, 0x31};
        HttpMessage httpMessage =
                new HttpMessage(
                        "POST /path HTTP/1.1\r\nContent-Type: application/octet-stream\r\n\r\n",
                        requestBody,
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain;charset=US-ASCII\r\n\r\n",
                        responseBody);

        HarLog harLog = HarUtils.createZapHarLog();
        HarEntries harEntries = new HarEntries();
        harEntries.addEntry(HarUtils.createHarEntry(httpMessage));
        harLog.setEntries(harEntries);
        // when
        List<HttpMessage> deserialized = HarUtils.getHttpMessages(harLog);
        // then
        assertThat(deserialized.size(), equalTo(1));
        assertThat(deserialized.get(0), equalTo(httpMessage));
    }
}
