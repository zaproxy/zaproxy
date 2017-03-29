/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 psiinon@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.SSLConnector;
import org.zaproxy.zap.eventBus.EventBus;
import org.zaproxy.zap.eventBus.SimpleEventBus;
import org.zaproxy.zap.utils.ClassLoaderUtil;

public class ZAP {

    /**
     * ZAP can be run in 4 different ways:
     * cmdline:	an inline process that exits when it completes the tasks specified by the parameters
     * daemon:	a single process with no Swing UI, typically run as a background process  
     * desktop:	a Swing based desktop tool (which is how is originated, as a fork of Paros Proxy)
     * zaas:	a highly scalable distributed system with a web based UI, aka 'ZAP as a Service' (this is 'work in progress') 
     */
    public enum ProcessType {cmdline, daemon, desktop, zaas}
    private static ProcessType processType;

    private static final EventBus eventBus = new SimpleEventBus();
    private static final Logger logger = Logger.getLogger(ZAP.class);

    static {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());

        // set SSLConnector as socketfactory in HttpClient.
        ProtocolSocketFactory sslFactory = null;
        try {
            final Protocol protocol = Protocol.getProtocol("https");
            sslFactory = protocol.getSocketFactory();

        } catch (final IllegalStateException e) {
            // Print the exception - log not yet initialised
            e.printStackTrace();
        }

        if (sslFactory == null || !(sslFactory instanceof SSLConnector)) {
            Protocol.registerProtocol("https", new Protocol("https",
                    (ProtocolSocketFactory) new SSLConnector(), 443));
        }
    }

    /**
     * Main method
     * 
     * @param args
     *            the arguments passed to the command line version
     * @throws Exception
     *             if something wrong happens
     */
    public static void main(String[] args) throws Exception {
        CommandLine cmdLine = null;
        try {
            cmdLine = new CommandLine(args);

        } catch (final Exception e) {
        	// Cant use the CommandLine help here as the 
        	// i18n messages wont have been loaded
            System.out.println("Failed due to invalid parameters. Use '-h' for more details.");
            System.exit(1);
        }

        initClassLoader();

        ZapBootstrap bootstrap = createZapBootstrap(cmdLine);
        try {
            int rc = bootstrap.start();
            if (rc != 0) {
                System.exit(rc);
            }

        } catch (final Exception e) {
            logger.fatal(e.getMessage(), e);
            System.exit(1);
        }

    }

    private static void initClassLoader() {
        try {
            // lang directory includes all of the language files
            final File langDir = new File(Constant.getZapInstall(), "lang");
            if (langDir.exists() && langDir.isDirectory()) {
                ClassLoaderUtil.addFile(langDir.getAbsolutePath());

            } else {
                System.out
                        .println("Warning: failed to load language files from "
                                + langDir.getAbsolutePath());
            }

            // Load all of the jars in the lib directory
            final File libDir = new File(Constant.getZapInstall(), "lib");
            if (libDir.exists() && libDir.isDirectory()) {
                final File[] files = libDir.listFiles();
                for (final File file : files) {
                    if (file.getName().toLowerCase(Locale.ENGLISH)
                            .endsWith("jar")) {
                        ClassLoaderUtil.addFile(file);
                    }
                }

            } else {
                System.out.println("Warning: failed to load jar files from "
                        + libDir.getAbsolutePath());
            }

        } catch (final IOException e) {
            System.out.println("Failed loading jars: " + e);
        }
    }

    private static ZapBootstrap createZapBootstrap(CommandLine cmdLineArgs) {
        ZapBootstrap bootstrap;
        if (cmdLineArgs.isGUI()) {
        	ZAP.processType = ProcessType.desktop;
            bootstrap = new GuiBootstrap(cmdLineArgs);
        } else if (cmdLineArgs.isDaemon()) {
        	ZAP.processType = ProcessType.daemon;
            bootstrap = new DaemonBootstrap(cmdLineArgs);
        } else {
        	ZAP.processType = ProcessType.cmdline;
            bootstrap = new CommandLineBootstrap(cmdLineArgs);
        }
        return bootstrap;
    }
    
    public static ProcessType getProcessType() {
    	return processType;
    }

    public static EventBus getEventBus() {
        return eventBus;
    }

    private static final class UncaughtExceptionLogger implements
            Thread.UncaughtExceptionHandler {

        private static final Logger logger = Logger
                .getLogger(UncaughtExceptionLogger.class);

        private static boolean loggerConfigured = false;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (!(e instanceof ThreadDeath)) {
                if (loggerConfigured || isLoggerConfigured()) {
                    logger.error("Exception in thread \"" + t.getName()
                            + "\"", e);

                } else {
                    System.err.println("Exception in thread \"" + t.getName()
                            + "\"");
                    e.printStackTrace();
                }
            }
        }

        private static boolean isLoggerConfigured() {
            if (loggerConfigured) {
                return true;
            }

            @SuppressWarnings("unchecked")
            Enumeration<Appender> appenders = LogManager.getRootLogger()
                    .getAllAppenders();
            if (appenders.hasMoreElements()) {
                loggerConfigured = true;
            } else {

                @SuppressWarnings("unchecked")
                Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
                while (loggers.hasMoreElements()) {
                    Logger c = loggers.nextElement();
                    if (c.getAllAppenders().hasMoreElements()) {
                        loggerConfigured = true;
                        break;
                    }
                }
            }

            return loggerConfigured;
        }
    }
}
