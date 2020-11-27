/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;

public class ExtensionGlobalExcludeURL extends ExtensionAdaptor {

    public static final String NAME = "ExtensionGlobalExcludeURL";
    public static final String TAG = "GlobalExcludeURL";

    private OptionsGlobalExcludeURLPanel optionsGlobalExcludeURLPanel = null;
    // TODO Implement later ... private PopupMenuGenerateForm popupMenuGenerateForm = null;

    private static Logger log = LogManager.getLogger(ExtensionGlobalExcludeURL.class);

    public ExtensionGlobalExcludeURL() {
        super();
        initialize();
    }

    private void initialize() {
        this.setName(NAME);
        this.setOrder(969); // TODO find optimal load order at some point
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("globalexcludeurl.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        if (getView() != null) {
            extensionHook.getHookView().addOptionPanel(getOptionsGlobalExcludeURLPanel());
            // extensionHook.getHookMenu().addPopupMenuItem(this.getPopupMenuGenerateForm());
        }

        /* In the future, add API hook here.
           GlobalExcludeURLAPI api = new GlobalExcludeURLAPI(this);
              api.addApiOptions(getParam());
              API.getInstance().registerApiImplementor(api);
        */
    }

    /** TODO Implement the "right click, add to GEURL list" function. */
    /*
    private PopupMenuGenerateForm getPopupMenuGenerateForm() {
    	if (popupMenuGenerateForm == null) {
    		this.popupMenuGenerateForm = new PopupMenuGenerateForm(Constant.messages.getString("globalexcludeurl.genForm.popup")); // FIXME lang todo
    	}
    	return popupMenuGenerateForm;
    }
       */

    private OptionsGlobalExcludeURLPanel getOptionsGlobalExcludeURLPanel() {
        if (optionsGlobalExcludeURLPanel == null) {
            optionsGlobalExcludeURLPanel = new OptionsGlobalExcludeURLPanel();
        }
        return optionsGlobalExcludeURLPanel;
    }

    protected GlobalExcludeURLParam getParam() {
        return Model.getSingleton().getOptionsParam().getGlobalExcludeURLParam();
    }

    @Override
    public void optionsLoaded() {
        GlobalExcludeURLParam geup = getParam();
        geup.parse();
    }

    @Override
    public String getAuthor() {
        return "Jay Ball @ Aspect Security";
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("globalexcludeurl.desc");
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
