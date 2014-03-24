/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.fuzz.impl.http;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.fuzz.ExtensionFuzz;
import org.zaproxy.zap.extension.fuzz.FuzzDialog;
import org.zaproxy.zap.extension.fuzz.FuzzProcessFactory;
import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.httppanel.view.FuzzableMessage;

public class HttpFuzzDialog extends FuzzDialog {

    private static final long serialVersionUID = -6286527080805168790L;
    
    private JCheckBox enableTokens = null;
    private JCheckBox showTokenRequests = null;
    private JCheckBox followRedirects = null;
    private JCheckBox urlEncode = null;

    private boolean incAcsrfToken = false;
    
    private FuzzableMessage fuzzableMessage;
    
    private HttpFuzzerDialogTokenPane tokenPane;
    
    private HttpFuzzerHandler handler;

    public HttpFuzzDialog(HttpFuzzerHandler handler, ExtensionFuzz extension, FuzzableComponent fuzzableComponent) {
        super(extension, fuzzableComponent.getFuzzTarget());
        
        this.handler = handler;
        
        fuzzableMessage = fuzzableComponent.getFuzzableMessage();
        
        ExtensionAntiCSRF extAntiCSRF = (ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);

        List<AntiCsrfToken> tokens = null;
        if (extAntiCSRF != null) {
            tokens = extAntiCSRF.getTokens((HttpMessage) fuzzableMessage.getMessage());
        }
        if (tokens == null || tokens.size() == 0) {
            incAcsrfToken = false;
        } else {
            incAcsrfToken = true;
        }
        
        initialize();
        
        if (incAcsrfToken) {
            setAntiCsrfTokens(tokens);
            this.setSize(500, 550);
        }
    }
    
    @Override
    protected int addCustomComponents(JPanel panel, int row) {
        int currentRow = row;
        if (incAcsrfToken) {
            panel.add(new JLabel(Constant.messages.getString("fuzz.label.anticsrf")), getGBC(0, currentRow, 3, 1.0D));
            panel.add(getEnableTokens(), getGBC(1, currentRow, 1, 0.0D));
            currentRow++;
            panel.add(getTokensPane(), getGBC(0, currentRow, 4, 1.0D, 0.0D));
            currentRow++;
            
            panel.add(new JLabel(Constant.messages.getString("fuzz.label.showtokens")), getGBC(0, currentRow, 3, 1.0D));
            panel.add(getShowTokenRequests(), getGBC(1, currentRow, 1, 0.0D));
            currentRow++;
            
        }
        panel.add(new JLabel(Constant.messages.getString("fuzz.label.followredirects")), getGBC(0, currentRow, 3, 1.0D));
        panel.add(getFollowRedirects(), getGBC(1, currentRow, 1, 0.0D));
        currentRow++;

        panel.add(new JLabel(Constant.messages.getString("fuzz.label.urlencode")), getGBC(0, currentRow, 3, 1.0D));
        panel.add(getUrlEncode(), getGBC(1, currentRow, 1, 0.0D));
        currentRow++;
        
        return currentRow;
    }
    
    @Override
    protected FuzzProcessFactory getFuzzProcessFactory() {
        AntiCsrfToken token = null;
        if (getEnableTokens().isSelected() && tokenPane.isEnable()) {
            token = tokenPane.getToken();
        }
        
        return new HttpFuzzProcessFactory(fuzzableMessage, token, getShowTokenRequests().isSelected(), getFollowRedirects().isSelected(), getUrlEncode().isSelected());
    }
    
    @Override
    protected StartFuzzAction getStartFuzzAction() {
        return new HttpStartFuzzAction();
    }
    
    private JComponent getTokensPane() {
        if (tokenPane == null) {
            tokenPane = new HttpFuzzerDialogTokenPane();
        }
        return tokenPane.getPane();
    }
    
    private void setAntiCsrfTokens(List <AntiCsrfToken> acsrfTokens) {
        if (acsrfTokens != null && acsrfTokens.size() > 0) {
            tokenPane.setAll(true, acsrfTokens.get(0), acsrfTokens.get(0).getTargetURL());
            this.getEnableTokens().setSelected(true);
            this.getEnableTokens().setEnabled(true);
            this.getTokensPane().setVisible(true);
        } else {
            tokenPane.reset();
            this.getEnableTokens().setSelected(false);
            this.getEnableTokens().setEnabled(false);
            this.getTokensPane().setVisible(false);
        }
    }

    private JCheckBox getEnableTokens() {
        if (enableTokens == null) {
            enableTokens = new JCheckBox();
            enableTokens.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tokenPane.setEnabled(enableTokens.isSelected());
                    getShowTokenRequests().setEnabled(enableTokens.isSelected());
                }});
        }
        return enableTokens;
    }

    private JCheckBox getShowTokenRequests() {
        if (showTokenRequests == null) {
            showTokenRequests = new JCheckBox();
        }
        return showTokenRequests;
    }

    private JCheckBox getFollowRedirects() {
        if (followRedirects == null) {
            followRedirects = new JCheckBox();
            followRedirects.setSelected(true);
        }
        return followRedirects;
    }

    private JCheckBox getUrlEncode() {
        if (urlEncode == null) {
            urlEncode = new JCheckBox();
            urlEncode.setSelected(true);
        }
        return urlEncode;
    }
    
    private class HttpStartFuzzAction extends StartFuzzAction {
        
        private static final long serialVersionUID = 1002081490933064015L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (incAcsrfToken && getShowTokenRequests().isSelected()) {
                handler.setShowTokenRequests(true);
                handler = null;
            }
            
            super.actionPerformed(e);
        }
    }

}
