package org.zaproxy.zap.extension.sensitive;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

public class OptionsSensitiveDataPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;

    private final JCheckBox maskingEnabled = new JCheckBox("Enable masking");
    private final JTextField maskValue = new JTextField();
    private final JTextArea keys = new JTextArea(10, 40);

    public OptionsSensitiveDataPanel() {
        setName("Sensitive Data");
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;

        add(maskingEnabled, gbc);

        gbc.gridy++;
        add(new JLabel("Mask value:"), gbc);

        gbc.gridy++;
        add(maskValue, gbc);

        gbc.gridy++;
        add(new JLabel("Sensitive keys (one per line):"), gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(keys), gbc);
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        OptionsParamSensitiveData param = optionsParam.getParamSet(OptionsParamSensitiveData.class);
        if (param == null) {
            return;
        }

        maskingEnabled.setSelected(param.isMaskingEnabled());
        maskValue.setText(param.getMaskValue());
        keys.setText(String.join("\n", param.getSensitiveKeys()));
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        OptionsParamSensitiveData param = optionsParam.getParamSet(OptionsParamSensitiveData.class);
        if (param == null) {
            return;
        }

        param.setMaskingEnabled(maskingEnabled.isSelected());
        param.setMaskValue(maskValue.getText());

        List<String> keyList =
                Arrays.stream(keys.getText().split("\\R"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
        param.setSensitiveKeys(keyList);
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.sensitivedata";
    }
}

