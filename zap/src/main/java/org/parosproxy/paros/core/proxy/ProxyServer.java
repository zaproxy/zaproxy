/*
 * Created on May 25, 2004
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/04/16 i18n
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2011/11/15 Warn the user if the host is unknown
// ZAP: 2012/03/15 Changed to sort the ProxyListeners. Set the name of the proxy server thread.
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/12/27 Added PersistentConnectionListener list, setter & getter.
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2014/01/22 Add the possibility to bound the proxy to all interfaces if null IP address has
// been set
// ZAP: 2014/03/23 Issue 1022: Proxy - Allow to override a proxied message
// ZAP: 2014/08/14 Issue 1312: Misleading error message when unable to bind the local proxy to
// specified address
// ZAP: 2015/11/04 Issue 1920: Report the host:port ZAP is listening on in daemon mode, or exit if
// it cant
// ZAP: 2016/05/30 Issue 2494: ZAP Proxy is not showing the HTTP CONNECT Request in history tab
// ZAP: 2016/09/22 JavaDoc tweaks
// ZAP: 2016/11/08 Tweak how exception's message is checked to show a specific error/info message
// ZAP: 2017/03/15 Disable API by default and allow thread name to be set
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/12/13 Handle proxy port conflict at startup (issue 2016).
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/02/09 Deprecate the class.
// ZAP: 2022/05/20 Address deprecation warnings with ConnectionParam.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.core.proxy;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.PersistentConnectionListener;

/** @deprecated (2.12.0) Use the network add-on instead. */
@Deprecated
public class ProxyServer implements Runnable {

    protected Thread thread = null;
    protected static final int PORT_TIME_OUT = 0;
    protected ServerSocket proxySocket = null;
    protected boolean isProxyRunning = false;
    protected ProxyParam proxyParam = new ProxyParam();
    protected org.parosproxy.paros.network.ConnectionParam connectionParam =
            new org.parosproxy.paros.network.ConnectionParam();
    protected Vector<ProxyListener> listenerList = new Vector<>();
    protected Vector<OverrideMessageProxyListener> overrideListeners = new Vector<>();
    protected Vector<PersistentConnectionListener> persistentConnectionListenerList =
            new Vector<>();
    private final List<ConnectRequestProxyListener> connectRequestProxyListeners;
    // ZAP: Added listenersComparator.
    private static Comparator<ArrangeableProxyListener> listenersComparator;
    protected boolean serialize = false;
    protected boolean enableCacheProcessing = false;
    protected Vector<CacheProcessingItem> cacheProcessingList = new Vector<>();
    private List<Pattern> excludeUrls = null;
    private boolean enableApi = false;
    private static Logger log = LogManager.getLogger(ProxyServer.class);
    private String threadName = "ZAP-ProxyServer";
    private boolean shouldPrompt = false;

    /** @return Returns the enableCacheProcessing. */
    public boolean isEnableCacheProcessing() {
        return enableCacheProcessing;
    }

    /** @param enableCacheProcessing The enableCacheProcessing to set. */
    public void setEnableCacheProcessing(boolean enableCacheProcessing) {
        this.enableCacheProcessing = enableCacheProcessing;

        if (!enableCacheProcessing) {
            cacheProcessingList.clear();
        }
    }

    /** @return Returns the serialize. */
    public boolean isSerialize() {
        return serialize;
    }

    public ProxyServer() {
        this(null);
    }

    public ProxyServer(String threadName) {
        connectRequestProxyListeners = new ArrayList<>(1);
        if (threadName != null) {
            this.threadName = threadName;
        }
    }

    public void setProxyParam(ProxyParam param) {
        proxyParam = param;
    }

    public ProxyParam getProxyParam() {
        return proxyParam;
    }

    public void setConnectionParam(org.parosproxy.paros.network.ConnectionParam connection) {
        connectionParam = connection;
    }

    public org.parosproxy.paros.network.ConnectionParam getConnectionParam() {
        return connectionParam;
    }

