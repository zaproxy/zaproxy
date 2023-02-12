/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.stdmenus;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;

@SuppressWarnings("serial")
public abstract class PopupContextMenuItemFactory extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 2282358266003940700L;

    private List<ExtensionPopupMenuItem> subMenus = new ArrayList<>();
    private String parentMenu;

    /** This method initializes */
    public PopupContextMenuItemFactory(String parentMenu) {
        super("ContextMenuFactory");
        this.parentMenu = parentMenu;
    }

    @Override
    public String getParentMenuName() {
        return parentMenu;
    }

    @Override
    public boolean isSubMenu() {
        return true;
    }

    @Override
    public boolean isDummyItem() {
        return true;
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        final List<JMenuItem> mainPopupMenuItems = View.getSingleton().getPopupList();
        for (ExtensionPopupMenuItem menu : subMenus) {
            mainPopupMenuItems.remove(menu);
        }
        subMenus.clear();

        // Add the existing contexts
        Session session = Model.getSingleton().getSession();
        List<Context> contexts = session.getContexts();
        for (Context context : contexts) {
            ExtensionPopupMenuItem piicm = getContextMenu(context, this.parentMenu);
            piicm.setMenuIndex(this.getMenuIndex());
            mainPopupMenuItems.add(piicm);
            this.subMenus.add(piicm);
        }
        return false;
    }

    public abstract ExtensionPopupMenuItem getContextMenu(Context context, String parentMenu);

    @Override
    public boolean isSafe() {
        return true;
    }
}
