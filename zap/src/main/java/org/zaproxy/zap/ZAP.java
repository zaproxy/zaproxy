/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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

import java.io.IOException;
import java.io.PrintStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Locale;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.parosproxy.paros.CommandLine;
import org.zaproxy.zap.eventBus.EventBus;
import org.zaproxy.zap.eventBus.SimpleEventBus;

public class ZAP {

    /**
     * ZAP can be run in 4 different ways: cmdline: an inline process that exits when it completes
     * the tasks specified by the parameters daemon: a single process with no Swing UI, typically
     * run as a background process desktop: a Swing based desktop tool (which is how it originated,
     * as a fork of Paros Proxy) zaas: a highly scalable distributed system with a web based UI, aka
     * 'ZAP as a Service' (this is 'work in progress')
     */
    public enum ProcessType {
        cmdline,
        daemon,
        desktop,
        zaas
    }

    private static ProcessType processType;

    private static final EventBus eventBus = new SimpleEventBus();
    private static final Logger logger = LogManager.getLogger(ZAP.class);

    static {
        try {
            // Disable JAR caching to avoid leaking add-on files and use of stale data.
            URLConnection.class
                    .getDeclaredMethod("setDefaultUseCaches", String.class, boolean.class)
                    .invoke(null, "jar", false);
        } catch (Exception e) {
            // Nothing to do, Java 9+ API and logger was not yet initialised.
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
    }

    /**
     * Main method
     *
     * @param args the arguments passed to the command line version
     * @throws Exception if something wrong happens
     */
    public static void main(String[] args) throws Exception {
        setCustomErrStream();

        CommandLine cmdLine = null;
        try {
            cmdLine = new CommandLine(args != null ? Arrays.copyOf(args, args.length) : null);

        } catch (final Exception e) {
            // Cant use the CommandLine help here as the
            // i18n messages wont have been loaded
            System.out.println("Failed due to invalid parameters: " + Arrays.toString(args));
            System.out.println(e.getMessage());
            System.out.println("Use '-h' for more details.");
            System.exit(1);
        }

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

    private static void setCustomErrStream() {
        System.setErr(
                new DelegatorPrintStream(System.err) {

                    @Override
                    public void println(String x) {
                        // Suppress Nashorn removal warnings, too verbose (a warn each time is
                        // used).
                        if ("Warning: Nashorn engine is planned to be removed from a future JDK release"
                                .equals(x)) {
                            return;
                        }
                        if (x.startsWith("Multiplexing LAF")) {
                            return;
                        }
                        super.println(x);
                    }
                });
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

    static final class UncaughtExceptionLogger implements Thread.UncaughtExceptionHandler {

        private static final Logger logger = LogManager.getLogger(UncaughtExceptionLogger.class);

        private boolean loggerConfigured = false;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (!(e instanceof ThreadDeath)) {
                if (loggerConfigured || isLoggerConfigured()) {
                    logger.error("Exception in thread \"{}\"", t.getName(), e);

                } else {
                    System.err.println("Exception in thread \"" + t.getName() + "\"");
                    e.printStackTrace();
                }
            }
        }

        private boolean isLoggerConfigured() {
            if (loggerConfigured) {
                return true;
            }

            LoggerContext context = LoggerContext.getContext();
            if (!context.getRootLogger().getAppenders().isEmpty()) {
                loggerConfigured = true;
            } else {
                for (LoggerConfig config : context.getConfiguration().getLoggers().values()) {
                    if (!config.getAppenders().isEmpty()) {
                        loggerConfigured = true;
                        break;
                    }
                }
            }

            return loggerConfigured;
        }
    }

    private static class DelegatorPrintStream extends PrintStream {

        private final PrintStream delegatee;

        public DelegatorPrintStream(PrintStream delegatee) {
            super(NullOutputStream.NULL_OUTPUT_STREAM);
            this.delegatee = delegatee;
        }

        @Override
        public void flush() {
            delegatee.flush();
        }

        @Override
        public void close() {
            delegatee.close();
        }

        @Override
        public boolean checkError() {
            return delegatee.checkError();
        }

        @Override
        protected void setError() {
            // delegatee manages its error state.
        }

        @Override
        protected void clearError() {
            // delegatee manages its error state.
        }

        @Override
        public void write(int b) {
            delegatee.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegatee.write(b);
        }

        @Override
        public void write(byte buf[], int off, int len) {
            delegatee.write(buf, off, len);
        }

        @Override
        public void print(boolean b) {
            delegatee.print(b);
        }

        @Override
        public void print(char c) {
            delegatee.print(c);
        }

        @Override
        public void print(int i) {
            delegatee.print(i);
        }

        @Override
        public void print(long l) {
            delegatee.print(l);
        }

        @Override
        public void print(float f) {
            delegatee.print(f);
        }

        @Override
        public void print(double d) {
            delegatee.print(d);
        }

        @Override
        public void print(char s[]) {
            delegatee.print(s);
        }

        @Override
        public void print(String s) {
            delegatee.print(s);
        }

        @Override
        public void print(Object obj) {
            delegatee.print(obj);
        }

        @Override
        public void println() {
            delegatee.println();
        }

        @Override
        public void println(boolean x) {
            delegatee.println(x);
        }

        @Override
        public void println(char x) {
            delegatee.println(x);
        }

        @Override
        public void println(int x) {
            delegatee.println(x);
        }

        @Override
        public void println(long x) {
            delegatee.println(x);
        }

        @Override
        public void println(float x) {
            delegatee.println(x);
        }

        @Override
        public void println(double x) {
            delegatee.println(x);
        }

        @Override
        public void println(char x[]) {
            delegatee.println(x);
        }

        @Override
        public void println(String x) {
            delegatee.println(x);
        }

        @Override
        public void println(Object x) {
            delegatee.println(x);
        }

        @Override
        public PrintStream printf(String format, Object... args) {
            return delegatee.printf(format, args);
        }

        @Override
        public PrintStream printf(Locale l, String format, Object... args) {
            return delegatee.printf(l, format, args);
        }

        @Override
        public PrintStream format(String format, Object... args) {
            delegatee.format(format, args);
            return this;
        }

        @Override
        public PrintStream format(Locale l, String format, Object... args) {
            delegatee.format(l, format, args);
            return this;
        }

        @Override
        public PrintStream append(CharSequence csq) {
            delegatee.append(csq);
            return this;
        }

        @Override
        public PrintStream append(CharSequence csq, int start, int end) {
            delegatee.append(csq, start, end);
            return this;
        }

        @Override
        public PrintStream append(char c) {
            delegatee.append(c);
            return this;
        }
    }
}
