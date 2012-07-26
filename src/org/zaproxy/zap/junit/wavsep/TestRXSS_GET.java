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

public class TestRXSS_GET {

	@Test
	public void testCase01 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case01-Tag2HtmlPageScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase02 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case02-Tag2TagScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase03 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case03-Tag2TagStructure.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase04 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case04-Tag2HtmlComment.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase05 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case05-Tag2Frameset.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase06 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case06-Event2TagScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase07 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case07-Event2DoubleQuotePropertyScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase08 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case08-Event2SingleQuotePropertyScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase09 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case09-SrcProperty2TagStructure.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase10 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case10-Js2DoubleQuoteJsEventScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase11 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case11-Js2SingleQuoteJsEventScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase12 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case12-Js2JsEventScope.jsp?userinput=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase13 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case13-Vbs2DoubleQuoteVbsEventScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase14 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case14-Vbs2SingleQuoteVbsEventScope.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase15 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case15-Vbs2VbsEventScope.jsp?userinput=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase16 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case16-Js2ScriptSupportingProperty.jsp?userinput=dummy.html",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase17 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case17-Js2PropertyJsScopeDoubleQuoteDelimiter.jsp?userinput=david",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase18 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case18-Js2PropertyJsScopeSingleQuoteDelimiter.jsp?userinput=david",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase19 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case19-Js2PropertyJsScope.jsp?userinput=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase20 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case20-Vbs2PropertyVbsScopeDoubleQuoteDelimiter.jsp?userinput=david",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase21 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case21-Vbs2PropertyVbsScope.jsp?userinput=david",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase22 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case22-Js2ScriptTagDoubleQuoteDelimiter.jsp?userinput=david",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase23 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case23-Js2ScriptTagSingleQuoteDelimiter.jsp?userinput=david",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase24 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case24-Js2ScriptTag.jsp?userinput=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase25 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case25-Vbs2ScriptTagDoubleQuoteDelimiter.jsp?userinput=david",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase26 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case26-Vbs2ScriptTag.jsp?userinput=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase27 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case27-Js2ScriptTagOLCommentScope.jsp?userinput=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase28 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case28-Js2ScriptTagMLCommentScope.jsp?userinput=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase29 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case29-Vbs2ScriptTagOLCommentScope.jsp?userinput=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase30 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case30-Tag2HtmlPageScopeMultipleVulnerabilities.jsp?userinput=1234&userinput2=1234",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING, WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase31 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case31-Tag2HtmlPageScopeDuringException.jsp?userinput=textvalue",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}

	@Test
	public void testCase32 () throws Exception {
		WavsepStatic.genericTest("/wavsep/active/RXSS-Detection-Evaluation-GET/Case32-Tag2HtmlPageScopeValidViewstateRequired.jsp?userinput=textvalue&__VIEWSTATE=%2FwEPDwUENTM4MWRkhsjF%2B62gWnhYUcEyuRwTHxGDVzA%3D",
				// Ignore alerts
				new Alert[] {
					WavsepStatic.INFO_DISCLOSURE_IN_URL, WavsepStatic.X_CONTENT_TYPE_HEADER_MISSING, WavsepStatic.X_FRAME_OPTIONS_HEADER_MISSING},
				// Require alerts
				new Alert[] {
					WavsepStatic.CROSS_SITE_SCRIPTING});
	}
}
