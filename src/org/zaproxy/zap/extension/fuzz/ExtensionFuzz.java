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
package org.zaproxy.zap.extension.fuzz;

import java.awt.Component;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.owasp.jbrofuzz.core.Fuzzer;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httppanel.HttpPanelTextArea;
import org.zaproxy.zap.extension.search.SearchResult;

public class ExtensionFuzz extends ExtensionAdaptor implements FuzzerListener {

	public final static String NAME = "ExtensionFuzz";
    private PopupFuzzMenu popupFuzzMenu = null;
    private FuzzerThread fuzzerThread = null;
    private FuzzerParam fuzzerParam = null;
    private FuzzerPanel fuzzerPanel = null;
    private OptionsFuzzerPanel optionsFuzzerPanel = null;
    private boolean fuzzing = false;
    
	/**
     * 
     */
    public ExtensionFuzz() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionFuzz(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName(NAME);

	    if (getView() != null) {
	    	ExtensionHelp.enableHelpKey(getFuzzerPanel(), "ui.tabs.fuzz");
	    }

	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuFuzz());
	        extensionHook.getHookView().addStatusPanel(getFuzzerPanel());
	        this.getFuzzerPanel().setDisplayPanel(getView().getRequestPanel(), getView().getResponsePanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsFuzzerPanel());
	    }
        extensionHook.addOptionsParamSet(getFuzzerParam());

	}

	private FuzzerPanel getFuzzerPanel() {
		if (fuzzerPanel == null) {
			fuzzerPanel = new FuzzerPanel(this, this.getFuzzerParam());
		}
		return fuzzerPanel;
	}

	protected void addFuzzResult(HttpMessage msg) {
		this.getFuzzerPanel().addFuzzResult(msg);
	}
	
	public void scanProgress(int done, int todo) {
		this.getFuzzerPanel().scanProgress(done, todo);
	}


	public void startFuzzers (HttpMessage msg, Fuzzer[] fuzzers, boolean fuzzHeader, 
			int startOffset, int endOffset, AntiCsrfToken acsrfToken, boolean showTokenRequests) {
		this.getFuzzerPanel().scanStarted();

		fuzzerThread = new FuzzerThread(this, getFuzzerParam(), getModel().getOptionsParam().getConnectionParam());
		fuzzerThread.setTarget(msg, fuzzers, fuzzHeader, startOffset, endOffset, acsrfToken, showTokenRequests);
		fuzzerThread.addFuzzerListener(this);
		fuzzerThread.start();

	}
	
	public void stopFuzzers() {
		fuzzerThread.stop();		
	}

	public void pauseFuzzers() {
		fuzzerThread.pause();		
	}

	public void resumeFuzzers() {
		fuzzerThread.resume();		
	}
	
	public List<SearchResult> searchFuzzResults(Pattern pattern) {
		return this.getFuzzerPanel().searchResults(pattern);
	}

    protected void showFuzzDialog(Component invoker) {
        showFuzzDialog(getView().getMainFrame(), invoker);
    }

    private void showFuzzDialog(JFrame frame, Component invoker) {
		List<AntiCsrfToken> tokens = null;

		ExtensionAntiCSRF extAntiCSRF = 
			(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);

		if (extAntiCSRF != null) {
			if (invoker instanceof HttpPanelTextArea) {
				HttpPanelTextArea ta = (HttpPanelTextArea) invoker;
				tokens = extAntiCSRF.getTokens(ta.getHttpMessage());
			}
		}
		
		FuzzDialog fuzzDialog;
		
		if (tokens == null || tokens.size() == 0) {
			fuzzDialog = new FuzzDialog(frame, false, false);            
			fuzzDialog.setExtension(this);
		} else {
			fuzzDialog = new FuzzDialog(frame, false, true);            
			fuzzDialog.setExtension(this);
			fuzzDialog.setAntiCsrfTokens(tokens);
		}
		fuzzDialog.setDefaultCategory(this.getFuzzerParam().getDefaultCategory());
		fuzzDialog.setSelection(invoker);
		fuzzDialog.setVisible(true);
        
    }
    
    private PopupFuzzMenu getPopupMenuFuzz() {
        if (popupFuzzMenu== null) {
            popupFuzzMenu = new PopupFuzzMenu(this);
            popupFuzzMenu.setText(Constant.messages.getString("fuzz.tools.menu.fuzz"));
            popupFuzzMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	Object source = e.getSource();
                	if (source != null && source instanceof PopupFuzzMenu) {
                		Component invoker = ((PopupFuzzMenu)source).getLastInvoker();
                        showFuzzDialog(invoker);
                	}
                }
            });
        }
        return popupFuzzMenu;
    }

    private FuzzerParam getFuzzerParam() {
		if (fuzzerParam == null) {
			fuzzerParam = new FuzzerParam();
		}
		return fuzzerParam;
	}

	private OptionsFuzzerPanel getOptionsFuzzerPanel() {
		if (optionsFuzzerPanel == null) {
			optionsFuzzerPanel = new OptionsFuzzerPanel();
		}
		return optionsFuzzerPanel;
	}

	@Override
	public void notifyFuzzProcessComplete(FuzzProcess fp) {
		if (fp.isShowTokenRequests()) {
			for (HttpMessage tokenMsg : fp.getTokenRequests()) {
				addFuzzResult(tokenMsg);
			}
		}
		addFuzzResult(fp.getHttpMessage());
	}

	@Override
	public void notifyFuzzProcessStarted(FuzzProcess fp) {
		this.fuzzing = true;
	}

	@Override
	public void notifyFuzzerComplete() {
		this.getFuzzerPanel().scanFinshed();
		this.fuzzing = false;
	}

	public boolean isFuzzing() {
		return fuzzing;
	}

}
