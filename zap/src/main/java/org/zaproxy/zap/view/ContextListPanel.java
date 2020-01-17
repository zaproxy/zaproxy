/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.model.Context;

public class ContextListPanel extends AbstractParamPanel {

    private static final long serialVersionUID = -8337361808959321380L;

    private JPanel panelContext = null;
    private JTable tableExt = null;
    private JScrollPane jScrollPane = null;
    private ContextListTableModel model = new ContextListTableModel();

    public ContextListPanel() {
        super();
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("context.list"));
        this.add(getPanelSession(), getPanelSession().getName());
    }
    /**
     * This method initializes panelSession
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelSession() {
        if (panelContext == null) {
            panelContext = new JPanel();
            panelContext.setLayout(new GridBagLayout());
            panelContext.setName(Constant.messages.getString("context.list"));
            if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption()
                    == 0) {
                panelContext.setSize(180, 101);
            }
            panelContext.add(getJScrollPane(), LayoutHelper.getGBC(0, 0, 4, 1.0D, 1.0D));
        }
        return panelContext;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getTableExtension());
            jScrollPane.setBorder(
                    javax.swing.BorderFactory.createEtchedBorder(
                            javax.swing.border.EtchedBorder.RAISED));
        }
        return jScrollPane;
    }

    /**
     * This method initializes tableAuth
     *
     * @return javax.swing.JTable
     */
    private JTable getTableExtension() {
        if (tableExt == null) {
            tableExt = new JTable();
            tableExt.setModel(this.model);
            tableExt.getColumnModel().getColumn(0).setPreferredWidth(30);
            tableExt.getColumnModel().getColumn(1).setPreferredWidth(320);
            tableExt.getColumnModel().getColumn(2).setPreferredWidth(50);
            // Issue 954: Force the JTable cell to auto-save when the focus changes.
            // Example, edit cell, click OK for a panel dialog box, the data will get saved.
            tableExt.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

            // Disable for now - would be useful but had some problems with this ;)
            /*
            ListSelectionListener sl = new ListSelectionListener() {

            	@Override
            	public void valueChanged(ListSelectionEvent arg0) {
                  		if (tableExt.getSelectedRow() > -1) {
                  			Context ctx = ((ContextListTableModel)tableExt.getModel()).getContext(
                  					tableExt.getSelectedRow());
                  			if (ctx != null) {
                  				try {
            					extName.setText(ext.getName());
            					extDescription.setText(ext.getDescription());
            					if (ext.getAuthor() != null) {
            						extAuthor.setText(ext.getAuthor());
            					} else {
            						extAuthor.setText("");
            					}
            					if (ext.getURL() != null) {
            						extURL.setText(ext.getURL().toString());
            						getUrlLaunchButton().setEnabled(true);
            					} else {
            						extURL.setText("");
            						getUrlLaunchButton().setEnabled(false);
            					}
            				} catch (Exception e) {
            					// Just to be safe
            					log.error(e.getMessage(), e);
            				}
                  			}
                  		}
            	}};

            tableExt.getSelectionModel().addListSelectionListener(sl);
            tableExt.getColumnModel().getSelectionModel().addListSelectionListener(sl);
            */

        }
        return tableExt;
    }

    @Override
    public void initParam(Object obj) {
        Session session = (Session) obj;

        List<Object[]> values = new ArrayList<>();
        List<Context> contexts = session.getContexts();
        for (Context context : contexts) {
            values.add(new Object[] {context.getId(), context.getName(), context.isInScope()});
        }
        this.model.setValues(values);
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        // Nothing to do, the table does not allow to edit its values.
        // NOTE: If changed to be editable it should be in sync with the view state (share view
        // models?) of
        // ContextGeneralPanel(s), the context name and "in scope" state is also shown (and
        // editable) there.
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.contexts";
    }
}
