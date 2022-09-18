/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.authorization;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.extension.authorization.BasicAuthorizationDetectionMethod.LogicalOperator;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;

@SuppressWarnings("serial")
public class ContextAuthorizationPanel extends AbstractContextPropertiesPanel {

    private static final long serialVersionUID = 2416553589170267959L;
    private static final Logger log = LogManager.getLogger(ContextAuthorizationPanel.class);

    private static final String PANEL_NAME =
            Constant.messages.getString("authorization.panel.title");

    private static final String LABEL_DESCRIPTION =
            Constant.messages.getString("authorization.panel.label.description");
    private static final String FIELD_LABEL_INTRO =
            Constant.messages.getString("authorization.detection.basic.field.intro");
    private static final String FIELD_LABEL_STATUS_CODE =
            Constant.messages.getString("authorization.detection.basic.field.statusCode");
    private static final String FIELD_LABEL_HEADER_PATTERN =
            Constant.messages.getString("authorization.detection.basic.field.headerPattern");
    private static final String FIELD_LABEL_BODY_PATTERN =
            Constant.messages.getString("authorization.detection.basic.field.bodyPattern");
    private static final String FIELD_VALUE_AND_COMPOSITION =
            Constant.messages.getString("authorization.detection.basic.field.composition.and");
    private static final String FIELD_VALUE_OR_COMPOSITION =
            Constant.messages.getString("authorization.detection.basic.field.composition.or");

    private static Object[] STATUS_CODES;

    static {
        // Prepare the status codes as an Object array which includes the "empty" option, as
        // required by the
        // ComboBox
        STATUS_CODES = new Object[HttpStatusCode.CODES.length + 1];
        STATUS_CODES[0] = " -- ";
        for (int i = 0; i < HttpStatusCode.CODES.length; i++)
            STATUS_CODES[i + 1] = HttpStatusCode.CODES[i];
    }

    private JComboBox<Object> statusCodeComboBox;
    private JTextField headerPatternText;
    private JTextField bodyPatternText;
    private JComboBox<String> logicalOperatorComboBox;

    private ExtensionAuthorization extension;
    private AuthorizationDetectionMethod authorizationMethod;

    public ContextAuthorizationPanel(ExtensionAuthorization extension, int contextId) {
        super(contextId);
        this.extension = extension;
        initialize();
    }

    /**
     * Builds the name of the panel based on the context id.
     *
     * @param contextId the context id
     * @return the string
     */
    public static String buildName(int contextId) {
        return contextId + ": " + PANEL_NAME;
    }

