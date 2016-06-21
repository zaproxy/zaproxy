/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package org.zaproxy.zap.control;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.zaproxy.zap.utils.ZapXmlConfiguration;


/**
 * Unit test for {@link AddOn}.
 */
public class AddOnUnitTest {

	private static final File ZAP_VERSIONS_XML = 
			Paths.get("test/resources/org/zaproxy/zap/control", "ZapVersions-deps.xml").toFile();

	@Test
	public void testIsAddon() throws Exception {
		assertTrue(AddOn.isAddOn("test-alpha-1.zap"));
	}

	@Test
	public void testNotAddonNoState() throws Exception {
		assertFalse(AddOn.isAddOn("test-1.zap"));
	}

	@Test
	public void testNotAddonBadExt() throws Exception {
		assertFalse(AddOn.isAddOn("test-beta-1.zip"));
	}

	@Test
	public void testNotAddonBadStatus() throws Exception {
		assertFalse(AddOn.isAddOn("test-xxx-1.zap"));
	}

	@Test
	public void testNotAddonBadVersion() throws Exception {
		assertFalse(AddOn.isAddOn("test-beta-A.zap"));
	}

	@Test
	public void testId() throws Exception {
		AddOn addOn = new AddOn("test-alpha-1.zap");
		assertThat(addOn.getId(), is("test"));
	}

	@Test
	public void testStatus() throws Exception {
		AddOn addOn = new AddOn("test-alpha-1.zap");
		assertThat(addOn.getStatus().name(), is("alpha"));
	}

	@Test
	public void testVersion() throws Exception {
		AddOn addOn = new AddOn("test-alpha-1.zap");
		assertThat(addOn.getFileVersion(), is(1));
	}

	@Test
	public void testAlpha2UpdatesAlpha1() throws Exception {
		AddOn addOnA1 = new AddOn("test-alpha-1.zap");
		AddOn addOnA2 = new AddOn("test-alpha-2.zap");
		assertTrue(addOnA2.isUpdateTo(addOnA1));
	}

	@Test
	public void testAlpha1DoesNotUpdateAlpha2() throws Exception {
		AddOn addOnA1 = new AddOn("test-alpha-1.zap");
		AddOn addOnA2 = new AddOn("test-alpha-2.zap");
		assertFalse(addOnA1.isUpdateTo(addOnA2));
	}

