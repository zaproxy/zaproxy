/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.parosproxy.paros.extension.report;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.zaproxy.zap.testutils.TestUtils;

/**
 * Unit test for {@link ReportGenerator}.
 *
 * @deprecated
 */
@Deprecated
class ReportGeneratorUnitTest extends TestUtils {

    private static final String NEWLINE = System.getProperty("line.separator");

    @Test
    void shouldNotEntityEncodeHigherUnicodeChars() {
        // Given
        String chars = "J/ψ → VP";
        // When
        String encoded = ReportGenerator.entityEncode(chars);
        // Then
        assertThat(encoded, is(equalTo(chars)));
    }

    @Test
    void shouldWriteReportWithWellformedXml(@TempDir Path tempDir) throws Exception {
        // Given
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><data>J/ψ → VP</data>" + NEWLINE;
        Path report = Files.createTempFile(tempDir, "", "");
        // When
        ReportGenerator.stringToHtml(data, identityXsl(), report.toString());
        // Then
        assertThat(contents(report), is(equalTo(data)));
    }

    @Test
    void shouldFailToWriteReportWithMalformedXml(@TempDir Path tempDir) throws Exception {
        // Given
        String data = "J/ψ → VP</data>";
        Path report = Files.createTempFile(tempDir, "", "");
        // When
        ReportGenerator.stringToHtml(data, identityXsl(), report.toString());
        // Then = nothing written.
        assertThat(contents(report), is(equalTo("")));
    }

    @Test
    void shouldWriteReportWhenPathContainsHashSymbol(@TempDir Path tempDir) throws Exception {
        // Given
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><data>ZAP</data>" + NEWLINE;
        Path report = Files.createTempFile(tempDir, "#", "");
        // When
        ReportGenerator.stringToHtml(data, identityXsl(), report.toString());
        // Then
        assertThat(contents(report), is(equalTo(data)));
    }

    @Test
    void shouldUseEmptyArrayForSitesInJsonReportIfNoSitePresent() {
        // Given
        String xmlReport =
                "<?xml version=\"1.0\"?><OWASPZAPReport version=\"Dev Build\"></OWASPZAPReport>";
        // When
        String jsonReport = ReportGenerator.stringToJson(xmlReport);
        // Then
        assertThat(jsonReport, containsString("\"site\":[]"));
    }

    @Test
    void shouldUseArrayForSitesInJsonReportIfOneSitePresent() {
        // Given
        String xmlReport =
                "<?xml version=\"1.0\"?><OWASPZAPReport version=\"Dev Build\">\n"
                        + "<site name=\"http://example.com\"></site>"
                        + "</OWASPZAPReport>";
        // When
        String jsonReport = ReportGenerator.stringToJson(xmlReport);
        // Then
        assertThat(jsonReport, containsString("\"site\":[{\"@name\":\"http://example.com\"}]"));
    }

    @Test
    void shouldUseArrayForSitesInJsonReportIfSeveralSitesPresent() {
        // Given
        String xmlReport =
                "<?xml version=\"1.0\"?><OWASPZAPReport version=\"Dev Build\">\n"
                        + "<site name=\"http://a.example.com\"></site>"
                        + "<site name=\"http://b.example.com\"></site>"
                        + "</OWASPZAPReport>";
        // When
        String jsonReport = ReportGenerator.stringToJson(xmlReport);
        // Then
        assertThat(
                jsonReport,
                containsString(
                        "\"site\":[{\"@name\":\"http://a.example.com\"},"
                                + "{\"@name\":\"http://b.example.com\"}]"));
    }

    private static String contents(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static StreamSource identityXsl() {
        String xslt =
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
                        + "  <xsl:template match=\"@*|node()\">\n"
                        + "    <xsl:copy>\n"
                        + "      <xsl:apply-templates select=\"@*|node()\"/>\n"
                        + "    </xsl:copy>\n"
                        + "  </xsl:template>\n"
                        + "</xsl:stylesheet>";
        return new StreamSource(new StringReader(xslt));
    }
}
