/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
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
package org.zaproxy.zap.extension.brk;

import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the breakpoints configurations saved in the configuration file.
 * <p>
 * It allows to change, programmatically, the following breakpoints options:
 * <ul>
 * <li>Confirm drop message - asks for confirmation when a trapped message is dropped.</li>
 * </ul>
 * </p>
 * 
 * @see org.zaproxy.zap.extension.brk.BreakPanelToolbarFactory#getBtnDrop()
 */
public class BreakpointsParam extends AbstractParam {

	public static final int BUTTON_MODE_SIMPLE = 1;
	public static final int BUTTON_MODE_DUAL = 2;
	
    private static final String PARAM_BASE_KEY = "breakpoints";

    private static final String PARAM_CONFIRM_DROP_MESSAGE_KEY = PARAM_BASE_KEY + ".confirmDropMessage";
    private static final String PARAM_UI_BUTTON_MODE = PARAM_BASE_KEY + ".buttonMode";
    private static final String PARAM_BRK_ALWAYS_ON_TOP = PARAM_BASE_KEY + ".alwaysOnTop";
    private static final String PARAM_BRK_IN_SCOPE_ONLY = PARAM_BASE_KEY + ".inScopeOnly";

    /**
     * Default is {@code false}.
     */
    private boolean confirmDropMessage;
    private int buttonMode = BUTTON_MODE_SIMPLE;
    private Boolean alwaysOnTop = null;
    private boolean inScopeOnly = false;

    public BreakpointsParam() {
        super();

        confirmDropMessage = false;
    }

    /**
     * Parses the breakpoints options.
     * <p>
     * The following options are parsed:
     * <ul>
     * <li>Confirm drop message.</li>
     * </ul>
     * </p>
     */
    @Override
    protected void parse() {
        confirmDropMessage = getConfig().getBoolean(PARAM_CONFIRM_DROP_MESSAGE_KEY, false);
        buttonMode = getConfig().getInt(PARAM_UI_BUTTON_MODE, BUTTON_MODE_SIMPLE);
        alwaysOnTop = getConfig().getBoolean(PARAM_BRK_ALWAYS_ON_TOP, null);
        inScopeOnly = getConfig().getBoolean(PARAM_BRK_IN_SCOPE_ONLY, false);
    }

    /**
     * Tells whether the user should confirm the drop of the trapped message.
     * 
     * @return {@code true} if the user should confirm the drop, {@code false} otherwise
     * @see #setConfirmDropMessage(boolean)
     */
    public boolean isConfirmDropMessage() {
        return confirmDropMessage;
    }

    /**
     * Sets whether the user should confirm the drop of the trapped message.
     * 
     * @param confirmDrop {@code true} if the user should confirm the drop, {@code false} otherwise
     * @see #isConfirmDropMessage()
     * @see org.zaproxy.zap.extension.brk.BreakPanelToolbarFactory#getBtnDrop()
     */
    public void setConfirmDropMessage(boolean confirmDrop) {
        if (confirmDropMessage != confirmDrop) {
            this.confirmDropMessage = confirmDrop;
            getConfig().setProperty(PARAM_CONFIRM_DROP_MESSAGE_KEY, Boolean.valueOf(confirmDrop));
        }
    }

	public int getButtonMode() {
		return buttonMode;
	}

	public void setButtonMode(int buttonMode) {
		this.buttonMode = buttonMode;
        getConfig().setProperty(PARAM_UI_BUTTON_MODE, buttonMode);
	}

	public Boolean getAlwaysOnTop() {
		return alwaysOnTop;
	}

	public void setAlwaysOnTop(Boolean alwaysOnTop) {
		this.alwaysOnTop = alwaysOnTop;
        getConfig().setProperty(PARAM_BRK_ALWAYS_ON_TOP, Boolean.valueOf(alwaysOnTop));
	}

	public boolean isInScopeOnly() {
		return inScopeOnly;
	}

	public void setInScopeOnly(boolean inScopeOnly) {
		this.inScopeOnly = inScopeOnly;
        getConfig().setProperty(PARAM_BRK_IN_SCOPE_ONLY, inScopeOnly);
	}
}
