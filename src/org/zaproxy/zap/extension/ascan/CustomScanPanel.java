/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright The ZAP development team
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
package org.zaproxy.zap.extension.ascan;

import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.model.Target;

/**
 * A panel that add-ons can add to the custom active scan dialog
 * @author psiinon
 *
 */
public interface CustomScanPanel {
	
	/**
	 * The i18n label to use for the panel title
	 * @return
	 */
	String getLabel();

	/**
	 * The panel to add to the custom active scan dialog
	 * @return
	 */
	AbstractParamPanel getPanel(boolean init);
	
	/**
	 * The target to use if (and only if) the user has not specified a target
	 * Return null if a target just for the information specified in this panel does not make sense
	 * @return
	 */
	Target getTarget();

	/**
	 * A translated error message to display if the information the user has provided in incorrect or incomplete
	 * @return
	 */
	String validateFields();
	
	/**
	 * Any context specific objects to add to the scan - return null for none.
	 * @return
	 */
	Object[] getContextSpecificObjects();

}