    /**
     * Starts the proxy server.
     *
     * <p>If the proxy server was already running it's stopped first.
     *
     * @param ip the IP/address the server should bind to
     * @param port the port
     * @param isDynamicPort {@code true} if it should use another port if the given one is already
     *     in use, {@code false} otherwise.
     * @return the port the server is listening to, or {@code -1} if not able to start
     */
    public synchronized int startServer(String ip, int port, boolean isDynamicPort) {

        if (isProxyRunning) {
            stopServer();
        }

        isProxyRunning = false;

        // ZAP: Set the name of the thread.
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        // the priority below should be higher than normal to allow fast accept on the server socket
        thread.setPriority(Thread.NORM_PRIORITY + 1);

        proxySocket = null;
        for (int i = 0; i < 20 && proxySocket == null; i++) {
            try {
                proxySocket = createServerSocket(ip, port);
                proxySocket.setSoTimeout(PORT_TIME_OUT);
                isProxyRunning = true;

            } catch (UnknownHostException e) {
                // ZAP: Warn the user if the host is unknown
                if (View.isInitialised()) {
                    View.getSingleton()
                            .showWarningDialog(
                                    Constant.messages.getString("proxy.error.host.unknown")
                                            + " "
                                            + ip);

                } else {
                    System.out.println(
                            Constant.messages.getString("proxy.error.host.unknown") + " " + ip);
                }

                return -1;
            } catch (BindException e) {
                String message = e.getMessage();
                if (message == null || message.isEmpty()) {
                    handleUnknownException(e);
                    return -1;
                }

                if (message.startsWith("Cannot assign requested address")) {
                    showErrorMessage(Constant.messages.getString("proxy.error.address") + " " + ip);
                    return -1;
                } else if (message.startsWith("Permission denied")
                        || message.startsWith("Address already in use")) {
                    if (!isDynamicPort) {
                        int originalPort = port;
                        if (shouldPrompt) {
                            port = promptForPort(ip, port);
                        }
                        if (!shouldPrompt || port == -1) {
                            showErrorMessage(
                                    Constant.messages.getString(
                                            "proxy.error.port",
                                            ip,
                                            Integer.toString(originalPort)));
                            return -1;
                        }
                    } else if (port < 65535) {
                        port++;
                    }
                } else {
                    handleUnknownException(e);
                    return -1;
                }
            } catch (IOException e) {
                handleUnknownException(e);
                return -1;
            }
        }

        if (proxySocket == null) {
            return -1;
        }

        thread.start();

        return proxySocket.getLocalPort();
    }

    private int promptForPort(String ip, int port) {
        if (!View.isInitialised()) {
            return -1;
        }
        String retryMessage =
                Constant.messages.getString("proxy.error.port.retry", String.valueOf(port));
        int tryPort;
        if (port > 65535 - 20) {
            port = 1024;
        }
        for (tryPort = port + 1; tryPort < port + 21; tryPort++) {
            try (ServerSocket trySocket = createServerSocket(ip, tryPort)) {
                if (trySocket != null) {
                    break;
                }
            } catch (IOException ioe) {
                // Do nothing
            }
        }
        ProxyPortRetryPanel pprp = new ProxyPortRetryPanel(retryMessage, tryPort);
        int result =
                JOptionPane.showConfirmDialog(
                        null,
                        pprp,
                        "",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null);
        if (result == JOptionPane.YES_OPTION) {
            return pprp.getProxyPort();
        }
        return -1;
    }

    private static void showErrorMessage(String error) {
        if (View.isInitialised()) {
            View.getSingleton().showWarningDialog(error);
        } else {
            log.error(error);
            System.out.println(error);
        }
    }

    private static void handleUnknownException(Exception e) {
        log.error("Failed to start the proxy server: ", e);
        showErrorMessage(
                Constant.messages.getString("proxy.error.generic") + e.getLocalizedMessage());
    }

    /**
     * Stops the proxy server.
     *
     * @return {@code true} if the proxy server was stopped, {@code false} if it was not running.
     */
    public synchronized boolean stopServer() {

        if (!isProxyRunning) {
            return false;
        }

        isProxyRunning = false;
        org.parosproxy.paros.network.HttpUtil.closeServerSocket(proxySocket);

        try {
            thread.join(); // (PORT_TIME_OUT);

        } catch (Exception e) {
        }

        proxySocket = null;

        return true;
    }

    @Override
    public void run() {

        Socket clientSocket;
        ProxyThread process;

        while (isProxyRunning) {
            try {
                clientSocket = proxySocket.accept();
                process = createProxyProcess(clientSocket);
                process.start();

            } catch (SocketTimeoutException e) {
                // nothing, socket time reached only.
            } catch (IOException e) {
                // unknown IO exception - continue but with delay to avoid eating up CPU time if
                // continue
                try {
                    Thread.sleep(100);

                } catch (InterruptedException e1) {
                }
            }
        }
    }

    protected ServerSocket createServerSocket(String ip, int port)
            throws UnknownHostException, IOException {
        // ServerSocket socket = new ServerSocket(port, 300,
        // InetAddress.getByName(ip)getProxyParam().getProxyIp()));
        //
        // ZAP: added the possibility to bound to all interfaces (using null as InetAddress)
        //      when the ip is null or an empty string
        InetAddress addr = null;
        if ((ip != null) && !ip.isEmpty()) {
            addr = InetAddress.getByName(ip);
        }

        ServerSocket socket = new ServerSocket(port, 400, addr);

        return socket;
    }

    protected ProxyThread createProxyProcess(Socket clientSocket) {
        ProxyThread process = new ProxyThread(this, clientSocket);
        return process;
    }

    protected void writeOutput(String s) {}

    public void addProxyListener(ProxyListener listener) {
        listenerList.add(listener);
        // ZAP: Added to sort the listeners.
        Collections.sort(listenerList, getListenersComparator());
    }

    public void removeProxyListener(ProxyListener listener) {
        listenerList.remove(listener);
    }

