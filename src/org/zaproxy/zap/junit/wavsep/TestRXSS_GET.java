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
package org.zaproxy.zap.junit.wavsep;

import org.junit.Test;
import org.zaproxy.clientapi.core.Alert;
import org.zaproxy.clientapi.core.Alert.Reliability;
import org.zaproxy.clientapi.core.Alert.Risk;

public class TestRXSS_GET {

	
	@Test
	public void testCase01 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case01-Tag2HtmlPageScope.jsp?userinput=textvalue", 
				// Ignore alerts
				new Alert[] {
					new Alert("Information disclosure - sensitive informations in URL", "", Risk.Informational, Reliability.Warning, "userinput", ""),
					new Alert("X-Content-Type-Options header missing", "", Risk.Low, Reliability.Warning, "userinput", ""),
					new Alert("X-Frame-Options header not set", "", Risk.Informational, Reliability.Warning, "", "")}, 
				// Require alerts
				new Alert[] {
					new Alert("Cross Site Scripting", "", Risk.High, Reliability.Warning, "userinput", "")});
	}

	@Test
	public void testCase02 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case02-Tag2TagScope.jsp?userinput=textvalue", 
				// Ignore alerts
				new Alert[] {
					new Alert("Information disclosure - sensitive informations in URL", "", Risk.Informational, Reliability.Warning, "userinput", ""),
					new Alert("X-Content-Type-Options header missing", "", Risk.Low, Reliability.Warning, "userinput", ""),
					new Alert("X-Frame-Options header not set", "", Risk.Informational, Reliability.Warning, "", "")}, 
				// Require alerts
				new Alert[] {
					new Alert("Cross Site Scripting", "", Risk.High, Reliability.Warning, "userinput", "")});
	}

	@Test
	public void testCase03 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case03-Tag2TagStructure.jsp?userinput=textvalue", 
				// Ignore alerts
				new Alert[] {
					new Alert("Information disclosure - sensitive informations in URL", "", Risk.Informational, Reliability.Warning, "userinput", ""),
					new Alert("X-Content-Type-Options header missing", "", Risk.Low, Reliability.Warning, "userinput", ""),
					new Alert("X-Frame-Options header not set", "", Risk.Informational, Reliability.Warning, "", "")}, 
				// Require alerts
				new Alert[] {
					new Alert("Cross Site Scripting", "", Risk.High, Reliability.Warning, "userinput", "")});
	}

}
