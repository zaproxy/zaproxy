package org.parosproxy.paros.extension.option;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.sensitive.OptionsParamSensitiveData;

public class OptionsSensitiveDataPanel extends AbstractParamPanel {
    private static final long serialVersionUID = 1L;

    private static final String PANEL_NAME = "Sensitive Data";

    private JCheckBox chkEnableMasking;
    private JTextField txtHeadersToMask;
    private JTextField txtBodyFieldsToMask;

    public OptionsSensitiveDataPanel() {
        setName(PANEL_NAME);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        chkEnableMasking = new JCheckBox("Enable sensitive data masking");
        add(chkEnableMasking, gbc);

        gbc.gridy++;
        add(new JLabel("Headers to mask (comma-separated):"), gbc);

        gbc.gridy++;
        txtHeadersToMask = new JTextField();
        add(txtHeadersToMask, gbc);

        gbc.gridy++;
        add(new JLabel("Body fields to mask (comma-separated):"), gbc);

        gbc.gridy++;
        txtBodyFieldsToMask = new JTextField();
        add(txtBodyFieldsToMask, gbc);
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam options = (OptionsParam) obj;
        OptionsParamSensitiveData param = options.getSensitiveDataParam();
        chkEnableMasking.setSelected(param.isEnabled());
        txtHeadersToMask.setText(param.getHeadersToMask());
        txtBodyFieldsToMask.setText(param.getBodyFieldsToMask());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam options = (OptionsParam) obj;
        OptionsParamSensitiveData param = options.getSensitiveDataParam();
        param.setEnabled(chkEnableMasking.isSelected());
        param.setHeadersToMask(txtHeadersToMask.getText().trim());
        param.setBodyFieldsToMask(txtBodyFieldsToMask.getText().trim());
    }

    @Override
    public String getHelpIndex() {
        return null;
    }
}

