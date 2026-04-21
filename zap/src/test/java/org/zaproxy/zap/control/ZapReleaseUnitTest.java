/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.control;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit test for {@link ZapRelease}. */
class ZapReleaseUnitTest {

    private static final String DEV_BUILD = "Dev Build";

    @Test
    void testDevBuildLaterThan1_4_1() {
        ZapRelease rel = new ZapRelease();
        rel.setVersion(DEV_BUILD);
        assertThat(rel.isNewerThan("1.4.1")).isTrue();
    }

    @Test
    void test1_4_2LaterThan1_4_1() {
        ZapRelease rel = new ZapRelease();
        rel.setVersion("1.4.2");
        assertThat(rel.isNewerThan("1.4.1")).isTrue();
    }

    @Test
    void test1_5_1LaterThan1_4_2() {
        ZapRelease rel = new ZapRelease();
        rel.setVersion("1.5.1");
        assertThat(rel.isNewerThan("1.4.2")).isTrue();
    }

    @Test
    void test1_5_1LaterThan1_4_2_1() {
        ZapRelease rel = new ZapRelease();
        rel.setVersion("1.5.1");
        assertThat(rel.isNewerThan("1.4.2.1")).isTrue();
    }

    @Test
    void testLots() {
        // Imported from old CheckForUpdates code
        assertThat(new ZapRelease("1.3.4").isNewerThan("1.4")).isFalse();
        assertThat(new ZapRelease("1.3.4").isNewerThan("1.4")).isFalse();
        assertThat(new ZapRelease("1.3.4").isNewerThan("2.0")).isFalse();
        assertThat(new ZapRelease("1.4").isNewerThan("1.4.1")).isFalse();
        assertThat(new ZapRelease("1.4.1").isNewerThan("1.4.2")).isFalse();
        assertThat(new ZapRelease("1.4.2").isNewerThan("1.4.11")).isFalse();
        // Dont support this right now
        // assertThat(new ZapRelease("1.4.alpha.1").isNewerThan("1.4")).isFalse();
        // assertThat(new ZapRelease("1.4.alpha.1").isNewerThan("1.4.1")).isFalse();
        assertThat(new ZapRelease("1.4.beta.1").isNewerThan("1.5")).isFalse();
        assertThat(new ZapRelease("D-2012-08-01").isNewerThan("D-2012-08-02")).isFalse();
        assertThat(new ZapRelease("D-2012-01-01").isNewerThan("D-2013-10-10")).isFalse();
        assertThat(new ZapRelease("1.4").isNewerThan("1.4")).isFalse();

        assertThat(new ZapRelease("1.4").isNewerThan("1.3.4")).isTrue();
        assertThat(new ZapRelease("1.4.2").isNewerThan("1.4.1")).isTrue();
        assertThat(new ZapRelease("1.4.20").isNewerThan("1.4.11")).isTrue();
        assertThat(new ZapRelease("1.4.alpha.1").isNewerThan("1.3.4")).isTrue();
        // Dont support this right now
        // assertThat(new ZapRelease("1.4").isNewerThan("1.4.alpha.1")).isTrue();
        assertThat(new ZapRelease("Dev Build").isNewerThan("1.5")).isTrue();
        assertThat(new ZapRelease("D-2012-08-02").isNewerThan("D-2012-08-01")).isTrue();
        assertThat(new ZapRelease("D-2013-10-10").isNewerThan("D-2012-01-01")).isTrue();
        assertThat(new ZapRelease("D-2013-01-01").isNewerThan("D-2012-12-31")).isTrue();
        assertThat(new ZapRelease("D-2013-01-07").isNewerThan("D-2012-12-31")).isTrue();
    }
}
