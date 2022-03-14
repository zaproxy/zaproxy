/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.uiutils;

import java.awt.EventQueue;
import java.util.Arrays;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.MainFrame;

/**
 * This extension was introduced so that the session persist and snapshot menu item and buttons can
 * be enabled and disabled when appropriate. It much easier doing this in an extension rather than
 * the UI components.
 *
 * @author psiinon
 */
public class ExtensionUiUtils extends ExtensionAdaptor implements SessionChangedListener {

    public static final String NAME = "ExtensionUiUtils";

    private static final Logger LOGGER = LogManager.getLogger(ExtensionUiUtils.class);

    public ExtensionUiUtils() {
        super(NAME);
        this.setOrder(200);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("uiutils.name");
    }

    @Override
    public void initView(ViewDelegate view) {
        super.initView(view);

        Arrays.asList(
                        new LookAndFeelInfo("Flat Light", "com.formdev.flatlaf.FlatLightLaf"),
                        new LookAndFeelInfo("Flat Dark", "com.formdev.flatlaf.FlatDarkLaf"),
                        new LookAndFeelInfo("Flat IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf"),
                        new LookAndFeelInfo("Flat Darcula", "com.formdev.flatlaf.FlatDarculaLaf"))
                .forEach(UIManager::installLookAndFeel);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        if (getView() != null) {
            extensionHook.addSessionListener(this);
        }
    }

    @Override
    public void sessionChanged(final Session session) {
        if (EventQueue.isDispatchThread()) {
            sessionChangedEventHandler(session);

        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                sessionChangedEventHandler(session);
                            }
                        });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void sessionChangedEventHandler(Session session) {
        if (session != null && !session.isNewState()) {
            getModel().getOptionsParam().getViewParam().addLatestSession(session.getFileName());
        }

        MainFrame mainFrame = getView().getMainFrame();
        mainFrame.getMainMenuBar().sessionChanged(session);
        mainFrame.getMainToolbarPanel().sessionChanged(session);
        mainFrame.setTitle(session);
    }

    @Override
    public void sessionAboutToChange(Session session) {}

    @Override
    public void sessionScopeChanged(Session session) {}

    @Override
    public void sessionPropertiesChanged(Session session) {
        if (EventQueue.isDispatchThread()) {
            getView().getMainFrame().setTitle(session);
            return;
        }

        EventQueue.invokeLater(() -> sessionPropertiesChanged(session));
    }

    @Override
    public boolean isCore() {
        return true;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("uiutils.desc");
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        // Ignore
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
