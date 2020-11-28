/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.zaproxy.zap.utils.Enableable;

public class DataDrivenNode extends Enableable implements Cloneable {
    private static final String CONFIG_NAME = "name";
    
    private static final String CONFIG_PREFIX_PATTERN = "prefixPattern";
    private static final String CONFIG_DATA_NODE_PATTERN = "dataNodePattern";
    private static final String CONFIG_SUFFIX_PATTERN = "suffixPattern";
    private static final String CONFIG_ENABLED = "enabled";
    private static final String CONFIG_CHILD_NODES = "childNodes";

    private DataDrivenNode parentNode;
    private List<DataDrivenNode> childNodes;

    private String name;
    
    private String prefixPattern;
    private String dataNodePattern;
    private String suffixPattern;
    
    private Pattern nodeRegex;

    public DataDrivenNode(String name, String prefixPattern, String dataNodePattern, String suffixPattern, DataDrivenNode parentNode) {
        super();

        this.name = name;

        this.prefixPattern = prefixPattern;
        this.dataNodePattern = dataNodePattern;
        this.suffixPattern = suffixPattern;

        this.parentNode = parentNode;
        this.childNodes = new ArrayList<DataDrivenNode>();
        invalidateNodePattern();
    }
    
    public DataDrivenNode(String name, DataDrivenNode parentNode) {
    	this(name, "", "", "", parentNode);
    }
    
    public DataDrivenNode(DataDrivenNode parentNode) {
    	this("", "", "", "", parentNode);
    }

    public DataDrivenNode(String config) {
        super();

        JSONObject configData = JSONObject.fromObject(config);

        this.name = configData.getString(CONFIG_NAME);
        
        this.prefixPattern = configData.getString(CONFIG_PREFIX_PATTERN);
        this.dataNodePattern = configData.getString(CONFIG_DATA_NODE_PATTERN);
        this.suffixPattern = configData.getString(CONFIG_SUFFIX_PATTERN);
        this.setEnabled(configData.getBoolean(CONFIG_ENABLED));

        this.childNodes = new ArrayList<DataDrivenNode>();
        JSONArray childrenConfig = configData.getJSONArray(CONFIG_CHILD_NODES);
        for (int childCounter = 0; childCounter < childrenConfig.size(); childCounter++) {
            JSONObject childConfig = childrenConfig.getJSONObject(childCounter);

            DataDrivenNode childNode = new DataDrivenNode(childConfig.toString());
            childNode.setParentNode(this);
            this.childNodes.add(childNode);
        }
        invalidateNodePattern();
    }

    public String getName() {
        return ValueOrEmptyString(name);
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getPrefixPattern() {
    	return ValueOrEmptyString(prefixPattern);
    }
    
    public void setPrefixPattern(String prefixPattern) {
    	this.prefixPattern = prefixPattern;
    	invalidateNodePattern();
    }
    
    public String getDataNodePattern() {
    	return ValueOrEmptyString(dataNodePattern);
    }
    
    public void setDataNodePattern(String dataNodePattern) {
    	this.dataNodePattern = dataNodePattern;
    	invalidateNodePattern();
    }
    
    public String getSuffixPattern() {
    	return ValueOrEmptyString(this.suffixPattern);
    }
    
    public void setSuffixPattern(String suffixPattern) {
    	this.suffixPattern = suffixPattern;
    	invalidateNodePattern();
    }

    public String getPattern() {
    	String parentReplacedPattern = "";
    	if (this.parentNode != null) {
    		parentReplacedPattern = this.parentNode.getReplacedPattern(true);
    	}
    	
        return parentReplacedPattern + getPrefixPattern() + getDataNodePattern() + getSuffixPattern();
    }
    
    public Pattern getRegEx() {
    	if (this.nodeRegex == null) {
    		this.nodeRegex = Pattern.compile(getPattern());
    	}
    	return this.nodeRegex;
    }
    
    public String getReplacedPattern(boolean includeParent) {
    	String parentReplacedPattern = "";
    	if (includeParent && this.parentNode != null) {
    		parentReplacedPattern = this.parentNode.getReplacedPattern(true);
    	}
    	
    	String ddnReplacement = "";
    	if (!this.dataNodePattern.isBlank()) {
    		ddnReplacement = getDataNodeReplacement();
    	}
    	return parentReplacedPattern + getPrefixPattern() + ddnReplacement + getSuffixPattern();
    }
    
    public String getReplacementPattern() {
    	return getReplacedPattern(true);
    }
    
    public String getDataNodeReplacement() {
    	return SessionStructure.DATA_DRIVEN_NODE_PREFIX + getName() + SessionStructure.DATA_DRIVEN_NODE_POSTFIX;
    }

    public DataDrivenNode getParentNode() {
        return parentNode;
    }
    
    protected void setParentNode(DataDrivenNode parentNode) {
    	this.parentNode = parentNode;
    }
    
    public void addChildNode(DataDrivenNode child) {
    	child.setParentNode(this);
    	this.childNodes.add(child);
    }

    public List<DataDrivenNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<DataDrivenNode> childNodes) {
        this.childNodes = new ArrayList<>();
        
        for (DataDrivenNode child : childNodes) {
        	child.setParentNode(this);
        }
    }
    
    public void removeChildNode(DataDrivenNode child) {
    	child.setParentNode(null);
    	this.childNodes.remove(child);
    }

    @Override
    public DataDrivenNode clone() {
        DataDrivenNode clone = new DataDrivenNode(this.name, this.prefixPattern, this.dataNodePattern, this.suffixPattern, this.parentNode);
        for (DataDrivenNode child : this.childNodes) {
        	clone.addChildNode(child.clone());
        }
        
        return clone;
    }

    public String getConfig() {
        JSONObject serialized = new JSONObject();

        serialized.put(CONFIG_NAME, this.name);

        serialized.put(CONFIG_PREFIX_PATTERN, this.prefixPattern);
        serialized.put(CONFIG_DATA_NODE_PATTERN, this.dataNodePattern);
        serialized.put(CONFIG_SUFFIX_PATTERN, this.suffixPattern);
        serialized.put(CONFIG_ENABLED, this.isEnabled());

        JSONArray serializedChildren = new JSONArray();
        for (DataDrivenNode childNode : this.childNodes) {
        	serializedChildren.add(childNode.getConfig());
        }
        serialized.put(CONFIG_CHILD_NODES, serializedChildren);

        return serialized.toString();
    }
    
    @Override
    public String toString() {
    	String pattern = getReplacedPattern(false);
    	return !pattern.isBlank() ? pattern : name;
    }
    
    protected String ValueOrEmptyString(String value) {
    	return (value != null) ? value : "";
    }
    
    protected void invalidateNodePattern() {
    	this.nodeRegex = null;
    }
}
