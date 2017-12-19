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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.zaproxy.zap.utils.ZapXmlConfiguration;


/**
 * Unit test for {@link AddOn}.
 */
public class AddOnUnitTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	private static final File ZAP_VERSIONS_XML = 
			Paths.get("test/resources/org/zaproxy/zap/control", "ZapVersions-deps.xml").toFile();

	@Test
	@SuppressWarnings("deprecation")
	public void testIsAddon() throws Exception {
		assertTrue(AddOn.isAddOn("test-alpha-1.zap"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testNotAddonNoState() throws Exception {
		assertFalse(AddOn.isAddOn("test-1.zap"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testNotAddonBadExt() throws Exception {
		assertFalse(AddOn.isAddOn("test-beta-1.zip"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testNotAddonBadStatus() throws Exception {
		assertFalse(AddOn.isAddOn("test-xxx-1.zap"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testNotAddonBadVersion() throws Exception {
		assertFalse(AddOn.isAddOn("test-beta-A.zap"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testId() throws Exception {
		AddOn addOn = new AddOn("test-alpha-1.zap");
		assertThat(addOn.getId(), is("test"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testStatus() throws Exception {
		AddOn addOn = new AddOn("test-alpha-1.zap");
		assertThat(addOn.getStatus().name(), is("alpha"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testVersion() throws Exception {
		AddOn addOn = new AddOn("test-alpha-1.zap");
		assertThat(addOn.getVersion().toString(), is(equalTo("1.0.0")));
		assertThat(addOn.getFileVersion(), is(1));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testAlpha2UpdatesAlpha1() throws Exception {
		AddOn addOnA1 = new AddOn("test-alpha-1.zap");
		AddOn addOnA2 = new AddOn("test-alpha-2.zap");
		assertTrue(addOnA2.isUpdateTo(addOnA1));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testAlpha1DoesNotUpdateAlpha2() throws Exception {
		AddOn addOnA1 = new AddOn("test-alpha-1.zap");
		AddOn addOnA2 = new AddOn("test-alpha-2.zap");
		assertFalse(addOnA1.isUpdateTo(addOnA2));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testAlpha2UpdatesBeta1() throws Exception {
		AddOn addOnB1 = new AddOn("test-beta-1.zap");
		AddOn addOnA2 = new AddOn("test-alpha-2.zap");
		assertTrue(addOnA2.isUpdateTo(addOnB1));
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("deprecation")
	public void testAlpha2DoesNotUpdateTestyAlpha1() throws Exception {
		// Given 
		AddOn addOnA1 = new AddOn("test-alpha-1.zap");
		AddOn addOnA2 = new AddOn("testy-alpha-2.zap");
		// When
		addOnA2.isUpdateTo(addOnA1);
		// Then = Exception
	}
	
	@Test
	@SuppressWarnings("deprecation")
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
	@SuppressWarnings("deprecation")
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
	@SuppressWarnings("deprecation")
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
	public void shouldNotBeAddOnFileNameIfNull() throws Exception {
		// Given
		String fileName = null;
		// When
		boolean addOnFileName = AddOn.isAddOnFileName(fileName);
		// Then
		assertThat(addOnFileName, is(equalTo(false)));
	}

	@Test
	public void shouldNotBeAddOnFileNameIfNotEndsWithZapExtension() throws Exception {
		// Given
		String fileName = "addon.txt";
		// When
		boolean addOnFileName = AddOn.isAddOnFileName(fileName);
		// Then
		assertThat(addOnFileName, is(equalTo(false)));
	}

	@Test
	public void shouldBeAddOnFileNameIfEndsWithZapExtension() throws Exception {
		// Given
		String fileName = "addon.zap";
		// When
		boolean addOnFileName = AddOn.isAddOnFileName(fileName);
		// Then
		assertThat(addOnFileName, is(equalTo(true)));
	}

	@Test
	public void shouldBeAddOnFileNameEvenIfZapExtensionIsUpperCase() throws Exception {
		// Given
		String fileName = "addon.ZAP";
		// When
		boolean addOnFileName = AddOn.isAddOnFileName(fileName);
		// Then
		assertThat(addOnFileName, is(equalTo(true)));
	}

	@Test
	public void shouldNotBeAddOnIfPathIsNull() throws Exception {
		// Given
		Path file = null;
		// When
		boolean addOnFile = AddOn.isAddOn(file);
		// Then
		assertThat(addOnFile, is(equalTo(false)));
	}

	@Test
	public void shouldNotBeAddOnIfPathIsDirectory() throws Exception {
		// Given
		Path file = tempDir.newFolder("addon.zap").toPath();
		// When
		boolean addOnFile = AddOn.isAddOn(file);
		// Then
		assertThat(addOnFile, is(equalTo(false)));
	}

	@Test
	public void shouldNotBeAddOnIfFileNameNotEndsWithZapExtension() throws Exception {
		// Given
		Path file = createAddOnFile("addon.txt", "alpha", "1");
		// When
		boolean addOnFile = AddOn.isAddOn(file);
		// Then
		assertThat(addOnFile, is(equalTo(false)));
	}

	@Test
	public void shouldNotBeAddOnIfAddOnDoesNotHaveManifestFile() throws Exception {
		// Given
		Path file = createEmptyAddOnFile("addon.zap");
		// When
		boolean addOnFile = AddOn.isAddOn(file);
		// Then
		assertThat(addOnFile, is(equalTo(false)));
	}

	@Test
	public void shouldBeAddOnIfPathEndsWithZapExtension() throws Exception {
		// Given
		Path file = createAddOnFile("addon.zap", "alpha", "1");
		// When
		boolean addOnFile = AddOn.isAddOn(file);
		// Then
		assertThat(addOnFile, is(equalTo(true)));
	}

	@Test
	public void shouldBeAddOnEvenIfZapExtensionIsUpperCase() throws Exception {
		// Given
		Path file = createAddOnFile("addon.ZAP", "alpha", "1");
		// When
		boolean addOnFile = AddOn.isAddOn(file);
		// Then
		assertThat(addOnFile, is(equalTo(true)));
	}

	@Test(expected = IOException.class)
	public void shouldFailToCreateAddOnFromNullFile() throws Exception {
		// Given
		Path file = null;
		// When
		new AddOn(file);
		// Then = IOException
	}

	@Test(expected = IOException.class)
	public void shouldFailToCreateAddOnFromFileWithInvalidFileName() throws Exception {
		// Given
		String invalidFileName = "addon.txt";
		Path file = createAddOnFile(invalidFileName, "alpha", "1");
		// When
		new AddOn(file);
		// Then = IOException
	}

	@Test
	@SuppressWarnings("deprecation")
	public void shouldCreateAddOnFromFileAndUseManifestData() throws Exception {
		// Given
		Path file = createAddOnFile("addon.zap", "beta", "1.6.7");
		// When
		AddOn addOn = new AddOn(file);
		// Then
		assertThat(addOn.getId(), is(equalTo("addon")));
		assertThat(addOn.getStatus(), is(equalTo(AddOn.Status.beta)));
		assertThat(addOn.getVersion().toString(), is(equalTo("1.6.7")));
		assertThat(addOn.getFileVersion(), is(equalTo(1)));
	}

	@Test
	public void shouldIgnoreStatusInFileNameWhenCreatingAddOnFromFile() throws Exception {
		// Given
		Path file = createAddOnFile("addon-alpha.zap", "release", "1");
		// When
		AddOn addOn = new AddOn(file);
		// Then
		assertThat(addOn.getStatus(), is(equalTo(AddOn.Status.release)));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void shouldIgnoreVersionInFileNameWhenCreatingAddOnFromFile() throws Exception {
		// Given
		Path file = createAddOnFile("addon-alpha-2.zap", "alpha", "3.2.10");
		// When
		AddOn addOn = new AddOn(file);
		// Then
		assertThat(addOn.getVersion().toString(), is(equalTo("3.2.10")));
		assertThat(addOn.getFileVersion(), is(equalTo(3)));
	}

	@Test
	public void shouldReturnNormalisedFileName() throws Exception {
		// Given
		Path file = createAddOnFile("addon.zap", "alpha", "2.8.1");
		AddOn addOn = new AddOn(file);
		// When
		String normalisedFileName = addOn.getNormalisedFileName();
		// Then
		assertThat(normalisedFileName, is(equalTo("addon-2.8.1.zap")));
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
		AddOn addOn = new AddOn(createAddOnFile("AddOn-release-1.zap", "release", "1"));
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

	@Test
	public void shouldBeUpdateToOlderVersionIfNewer() throws Exception {
		// Given
		AddOn olderAddOn = new AddOn(createAddOnFile("addon-2.4.8.zap", "release", "2.4.8"));
		AddOn newerAddOn = new AddOn(createAddOnFile("addon-3.5.9.zap", "release", "3.5.9"));
		// When
		boolean update = newerAddOn.isUpdateTo(olderAddOn);
		// Then
		assertThat(update, is(equalTo(true)));
	}

	@Test
	public void shouldNotBeUpdateToNewerVersionIfOlder() throws Exception {
		// Given
		AddOn olderAddOn = new AddOn(createAddOnFile("addon-2.4.8.zap", "release", "2.4.8"));
		AddOn newerAddOn = new AddOn(createAddOnFile("addon-3.5.9.zap", "release", "3.5.9"));
		// When
		boolean update = olderAddOn.isUpdateTo(newerAddOn);
		// Then
		assertThat(update, is(equalTo(false)));
	}

	@Test
	public void shouldBeAbleToRunIfItHasNoMinimumJavaVersion() throws Exception {
		// Given
		String minimumJavaVersion = null;
		String runningJavaVersion = "1.8";
		AddOn addOn = new AddOn(createAddOnFile("addon-2.4.8.zap", "release", "2.4.8", minimumJavaVersion));
		// When
		boolean canRun = addOn.canRunInJavaVersion(runningJavaVersion);
		// Then
		assertThat(canRun, is(equalTo(true)));
	}

	@Test
	public void shouldBeAbleToRunInJava9MajorIfMinimumJavaVersionIsMet() throws Exception {
		// Given
		String minimumJavaVersion = "1.8";
		String runningJavaVersion = "9";
		AddOn addOn = new AddOn(createAddOnFile("addon-2.4.8.zap", "release", "2.4.8", minimumJavaVersion));
		// When
		boolean canRun = addOn.canRunInJavaVersion(runningJavaVersion);
		// Then
		assertThat(canRun, is(equalTo(true)));
	}

	@Test
	public void shouldBeAbleToRunInJava9MinorIfMinimumJavaVersionIsMet() throws Exception {
		// Given
		String minimumJavaVersion = "1.8";
		String runningJavaVersion = "9.1.2";
		AddOn addOn = new AddOn(createAddOnFile("addon-2.4.8.zap", "release", "2.4.8", minimumJavaVersion));
		// When
		boolean canRun = addOn.canRunInJavaVersion(runningJavaVersion);
		// Then
		assertThat(canRun, is(equalTo(true)));
	}

	@Test
	public void shouldNotBeAbleToRunInJava9MajorIfMinimumJavaVersionIsNotMet() throws Exception {
		// Given
		String minimumJavaVersion = "10";
		String runningJavaVersion = "9";
		AddOn addOn = new AddOn(createAddOnFile("addon-2.4.8.zap", "release", "2.4.8", minimumJavaVersion));
		// When
		boolean canRun = addOn.canRunInJavaVersion(runningJavaVersion);
		// Then
		assertThat(canRun, is(equalTo(false)));
	}

	@Test
	public void shouldNotBeAbleToRunInJava9MinorIfMinimumJavaVersionIsNotMet() throws Exception {
		// Given
		String minimumJavaVersion = "10";
		String runningJavaVersion = "9.1.2";
		AddOn addOn = new AddOn(createAddOnFile("addon-2.4.8.zap", "release", "2.4.8", minimumJavaVersion));
		// When
		boolean canRun = addOn.canRunInJavaVersion(runningJavaVersion);
		// Then
		assertThat(canRun, is(equalTo(false)));
	}

	private static ZapXmlConfiguration createZapVersionsXml() throws Exception {
		ZapXmlConfiguration zapVersionsXml = new ZapXmlConfiguration(ZAP_VERSIONS_XML);
		zapVersionsXml.setExpressionEngine(new XPathExpressionEngine());
		return zapVersionsXml;
	}

	private Path createEmptyAddOnFile(String fileName) {
		try {
			File file = tempDir.newFile(fileName);
			new ZipOutputStream(new FileOutputStream(file)).close();
			return file.toPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Path createAddOnFile(String fileName, String status, String version) {
		return createAddOnFile(fileName, status, version, null);
	}

	private Path createAddOnFile(String fileName, String status, String version, String javaVersion) {
		try {
			File file = tempDir.newFile(fileName);
			try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
				ZipEntry manifest = new ZipEntry(AddOn.MANIFEST_FILE_NAME);
				zos.putNextEntry(manifest);
				StringBuilder strBuilder = new StringBuilder(150);
				strBuilder.append("<zapaddon>");
				strBuilder.append("<version>").append(version).append("</version>");
				strBuilder.append("<status>").append(status).append("</status>");
				if (javaVersion != null && !javaVersion.isEmpty()) {
					strBuilder.append("<dependencies>");
					strBuilder.append("<javaversion>").append(javaVersion).append("</javaversion>");
					strBuilder.append("</dependencies>");
				}
				strBuilder.append("</zapaddon>");
				byte[] bytes = strBuilder.toString().getBytes(StandardCharsets.UTF_8);
				zos.write(bytes, 0, bytes.length);
				zos.closeEntry();
			}
			return file.toPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected static AddOn createAddOn(String addOnId, ZapXmlConfiguration zapVersions) throws Exception {
		return new AddOn(addOnId, Paths.get("").toFile(), zapVersions.configurationAt("addon_" + addOnId));
	}
}
