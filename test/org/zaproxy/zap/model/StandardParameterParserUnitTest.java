/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.model.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class StandardParameterParserUnitTest {

	//@Mock
	Session session;

	private StandardParameterParser spp;

	@Before
	public void setUp() throws Exception {
		spp = new StandardParameterParser();
	}

	@Test
	public void defaultValues() {
		assertEquals(spp.getDefaultKeyValuePairSeparator(), "&");
		assertEquals(spp.getDefaultKeyValueSeparator(), "=");
	}

	@Test
	public void defaultParser() {
		assertEquals(spp.getKeyValuePairSeparators(), "&");
		assertEquals(spp.getKeyValueSeparators(), "=");
		assertEquals(spp.getStructuralParameters().size(), 0);
		Map<String, String> res = spp.parse("a=b&b=c&d=f");
		assertEquals(res.size(), 3);
		assertEquals(res.get("a"), "b");
		assertEquals(res.get("b"), "c");
		assertEquals(res.get("d"), "f");
	}

	@Test
	public void nonDefaultParser() {
		spp.setKeyValuePairSeparators(";");
		spp.setKeyValueSeparators(":=");
		List<String> sps = new ArrayList<String>();
		sps.add("page");
		spp.setStructuralParameters(sps );
		Map<String, String> res = spp.parse("a=b&c;b:c");

		assertEquals(spp.getKeyValuePairSeparators(), ";");
		assertEquals(spp.getKeyValueSeparators(), ":=");
		assertEquals(spp.getStructuralParameters().size(), 1);
		assertEquals(spp.getStructuralParameters().get(0), "page");

		assertEquals(res.size(), 2);
		assertEquals(res.get("a"), "b&c");
		assertEquals(res.get("b"), "c");
	}

	@Test
	public void saveAndLoad() {
		spp.setKeyValuePairSeparators(";");
		spp.setKeyValueSeparators(":=");
		List<String> sps = new ArrayList<String>();
		sps.add("page");
		spp.setStructuralParameters(sps );
		
		StandardParameterParser spp2 = new StandardParameterParser();
		spp2.init(spp.getConfig());

		Map<String, String> res = spp2.parse("a=b&c;b:c");

		assertEquals(spp2.getKeyValuePairSeparators(), ";");
		assertEquals(spp2.getKeyValueSeparators(), ":=");
		assertEquals(spp2.getStructuralParameters().size(), 1);
		assertEquals(spp2.getStructuralParameters().get(0), "page");

		assertEquals(res.size(), 2);
		assertEquals(res.get("a"), "b&c");
		assertEquals(res.get("b"), "c");
	}

}
