/*
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
// ZAP: 2012/06/25 Added support for WebSocket observer
package org.parosproxy.paros.extension;

import java.util.Vector;

import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.view.SiteMapListener;


public class ExtensionHook {

    private ExtensionHookMenu hookMenu = new ExtensionHookMenu();
    private ExtensionHookView hookView = new ExtensionHookView();
    private Model model = null;    
    private Vector<OptionsChangedListener> optionsListenerList = new Vector<OptionsChangedListener>();

    private Vector<ProxyListener> proxyListenerList = new Vector<ProxyListener>();
    private Vector<SessionChangedListener> sessionListenerList = new Vector<SessionChangedListener>();
    private Vector<AbstractParam> optionsParamSetList = new Vector<AbstractParam>();
    // ZAP: Added support for site map listeners
    private Vector<SiteMapListener> siteMapListenerList = new Vector<SiteMapListener>();
    // ZAP: Added support for WebSocket observer
    private Vector<WebSocketObserver> webSocketObserverList = new Vector<WebSocketObserver>();
    
    private ViewDelegate view = null;
    private CommandLineArgument arg[] = new CommandLineArgument[0];
    
    public ExtensionHook(Model model, ViewDelegate view) {
        this.view = view;
        this.model = model;
    }
    
    public void addOptionsChangedListener(OptionsChangedListener listener) {
        optionsListenerList.add(listener);
    }    

    public void addOptionsParamSet(AbstractParam paramSet) {
        optionsParamSetList.add(paramSet);
    }    

    public void addProxyListener(ProxyListener listener) {
        proxyListenerList.add(listener);
    }
    public void addSessionListener(SessionChangedListener listener) {
        sessionListenerList.add(listener);
    }
    public void addSiteMapListner(SiteMapListener listener) {
    	siteMapListenerList.add(listener);
    }
    
    /**
     * The added observer is attached to each new channel.
     * 
     * @param observer
     */
    public void addWebSocketObserver(WebSocketObserver observer) {
    	webSocketObserverList.add(observer);
    }
    
    public void addCommandLine(CommandLineArgument arg[]) {
        this.arg = arg;
    }
    
    /**
     * @return Returns the hookMenu.
     */
    public ExtensionHookMenu getHookMenu() {
        return hookMenu;
    }
    /**
     * @return Returns the hookView.
     */
    public ExtensionHookView getHookView() {
        return hookView;
    }
    /**
     * @return Returns the model.
     */
    public Model getModel() {
        return model;
    }
    
    /**
     * @return Returns the optionsListenerList.
     */
    public Vector<OptionsChangedListener> getOptionsChangedListenerList() {
        return optionsListenerList;
    }

    public Vector<AbstractParam> getOptionsParamSetList() {
        return optionsParamSetList;
    }

    /**
     * @return Returns the proxyListenerList.
     */
    public Vector<ProxyListener> getProxyListenerList() {
        return proxyListenerList;
    }
    
    /**
     * @return Returns the sessionListenerList.
     */
    public Vector<SessionChangedListener> getSessionListenerList() {
        return sessionListenerList;
    }
    
    public Vector<SiteMapListener> getSiteMapListenerList() {
        return siteMapListenerList;
    }
    
    public Vector<WebSocketObserver> getWebSocketObserverList() {
        return webSocketObserverList;
    }
    
    /**
     * @return Returns the view.
     */
    public ViewDelegate getView() {
        return view;
    }
    
    public CommandLineArgument[] getCommandLineArgument() {
        return arg;
    }
}