    synchronized List<ProxyListener> getListenerList() {
        return listenerList;
    }

    public void addPersistentConnectionListener(PersistentConnectionListener listener) {
        persistentConnectionListenerList.add(listener);
        Collections.sort(persistentConnectionListenerList, getListenersComparator());
    }

    public void removePersistentConnectionListener(PersistentConnectionListener listener) {
        persistentConnectionListenerList.remove(listener);
    }

    synchronized List<PersistentConnectionListener> getPersistentConnectionListenerList() {
        return persistentConnectionListenerList;
    }

    public void addOverrideMessageProxyListener(OverrideMessageProxyListener listener) {
        overrideListeners.add(listener);
        Collections.sort(overrideListeners, getListenersComparator());
    }

    public void removeOverrideMessageProxyListener(OverrideMessageProxyListener listener) {
        overrideListeners.remove(listener);
    }

    List<OverrideMessageProxyListener> getOverrideMessageProxyListeners() {
        return overrideListeners;
    }

    /**
     * Adds the given {@code listener}, that will be notified of the received CONNECT requests.
     *
     * @param listener the listener that will be added
     * @throws IllegalArgumentException if the given {@code listener} is {@code null}.
     * @since 2.5.0
     */
    public void addConnectRequestProxyListener(ConnectRequestProxyListener listener) {
        connectRequestProxyListeners.add(listener);
    }

    /**
     * Validates that the given {@code listener} is not {@code null}, throwing an {@code
     * IllegalArgumentException} if it is.
     *
     * @param listener the listener that will be validated
     * @throws IllegalArgumentException if the given {@code listener} is {@code null}.
     */
    private static void validateListenerNotNull(Object listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Parameter listener must not be null.");
        }
    }

    /**
     * Removes the given {@code listener}, to no longer be notified of the received CONNECT
     * requests.
     *
     * @param listener the listener that should be removed
     * @throws IllegalArgumentException if the given {@code listener} is {@code null}.
     * @since 2.5.0
     */
    public void removeConnectRequestProxyListener(ConnectRequestProxyListener listener) {
        validateListenerNotNull(listener);
        connectRequestProxyListeners.remove(listener);
    }

    /**
     * Gets the {@code ConnectRequestProxyListener}s added.
     *
     * @return an unmodifiable {@code List} with the {@code ConnectRequestProxyListener}s, never
     *     {@code null}
     * @since 2.5.0
     */
    List<ConnectRequestProxyListener> getConnectRequestProxyListeners() {
        return Collections.unmodifiableList(connectRequestProxyListeners);
    }

    public boolean isAnyProxyThreadRunning() {
        return ProxyThread.isAnyProxyThreadRunning();
    }

    /** @param serialize The serialize to set. */
    public void setSerialize(boolean serialize) {
        this.serialize = serialize;
    }

    public void addCacheProcessingList(CacheProcessingItem item) {
        cacheProcessingList.add(item);
    }

    Vector<CacheProcessingItem> getCacheProcessingList() {
        return cacheProcessingList;
    }

    public void setExcludeList(List<String> urls) {
        excludeUrls = new ArrayList<>(urls.size());
        for (String url : urls) {
            Pattern p = Pattern.compile(url, Pattern.CASE_INSENSITIVE);
            excludeUrls.add(p);
        }
    }

    public boolean excludeUrl(URI uri) {
        boolean ignore = false;
        if (excludeUrls != null) {
            String uriString = uri.toString();
            for (Pattern p : excludeUrls) {
                if (p.matcher(uriString).matches()) {
                    ignore = true;
                    log.debug("URL excluded: {} Regex: {}", uriString, p.pattern());

                    break;
                }
            }
        }

        return ignore;
    }

    // ZAP: Added the method.
    private Comparator<ArrangeableProxyListener> getListenersComparator() {
        if (listenersComparator == null) {
            createListenersComparator();
        }

        return listenersComparator;
    }

    // ZAP: Added the method.
    private synchronized void createListenersComparator() {
        if (listenersComparator == null) {
            listenersComparator =
                    new Comparator<ArrangeableProxyListener>() {
                        @Override
                        public int compare(
                                ArrangeableProxyListener o1, ArrangeableProxyListener o2) {
                            int order1 = o1.getArrangeableListenerOrder();
                            int order2 = o2.getArrangeableListenerOrder();

                            if (order1 < order2) {
                                return -1;

                            } else if (order1 > order2) {
                                return 1;
                            }

                            return 0;
                        }
                    };
        }
    }

    public void setEnableApi(boolean enableApi) {
        this.enableApi = enableApi;
    }

    public boolean isEnableApi() {
        return enableApi;
    }

    /**
     * Sets whether or not the {@code ProxyServer} should prompt the user for an alternate port when
     * there is a conflict.
     *
     * @param shouldPrompt {@code true} if the user should be prompted, {@code false} otherwise
     * @since 2.9.0
     */
    public void setShouldPrompt(boolean shouldPrompt) {
        this.shouldPrompt = shouldPrompt;
    }
}
