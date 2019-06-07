/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.log4j;

import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.ScanStatus;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * This class adds a count of the number of log4j errors encountered and outputs the details to the
 * Output tab. It will only be enabled in a developer build.
 *
 * @author Psiinon
 */
public class ExtensionLog4j extends ExtensionAdaptor {

    private static final String NAME = "ExtensionLog4j";

    private ZapMenuItem menuGarbageCollect = null;

    private ScanStatus scanStatus;

    public ExtensionLog4j() {
        super(NAME);
        this.setOrder(56);

        if (Constant.isDevMode() && View.isInitialised()) {
            // Only enable if this is a developer build, ie build from source, or explicitly enabled

            scanStatus =
                    new ScanStatus(
                            new ImageIcon(
                                    ExtensionLog4j.class.getResource(
                                            "/resource/icon/fugue/bug.png")),
                            Constant.messages.getString("log4j.icon.title"));

            Logger.getRootLogger().addAppender(new ZapOutputWriter(scanStatus));

            View.getSingleton()
                    .getMainFrame()
                    .getMainFooterPanel()
                    .addFooterToolbarRightLabel(scanStatus.getCountLabel());
        }
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("log4j.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        if (getView() != null) {
            extensionHook.getHookMenu().addToolsMenuItem(getMenuGarbageCollect());

            if (scanStatus != null) {
                extensionHook.addSessionListener(new ResetCounterOnSessionChange(scanStatus));
            }
        }
    }

    private ZapMenuItem getMenuGarbageCollect() {
        if (menuGarbageCollect == null) {
            menuGarbageCollect = new ZapMenuItem("log4j.tools.menu.gc");

            menuGarbageCollect.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            Runtime.getRuntime().gc();
                        }
                    });
        }
        return menuGarbageCollect;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("log4j.desc");
    }

    @Override
    public URL getURL() {
        try {
            return new URL(Constant.ZAP_HOMEPAGE);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    private static class ResetCounterOnSessionChange implements SessionChangedListener {
        /** Keep track of errors logged while the session changes. */
        private int previousCount;
        /** Do not reset the counter if ZAP is starting. */
        private boolean starting;

        private ScanStatus scanStatus;

        public ResetCounterOnSessionChange(ScanStatus scanStatus) {
            this.scanStatus = scanStatus;
            this.starting = true;
        }

        @Override
        public void sessionAboutToChange(Session session) {
            EventQueue.invokeLater(
                    () -> {
                        previousCount = scanStatus.getScanCount();
                    });
        }

        @Override
        public void sessionChanged(Session session) {
            if (starting) {
                starting = false;
                return;
            }

            EventQueue.invokeLater(
                    () -> {
                        scanStatus.setScanCount(scanStatus.getScanCount() - previousCount);
                        previousCount = 0;
                    });
        }

        @Override
        public void sessionScopeChanged(Session session) {
            // Nothing to do.
        }

        @Override
        public void sessionModeChanged(Mode mode) {
            // Nothing to do.
        }
    }
}
