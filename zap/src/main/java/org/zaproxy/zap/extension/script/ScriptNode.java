/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development team
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
package org.zaproxy.zap.extension.script;

import javax.swing.tree.DefaultMutableTreeNode;

import org.parosproxy.paros.Constant;

/**
 * A {@link javax.swing.tree.MutableTreeNode MutableTreeNode} for model of scripts tree.
 * 
 * @since 2.2.0
 * @see ScriptTreeModel
 */
public class ScriptNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	private String nodeName = null;
	private ScriptType type = null;
	private boolean template = false;
    
	public ScriptNode() {
		// Only use for the root node
        super();
        this.nodeName = Constant.messages.getString("script.tree.root");
    }

	public ScriptNode(boolean template) {
		// Only use for the second level nodes
        this.template = template;
        if (template) {
            this.nodeName = Constant.messages.getString("script.tree.templates");
        } else {
            this.nodeName = Constant.messages.getString("script.tree.scripts");
        }
        
    }

	public ScriptNode(ScriptType type, boolean template) {
		this.nodeName = Constant.messages.getString(type.getI18nKey());
		this.type = type;
        this.template = template;
	}

	public ScriptNode(String name) {
		this.nodeName = name;
	}

	public ScriptNode(ScriptWrapper script) {
		this(script, false);
	}

	public ScriptNode(ScriptWrapper script, boolean template) {
		this.nodeName = script.getName();
		this.type  = script.getType();
		this.template = template;
		this.setUserObject(script);
	}

    @Override
    public String toString() {
        return nodeName;
    }

	public String getNodeName() {
		return nodeName;
	}
	
	public void setNodeName(String name) {
		this.nodeName = name;
	}
	
	@Override
	public ScriptNode getParent() {
		return (ScriptNode) super.getParent();
	}

	public ScriptType getType() {
		return type;
	}

	public boolean isTemplate() {
		return template;
	}
}
