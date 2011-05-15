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

package org.parosproxy.paros.model;

import java.util.ArrayList;
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
    	for (Alert alert : alerts) {
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
    	return ((SiteNode)this.getParent()).getHierarchicNodeName() + "/" + this.getNodeName();
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
		for (Alert a : alerts) {
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
		for (Alert a : alerts) {
			if (a.getAlertId() == alert.getAlertId()) {
				// Have to use the alertId instead of 'equals' as any of the
				// other params might have changed
				this.alerts.remove(a);
				this.alerts.add(alert);
			 	if (this.getParent() != null && 
			 			(! this.getParent().equals(this)) && this.getParent() instanceof SiteNode) {
			 		((SiteNode)this.getParent()).updateAlert(alert);
			 	}
			}
		}
    }
    
    public List<Alert> getAlerts() {
 	   return this.alerts;
    }
}
