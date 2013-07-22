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
package org.zaproxy.zap.extension.encoder2;

import org.parosproxy.paros.common.AbstractParam;

class EncodeDecodeParam extends AbstractParam {

	private static final String PARAM_BASE64_CHARSET = "encode.param.base64charset";
	private static final String PARAM_BASE64_DO_BREAK_LINES = "encode.param.base64dobreaklines";
	
	private String base64Charset;
	private boolean base64DoBreakLines;
	
	public EncodeDecodeParam() {
		base64Charset = "UTF-8";
		base64DoBreakLines = true;
	}
	
	@Override
	protected void parse() {
		base64Charset = getConfig().getString(PARAM_BASE64_CHARSET, base64Charset);
		
		base64DoBreakLines = getConfig().getBoolean(PARAM_BASE64_DO_BREAK_LINES, base64DoBreakLines);
	}

	public String getBase64Charset() {
		return base64Charset;
	}

	public void setBase64Charset(String base64FromCharset) {
		this.base64Charset = base64FromCharset;
		getConfig().setProperty(PARAM_BASE64_CHARSET, base64FromCharset);
	}

	public boolean isBase64DoBreakLines() {
		return base64DoBreakLines;
	}

	public void setBase64DoBreakLines(boolean base64OuputBreak) {
		this.base64DoBreakLines = base64OuputBreak;
		getConfig().setProperty(PARAM_BASE64_DO_BREAK_LINES, Boolean.valueOf(base64OuputBreak));
	}

}
