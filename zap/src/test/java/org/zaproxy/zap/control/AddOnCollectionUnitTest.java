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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.StringReader;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.control.AddOnCollection.Platform;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link AddOnCollection}. */
class AddOnCollectionUnitTest {

    private ZapXmlConfiguration configA;
    private ZapXmlConfiguration configB;

    private static final String CONF_A =
            "<ZAP>\n"
                    + "	<core>\n"
                    + "		<version>2.0.0</version>\n"
                    + "		<daily-version>D-2012-12-31</daily-version>\n"
                    + "		<daily>\n"
                    + "			<url>http://zaproxy.googlecode.com/files/ZAP_WEEKLY_D-2012-12-31.zip</url>\n"
                    + "			<file>ZAP_WEEKLY_D-2012-12-31.zip</file>\n"
                    + "			<size>58342498</size>\n"
                    + "		</daily>\n"
                    + "		<windows>\n"
                    + "			<url>http://zaproxy.googlecode.com/files/ZAP_fake_windows_2.0.0.exe</url>\n"
                    + "			<file>ZAP_fake_windows_2.0.0.exe</file>\n"
                    + "			<size>56777000</size>\n"
                    + "		</windows>\n"
                    + "		<linux>\n"
                    + "			<url>http://zaproxy.googlecode.com/files/ZAP_fake_linux_2.0.0.tar.gzip</url>\n"
                    + "			<file>ZAP_fake_linux_2.0.0.tar.gzip</file>\n"
                    + "			<size>56776000</size>\n"
                    + "		</linux>\n"
                    + "		<mac>\n"
                    + "			<url>http://zaproxy.googlecode.com/files/ZAP_fake_mac_2.0.0.zip</url>\n"
                    + "			<file>ZAP_fake_mac_2.0.0.zip</file>\n"
                    + "			<size>56775000</size>\n"
                    + "		</mac>\n"
                    + "		<relnotes>\n"
                    + "			This release includes blah, blah, blah\n"
                    + "			&lt;p>Including a list:\n"
                    + "				&lt;ul>\n"
                    + "			&lt;li> Item 1\n"
                    + "			&lt;li> Item 2\n"
                    + "			&lt;li> Item 3&lt;/ul>\n"
                    + "		</relnotes>\n"
                    + "		<relnotes-url>http://zaproxy.googlecode.com/files/fake_release_notes.html</relnotes-url>\n"
                    + "	</core>\n"
                    + "	<addon>aaa</addon>\n"
                    + "	<addon_aaa>\n"
                    + "		<name>This could be a long name</name>\n"
                    + "		<version>1</version>\n"
                    + "		<file>aaa-alpha-1.zap</file>\n"
                    + "		<status>alpha</status>\n"
                    + "		<description>This could be a longer description for aaa</description>\n"
                    + "		<changes>A list of changes for aaa</changes>\n"
                    + "		<url>https://zap-extensions.googlecode.com/files/aaa-alpha-1.zap</url>\n"
                    + "		<size>12345</size>\n"
                    + "	</addon_aaa>\n"
                    + "	<addon>bbb</addon>\n"
                    + "	<addon_bbb>\n"
                    + "		<name>Blah blah blah</name>\n"
                    + "		<version>2</version>\n"
                    + "		<file>bbb-beta-2.zap</file>\n"
                    + "		<status>beta</status>\n"
                    + "		<description>This could be a longer description for bbb</description>\n"
                    + "		<changes>A list of changes for bbb</changes>\n"
                    + "		<url>https://zap-extensions.googlecode.com/files/bbb-beta-2.zap</url>\n"
                    + "		<size>23456</size>\n"
                    + "		<not-before-version>2.4.0</not-before-version>\n"
                    + "	</addon_bbb>\n"
                    + "	<addon>ddd</addon>\n"
                    + "		<addon_ddd>\n"
                    + "		<name>Yet another addon</name>\n"
                    + "		<version>3</version>\n"
                    + "		<file>ddd-release-3.zap</file>\n"
                    + "		<status>release</status>\n"
                    + "		<description>This could be a longer description for ddd</description>\n"
                    + "		<changes>A list of changes for ddd</changes>\n"
                    + "		<url>https://zap-extensions.googlecode.com/files/ddd-release-3.zap</url>\n"
                    + "		<size>3456</size>\n"
                    + "		<not-before-version>2.4.0</not-before-version>\n"
                    + "		<date>2020-05-22</date>\n"
                    + "	</addon_ddd>\n"
                    + "</ZAP>";

    private static final String CONF_B =
            "<ZAP>\n"
                    + "	<addon>aaa</addon>\n"
                    + "	<addon_aaa>\n"
                    + "		<name>This could be a long name</name>\n"
                    + "		<version>1</version>\n"
                    + "		<file>aaa-alpha-1.zap</file>\n"
                    + "		<status>alpha</status>\n"
                    + "		<description>This could be a longer description for aaa</description>\n"
                    + "		<changes>A list of changes for aaa</changes>\n"
                    + "		<url>https://zap-extensions.googlecode.com/files/aaa-alpha-1.zap</url>\n"
                    + "		<size>12345</size>\n"
                    + "		<not-before-version>2.4.0</not-before-version>\n"
                    + "	</addon_aaa>\n"
                    + "	<addon>bbb</addon>\n"
                    + "	<addon_bbb>\n"
                    + "		<name>Blah blah blah</name>\n"
                    + "		<version>1</version>\n"
                    + "		<file>bbb-beta-1.zap</file>\n"
                    + "		<status>beta</status>\n"
                    + "		<description>This could be a longer description for bbb</description>\n"
                    + "		<changes>A list of changes for bbb</changes>\n"
                    + "		<url>https://zap-extensions.googlecode.com/files/bbb-beta-1.zap</url>\n"
                    + "		<size>23456</size>\n"
                    + "		<not-before-version>2.4.0</not-before-version>\n"
                    + "	</addon_bbb>\n"
                    + "</ZAP>";

