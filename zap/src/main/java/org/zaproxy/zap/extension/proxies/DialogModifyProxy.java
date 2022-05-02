/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.extension.proxies;

import java.awt.Dialog;
import org.parosproxy.paros.Constant;

/** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
@Deprecated
class DialogModifyProxy extends DialogAddProxy {

    private static final long serialVersionUID = 6675509994290748494L;

    private static final String DIALOG_TITLE =
            Constant.messages.getString("options.proxy.dialog.proxy.modify.title");

    private static final String CONFIRM_BUTTON_LABEL =
            Constant.messages.getString("options.proxy.dialog.proxy.modify.button.confirm");

    protected DialogModifyProxy(Dialog owner, ExtensionProxies extension) {
        super(owner, DIALOG_TITLE, extension);
        setConfirmButtonEnabled(true);
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    public void setProxy(ProxiesParamProxy paramProxy) {
        this.proxy = paramProxy;
        this.proxyPanel.setProxy(proxy);
    }

    @Override
    protected boolean validateFields() {
        ProxiesParamProxy testProxy = proxyPanel.getProxy();
        if (ExtensionProxies.isSameAddress(testProxy.getAddress(), proxy.getAddress())
                && testProxy.getPort() == proxy.getPort()) {
            // No change, assume its ok
            return true;
        }
        return super.validateFields();
    }

    @Override
    protected void init() {
        this.setProxy(proxy);
    }
}
