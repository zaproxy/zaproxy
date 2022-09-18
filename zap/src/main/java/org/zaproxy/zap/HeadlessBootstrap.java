/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnLoader;
import org.zaproxy.zap.control.AddOnRunIssuesUtils;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.extension.autoupdate.ExtensionAutoUpdate;
import org.zaproxy.zap.model.SessionUtils;

/**
 * A {@code ZapBootstrap} with common functionalities for headless modes (command line and daemon).
 *
 * @since 2.4.2
 */
abstract class HeadlessBootstrap extends ZapBootstrap {

    public HeadlessBootstrap(CommandLine args) {
        super(args);

        // XXX Do not force headless to allow to run JxBrowser in daemon mode,
        // at least until it can be run without a window/dialogue.
        // System.setProperty("java.awt.headless", "true");
    }

    /**
     * Initialises the {@code Control} singleton without view and proxy.
     *
     * @return the initialised {@code Control} singleton.
     * @throws IllegalStateException if an error occurred while initialising the {@code Control}.
     * @see Control#initSingletonWithoutViewAndProxy(org.zaproxy.zap.control.ControlOverrides)
     */
    protected Control initControl() {
        Control.initSingletonWithoutView(getControlOverrides());
        return Control.getSingleton();
    }

    /**
     * Gets the logger of the class. Used to log errors/warnings that occur during bootstrap.
     *
     * @return the logger of the class, must not be {@code null}.
     */
    protected abstract Logger getLogger();

    /**
     * Handles command line session related arguments, synchronously.
     *
     * @param control the {@code Control} singleton
     * @return {@code true} if the arguments were handled successfully, {@code false} otherwise.
     */
    protected boolean handleCmdLineSessionArgsSynchronously(Control control) {
        if (getArgs().isEnabled(CommandLine.SESSION)
                && getArgs().isEnabled(CommandLine.NEW_SESSION)) {
            System.err.println(
                    "Error: Invalid command line options: option '"
                            + CommandLine.SESSION
                            + "' not allowed with option '"
                            + CommandLine.NEW_SESSION
                            + "'");

            return false;
        }

        if (getArgs().isEnabled(CommandLine.SESSION)) {
            String path = getArgs().getArgument(CommandLine.SESSION);
            Path sessionPath;
            try {
                sessionPath = SessionUtils.getSessionPath(path);
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to open session, file path is not valid: " + path);
                return false;
            }
            String absolutePath = sessionPath.toAbsolutePath().toString();
            try {
                control.runCommandLineOpenSession(absolutePath);

            } catch (Exception e) {
                getLogger().error(e.getMessage(), e);
                System.err.println("Failed to open session: " + absolutePath);
                e.printStackTrace(System.err);
                return false;
            }

        } else if (getArgs().isEnabled(CommandLine.NEW_SESSION)) {
            String path = getArgs().getArgument(CommandLine.NEW_SESSION);
            Path sessionPath;
            try {
                sessionPath = SessionUtils.getSessionPath(path);
            } catch (IllegalArgumentException e) {
                System.err.println(
                        "Failed to create a new session, file path is not valid: " + path);
                return false;
            }

            String absolutePath = sessionPath.toAbsolutePath().toString();
            if (Files.exists(sessionPath)) {
                System.err.println(
                        "Failed to create a new session, file already exists: " + absolutePath);
                return false;
            }

            try {
                control.runCommandLineNewSession(absolutePath);

            } catch (Exception e) {
                getLogger().error(e.getMessage(), e);
                System.err.println("Failed to create a new session: " + absolutePath);
                e.printStackTrace(System.err);
                return false;
            }
        }

        return true;
    }

    /**
     * Warns, through logging, about add-ons and extensions that are no longer runnable because of
     * changes in its dependencies.
     */
    protected void warnAddOnsAndExtensionsNoLongerRunnable() {
        final AddOnLoader addOnLoader = ExtensionFactory.getAddOnLoader();
        List<String> idsAddOnsNoLongerRunning =
                addOnLoader.getIdsAddOnsWithRunningIssuesSinceLastRun();
        if (idsAddOnsNoLongerRunning.isEmpty()) {
            return;
        }

        List<AddOn> addOnsNoLongerRunning = new ArrayList<>(idsAddOnsNoLongerRunning.size());
        for (String id : idsAddOnsNoLongerRunning) {
            addOnsNoLongerRunning.add(addOnLoader.getAddOnCollection().getAddOn(id));
        }

        for (AddOn addOn : addOnsNoLongerRunning) {
            AddOn.AddOnRunRequirements requirements =
                    addOn.calculateRunRequirements(addOnLoader.getAddOnCollection().getAddOns());
            List<String> issues = AddOnRunIssuesUtils.getRunningIssues(requirements);
            if (issues.isEmpty()) {
                issues = AddOnRunIssuesUtils.getExtensionsRunningIssues(requirements);
            }

            getLogger()
                    .warn(
                            "Add-on \"{}\" or its extensions will no longer be run until its requirements are restored: {}",
                            addOn.getId(),
                            issues);
        }
    }

    protected static void checkForUpdates() {
        ExtensionAutoUpdate eau =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionAutoUpdate.class);
        if (eau != null) {
            eau.alertIfNewVersions();
        }
    }
}
