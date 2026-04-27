package org.zaproxy.zap.extension.sensitive;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

public class ExtensionSensitiveData extends ExtensionAdaptor {

    public static final String NAME = "ExtensionSensitiveData";

    private OptionsParamSensitiveData optionsParam;

    public ExtensionSensitiveData() {
        super(NAME);
    }

    @Override
    public void init() {
        super.init();
        optionsParam = new OptionsParamSensitiveData();
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        extensionHook.addOptionsParamSet(optionsParam);

        if (hasView()) {
            extensionHook.getHookView().addOptionPanel(new OptionsSensitiveDataPanel());
        }
    }

    @Override
    public String getUIName() {
        return "Sensitive Data";
    }

    @Override
    public String getDescription() {
        return "Adds configurable masking for sensitive HTTP data displayed in ZAP.";
    }

    @Override
    public boolean canUnload() {
        return true;
    }
}

