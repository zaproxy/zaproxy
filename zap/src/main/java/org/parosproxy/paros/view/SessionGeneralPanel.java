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
// ZAP: 2011/05/15 i19n
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/14 Changed the method initParam to discard all edits.
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2015/02/05 Issue 1524: New Persist Session dialog
// ZAP: 2015/02/10 Issue 1528: Support user defined font size
// ZAP: 2017/01/09 Remove method no longer needed.
// ZAP: 2017/06/01 Issue 3555: setTitle() functionality moved in order to ensure consistent
// application
// ZAP: 2017/06/07 Don't close the Session when changing session's name/description.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
package org.parosproxy.paros.view;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

public class SessionGeneralPanel extends AbstractParamPanel {

    private static final long serialVersionUID = -8337361808959321380L;

    private static final Logger LOGGER = LogManager.getLogger(SessionGeneralPanel.class);

    private JPanel panelSession = null; //  @jve:decl-index=0:visual-constraint="10,320"
    private ZapTextField txtSessionName = null;
    private ZapTextArea txtDescription = null;
    private ZapTextArea location = null;

    public SessionGeneralPanel() {
        super();
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("session.general"));
        this.add(getPanelSession(), getPanelSession().getName());
    }
    /**
     * This method initializes panelSession
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelSession() {
        if (panelSession == null) {
            panelSession = new JPanel();
            panelSession.setLayout(new GridBagLayout());
            panelSession.setName(Constant.messages.getString("session.general"));
            if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption()
                    == 0) {
                panelSession.setSize(180, 101);
            }
            panelSession.add(
                    new JLabel(Constant.messages.getString("session.label.name")),
                    LayoutHelper.getGBC(0, 0, 1, 1.0D));
            panelSession.add(
                    getTxtSessionName(),
                    LayoutHelper.getGBC(0, 1, 1, 1.0D, new Insets(2, 0, 2, 0)));
            panelSession.add(
                    new JLabel(Constant.messages.getString("session.label.loc")),
                    LayoutHelper.getGBC(0, 2, 1, 1.0D));
            panelSession.add(getSessionLocation(), LayoutHelper.getGBC(0, 3, 1, 1.0D));
            panelSession.add(
                    new JLabel(Constant.messages.getString("session.label.desc")),
                    LayoutHelper.getGBC(0, 4, 1, 1.0D, new Insets(2, 0, 2, 0)));
            panelSession.add(
                    getTxtDescription(),
                    LayoutHelper.getGBC(
                            0, 5, 1, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(2, 0, 2, 0)));
        }
        return panelSession;
    }
    /**
     * This method initializes txtSessionName
     *
     * @return org.zaproxy.zap.utils.ZapTextField
     */
    private ZapTextField getTxtSessionName() {
        if (txtSessionName == null) {
            txtSessionName = new ZapTextField();
        }
        return txtSessionName;
    }
    /**
     * This method initializes txtDescription
     *
     * @return org.zaproxy.zap.utils.ZapTextArea
     */
    private ZapTextArea getTxtDescription() {
        if (txtDescription == null) {
            txtDescription = new ZapTextArea();
            txtDescription.setBorder(
                    javax.swing.BorderFactory.createBevelBorder(
                            javax.swing.border.BevelBorder.LOWERED));
            txtDescription.setLineWrap(true);
        }
        return txtDescription;
    }

    private ZapTextArea getSessionLocation() {
        if (location == null) {
            location = new ZapTextArea();
            location.setEditable(false);
        }
        return location;
    }

    @Override
    public void initParam(Object obj) {
        Session session = (Session) obj;
        getTxtSessionName().setText(session.getSessionName());
        getTxtSessionName().discardAllEdits();
        getTxtDescription().setText(session.getSessionDesc());
        getTxtDescription().discardAllEdits();
        if (session.getFileName() != null) {
            getSessionLocation().setText(session.getFileName());
            getSessionLocation().setToolTipText(session.getFileName()); // In case its really long
        }
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        Session session = (Session) obj;
        boolean changed = false;
        if (!getTxtSessionName().getText().equals(session.getSessionName())) {
            session.setSessionName(getTxtSessionName().getText());
            changed = true;
        }
        if (!getTxtDescription().getText().equals(session.getSessionDesc())) {
            session.setSessionDesc(getTxtDescription().getText());
            changed = true;
        }
        if (changed) {
            try {
                Control.getSingleton().persistSessionProperties();
            } catch (Exception e) {
                LOGGER.error("Failed to persist the session properties:", e);
                throw new Exception(
                        Constant.messages.getString("session.general.error.persist.session.props"));
            }
        }
    }

    @Override
    public String getHelpIndex() {
        // ZAP: added help index support
        return "ui.dialogs.sessprop";
    }
} //  @jve:decl-index=0:visual-constraint="10,10"
