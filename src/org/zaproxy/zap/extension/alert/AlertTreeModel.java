/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The ZAP Development team
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
package org.zaproxy.zap.extension.alert;

import javax.swing.tree.DefaultTreeModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.view.View;


class AlertTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;
	private int totalInfo = 0;
	private int totalLow = 0;
	private int totalMedium = 0;
	private int totalHigh = 0;
	
    AlertTreeModel() {
        super(new AlertNode(-1, Constant.messages.getString("alerts.tree.title")));
    }
    
    public void recalcAlertCounts() {
    	AlertNode parent = (AlertNode) getRoot();
    	totalInfo = 0;
    	totalLow = 0;
    	totalMedium = 0;
    	totalHigh = 0;
    	if (parent != null) {
            for (int i=0; i<parent.getChildCount(); i++) {
                AlertNode child = (AlertNode) parent.getChildAt(i);
                
            	switch (child.getRisk()) {
            	case Alert.RISK_INFO:
                    View.getSingleton().getMainFrame().getMainFooterPanel().setAlertInfo(++totalInfo);
                    break;
            	case Alert.RISK_LOW:
                    View.getSingleton().getMainFrame().getMainFooterPanel().setAlertLow(++totalLow);
                    break;
            	case Alert.RISK_MEDIUM:
                    View.getSingleton().getMainFrame().getMainFooterPanel().setAlertMedium(++totalMedium);
                    break;
            	case Alert.RISK_HIGH:
                    View.getSingleton().getMainFrame().getMainFooterPanel().setAlertHigh(++totalHigh);
                    break;
            	}
            }
    	}
        View.getSingleton().getMainFrame().getMainFooterPanel().setAlertInfo(totalInfo);
        View.getSingleton().getMainFrame().getMainFooterPanel().setAlertLow(totalLow);
        View.getSingleton().getMainFrame().getMainFooterPanel().setAlertMedium(totalMedium);
        View.getSingleton().getMainFrame().getMainFooterPanel().setAlertHigh(totalHigh);
    }
    
    private String getRiskString (Alert alert) {
    	// Note that the number comments are to ensure the right ordering in the tree :)
    	if (alert.getReliability() == Alert.FALSE_POSITIVE) {
    		return "<html><!--5--><img src=\"" + Constant.OK_FLAG_IMAGE_URL + "\">&nbsp;" + alert.getAlert() + "<html>";
    	}
		return "<html><!--" + (5 - alert.getRisk()) + "--><img src=\"" + alert.getIconUrl() + "\">&nbsp;" + alert.getAlert() + "<html>";
    }
    
    /**
     * 
     * @param msg
     * @return true if the node is added.  False if not.
     */
    synchronized void addPath(Alert alert) {
        
        AlertNode parent = (AlertNode) getRoot();
        String alertNodeName = getRiskString(alert);
    
        parent = findAndAddChild(parent, alertNodeName, alert);
        // Show the method first, if present
        String method = "";
        if (alert.getMethod() != null) {
        	method = alert.getMethod() + ": ";
        }
        findAndAddLeaf(parent, method + alert.getUri(), alert);
    }
    
    private AlertNode findLeafNodeForAlert(AlertNode parent, Alert alert) {
        for (int i=0; i<parent.getChildCount(); i++) {
            AlertNode child = (AlertNode) parent.getChildAt(i);
            if (child.getChildCount() == 0) {
            	// Its a leaf node
	        	if (child.getUserObject() != null && 
	        			((Alert)child.getUserObject()).getAlertId() == alert.getAlertId()) {
	        		return child;
	        	}
            } else {
            	// check its children
	        	AlertNode node = findLeafNodeForAlert(child, alert);
	        	if (node != null) {
	        		return node;
	        	}
            }
        }
    	return null;
    }
    
    synchronized void updatePath(Alert originalAlert, Alert alert) {

        AlertNode node = findLeafNodeForAlert((AlertNode) getRoot(), alert);
        if (node != null) {
        	
        	// Remove the old version
        	AlertNode parent = (AlertNode) node.getParent();

        	this.removeNodeFromParent(node);
            nodeStructureChanged(parent);
        	
        	if (parent.getChildCount() == 0) {
        		// Parent has no other children, remove it also
        		this.removeNodeFromParent(parent);
                nodeStructureChanged((AlertNode) this.getRoot());
        	}
        }
        // Add it back in again
        this.addPath(alert);
        recalcAlertCounts();
    }
    
 
    
    private AlertNode findAndAddChild(AlertNode parent, String nodeName, Alert alert) {
        AlertNode result = findChild(parent, nodeName);
        if (result == null) {
            AlertNode newNode = new AlertNode(alert.getRisk(), nodeName);
            if (alert.getReliability() == Alert.FALSE_POSITIVE) {
            	// Special case!
            	newNode.setRisk(-1);
            }
            int pos = parent.getChildCount();
            for (int i=0; i< parent.getChildCount(); i++) {
                if (nodeName.compareToIgnoreCase(parent.getChildAt(i).toString()) <= 0) {
                    pos = i;
                    break;
                }
            }
            insertNodeInto(newNode, parent, pos);
            result = newNode;
            result.setUserObject(alert);
        }
        return result;
    }

    private AlertNode findAndAddLeaf(AlertNode parent, String nodeName, Alert alert) {
        AlertNode result = findLeaf(parent, nodeName, alert);
        
        if (result == null) {
            AlertNode newNode = new AlertNode(alert.getRisk(), nodeName);
            if (alert.getReliability() == Alert.FALSE_POSITIVE) {
            	// Special case!
            	newNode.setRisk(-1);
            }
            int pos = parent.getChildCount();
            for (int i=0; i< parent.getChildCount(); i++) {
            	String childName = ((AlertNode)parent.getChildAt(i)).getNodeName();
                if (nodeName.compareToIgnoreCase(childName) <= 0) {
                    pos = i;
                    break;
                    
                }
            }
            
            insertNodeInto(newNode, parent, pos);
            result = newNode;
            result.setUserObject(alert);
        	this.nodesChanged(newNode);
        }
        recalcAlertCounts();
        return result;
    }
    
    private void nodesChanged(AlertNode node) {
    	// Loop up as parent node names include counts which might have changed
    	this.nodeChanged(node);
    	AlertNode parent = (AlertNode) node.getParent();
    	if (parent != null) {
    		nodesChanged(parent);
    	}
    }
    
    private AlertNode findChild(AlertNode parent, String nodeName) {
        for (int i=0; i<parent.getChildCount(); i++) {
            AlertNode child = (AlertNode) parent.getChildAt(i);
            if (child.getNodeName().equals(nodeName)) {
                return child;
            }
        }
        return null;
    }

    private AlertNode findLeaf(AlertNode parent, String nodeName, Alert alert) {
        for (int i=0; i<parent.getChildCount(); i++) {
            AlertNode child = (AlertNode) parent.getChildAt(i);
            if (child.getNodeName().equals(nodeName)) {
                if (child.getUserObject() == null) {
                    return null;
                }
                
                Alert tmp = (Alert) child.getUserObject();

                if (tmp.getParam().equals(alert.getParam()) && tmp.getAttack().equals(alert.getAttack())) {
                	return child;
                }
            }
        }
        return null;
    }

    public synchronized void deletePath(Alert alert) {

        AlertNode node = findLeafNodeForAlert((AlertNode) getRoot(), alert);
        if (node != null) {
        	
        	// Remove it
        	AlertNode parent = (AlertNode) node.getParent();

        	this.removeNodeFromParent(node);
            nodeStructureChanged(parent);
        	
        	if (parent.getChildCount() == 0) {
        		// Parent has no other children, remove it also
        		this.removeNodeFromParent(parent);
                nodeStructureChanged((AlertNode) this.getRoot());
        	}
        }
        recalcAlertCounts();
    }
    
}
