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
package org.zaproxy.zap.control;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.Version;

/** Unit test for {@link ZapAddOnXmlFile}. */
class ZapAddOnXmlFileUnitTest {

    @Test
    void shouldRequireInputStream() throws Exception {
        // Given
        InputStream manifestData = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new ZapAddOnXmlFile(manifestData));
    }

    @Test
    void shouldFailToLoadEmptyData() throws Exception {
        // Given
        InputStream manifestData = manifestData("");
        // When / Then
        assertThrows(IOException.class, () -> new ZapAddOnXmlFile(manifestData));
    }

    @Test
    void shouldLoadEmptyManifest() throws Exception {
        // Given
        InputStream manifestData = manifestData("<zapaddon>", "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getName(), is(equalTo("")));
        assertThat(manifest.getVersion(), is(equalTo(version("1.0.0"))));
        assertThat(manifest.getStatus(), is(equalTo("alpha")));
        assertThat(manifest.getDescription(), is(emptyString()));
        assertThat(manifest.getAuthor(), is(emptyString()));
        assertThat(manifest.getUrl(), is(emptyString()));
        assertThat(manifest.getChanges(), is(emptyString()));
        assertThat(manifest.getRepo(), is(emptyString()));
        assertThat(manifest.getNotBeforeVersion(), is(emptyString()));
        assertThat(manifest.getNotFromVersion(), is(emptyString()));
        assertThat(manifest.getBundleBaseName(), is(emptyString()));
        assertThat(manifest.getBundlePrefix(), is(emptyString()));
        assertThat(manifest.getHelpSetBaseName(), is(emptyString()));
        assertThat(manifest.getHelpSetLocaleToken(), is(emptyString()));
        assertThat(manifest.getDependencies(), is(nullValue()));
        assertThat(manifest.getAddOnClassnames(), is(equalTo(AddOnClassnames.ALL_ALLOWED)));
        assertThat(manifest.getExtensions(), is(empty()));
        assertThat(manifest.getExtensionsWithDeps(), is(empty()));
        assertThat(manifest.getAscanrules(), is(empty()));
        assertThat(manifest.getPscanrules(), is(empty()));
        assertThat(manifest.getFiles(), is(empty()));
        assertThat(manifest.getLibs(), is(empty()));
    }

    @Test
    void shouldLoadManifestWithBasicData() throws Exception {
        // Given
        String name = "Name";
        String version = "7";
        String semver = "1.1.0";
        String status = "beta";
        String description = "Description";
        String author = "Author";
        String url = "http://example.org";
        String changes = "Changes";
        String repo = "http://example.org/repo/";
        String notBeforeVersion = "1.2.3";
        String notFromVersion = "3.2.1";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<name>" + name + "</name>",
                        "<version>" + version + "</version>",
                        "<semver>" + semver + "</semver>",
                        "<status>" + status + "</status>",
                        "<description>" + description + "</description>",
                        "<author>" + author + "</author>",
                        "<url>" + url + "</url>",
                        "<changes>" + changes + "</changes>",
                        "<repo>" + repo + "</repo>",
                        "<not-before-version>" + notBeforeVersion + "</not-before-version>",
                        "<not-from-version>" + notFromVersion + "</not-from-version>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getName(), is(equalTo(name)));
        assertThat(manifest.getVersion(), is(equalTo(version("7.0.0"))));
        assertThat(manifest.getSemVer(), is(equalTo(version("1.1.0"))));
        assertThat(manifest.getStatus(), is(equalTo(status)));
        assertThat(manifest.getDescription(), is(equalTo(description)));
        assertThat(manifest.getAuthor(), is(equalTo(author)));
        assertThat(manifest.getUrl(), is(equalTo(url)));
        assertThat(manifest.getChanges(), is(equalTo(changes)));
        assertThat(manifest.getRepo(), is(equalTo(repo)));
        assertThat(manifest.getNotBeforeVersion(), is(equalTo(notBeforeVersion)));
        assertThat(manifest.getNotFromVersion(), is(equalTo(notFromVersion)));
    }

    @Test
    void shouldLoadManifestWithRecognisedStatuses() throws Exception {
        // Given
        List<String> statuses = Arrays.asList("alpha", "beta", "release");
        for (String status : statuses) {
            InputStream manifestData =
                    manifestData("<zapaddon>", "<status>" + status + "</status>", "</zapaddon>");
            // When
            ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
            // Then
            assertThat(manifest.getStatus(), is(equalTo(status)));
        }
    }

    @Test
    void shouldFailToLoadManifestWithUnrecognisedStatus() throws Exception {
        // Given
        String status = "unrecognised-status";
        InputStream manifestData =
                manifestData("<zapaddon>", "<status>" + status + "</status>", "</zapaddon>");
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> new ZapAddOnXmlFile(manifestData));
        // Then
        assertThat(e.getMessage(), containsString("status"));
    }

    @Test
    void shouldLoadManifestWithBundleAndHelpSet() throws Exception {
        // Given
        String bundleBaseName = "org.example.Messages";
        String bundlePrefix = "example";
        String helpSetBaseName = "org.example.help%LC%.helpset";
        String helpSetLocaleToken = "%LC%";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<bundle prefix=\"" + bundlePrefix + "\">" + bundleBaseName + "</bundle>",
                        "<helpset localetoken=\""
                                + helpSetLocaleToken
                                + "\">"
                                + helpSetBaseName
                                + "</helpset>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getBundleBaseName(), is(equalTo(bundleBaseName)));
        assertThat(manifest.getBundlePrefix(), is(equalTo(bundlePrefix)));
        assertThat(manifest.getHelpSetBaseName(), is(equalTo(helpSetBaseName)));
        assertThat(manifest.getHelpSetLocaleToken(), is(equalTo(helpSetLocaleToken)));
    }

    @Test
    void shouldLoadManifestWithLibs() throws Exception {
        // Given
        String lib1 = "lib1.jar";
        String lib2 = "dir/lib2.jar";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<libs>",
                        "  <lib>" + lib1 + "</lib>",
                        "  <lib>" + lib2 + "</lib>",
                        "</libs>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getLibs(), contains(lib1, lib2));
    }

    @Test
    void shouldLoadManifestWithDependencies() throws Exception {
        // Given
        String javaVersion = "11";
        String addOn1Id = "id1";
        String addOn1NotBeforeVersion = "4";
        String addOn1NotFromVersion = "6";
        String addOn2Id = "id2";
        String addOn2SemVer = "2.0.*";
        String addOn3Id = "id3";
        String addOn3Version = "3.*";
        String addOn4Id = "id4";
        String addOn4Version = ">= 1.2.3";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<javaversion>" + javaVersion + "</javaversion>",
                        "<addons>",
                        "<addon>",
                        "<id>" + addOn1Id + "</id>",
                        "<not-before-version>" + addOn1NotBeforeVersion + "</not-before-version>",
                        "<not-from-version>" + addOn1NotFromVersion + "</not-from-version>",
                        "</addon>",
                        "<addon>",
                        "<id>" + addOn2Id + "</id>",
                        "<semver>" + addOn2SemVer + "</semver>",
                        "</addon>",
                        "<addon>",
                        "<id>" + addOn3Id + "</id>",
                        "<version>" + addOn3Version + "</version>",
                        "</addon>",
                        "<addon>",
                        "<id>" + addOn4Id + "</id>",
                        "<version><![CDATA[ " + addOn4Version + " ]]></version>",
                        "<not-before-version>1</not-before-version>",
                        "<not-from-version>2</not-from-version>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getDependencies(), is(notNullValue()));
        assertThat(manifest.getDependencies().getJavaVersion(), is(equalTo(javaVersion)));
        assertThat(manifest.getDependencies().getAddOns().get(0).getId(), is(equalTo(addOn1Id)));
        assertThat(
                manifest.getDependencies().getAddOns().get(0).getVersion(),
                is(equalTo(" >= 4.0.0 & < 6.0.0")));
        assertThat(manifest.getDependencies().getAddOns().get(1).getId(), is(equalTo(addOn2Id)));
        assertThat(
                manifest.getDependencies().getAddOns().get(1).getVersion(),
                is(equalTo(addOn2SemVer)));
        assertThat(manifest.getDependencies().getAddOns().get(2).getId(), is(equalTo(addOn3Id)));
        assertThat(
                manifest.getDependencies().getAddOns().get(2).getVersion(),
                is(equalTo(addOn3Version)));
        assertThat(manifest.getDependencies().getAddOns().get(3).getId(), is(equalTo(addOn4Id)));
        assertThat(
                manifest.getDependencies().getAddOns().get(3).getVersion(),
                is(equalTo(addOn4Version)));
    }

    @Test
    void shouldLoadManifestWithAddOnDepWithJustNotBeforeVersion() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "<id>id</id>",
                        "<not-before-version>6</not-before-version>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getDependencies(), is(notNullValue()));
        assertThat(
                manifest.getDependencies().getAddOns().get(0).getVersion(),
                is(equalTo(" >= 6.0.0")));
    }

    @Test
    void shouldLoadManifestWithAddOnDepWithJustNotFromVersion() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "<id>id</id>",
                        "<not-from-version>6</not-from-version>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getDependencies(), is(notNullValue()));
        assertThat(
                manifest.getDependencies().getAddOns().get(0).getVersion(),
                is(equalTo(" < 6.0.0")));
    }

    @Test
    void shouldFailToLoadManifestWithAddOnDepWithMissingId() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> new ZapAddOnXmlFile(manifestData));
        // Then
        assertThat(e.getMessage(), containsString("id"));
    }

    @Test
    void shouldFailToLoadManifestWithAddOnDepWithEmptyId() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "<id></id>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> new ZapAddOnXmlFile(manifestData));
        // Then
        assertThat(e.getMessage(), containsString("id"));
    }

    @Test
    void shouldFailToLoadManifestWithAddOnDepWithMalformedVersion() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "<id>id</id>",
                        "<version>not-a-valid-version-or-range</version>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> new ZapAddOnXmlFile(manifestData));
        // Then
        assertThat(e.getMessage(), containsString("version"));
    }

    @Test
    void shouldFailToLoadManifestWithAddOnDepWithMalformedSemVer() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "<id>id</id>",
                        "<semver>not-a-valid-version-or-range</semver>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> new ZapAddOnXmlFile(manifestData));
        // Then
        assertThat(e.getMessage(), containsString("version range"));
    }

    @Test
    void shouldFailToLoadManifestWithAddOnDepWithMalformedNotBeforeVersion() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "<id>id</id>",
                        "<not-before-version>a</not-before-version>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> new ZapAddOnXmlFile(manifestData));
        // Then
        assertThat(e.getMessage(), containsString("not-before-version"));
    }

    @Test
    void shouldFailToLoadManifestWithAddOnDepWithMalformedNotFromVersion() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "<id>id</id>",
                        "<not-from-version>a</not-from-version>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</zapaddon>");
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> new ZapAddOnXmlFile(manifestData));
        // Then
        assertThat(e.getMessage(), containsString("not-from-version"));
    }

    @Test
    void shouldLoadManifestWithClassnames() throws Exception {
        // Given
        String allowedClass1 = "org.example.ClassAllowed1";
        String allowedClass2 = "org.example.ClassAllowed2";
        String allowedPackage = "org.example.allowed";
        String restrictedClass1 = "org.example.ClassRestricted1";
        String restrictedClass2 = "org.example.ClassRestricted2";
        String restrictedPackage = "org.example.restricted";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<classnames>",
                        "<allowed>" + allowedClass1 + "</allowed>",
                        "<allowed>" + allowedClass2 + "</allowed>",
                        "<allowed>" + allowedPackage + "</allowed>",
                        "<restricted>" + restrictedClass1 + "</restricted>",
                        "<restricted>" + restrictedClass2 + "</restricted>",
                        "<restricted>" + restrictedPackage + "</restricted>",
                        "</classnames>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getAddOnClassnames(), is(notNullValue()));
        assertThat(
                manifest.getAddOnClassnames().getAllowedClassnames(),
                contains(allowedClass1, allowedClass2, allowedPackage));
        assertThat(
                manifest.getAddOnClassnames().getRestrictedClassnames(),
                contains(restrictedClass1, restrictedClass2, restrictedPackage));
    }

    @Test
    void shouldIgnoreEmptyClassnamesElements() throws Exception {
        // Given
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<classnames>",
                        "<allowed></allowed>",
                        "<restricted></restricted>",
                        "</classnames>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getAddOnClassnames(), is(equalTo(AddOnClassnames.ALL_ALLOWED)));
    }

    @Test
    void shouldLoadManifestWithoutRestrictedClassnamesElements() throws Exception {
        // Given
        String allowedPackage = "org.example.allowed";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<classnames>",
                        "<allowed>" + allowedPackage + "</allowed>",
                        "</classnames>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getAddOnClassnames(), is(notNullValue()));
        assertThat(manifest.getAddOnClassnames().getAllowedClassnames(), contains(allowedPackage));
        assertThat(manifest.getAddOnClassnames().getRestrictedClassnames(), is(empty()));
    }

    @Test
    void shouldLoadManifestWithoutAllowedClassnamesElements() throws Exception {
        // Given
        String restrictedPackage = "org.example.restricted";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<classnames>",
                        "<restricted>" + restrictedPackage + "</restricted>",
                        "</classnames>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getAddOnClassnames(), is(notNullValue()));
        assertThat(
                manifest.getAddOnClassnames().getRestrictedClassnames(),
                contains(restrictedPackage));
        assertThat(manifest.getAddOnClassnames().getAllowedClassnames(), is(empty()));
    }

    @Test
    void shouldLoadManifestWithScannersAndFiles() throws Exception {
        // Given
        String ascanrule1 = "org.example.ActiveScanner1";
        String ascanrule2 = "org.example.ActiveScanner2";
        String pscanrule1 = "org.example.PassiveScanner1";
        String pscanrule2 = "org.example.PassiveScanner2";
        String file1 = "dir/file1.txt";
        String file2 = "file2.xml";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<ascanrules>",
                        "<ascanrule>" + ascanrule1 + "</ascanrule>",
                        "<ascanrule>" + ascanrule2 + "</ascanrule>",
                        "</ascanrules>",
                        "<pscanrules>",
                        "<pscanrule>" + pscanrule1 + "</pscanrule>",
                        "<pscanrule>" + pscanrule2 + "</pscanrule>",
                        "</pscanrules>",
                        "<files>",
                        "<file>" + file1 + "</file>",
                        "<file>" + file2 + "</file>",
                        "</files>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getAscanrules(), contains(ascanrule1, ascanrule2));
        assertThat(manifest.getPscanrules(), contains(pscanrule1, pscanrule2));
        assertThat(manifest.getFiles(), contains(file1, file2));
    }

    @Test
    void shouldLoadManifestWithExtensions() throws Exception {
        // Given
        String extension1 = "org.example.Extension1";
        String extension2 = "org.example.Extension2";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<extensions>",
                        "<extension>" + extension1 + "</extension>",
                        "<extension>" + extension2 + "</extension>",
                        "</extensions>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getExtensions(), contains(extension1, extension2));
    }

    @Test
    void shouldLoadManifestWithExtensionsV1() throws Exception {
        // Given
        String extension1 = "org.example.Extension1";
        String extension2 = "org.example.Extension2";
        String allowedClass = "org.example.Extension2";
        String restrictedClass = "org.example.Extension1";
        String addOnId = "addOnId";
        String version = "2.*";
        InputStream manifestData =
                manifestData(
                        "<zapaddon>",
                        "<extensions>",
                        "<extension v=\"1\">",
                        "<classname>" + extension1 + "</classname>",
                        "</extension>",
                        "<extension v=\"1\">",
                        "<classname>" + extension2 + "</classname>",
                        "<classnames>",
                        "<allowed>" + allowedClass + "</allowed>",
                        "<restricted>" + restrictedClass + "</restricted>",
                        "</classnames>",
                        "<dependencies>",
                        "<addons>",
                        "<addon>",
                        "<id>" + addOnId + "</id>",
                        "<version>" + version + "</version>",
                        "</addon>",
                        "</addons>",
                        "</dependencies>",
                        "</extension>",
                        "</extensions>",
                        "</zapaddon>");
        // When
        ZapAddOnXmlFile manifest = new ZapAddOnXmlFile(manifestData);
        // Then
        assertThat(manifest.getExtensions(), contains(extension1));
        assertThat(manifest.getExtensionsWithDeps().get(0).getClassname(), is(equalTo(extension2)));
        assertThat(
                manifest.getExtensionsWithDeps().get(0).getAddOnClassnames().getAllowedClassnames(),
                contains(allowedClass));
        assertThat(
                manifest.getExtensionsWithDeps()
                        .get(0)
                        .getAddOnClassnames()
                        .getRestrictedClassnames(),
                contains(restrictedClass));
        assertThat(
                manifest.getExtensionsWithDeps().get(0).getDependencies().get(0).getId(),
                is(equalTo(addOnId)));
        assertThat(
                manifest.getExtensionsWithDeps().get(0).getDependencies().get(0).getVersion(),
                is(equalTo(version)));
    }

    private static Version version(String version) {
        return new Version(version);
    }

    private static InputStream manifestData(String... lines) {
        return new ByteArrayInputStream(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
    }
}
