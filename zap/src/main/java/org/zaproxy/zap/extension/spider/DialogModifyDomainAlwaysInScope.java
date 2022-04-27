/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.spider;

import java.awt.Dialog;
import org.parosproxy.paros.Constant;

/** @deprecated (2.12.0) See the spider add-on in zap-extensions instead. */
@Deprecated
class DialogModifyDomainAlwaysInScope extends DialogAddDomainAlwaysInScope {

    private static final long serialVersionUID = -4031122965844883255L;

    private static final String DIALOG_TITLE =
            Constant.messages.getString("spider.options.domains.in.scope.modify.title");

    private static final String CONFIRM_BUTTON_LABEL =
            Constant.messages.getString("spider.options.domains.in.scope.modify.button.confirm");

    protected DialogModifyDomainAlwaysInScope(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    public void setDomainAlwaysInScope(
            org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher excludedDomain) {
        this.domainAlwaysInScope = excludedDomain;
    }

    @Override
    protected void init() {
        getDomainTextField().setText(domainAlwaysInScope.getValue());
        getDomainTextField().discardAllEdits();

        getRegexCheckBox().setSelected(domainAlwaysInScope.isRegex());

        getEnabledCheckBox().setSelected(domainAlwaysInScope.isEnabled());
    }
}
