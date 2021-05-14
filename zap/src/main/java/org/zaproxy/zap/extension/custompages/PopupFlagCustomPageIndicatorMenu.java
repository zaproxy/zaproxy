/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.custompages;

import java.awt.Component;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.model.Context;

/** The Popup Menu item used for marking a text in the response panel as a {@code CustomPage}. */
public class PopupFlagCustomPageIndicatorMenu extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 6071498013529265858L;
    private String selectedText = null;
    private int contextId;

    public PopupFlagCustomPageIndicatorMenu(Context ctx) {
        this.contextId = ctx.getId();

        this.setText(Constant.messages.getString("custompages.popup.indicator", ctx.getName()));
        this.addActionListener(event -> performAction());
    }

    private void performAction() {
        Context currentContext = Model.getSingleton().getSession().getContext(this.contextId);

        DialogAddCustomPage dialogAddCustomPage =
                getDialogAddCustomPage(currentContext, getSelectedText());
        dialogAddCustomPage.setVisible(true);
        currentContext.addCustomPage(dialogAddCustomPage.getCustomPage());
    }

    private DialogAddCustomPage getDialogAddCustomPage(
            Context currentContext, String selectedText) {
        DialogAddCustomPage dialogAddCustomPage =
                new DialogAddCustomPage(View.getSingleton().getMainFrame());
        dialogAddCustomPage.setWorkingContext(currentContext);
        dialogAddCustomPage.getPageMatcherTextField().setText(Pattern.quote(selectedText));
        dialogAddCustomPage.getRegexCheckBox().setSelected(true);
        return dialogAddCustomPage;
    }

    @Override
    public boolean isSubMenu() {
        return true;
    }

    @Override
    public String getParentMenuName() {
        return Constant.messages.getString("context.flag.popup");
    }

    @Override
    public int getParentMenuIndex() {
        return CONTEXT_FLAG_MENU_INDEX;
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTextComponent) {
            // Is it the HttpPanelResponse?
            JTextComponent txtComponent = (JTextComponent) invoker;
            boolean responsePanel =
                    (SwingUtilities.getAncestorOfClass(HttpPanelResponse.class, txtComponent)
                            != null);

            if (!responsePanel) {
                selectedText = null;
                return false;
            }

            // Is anything selected?
            selectedText = txtComponent.getSelectedText();
            this.setEnabled(selectedText != null && selectedText.length() != 0);

            return true;
        } else {
            selectedText = null;
            return false;
        }
    }

    public String getSelectedText() {
        return selectedText;
    }
}
