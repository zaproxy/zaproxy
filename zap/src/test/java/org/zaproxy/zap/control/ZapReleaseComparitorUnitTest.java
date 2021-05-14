/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit test for {@link ZapReleaseComparitor}. */
class ZapReleaseComparitorUnitTest {

    private static final String DEV_BUILD = "Dev Build";

    @Test
    void testComparitor() {
        ZapReleaseComparitor zrc = new ZapReleaseComparitor();

        // Test equals
        assertTrue(zrc.compare(new ZapRelease(DEV_BUILD), new ZapRelease(DEV_BUILD)) == 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.0"), new ZapRelease("2.0.0")) == 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.alpha"), new ZapRelease("2.0.alpha")) == 0);
        assertTrue(
                zrc.compare(new ZapRelease("D-2013-01-01"), new ZapRelease("D-2013-01-01")) == 0);

        // Test first more recent that second
        assertTrue(zrc.compare(new ZapRelease(DEV_BUILD), new ZapRelease("D-2012-08-01")) > 0);
        assertTrue(zrc.compare(new ZapRelease(DEV_BUILD), new ZapRelease("1.4.1")) > 0);
        assertTrue(zrc.compare(new ZapRelease(DEV_BUILD), new ZapRelease("2.4.beta")) > 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.0.1"), new ZapRelease("2.0.0")) > 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.0.1"), new ZapRelease("2.0.alpha")) > 0);
        assertTrue(zrc.compare(new ZapRelease("1.4"), new ZapRelease("1.3.4")) > 0);
        assertTrue(zrc.compare(new ZapRelease("2.0"), new ZapRelease("1.3.4")) > 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.11"), new ZapRelease("2.0.5")) > 0);
        assertTrue(zrc.compare(new ZapRelease("1.4.alpha"), new ZapRelease("1.3.4")) > 0);
        assertTrue(zrc.compare(new ZapRelease("D-2012-08-02"), new ZapRelease("D-2012-08-01")) > 0);
        assertTrue(zrc.compare(new ZapRelease("D-2013-10-10"), new ZapRelease("D-2012-01-01")) > 0);
        assertTrue(zrc.compare(new ZapRelease("D-2013-01-01"), new ZapRelease("D-2012-12-31")) > 0);
        assertTrue(zrc.compare(new ZapRelease("D-2013-01-07"), new ZapRelease("D-2012-12-31")) > 0);
        assertTrue(zrc.compare(new ZapRelease("D-2013-01-07"), new ZapRelease("2.0.1")) > 0);

        // Test first older that second
        assertTrue(zrc.compare(new ZapRelease("1.4.1"), new ZapRelease(DEV_BUILD)) < 0);
        assertTrue(zrc.compare(new ZapRelease("2.4.beta"), new ZapRelease(DEV_BUILD)) < 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.0"), new ZapRelease("2.0.0.1")) < 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.alpha"), new ZapRelease("2.0.0.1")) < 0);
        assertTrue(zrc.compare(new ZapRelease("1.3.4"), new ZapRelease("1.4")) < 0);
        assertTrue(zrc.compare(new ZapRelease("1.3.4"), new ZapRelease("2.0")) < 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.6"), new ZapRelease("2.0.12")) < 0);
        assertTrue(zrc.compare(new ZapRelease("1.3.4"), new ZapRelease("1.4.alpha")) < 0);
        assertTrue(zrc.compare(new ZapRelease("D-2012-08-01"), new ZapRelease("D-2012-08-02")) < 0);
        assertTrue(zrc.compare(new ZapRelease("D-2012-01-01"), new ZapRelease("D-2013-10-10")) < 0);
        assertTrue(zrc.compare(new ZapRelease("D-2012-12-31"), new ZapRelease("D-2013-01-01")) < 0);
        assertTrue(zrc.compare(new ZapRelease("D-2012-12-31"), new ZapRelease("D-2013-01-07")) < 0);
        assertTrue(zrc.compare(new ZapRelease("2.0.1"), new ZapRelease("D-2013-01-07")) < 0);

        // Bad versions
        assertThrows(
                IllegalArgumentException.class,
                () -> zrc.compare(new ZapRelease("1.4.1.theta"), new ZapRelease("1.4.1.alpha")));
        assertThrows(
                IllegalArgumentException.class,
                () -> zrc.compare(new ZapRelease("1.4.1.0"), new ZapRelease("1.4.1.theta")));
    }
}
