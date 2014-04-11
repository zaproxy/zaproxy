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
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AddOnUnitTest {

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
		assertThat(addOn.getVersion(), is(1));
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

	@Test
	public void testAlpha2DoesNotUpdateTestyAlpha1() throws Exception {
		AddOn addOnA1 = new AddOn("test-alpha-1.zap");
		AddOn addOnA2 = new AddOn("testy-alpha-2.zap");
		
		try {
			assertFalse(addOnA2.isUpdateTo(addOnA1));
			fail("Should have thrown an exception");
		} catch (Exception e) {
			// Passed
		}
	}
	
	@Test
	public void testCanLoadAddOnNotBefore() throws Exception {
		AddOn ao = new AddOn("test-alpha-1.zap");
		assertTrue(ao.canLoadInVersion("2.0.0"));
		
		ao.setNotBeforeVersion("2.0.0");
		assertTrue(ao.canLoadInVersion("2.0.0"));
		assertTrue(ao.canLoadInVersion("2.1.0"));
		assertFalse(ao.canLoadInVersion("1.4.0"));
		assertFalse(ao.canLoadInVersion("2.0.alpha"));
	}

	@Test
	public void testCanLoadAddOnNotFrom() throws Exception {
		AddOn ao = new AddOn("test-alpha-1.zap");
		ao.setNotFromVersion("2.3.0");
		assertTrue(ao.canLoadInVersion("1.4.0"));
		assertTrue(ao.canLoadInVersion("2.1.0"));
		assertTrue(ao.canLoadInVersion("2.2.0"));
		assertFalse(ao.canLoadInVersion("2.3.0"));
		assertFalse(ao.canLoadInVersion("2.3.0.1"));
		assertFalse(ao.canLoadInVersion("2.4.0"));
	}

	@Test
	public void testCanLoadAddOnNotBeforeNotFrom() throws Exception {
		AddOn ao = new AddOn("test-alpha-1.zap");
		assertTrue(ao.canLoadInVersion("2.0.0"));
		ao.setNotBeforeVersion("2.0.0");
		ao.setNotFromVersion("2.3.0");
		assertTrue(ao.canLoadInVersion("2.0.0"));
		assertTrue(ao.canLoadInVersion("2.1.0"));
		assertTrue(ao.canLoadInVersion("2.2.0"));
		assertFalse(ao.canLoadInVersion("2.3.0"));
		assertFalse(ao.canLoadInVersion("2.3.0.1"));
		assertFalse(ao.canLoadInVersion("2.4.0"));
		
	}
}
