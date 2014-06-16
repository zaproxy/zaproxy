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

import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;


class AlertTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;
	
    private static final Comparator<AlertNode> GROUP_ALERT_CHILD_COMPARATOR = new GroupAlertChildNodeComparator();
    private static final Comparator<AlertNode> ALERT_CHILD_COMPARATOR = new AlertChildNodeComparator();

    private static Logger logger = Logger.getLogger(AlertTreeModel.class);

    AlertTreeModel() {
        super(new AlertNode(-1, Constant.messages.getString("alerts.tree.title"), GROUP_ALERT_CHILD_COMPARATOR));
    }
    
    private String getRiskString (Alert alert) {
    	// Note that the number comments are to ensure the right ordering in the tree :)
    	if (alert.getConfidence() == Alert.FALSE_POSITIVE) {
    		return "<html><!--5--><img src=\"" + Constant.OK_FLAG_IMAGE_URL + "\">&nbsp;" + alert.getAlert() + "<html>";
    	}
		return "<html><!--" + (5 - alert.getRisk()) + "--><img src=\"" + alert.getIconUrl() + "\">&nbsp;" + alert.getAlert() + "<html>";
    }
    
    void addPath(final Alert alert) {
        if (EventQueue.isDispatchThread()) {
        	addPathEventHandler(alert);
        } else {
            try {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	addPathEventHandler(alert);
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    private synchronized void addPathEventHandler(Alert alert) {
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
            AlertNode child = parent.getChildAt(i);
            if (child.getChildCount() == 0) {
            	// Its a leaf node
	        	if (child.getUserObject() != null && 
	        			child.getUserObject().getAlertId() == alert.getAlertId()) {
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
    
    void updatePath(final Alert originalAlert, final Alert alert) {
        if (EventQueue.isDispatchThread()) {
        	updatePathEventHandler(originalAlert, alert);
        } else {
            try {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	updatePathEventHandler(originalAlert, alert);
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    private synchronized void updatePathEventHandler(Alert originalAlert, Alert alert) {

        AlertNode node = findLeafNodeForAlert((AlertNode) getRoot(), alert);
        if (node != null) {
        	
        	// Remove the old version
        	AlertNode parent = node.getParent();

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
    }
    
    private AlertNode findAndAddChild(AlertNode parent, String nodeName, Alert alert) {
        int risk = alert.getRisk();
        if (alert.getConfidence() == Alert.FALSE_POSITIVE) {
            // Special case!
            risk = -1;
        }

        int idx = parent.findIndex(new AlertNode(risk, nodeName));
        if (idx < 0) {
            idx = -(idx+1);
            AlertNode node = new AlertNode(risk, nodeName, ALERT_CHILD_COMPARATOR);
            node.setUserObject(alert);
            parent.insert(node, idx);
            nodesWereInserted(parent, new int[] { idx });
            return node;
        }
        return parent.getChildAt(idx);
    }

    private AlertNode findAndAddLeaf(AlertNode parent, String nodeName, Alert alert) {
        int risk = alert.getRisk();
        if (alert.getConfidence() == Alert.FALSE_POSITIVE) {
            // Special case!
            risk = -1;
        }

        AlertNode needle = new AlertNode(risk, nodeName);
        needle.setUserObject(alert);
        int idx = parent.findIndex(needle);
        if (idx < 0) {
            idx = -(idx+1);
            parent.insert(needle, idx);
            nodesWereInserted(parent, new int[] { idx });
        }
        AlertNode node = parent.getChildAt(idx);
        nodesChanged(node);
        return node;
    }
    
    private void nodesChanged(final AlertNode node) {
        if (EventQueue.isDispatchThread()) {
        	nodesChangedEventHandler(node);
        } else {
            try {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	nodesChangedEventHandler(node);
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    private void nodesChangedEventHandler(AlertNode node) {
    	// Loop up as parent node names include counts which might have changed
    	this.nodeChanged(node);
    	AlertNode parent = node.getParent();
    	if (parent != null) {
    		nodesChangedEventHandler(parent);
    	}
    }

    public synchronized void deletePath(Alert alert) {

        AlertNode node = findLeafNodeForAlert((AlertNode) getRoot(), alert);
        if (node != null) {
        	
        	// Remove it
        	AlertNode parent = node.getParent();

        	this.removeNodeFromParent(node);
            nodeStructureChanged(parent);
        	
        	if (parent.getChildCount() == 0) {
        		// Parent has no other children, remove it also
        		this.removeNodeFromParent(parent);
                nodeStructureChanged((AlertNode) this.getRoot());
            } else if (parent.getUserObject() == node.getUserObject()) {
                parent.setUserObject(parent.getChildAt(0).getUserObject());
            }
        }
    }
    
    private static class GroupAlertChildNodeComparator implements Comparator<AlertNode> {

        @Override
        public int compare(AlertNode alertNode, AlertNode anotherAlertNode) {
            return alertNode.getNodeName().compareTo(anotherAlertNode.getNodeName());
        }
    }

    private static class AlertChildNodeComparator implements Comparator<AlertNode> {

        @Override
        public int compare(AlertNode alertNode, AlertNode anotherAlertNode) {
            int result = alertNode.getNodeName().compareTo(anotherAlertNode.getNodeName());
            if (result != 0) {
                return result;
            }

            Alert alert = alertNode.getUserObject();
            Alert anotherAlert = anotherAlertNode.getUserObject();

            result = alert.getParam().compareTo(anotherAlert.getParam());
            if (result != 0) {
                return result;
            }

            if (alert.getAttack() == null) {
                if (anotherAlert.getAttack() == null) {
                    return 0;
                }
                return -1;
            }

            if (anotherAlert.getAttack() == null) {
                return 1;
            }

            return alert.getAttack().compareTo(anotherAlert.getAttack());
        }
    }
}
