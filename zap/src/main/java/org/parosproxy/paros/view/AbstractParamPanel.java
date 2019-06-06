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
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2013/08/21 Added support for detecting when AbstractParamPanels are being shown/hidden in a
// AbstractParamDialog
// ZAP: 2016/11/17 Issue 2701 Support Factory Reset
// ZAP: 2017/01/09 Add default implementations to some methods.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.view;

import javax.swing.JPanel;
import org.parosproxy.paros.model.Model;

public abstract class AbstractParamPanel extends JPanel {

    private static final long serialVersionUID = 3245127348676340802L;

    /** This is the default constructor */
    public AbstractParamPanel() {
        super();
        initialize();
    }
    /** This method initializes this */
    private void initialize() {
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(500, 375);
        }
    }

    /**
     * Initialises the panel with the given data.
     *
     * @param obj the object used to initialise the panel and save the data
     */
    public abstract void initParam(Object obj);

    /**
     * Validates the panel, throwing an exception if there's any validation error.
     *
     * <p>The message of the exception is expected to be internationalised (as it might be shown in
     * GUI components, for example, an error dialogue).
     *
     * <p>Does nothing by default.
     *
     * @param obj the object used to initialise the panel and save the data
     * @throws Exception if there's any validation error.
     */
    public void validateParam(Object obj) throws Exception {
        // Nothing to validate.
    }

    /**
     * Saves (the data of) the panel, throwing an exception if there's any error.
     *
     * <p>The message of the exception is expected to be internationalised (as it might be shown in
     * GUI components, for example, an error dialogue).
     *
     * @param obj the object used to initialise the panel and save the data
     * @throws Exception if there's any error while saving the data.
     */
    public abstract void saveParam(Object obj) throws Exception;

    /**
     * Gets the index of the help page for this options panel.
     *
     * <p>The help index is the value of the {@code target} attribute of the corresponding {@code
     * mapID} element defined in the JHM file.
     *
     * <p>If the help index is provided a button is shown to access the help page.
     *
     * @return the help index, or {@code null} if none.
     */
    public String getHelpIndex() {
        return null;
    }

    /**
     * Called when the panel is shown (becomes visible) in the containing {@link
     * AbstractParamDialog}.
     */
    public void onShow() {}

    /**
     * Called when the panel is hidden (another panel becomes visible) in the containing {@link
     * AbstractParamDialog}.
     */
    public void onHide() {}

    /** Called when the 'Reset to Factory Settings' option is selected */
    public void reset() {}
}
