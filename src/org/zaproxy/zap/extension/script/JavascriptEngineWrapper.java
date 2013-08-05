/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP development team
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

package org.zaproxy.zap.extension.script;

import javax.script.ScriptEngine;
import javax.swing.ImageIcon;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.zaproxy.zap.ZAP;

public class JavascriptEngineWrapper extends DefaultEngineWrapper {

	private static final ImageIcon ICON = 
			new ImageIcon(ZAP.class.getResource("/resource/icon/16/cup.png"));

	public JavascriptEngineWrapper(ScriptEngine engine) {
		super(engine);
	}

	@Override
	public ImageIcon getIcon() {
		return ICON;
	}

	@Override
	public String getSyntaxStyle() {
		return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
	}

	@Override
	public boolean isTextBased() {
		return true;
	}

	@Override
	public boolean isRawEngine() {
		return false;
	}

}
