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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ScriptTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;

	//private static final Logger logger = Logger.getLogger(ScriptTreeModel.class);
	
	private Map<String, ScriptNode> nodeMap = new HashMap<String, ScriptNode>(); 

    ScriptTreeModel() {
        super(new ScriptNode());
    }
    
    protected void addType(ScriptType type) {
        ScriptNode node = new ScriptNode(type);
        nodeMap.put(type.getName(), node);
        this.getRoot().add(node);
    	
    }
    
    public ScriptNode getTypeNode(String type) {
    	return nodeMap.get(type);
    }
    
    @Override
    public ScriptNode getRoot() {
    	return (ScriptNode)this.root;
    }
    
    public List<ScriptNode> getNodes(String type) {
		List<ScriptNode> list = new ArrayList<ScriptNode>();
		ScriptNode parent = nodeMap.get(type);
		if (parent != null) {
			for (int i=0; i < parent.getChildCount(); i++) {
				list.add((ScriptNode)parent.getChildAt(i));
			}
		}
		return list;
    }

	public ScriptNode addScript(ScriptWrapper script) {
		if (script == null) {
			return null;
		}
		if (this.getScript(script.getName()) != null) {
			throw new InvalidParameterException("A script with the same name already exists: " + script.getName());
		}
		
		ScriptNode node = new ScriptNode(script);
		ScriptNode parent = nodeMap.get(script.getType().getName());
		
		if (parent != null) {
			parent.add(node);
			this.nodeStructureChanged(parent);
			return node;
		} else {
			throw new InvalidParameterException("Unrecognised type: " + script.getType());
		}
	}

	public void removeScript(ScriptWrapper script) {
		if (script == null) {
			return;
		}
		
		ScriptNode node = this.getNodeForScript(script);
		ScriptNode parent = node.getParent();
		if (parent != null) {
			parent.remove(node);
			this.nodeStructureChanged(parent);
		}
	}
	
	public ScriptNode getNodeForScript(ScriptWrapper script) {
		ScriptNode parent = nodeMap.get(script.getType().getName());
		
		if (parent != null) {
			ScriptNode node = (ScriptNode) parent.getFirstChild();
			while (node != null) {
				if (script.equals(node.getUserObject())) {
					return node;
				}
				node = (ScriptNode) parent.getChildAfter(node);
			}
		}
		return null;
		
	}

	public void nodeStructureChanged(ScriptWrapper script) {
		ScriptNode node = this.getNodeForScript(script);
		if (node != null) {
			this.nodeStructureChanged(node);
		}
	}

	public ScriptWrapper getScript(String name) {
		ScriptNode typeNode = (ScriptNode) getRoot().getFirstChild();
		while (typeNode != null) {
			// Loop through their children
			if (typeNode.getChildCount() > 0) {
				ScriptNode scriptNode = (ScriptNode) typeNode.getFirstChild();
				while (scriptNode != null) {
					if (((ScriptWrapper)scriptNode.getUserObject()).getName().equals(name)) {
						return (ScriptWrapper)scriptNode.getUserObject();
					}
					scriptNode = (ScriptNode) typeNode.getChildAfter(scriptNode);
				}
			}
			typeNode = (ScriptNode) getRoot().getChildAfter(typeNode);
		}
		return null;
	}

}