	@Test
	public void testAlpha2UpdatesBeta1() throws Exception {
		AddOn addOnB1 = new AddOn("test-beta-1.zap");
		AddOn addOnA2 = new AddOn("test-alpha-2.zap");
		assertTrue(addOnA2.isUpdateTo(addOnB1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAlpha2DoesNotUpdateTestyAlpha1() throws Exception {
		// Given 
		AddOn addOnA1 = new AddOn("test-alpha-1.zap");
		AddOn addOnA2 = new AddOn("testy-alpha-2.zap");
		// When
		addOnA2.isUpdateTo(addOnA1);
		// Then = Exception
	}
	
	@Test
	public void testCanLoadAddOnNotBefore() throws Exception {
		AddOn ao = new AddOn("test-alpha-1.zap");
		ao.setNotBeforeVersion("2.4.0");
		assertTrue(ao.canLoadInVersion("2.4.0"));
		
		ao.setNotBeforeVersion("2.4.0");
		assertTrue(ao.canLoadInVersion("2.4.0"));
		assertTrue(ao.canLoadInVersion("2.5.0"));
		assertFalse(ao.canLoadInVersion("1.4.0"));
		assertFalse(ao.canLoadInVersion("2.0.alpha"));
	}

	@Test
	public void testCanLoadAddOnNotFrom() throws Exception {
		AddOn ao = new AddOn("test-alpha-1.zap");
		ao.setNotBeforeVersion("2.4.0");
		ao.setNotFromVersion("2.8.0");
		assertTrue(ao.canLoadInVersion("2.4.0"));
		assertTrue(ao.canLoadInVersion("2.5.0"));
		assertTrue(ao.canLoadInVersion("2.7.0"));
		assertFalse(ao.canLoadInVersion("2.8.0"));
		assertFalse(ao.canLoadInVersion("2.8.0.1"));
		assertFalse(ao.canLoadInVersion("2.9.0"));
	}

	@Test
	public void testCanLoadAddOnNotBeforeNotFrom() throws Exception {
		AddOn ao = new AddOn("test-alpha-1.zap");
		ao.setNotBeforeVersion("2.4.0");
		assertTrue(ao.canLoadInVersion("2.4.0"));
		ao.setNotFromVersion("2.7.0");
		assertTrue(ao.canLoadInVersion("2.4.0"));
		assertTrue(ao.canLoadInVersion("2.5.0"));
		assertTrue(ao.canLoadInVersion("2.6.0"));
		assertFalse(ao.canLoadInVersion("2.7.0"));
		assertFalse(ao.canLoadInVersion("2.7.0.1"));
		assertFalse(ao.canLoadInVersion("2.8.0"));
		
	}

	@Test
	public void shouldDependOnDependency() throws Exception {
		// Given
		ZapXmlConfiguration zapVersionsXml = createZapVersionsXml();
		AddOn addOn = createAddOn("AddOn1", zapVersionsXml);
		AddOn dependency = createAddOn("AddOn3", zapVersionsXml);
		// When
		boolean depends = addOn.dependsOn(dependency);
		// Then
		assertThat(depends, is(equalTo(true)));
	}

	@Test
	public void shouldNotDependIfNoDependencies() throws Exception {
		// Given
		AddOn addOn = new AddOn("AddOn-release-1.zap");
		AddOn nonDependency = createAddOn("AddOn3", createZapVersionsXml());
		// When
		boolean depends = addOn.dependsOn(nonDependency);
		// Then
		assertThat(depends, is(equalTo(false)));
	}

	@Test
	public void shouldNotDependOnNonDependency() throws Exception {
		// Given
		ZapXmlConfiguration zapVersionsXml = createZapVersionsXml();
		AddOn addOn = createAddOn("AddOn9", zapVersionsXml);
		AddOn nonDependency = createAddOn("AddOn3", zapVersionsXml);
		// When
		boolean depends = addOn.dependsOn(nonDependency);
		// Then
		assertThat(depends, is(equalTo(false)));
	}

	@Test
	public void shouldNotDirectlyDependOnNonDirectDependency() throws Exception {
		// Given
		ZapXmlConfiguration zapVersionsXml = createZapVersionsXml();
		AddOn addOn = createAddOn("AddOn1", zapVersionsXml);
		AddOn nonDirectDependency = createAddOn("AddOn8", zapVersionsXml);
		// When
		boolean depends = addOn.dependsOn(nonDirectDependency);
		// Then
		assertThat(depends, is(equalTo(false)));
	}

	@Test
	public void shouldNotDependOnItSelf() throws Exception {
		// Given
		ZapXmlConfiguration zapVersionsXml = createZapVersionsXml();
		AddOn addOn = createAddOn("AddOn1", zapVersionsXml);
		AddOn sameAddOn = createAddOn("AddOn1", zapVersionsXml);
		// When
		boolean depends = addOn.dependsOn(sameAddOn);
		// Then
		assertThat(depends, is(equalTo(false)));
	}

	@Test
	public void shouldDependOnDependencies() throws Exception {
		// Given
		ZapXmlConfiguration zapVersionsXml = createZapVersionsXml();
		AddOn addOn = createAddOn("AddOn1", zapVersionsXml);
		AddOn nonDependency = createAddOn("AddOn9", zapVersionsXml);
		AddOn dependency = createAddOn("AddOn3", zapVersionsXml);
		Collection<AddOn> addOns = Arrays.asList(new AddOn[] { nonDependency, dependency });
		// When
		boolean depends = addOn.dependsOn(addOns);
		// Then
		assertThat(depends, is(equalTo(true)));
	}

	@Test
	public void shouldNotDirectlyDependOnNonDirectDependencies() throws Exception {
		// Given
		ZapXmlConfiguration zapVersionsXml = createZapVersionsXml();
		AddOn addOn = createAddOn("AddOn1", zapVersionsXml);
		AddOn nonDependency = createAddOn("AddOn9", zapVersionsXml);
		AddOn nonDirectDependency = createAddOn("AddOn8", zapVersionsXml);
		Collection<AddOn> addOns = Arrays.asList(new AddOn[] { nonDependency, nonDirectDependency });
		// When
		boolean depends = addOn.dependsOn(addOns);
		// Then
		assertThat(depends, is(equalTo(false)));
	}

	@Test
	public void shouldNotDependOnNonDependencies() throws Exception {
		// Given
		ZapXmlConfiguration zapVersionsXml = createZapVersionsXml();
		AddOn addOn = createAddOn("AddOn1", zapVersionsXml);
		AddOn nonDependency1 = createAddOn("AddOn1", zapVersionsXml);
		AddOn nonDependency2 = createAddOn("AddOn9", zapVersionsXml);
		Collection<AddOn> addOns = Arrays.asList(new AddOn[] { nonDependency1, nonDependency2 });
		// When
		boolean depends = addOn.dependsOn(addOns);
		// Then
		assertThat(depends, is(equalTo(false)));
	}

	private static ZapXmlConfiguration createZapVersionsXml() throws Exception {
		ZapXmlConfiguration zapVersionsXml = new ZapXmlConfiguration(ZAP_VERSIONS_XML);
		zapVersionsXml.setExpressionEngine(new XPathExpressionEngine());
		return zapVersionsXml;
	}

	protected static AddOn createAddOn(String addOnId, ZapXmlConfiguration zapVersions) throws Exception {
		return new AddOn(addOnId, Paths.get("").toFile(), zapVersions.configurationAt("addon_" + addOnId));
	}
}
