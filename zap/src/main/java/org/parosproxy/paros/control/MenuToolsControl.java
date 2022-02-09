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
// ZAP: 2012/03/15 Changed the method options to notify all OptionsChangedListener that the
//      options have changed.
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2016/04/04 Do not require a restart to show/hide the tool bar
// ZAP: 2016/04/06 Fix layouts' issues
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/02/09 Remove proxy related code.
package org.parosproxy.paros.control;

import javax.swing.JOptionPane;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.OptionsDialog;
import org.parosproxy.paros.view.View;

public class MenuToolsControl {

    private static final Logger logger = LogManager.getLogger(MenuToolsControl.class);

    private View view = null;
    private Model model = null;
    private Control control = null;

    public MenuToolsControl() {
        // use implicit MVC if not given
        view = View.getSingleton();
        model = Model.getSingleton();
        control = Control.getSingleton();
    }

    public MenuToolsControl(Model model, View view, Control control) {
        // best use explicit class constructor
        this.model = model;
        this.view = view;
        this.control = control;
    }

    public void options() {
        this.options(null);
    }

    // ZAP: added ability to select panel
    public void options(String panel) {
        OptionsDialog dialog =
                view.getOptionsDialog(Constant.messages.getString("options.dialog.title"));
        dialog.initParam(model.getOptionsParam());

        int result = dialog.showDialog(false, panel);
        if (result == JOptionPane.OK_OPTION) {
            try {
                model.getOptionsParam().getConfig().save();
            } catch (ConfigurationException e) {
                logger.error(e.getMessage(), e);
                view.showWarningDialog(
                        Constant.messages.getString("menu.tools.options.errorSavingOptions"));
                return;
            }
            // ZAP: Notify all OptionsChangedListener.
            control.getExtensionLoader().optionsChangedAllPlugin(model.getOptionsParam());

            view.getMainFrame().applyViewOptions();
        }
    }
}