    /** Initialize the panel. */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(getContextId() + ": " + PANEL_NAME);
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(2, 2, 2, 2));

        this.add(
                new ZapHtmlLabel(LABEL_DESCRIPTION),
                LayoutHelper.getGBC(0, 0, 2, 0.0D, new Insets(0, 0, 20, 0)));

        // Basic Authorization detection
        Insets insets = new Insets(2, 5, 2, 5);

        this.add(
                new ZapHtmlLabel(FIELD_LABEL_INTRO),
                LayoutHelper.getGBC(0, 1, 2, 0.0D, new Insets(0, 0, 5, 0)));

        JPanel configContainerPanel = new JPanel(new GridBagLayout());
        configContainerPanel.setBorder(
                javax.swing.BorderFactory.createTitledBorder(
                        null,
                        "",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        FontUtils.getFont(FontUtils.Size.standard)));
        this.add(configContainerPanel, LayoutHelper.getGBC(0, 2, 2, 0.0D));

        configContainerPanel.add(
                new JLabel(FIELD_LABEL_STATUS_CODE), LayoutHelper.getGBC(0, 2, 1, 0.0D));
        statusCodeComboBox = new JComboBox<>(STATUS_CODES);
        configContainerPanel.add(statusCodeComboBox, LayoutHelper.getGBC(1, 2, 1, 1.0D, insets));

        configContainerPanel.add(
                new JLabel(FIELD_LABEL_HEADER_PATTERN), LayoutHelper.getGBC(0, 3, 1, 0.0D));
        headerPatternText = new JTextField();
        configContainerPanel.add(headerPatternText, LayoutHelper.getGBC(1, 3, 1, 1.0D, insets));

        configContainerPanel.add(
                new JLabel(FIELD_LABEL_BODY_PATTERN), LayoutHelper.getGBC(0, 4, 1, 0.0D));
        bodyPatternText = new JTextField();
        configContainerPanel.add(bodyPatternText, LayoutHelper.getGBC(1, 4, 1, 1.0D, insets));

        logicalOperatorComboBox =
                new JComboBox<>(
                        new String[] {FIELD_VALUE_AND_COMPOSITION, FIELD_VALUE_OR_COMPOSITION});
        configContainerPanel.add(
                logicalOperatorComboBox,
                LayoutHelper.getGBC(0, 5, 2, 0.0D, new Insets(2, 0, 2, 5)));

        // Padding
        this.add(new JLabel(), LayoutHelper.getGBC(0, 99, 2, 1.0D, 1.0D));
    }

    @Override
    public void initContextData(Session session, Context uiSharedContext) {
        this.authorizationMethod = uiSharedContext.getAuthorizationDetectionMethod();
        if (this.authorizationMethod != null) {
            if (authorizationMethod instanceof BasicAuthorizationDetectionMethod) {
                log.debug(
                        "Initializing panel with {}: {}",
                        BasicAuthorizationDetectionMethod.class.getSimpleName(),
                        authorizationMethod);
                BasicAuthorizationDetectionMethod method =
                        (BasicAuthorizationDetectionMethod) this.authorizationMethod;

                if (method.bodyPattern != null)
                    this.bodyPatternText.setText(method.bodyPattern.pattern());
                if (method.headerPattern != null)
                    this.headerPatternText.setText(method.headerPattern.pattern());
                if (method.statusCode != BasicAuthorizationDetectionMethod.NO_STATUS_CODE)
                    this.statusCodeComboBox.setSelectedItem(method.statusCode);
                if (method.logicalOperator == LogicalOperator.AND)
                    this.logicalOperatorComboBox.setSelectedItem(FIELD_VALUE_AND_COMPOSITION);
                else this.logicalOperatorComboBox.setSelectedItem(FIELD_VALUE_OR_COMPOSITION);
                return;
            }
            log.warn(
                    "Unsupported authorization method on panel: {}",
                    authorizationMethod.getClass().getSimpleName());
        }
    }

    @Override
    public void validateContextData(Session session) throws Exception {
        try {
            Pattern.compile(headerPatternText.getText());
            Pattern.compile(bodyPatternText.getText());
        } catch (PatternSyntaxException e) {
            throw new IllegalStateException(
                    Constant.messages.getString(
                            "authorization.detection.basic.error.illegalPattern",
                            getUISharedContext().getName()),
                    e);
        }
    }

    private void saveMethod() {
        Integer selectedStatusCode =
                (Integer)
                        (statusCodeComboBox.getSelectedIndex() == 0
                                ? null
                                : statusCodeComboBox.getSelectedItem());
        LogicalOperator selectedComposition =
                logicalOperatorComboBox.getSelectedItem().equals(FIELD_VALUE_AND_COMPOSITION)
                        ? LogicalOperator.AND
                        : LogicalOperator.OR;

        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        selectedStatusCode,
                        headerPatternText.getText(),
                        bodyPatternText.getText(),
                        selectedComposition);
    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        saveMethod();
        uiSharedContext.setAuthorizationDetectionMethod(authorizationMethod);
    }

    @Override
    public void saveContextData(Session session) throws Exception {
        saveMethod();
        session.getContext(getContextId()).setAuthorizationDetectionMethod(authorizationMethod);
        log.debug("Saving authorization method: {}", authorizationMethod);
    }

    @Override
    public String getHelpIndex() {
        // TODO Auto-generated method stub
        return null;
    }
}
