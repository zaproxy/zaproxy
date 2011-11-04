/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2011/09/06 Fix alert save plus concurrent mod exceptions
// ZAP: 2011/11/04 Correct getHierarchicNodeName

package org.parosproxy.paros.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;

public class SiteNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 7987615016786179150L;

	private String nodeName = null;
    private HistoryReference historyReference = null;
    private Vector<HistoryReference> pastHistoryList = new Vector<HistoryReference>(10);
	// ZAP: Support for linking Alerts to SiteNodes
	private List<Alert> alerts = new ArrayList<Alert>();
	
    public SiteNode(String nodeName) {
        super();
        this.nodeName = nodeName;
    }
    
    private String getMaxAlertString() {
    	if (alerts.size() == 0) {
    		return "";
    	}
    	int highest = -1;

    	for (Alert alert : this.getAlerts()) {
    		if (alert.getReliability() != Alert.FALSE_POSITIVE && alert.getRisk() > highest) {
    			highest = alert.getRisk();
    		}
    	}
    	switch (highest) {
    	case Alert.RISK_INFO:
    		return "&nbsp;<img src=\"" + Constant.INFO_FLAG_IMAGE_URL + "\">";
    	case Alert.RISK_LOW:
    		return "&nbsp;<img src=\"" + Constant.LOW_FLAG_IMAGE_URL + "\">";
    	case Alert.RISK_MEDIUM:
    		return "&nbsp;<img src=\"" + Constant.MED_FLAG_IMAGE_URL + "\">";
    	case Alert.RISK_HIGH:
    		return "&nbsp;<img src=\"" + Constant.HIGH_FLAG_IMAGE_URL + "\">";
    	}
    	return "";
    }
    
    public String toString() {
    	return "<html>" + nodeName + getMaxAlertString() + "</html>";
    }
    
    public static String cleanName(String name) {
    	String cname = name;
    	if (cname.startsWith("<html>")) {
    		cname = cname.substring(6);
    	}
    	if (cname.indexOf("&nbsp;") > 0) {
    		cname = cname.substring(0, cname.indexOf("&nbsp;"));
    	}
    	if (cname.indexOf("</html>") > 0) {
    		cname = cname.substring(0, cname.indexOf("</html>"));
    	}
    	return cname;
    }
    
    public String getNodeName() {
    	return this.nodeName;
    }
    
    public String getHierarchicNodeName() {
    	if (this.isRoot()) {
    		return "";
    	}
    	if (((SiteNode)this.getParent()).isRoot()) {
    		return this.getNodeName();
    	}
    	String nodeName = this.getNodeName();
    	if (this.isLeaf()) {
    		// Need to clean up
    		int colonIndex = nodeName.indexOf(":");
    		if (colonIndex > 0) {
    			// Strip the GET/POST etc off
    			nodeName = nodeName.substring(colonIndex+1);
    		}
    		int bracketIndex = nodeName.indexOf("(");
    		if (bracketIndex > 0) {
    			// Strip the param summary off
    			nodeName = nodeName.substring(0, bracketIndex);
    		}
    	}
    	return ((SiteNode)this.getParent()).getHierarchicNodeName() + "/" + nodeName;
    }
    
    public HistoryReference getHistoryReference() {
        return historyReference;
    }
    
    /**
     * Set current node reference.
     * If there is any existing reference, delete if spider record.
     * Otherwise, put into past history list.
     * @param historyReference
     */
    public void setHistoryReference(HistoryReference historyReference) {

        if (getHistoryReference() != null) {
//            if (getHistoryReference().getHistoryType() == HistoryReference.TYPE_SPIDER) {
//                getHistoryReference().delete();
//                getHistoryReference().setSiteNode(null);
//            } else if (!getPastHistoryReference().contains(historyReference)) {
//                getPastHistoryReference().add(getHistoryReference());
//            }
            
            // above code commented as to always add all into past reference.  For use in scanner
            if (!getPastHistoryReference().contains(historyReference)) {
                getPastHistoryReference().add(getHistoryReference());
            }
        }
        
        this.historyReference = historyReference;
        this.historyReference.setSiteNode(this);
    }    
    
    public Vector<HistoryReference> getPastHistoryReference() {
        return pastHistoryList;
    }
    
    public boolean hasAlert(Alert alert) {
		for (Alert a : this.getAlerts()) {
			   if (a.equals(alert)) {
				   // We've already recorded it
				   return true;
			   }
		}
    	return false;
    }
    
    public void addAlert(Alert alert) {
    	if (this.hasAlert(alert)) {
    		return;
    	}
    	this.alerts.add(alert);
    	if (this.getParent() != null && 
 			   (! this.getParent().equals(this)) && this.getParent() instanceof SiteNode) {
 			((SiteNode)this.getParent()).addAlert(alert);
    	}
    }
    
    public void updateAlert(Alert alert) {
		Alert foundAlert = null;
		for (Alert a : this.getAlerts()) {
			if (a.getAlertId() == alert.getAlertId()) {
				// Do the work outside of the loop to prevent a concurrent mod exception
				foundAlert = a;
				break;
			}
		}
		if (foundAlert != null) {
			this.alerts.remove(foundAlert);
			this.alerts.add(alert);
		 	if (this.getParent() != null && 
		 			(! this.getParent().equals(this)) && this.getParent() instanceof SiteNode) {
		 		((SiteNode)this.getParent()).updateAlert(alert);
		 	}
			
		}
    }
    
    public List<Alert> getAlerts() {
 	   return Collections.synchronizedList(this.alerts);
    }
    
    private void clearChildAlert (Alert alert, SiteNode child) {
    	// Alerts are propagated up, which means when one is deleted we need to work out if it still
    	// is present in another child node
    	boolean removed = true;
    	alerts.remove(alert);
    	SiteNode c = (SiteNode) this.getFirstChild();
    	while (c != null) {
    		if (! c.equals(child)) {
    			if (c.hasAlert(alert)) {
    				alerts.add(alert);
    				removed = false;
    				break;
    			}
    		}
    		c = (SiteNode) this.getChildAfter(c);
    	}
	 	if (removed && this.getParent() != null && 
	 			(! this.getParent().equals(this)) && this.getParent() instanceof SiteNode) {
	 		((SiteNode)this.getParent()).clearChildAlert(alert, this);
	 	}
    }

	public void deleteAlert(Alert alert) {
		alerts.remove(alert);
		
		// Remove from parents, if not in siblings
	 	if (this.getParent() != null && 
	 			(! this.getParent().equals(this)) && this.getParent() instanceof SiteNode) {
	 		((SiteNode)this.getParent()).clearChildAlert(alert, this);
	 	}
	}
}
