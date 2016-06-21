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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;

/**
 * The bootstrap process for command line mode.
 *
 * @since 2.4.2
 */
public class CommandLineBootstrap extends HeadlessBootstrap {

    private final Logger logger = Logger.getLogger(CommandLineBootstrap.class);

    public CommandLineBootstrap(CommandLine cmdLineArgs) {
        super(cmdLineArgs);
    }

    @Override
    public int start() {
        int rc = super.start();
        if (rc != 0) {
            return rc;
        }

        // Turn off log4j
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new NullAppender());
        Logger.getRootLogger().setLevel(Level.OFF);

        try {
            initModel();
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                System.out.println(Constant.messages.getString("start.db.error"));
                System.out.println(e.getLocalizedMessage());
            }

            throw new RuntimeException(e);
        }

        Control control = initControl(true);

        warnAddOnsAndExtensionsNoLongerRunnable();

        try {
            control.getExtensionLoader().hookCommandLineListener(getArgs());
            if (getArgs().isEnabled(CommandLine.HELP) || getArgs().isEnabled(CommandLine.HELP2)) {
                System.out.println(getArgs().getHelp());

            } else if (getArgs().isReportVersion()) {
                System.out.println(Constant.PROGRAM_VERSION);

            } else {
                if (handleCmdLineSessionArgsSynchronously(control)) {
                    control.runCommandLine();

                    try {
                        Thread.sleep(1000);

                    } catch (final InterruptedException e) {
                    }

                } else {
                    rc = 1;
                }
            }

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            System.out.println(e.getMessage());
            System.out.println();
            // Help is kind of useful too ;)
            System.out.println(getArgs().getHelp());
            rc = 1;

        } finally {
            control.shutdown(Model.getSingleton().getOptionsParam().getDatabaseParam().isCompactDatabase());
            logger.info(Constant.PROGRAM_TITLE + " terminated.");
        }

        return rc;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
