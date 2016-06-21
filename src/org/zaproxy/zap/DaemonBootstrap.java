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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.proxy.ProxyParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;

/**
 * The bootstrap process for daemon mode.
 *
 * @since 2.4.2
 */
class DaemonBootstrap extends HeadlessBootstrap {

    private final Logger logger = Logger.getLogger(DaemonBootstrap.class);

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

        BasicConfigurator.configure();
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
        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                Control control = initControl(false);
                if (control == null) {
                	// Failed to listen on the specified proxy, no point in continuing (an error will already have been shown)
                	return;
                }

                warnAddOnsAndExtensionsNoLongerRunnable();

                if (!handleCmdLineSessionArgsSynchronously(control)) {
                    return;
                }

                try {
                    // Allow extensions to pick up command line args in daemon mode
                    control.getExtensionLoader().hookCommandLineListener(getArgs());
                    control.runCommandLine();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                
                ProxyParam proxyParams = Model.getSingleton().getOptionsParam().getProxyParam();
                logger.info("ZAP is now listening on " + proxyParams.getRawProxyIP() + ":" + proxyParams.getProxyPort());

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
