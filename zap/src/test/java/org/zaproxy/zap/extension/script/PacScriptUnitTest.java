/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.script;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import org.junit.Test;
import org.zaproxy.zap.testutils.TestUtils;

/**
 * Unit test for {@link PacScript}.
 *
 * @author aine-rb
 */
public class PacScriptUnitTest extends TestUtils {

    private static final String DATERANGE_FILE_NAME = "dateRange.pac";
    private static final String DNSDOMAINIS_FILE_NAME = "dnsDomainIs.pac";
    private static final String DNSDOMAINLEVELS_FILE_NAME = "dnsDomainLevels.pac";
    private static final String DNSRESOLVE_FILE_NAME = "dnsResolve.pac";
    private static final String ISINNET_FILE_NAME = "isInNet.pac";
    private static final String ISPLAINHOSTNAME_FILE_NAME = "isPlainHostName.pac";
    private static final String ISRESOLVABLE_FILE_NAME = "isResolvable.pac";
    private static final String LOCALHOSTORDOMAINIS_FILE_NAME = "localHostOrDomainIs.pac";
    private static final String MYIPADDRESS_FILE_NAME = "myIpAddress.pac";
    private static final String SHEXPMATCH_FILE_NAME = "shExpMatch.pac";
    private static final String TIMERANGE_FILE_NAME = "timeRange.pac";
    private static final String WEEKDAYRANGE_FILE_NAME = "weekdayRange.pac";