    @BeforeEach
    void setUp() throws Exception {
        configA = new ZapXmlConfiguration();
        configA.setDelimiterParsingDisabled(true);
        configA.load(new StringReader(CONF_A));

        configB = new ZapXmlConfiguration();
        configB.setDelimiterParsingDisabled(true);
        configB.load(new StringReader(CONF_B));
    }

    @Test
    void testMainVersion() throws Exception {
        AddOnCollection coll = new AddOnCollection(configA, Platform.windows);
        assertThat(coll.getZapRelease().getVersion(), is("2.0.0"));
    }

    @Test
    void testDailyUrl() throws Exception {
        AddOnCollection coll = new AddOnCollection(configA, Platform.daily);
        assertThat(
                coll.getZapRelease().getUrl().toString(),
                is("http://zaproxy.googlecode.com/files/ZAP_WEEKLY_D-2012-12-31.zip"));
    }

    @Test
    void testWinUrl() throws Exception {
        AddOnCollection coll = new AddOnCollection(configA, Platform.windows);
        assertThat(
                coll.getZapRelease().getUrl().toString(),
                is("http://zaproxy.googlecode.com/files/ZAP_fake_windows_2.0.0.exe"));
    }

    @Test
    void testLinuxUrl() throws Exception {
        AddOnCollection coll = new AddOnCollection(configA, Platform.linux);
        assertThat(
                coll.getZapRelease().getUrl().toString(),
                is("http://zaproxy.googlecode.com/files/ZAP_fake_linux_2.0.0.tar.gzip"));
    }

    @Test
    void testMacUrl() throws Exception {
        AddOnCollection coll = new AddOnCollection(configA, Platform.mac);
        assertThat(
                coll.getZapRelease().getUrl().toString(),
                is("http://zaproxy.googlecode.com/files/ZAP_fake_mac_2.0.0.zip"));
    }

    @Test
    void testDailyVersion() throws Exception {
        AddOnCollection coll = new AddOnCollection(configA, Platform.daily);
        assertThat(coll.getZapRelease().getVersion(), is("D-2012-12-31"));
    }

    @Test
    void testUpdatedAddons() throws Exception {
        AddOnCollection collA = new AddOnCollection(configA, Platform.daily);
        AddOnCollection collB = new AddOnCollection(configB, Platform.daily);
        List<AddOn> updAddOns = collB.getUpdatedAddOns(collA);
        assertThat(updAddOns.size(), is(1));
        assertThat(updAddOns.get(0).getId(), is("bbb"));
    }

    @Test
    void testNewAddons() throws Exception {
        AddOnCollection collA = new AddOnCollection(configA, Platform.daily);
        AddOnCollection collB = new AddOnCollection(configB, Platform.daily);
        List<AddOn> newAddOns = collB.getNewAddOns(collA);
        assertThat(newAddOns.size(), is(1));
        assertThat(newAddOns.get(0).getId(), is("ddd"));
    }

    @Test
    void shouldAcceptAddOnsWithoutDependencyIssues() throws Exception {
        // Given
        ZapXmlConfiguration zapVersions = createConfiguration("ZapVersions-deps.xml");
        // When
        AddOnCollection addOnCollection = new AddOnCollection(zapVersions, Platform.daily, false);
        // Then
        assertThat(addOnCollection.getAddOns().size(), is(equalTo(9)));
        assertThat(addOnCollection.getAddOn("AddOn1"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn2"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn3"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn4"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn5"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn6"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn7"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn8"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn9"), is(notNullValue()));
    }

    @Test
    void shouldRejectAddOnsWithCircularDependencies() throws Exception {
        // Given
        ZapXmlConfiguration zapVersions = createConfiguration("ZapVersions-cyclic-deps.xml");
        // When
        AddOnCollection addOnCollection = new AddOnCollection(zapVersions, Platform.daily, false);
        // Then
        assertThat(addOnCollection.getAddOns().size(), is(equalTo(4)));
        assertThat(addOnCollection.getAddOn("AddOn2"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn3"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn8"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn9"), is(notNullValue()));
    }

    @Test
    void shouldRejectAddOnsWithMissingDependencies() throws Exception {
        // Given
        ZapXmlConfiguration zapVersions = createConfiguration("ZapVersions-missing-deps.xml");
        // When
        AddOnCollection addOnCollection = new AddOnCollection(zapVersions, Platform.daily, false);
        // Then
        assertThat(addOnCollection.getAddOns().size(), is(equalTo(3)));
        assertThat(addOnCollection.getAddOn("AddOn3"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn8"), is(notNullValue()));
        assertThat(addOnCollection.getAddOn("AddOn9"), is(notNullValue()));
    }

    @Test
    void shouldHaveReleaseDateInAddOn() throws Exception {
        // Given
        AddOnCollection coll = new AddOnCollection(configA, Platform.windows);
        AddOn addOn = coll.getAddOn("ddd");
        // When
        String releaseDate = addOn.getReleaseDate();
        // Then
        assertThat(releaseDate, is(equalTo("2020-05-22")));
    }

    private ZapXmlConfiguration createConfiguration(String file) throws ConfigurationException {
        return new ZapXmlConfiguration(getClass().getResource(file));
    }
}
