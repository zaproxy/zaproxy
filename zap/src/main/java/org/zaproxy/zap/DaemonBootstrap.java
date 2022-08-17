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

import java.io.FileNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.View;

/**
 * The bootstrap process for daemon mode.
 *
 * @since 2.4.2
 */
class DaemonBootstrap extends HeadlessBootstrap {

    private final Logger logger = LogManager.getLogger(DaemonBootstrap.class);

    public DaemonBootstrap(CommandLine cmdLineArgs) {
        super(cmdLineArgs);
    }

    @Override
    public int start() {
        int rc = super.start();
        if (rc != 0) {
            return rc;
        }

        View.setDaemon(true); // Prevents the View ever being initialised

        logger.info(getStartingMessage());

        try {
            initModel();
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                System.out.println(Constant.messages.getString("start.db.error"));
                System.out.println(e.getLocalizedMessage());
            }

            logger.fatal(e.getMessage(), e);
            return 1;
        }

        // start in a background thread
        final Thread t =
                new Thread(
                        new Runnable() {

                            @Override
                            public void run() {
                                Control control;
                                try {
                                    control = initControl();
                                } catch (IllegalStateException e) {
                                    System.err.println("Failed to start ZAP. " + e.getMessage());
                                    return;
                                }

                                warnAddOnsAndExtensionsNoLongerRunnable();

                                if (!handleCmdLineSessionArgsSynchronously(control)) {
                                    return;
                                }

                                HeadlessBootstrap.checkForUpdates();

                                try {
                                    // Allow extensions to pick up command line args in daemon mode
                                    control.getExtensionLoader().hookCommandLineListener(getArgs());
                                    control.runCommandLine();
                                } catch (ShutdownRequestedException e) {
                                    control.shutdown(false);
                                    logger.info("{} terminated.", Constant.PROGRAM_TITLE);
                                    return;
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }

                                // This is the only non-daemon thread, so should keep running
                                // CoreAPI.handleApiAction uses System.exit to shutdown
                                while (true) {
                                    try {
                                        Thread.sleep(100000);

                                    } catch (InterruptedException e) {
                                        // Ignore
                                    }
                                }
                            }
                        });

        t.setName("ZAP-daemon");
        t.start();

        return 0;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
