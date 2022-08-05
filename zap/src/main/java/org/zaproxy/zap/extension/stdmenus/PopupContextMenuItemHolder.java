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

import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;
import org.zaproxy.zap.view.popup.ExtensionPopupMenuMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemContext;

/**
 * The Class PopupContextMenuItemHolder is used as a holder for multiple {@link
 * PopupMenuItemContext}. Depending on the initialization, it can be shown by itself containing the
 * Popup Menus for each Context or it can just place the Popup Menus in its parent.
 */
@SuppressWarnings("serial")
public abstract class PopupContextMenuItemHolder extends ExtensionPopupMenuMessageContainer {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2976582263073602339L;

    /** The parent's name. */
    private String parentName;

    /**
     * The sub menus. Used only in the case it is not visible itself, to keep a reference to what
     * popup menus it has added in the parent, so it can dynamically update them before each show.
     */
    private List<ExtensionPopupMenuItem> subMenuItems = null;

    /** Whether it is visible itself. */
    private boolean visibleItself;

    /**
     * Instantiates a new popup context menu item holder. This initializes the holder so that the
     * Popup Menus for each Context are shown as submenus of this Holder.
     *
     * @param label the label
     * @param parentName the parent menu's name
     */
    public PopupContextMenuItemHolder(String label, String parentName) {
        super(label);
        this.parentName = parentName;
        this.visibleItself = true;
    }

    /**
     * Instantiates a new popup context menu item holder. This initializes the holder so that the
     * Popup Menus for each Context are shown as submenus of the parent, the holder not being
     * visible.
     *
     * @param parentName the parent menu's name
     */
    public PopupContextMenuItemHolder(String parentName) {
        super("ContextMenuItemHolder");
        this.parentName = parentName;
        this.visibleItself = false;
    }

    @Override
    public String getParentMenuName() {
        return this.parentName;
    }

    @Override
    public int getParentMenuIndex() {
        return 0;
    }

    @Override
    public boolean isSubMenu() {
        return true;
    }

    /**
     * Gets the submenu items.
     *
     * @return the submenu items
     */
    private List<ExtensionPopupMenuItem> getSubmenuItems() {
        if (subMenuItems == null) subMenuItems = new ArrayList<>();
        return subMenuItems;
    }

    @Override
    public boolean isEnableForMessageContainer(MessageContainer<?> invoker) {
        resetMenu();

        if (visibleItself) {
            return super.isEnableForMessageContainer(invoker);
        }
        return false;
    }

    private void resetMenu() {
        final List<JMenuItem> mainPopupMenuItems = View.getSingleton().getPopupList();
        // Remove existing popup menu items
        if (visibleItself) this.removeAll();
        else {
            for (ExtensionPopupMenuItem menu : getSubmenuItems()) {
                mainPopupMenuItems.remove(menu);
            }
            subMenuItems.clear();
        }

        // Add a popup menu item for each existing context
        Session session = Model.getSingleton().getSession();
        List<Context> contexts = session.getContexts();
        for (Context context : contexts) {
            ExtensionPopupMenuItem piicm;
            if (visibleItself) {
                piicm = getPopupContextMenu(context, this.getText());
                this.add(piicm);
            } else {
                piicm = getPopupContextMenu(context, this.parentName);
                piicm.setMenuIndex(this.getMenuIndex());
                mainPopupMenuItems.add(piicm);
                subMenuItems.add(piicm);
            }
        }
    }

    /**
     * Gets the {@link PopupMenuItemContext} associated with a particular context.
     *
     * @param context the context
     * @param parentName the parent menu's name
     * @return the popup context menu
     */
    public abstract ExtensionPopupMenuItem getPopupContextMenu(Context context, String parentName);
}
