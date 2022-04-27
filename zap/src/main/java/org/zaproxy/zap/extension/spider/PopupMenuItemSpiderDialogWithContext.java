/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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

import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.stdmenus.PopupContextTreeMenu;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.Target;

/**
 * A {@code PopupContextTreeMenu} that allows to show the Spider dialogue for a selected {@link
 * Context}.
 *
 * @see ExtensionSpider#showSpiderDialog(Target)
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class PopupMenuItemSpiderDialogWithContext extends PopupContextTreeMenu {

    private static final long serialVersionUID = 1L;

    public PopupMenuItemSpiderDialogWithContext(ExtensionSpider extension) {
        super(false);

        this.setText(extension.getMessages().getString("spider.custom.popup"));
        this.setIcon(extension.getIcon());

        this.addActionListener(
                e -> {
                    Context context = Model.getSingleton().getSession().getContext(getContextId());
                    extension.showSpiderDialog(new Target(context));
                });
    }
}
