/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
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

import java.awt.Frame;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.utils.DisplayUtils;

public class ContextCreateDialog extends StandardFieldsDialog {

	private static final long serialVersionUID = 1L;
	
	private static final String NAME_FIELD = "context.label.name"; 
	private static final String DESC_FIELD = "context.label.desc"; 
	private static final String TOP_NODE = "context.label.top"; 
	private static final String IN_SCOPE_FIELD = "context.inscope.label"; 
	
	private SiteNode topNode = null;

	public ContextCreateDialog(Frame owner) {
		super(owner, "context.create.title", DisplayUtils.getScaledDimension(400,300));
		this.addTextField(NAME_FIELD, null);
		this.addNodeSelectField(TOP_NODE, null, false, false);
		this.addMultilineField(DESC_FIELD, "");
		this.addCheckBoxField(IN_SCOPE_FIELD, true);
	}
	
	@Override
	public void siteNodeSelected(String field, SiteNode node) {
		topNode = node;
		if (node != null && this.isEmptyField(NAME_FIELD)) {
			// They havnt chosen a context name yet, default to the name of the node they chose
			this.setFieldValue(NAME_FIELD, node.getNodeName());
		}
	}

	@Override
	public void save() {
		Context ctx = Model.getSingleton().getSession().getNewContext(this.getStringValue(NAME_FIELD));
		ctx.setDescription(this.getStringValue(DESC_FIELD));
		ctx.setInScope(this.getBoolValue(IN_SCOPE_FIELD));
		if (topNode != null) {
	        try {
				ctx.addIncludeInContextRegex(new StructuralSiteNode(topNode).getRegexPattern());
			} catch (DatabaseException e) {
				// Ignore
			}
		}
		
		Model.getSingleton().getSession().saveContext(ctx);
	}

	@Override
	public String validateFields() {
		if (this.isEmptyField(NAME_FIELD)) {
			return Constant.messages.getString("context.create.warning.noname");
		}
		return null;
	}

}