    private static final Clock FIXED_CLOCK =
            Clock.fixed(
                    Instant.ofEpochMilli(1534567890123L),
                    ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-2)));
    private static final Clock FIXED_CLOCK_GMT = FIXED_CLOCK.withZone(ZoneId.of("GMT"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-M-d");

    @Test
    public void testDateRange() throws Exception {
        // Given
        URL pacUrl = getFileUrl(DATERANGE_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);
        pacScript.setBaseClock(FIXED_CLOCK);

        // When (Arguments provided to FindProxyForURL don't correspond to an url or a hostname for
        // testing purpose)
        String result1 = pacScript.evaluate("http://www.example.com", "");
        String result2 = pacScript.evaluate("http://www.example.com", "GMT");

        // Then
        assertEquals(LocalDate.now(FIXED_CLOCK).format(DATE_FORMAT), result1);
        assertEquals(LocalDate.now(FIXED_CLOCK_GMT).format(DATE_FORMAT), result2);
    }

    @Test
    public void testDnsDomainIs() throws Exception {
        // Given
        URL pacUrl = getFileUrl(DNSDOMAINIS_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When
        String result1 = pacScript.evaluate("http://www.example.com", "www.example.com");
        String result2 = pacScript.evaluate("http://www.example.com", "localhost");

        // Then
        assertEquals("SUCCESS", result1);
        assertEquals("FAILURE", result2);
    }

    @Test
    public void testDnsDomainLevels() throws Exception {
        // Given
        URL pacUrl = getFileUrl(DNSDOMAINLEVELS_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When
        String result1 = pacScript.evaluate("http://www.example.com", "localhost");
        String result2 = pacScript.evaluate("http://www.example.com", "example.com");
        String result3 = pacScript.evaluate("http://www.example.com", "www.example.com");

        // Then
        assertEquals("0 LEVEL", result1);
        assertEquals("1 LEVEL", result2);
        assertEquals("MORE THAN 1 LEVEL", result3);
    }

    @Test
    public void testDnsResolve() throws Exception {
        // Given
        URL pacUrl = getFileUrl(DNSRESOLVE_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When (in the second test, the second argument is not a hostname. It's for testing
        // purpose)
        String result1 = pacScript.evaluate("http://www.example.com", "localhost");
        String result2 = pacScript.evaluate("http://www.example.com", "192.168.42.42");

        // Then
        assertEquals("SUCCESS", result1);
        assertEquals("FAILURE", result2);
    }

    @Test
    public void testIsInNet() throws Exception {
        // Given
        URL pacUrl = getFileUrl(ISINNET_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When (in the second test, the second argument is not a hostname. It's for testing
        // purpose)
        String result1 = pacScript.evaluate("http://www.example.com", "localhost");
        String result2 = pacScript.evaluate("http://www.example.com", "192.168.42.42");

        // Then
        assertEquals("SUCCESS", result1);
        assertEquals("FAILURE", result2);
    }

    @Test
    public void testIsPlainHostName() throws Exception {
        // Given
        URL pacUrl = getFileUrl(ISPLAINHOSTNAME_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When
        String result1 = pacScript.evaluate("http://localhost", "localhost");
        String result2 = pacScript.evaluate("http://www.example.com", "192.168.42.42");

        // Then
        assertEquals("SUCCESS", result1);
        assertEquals("FAILURE", result2);
    }

    @Test
    public void testIsResolvable() throws Exception {
        // Given
        URL pacUrl = getFileUrl(ISRESOLVABLE_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When (in the second test, the second argument is not a hostname. It's for testing
        // purpose)
        String result1 = pacScript.evaluate("http://www.example.com", "localhost");
        String result2 = pacScript.evaluate("http://www.example.com", "192.168.42.42");

        // Then
        assertEquals("SUCCESS", result1);
        assertEquals("SUCCESS", result2);
    }

    @Test
    public void testLocalHostOrDomainIs() throws Exception {
        // Given
        URL pacUrl = getFileUrl(LOCALHOSTORDOMAINIS_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When
        String result1 = pacScript.evaluate("http://www.example.com", "www.example.com");
        String result2 = pacScript.evaluate("http://www.example.com", "www");
        String result3 = pacScript.evaluate("http://www.example.com", "localhost");

        // Then
        assertEquals("SUCCESS", result1);
        assertEquals("SUCCESS", result2);
        assertEquals("FAILURE", result3);
    }

    @Test
    public void testMyIpAddress() throws Exception {
        // Given
        URL pacUrl = getFileUrl(MYIPADDRESS_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When
        String result = pacScript.evaluate("http://www.example.com", "localhost");

        // Then
        assertEquals("SUCCESS", result);
    }

    @Test
    public void testShExpMatch() throws Exception {
        // Given
        URL pacUrl = getFileUrl(SHEXPMATCH_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);

        // When
        String result1 = pacScript.evaluate("http://www.example.com", "www.example.com");
        String result2 = pacScript.evaluate("http://www.example.com", "localhost");
        String result3 = pacScript.evaluate("http://www.example.com", "www.example.org");
        String result4 = pacScript.evaluate("http://www.example.com", "somesite");
        String result5 = pacScript.evaluate("http://www.example.com", "something");

        // Then
        assertEquals("MATCH 1", result1);
        assertEquals("MATCH 2", result2);
        assertEquals("MATCH 3", result3);
        assertEquals("MATCH 4", result4);
        assertEquals("FAILURE", result5);
    }

    @Test
    public void testTimeRange() throws Exception {
        // Given
        URL pacUrl = getFileUrl(TIMERANGE_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);
        pacScript.setBaseClock(FIXED_CLOCK);

        // When (Arguments provided to FindProxyForURL don't correspond to an url or a hostname for
        // testing purpose)
        String result1 = pacScript.evaluate("http://www.example.com", "");
        String result2 = pacScript.evaluate("http://www.example.com", "GMT");

        // Then
        assertEquals("SUCCESS", result1);
        assertEquals("SUCCESS", result2);
    }

    @Test
    public void testWeekdayRange() throws Exception {
        // Given
        URL pacUrl = getFileUrl(WEEKDAYRANGE_FILE_NAME);
        PacScript pacScript = new PacScript(pacUrl);
        pacScript.setBaseClock(FIXED_CLOCK);
        String currentGmtDay = currentDayOfWeek(FIXED_CLOCK_GMT);
        String currentLocaleDay = currentDayOfWeek(FIXED_CLOCK);

        // When (Arguments provided to FindProxyForURL don't correspond to an url or a hostname for
        // testing purpose)
        String result1 = pacScript.evaluate("GMT", currentGmtDay);
        String result2 = pacScript.evaluate("locale", currentLocaleDay);
        String result3 = pacScript.evaluate("GMTRange", currentGmtDay);
        String result4 = pacScript.evaluate("localeRange", currentLocaleDay);
        String result5 = pacScript.evaluate("FAILURE", currentGmtDay);

        // Then
        assertEquals("SUCCESS GMT", result1);
        assertEquals("SUCCESS LOCALE", result2);
        assertEquals("SUCCESS GMT RANGE", result3);
        assertEquals("SUCCESS LOCALE RANGE", result4);
        assertEquals("FAILURE", result5);
    }

    private static URL getFileUrl(String fileName) {
        return PacScriptUnitTest.class.getResource(fileName);
    }

    private static String currentDayOfWeek(Clock clock) {
        DayOfWeek dayOfWeek = LocalDate.now(clock).getDayOfWeek();
        return PacScript.DAYS.get(dayOfWeek.get(WeekFields.SUNDAY_START.dayOfWeek()) - 1);
    }
}
