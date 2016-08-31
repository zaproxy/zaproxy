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
// ZAP: 2011/11/29 Added blank image to node names to fix redrawing issue
// ZAP: 2012/02/11 Re-ordered icons, added spider icon and notify via SiteMap 
// ZAP: 2012/03/11 Issue 280: Escape URLs in sites tree
// ZAP: 2012/03/15 Changed the method toString to use the class StringBuilder 
//      and reworked the method toString and getIcons. Renamed the method 
//      getIcons to appendIcons.
// ZAP: 2012/07/29 Issue 43: Added support for Scope
// ZAP: 2012/08/29 Issue 250 Support for authentication management
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2013/01/23 Ignore Active scanner history refs
// ZAP: 2013/08/23 Make sure #nodeChanged() is called after removing a custom icon
// ZAP: 2013/11/16 Issue 869: Differentiate proxied requests from (ZAP) user requests
// ZAP: 2014/03/23 Issue 1084: NullPointerException while selecting a node in the "Sites" tab
// ZAP: 2014/04/10 Do not allow to set the parent node as itself
// ZAP: 2014/04/10 Issue 1118: Alerts Tab can get out of sync
// ZAP: 2014/05/05 Issue 1181: Vulnerable pages active scanned only once
// ZAP: 2014/05/23 Issue 1209: Reliability becomes Confidence and add levels
// ZAP: 2014/06/16 Fixed an issue in SiteNode#setHistoryReference(HistoryReference) that led
// to multiple occurrences of same HistoryReference(s) in the pastHistoryList.
// ZAP: 2014/06/16 Issue 990: Allow to delete alerts through the API
// ZAP: 2014/11/19 Issue 1412: Prevent ConcurrentModificationException when icons updated frequently
// ZAP: 2014/12/17 Issue 1174: Support a Site filter
// ZAP: 2015/04/02 Issue 1582: Low memory option
// ZAP: 2015/10/21 Issue 1576: Support data driven content
// ZAP: 2016/01/26 Fixed findbugs warning
// ZAP: 2016/03/24 Do not access EDT in daemon mode
// ZAP: 2016/04/12 Notify of changes when an alert is updated
// ZAP: 2016/08/30 Use a Set instead of a List for the alerts

package org.parosproxy.paros.model;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.SessionStructure;

public class SiteNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 7987615016786179150L;

	private String nodeName = null;
	private String hierarchicNodeName = null;
    private HistoryReference historyReference = null;
    private Vector<HistoryReference> pastHistoryList = new Vector<>(10);
	// ZAP: Support for linking Alerts to SiteNodes
    private SiteMap siteMap = null;
	private Set<Alert> alerts = Collections.synchronizedSet(new HashSet<Alert>());
	private boolean justSpidered = false;
	//private boolean justAJAXSpidered = false;
	private ArrayList<String> icons = null;
	private ArrayList<Boolean> clearIfManual = null;

    private static Logger log = Logger.getLogger(SiteNode.class);
    private boolean isIncludedInScope = false;
    private boolean isExcludedFromScope = false;
    private boolean filtered = false;
    private boolean dataDriven = false;

    /**
     * Flag that indicates whether or not the {@link #calculateHighestAlert() highest alert needs to be calculated}, when
     * {@link #appendIcons(StringBuilder) building the string representation}.
     */
    private boolean calculateHighestAlert;

    /**
     * The {@code Alert} with highest risk (and not a false positive).
     * 
     * @see #isHighestAlert(Alert)
     */
    private Alert highestAlert;

    public SiteNode(SiteMap siteMap, int type, String nodeName) {
        super();
        this.siteMap = siteMap;
		this.nodeName = nodeName;
		if (nodeName.startsWith(SessionStructure.DATA_DRIVEN_NODE_PREFIX)) {
			this.dataDriven = true; 
		}
		this.icons = new ArrayList<>();
		this.clearIfManual = new ArrayList<>();
		if (type == HistoryReference.TYPE_SPIDER) {
			this.justSpidered = true;
		}
	}
    
    public void setCustomIcons(ArrayList<String> i, ArrayList<Boolean> c) {
    	synchronized (this.icons) {
    		this.icons.clear();
    		this.icons.addAll(i);
    		this.clearIfManual = c;
    	}
    }
    
    public void addCustomIcon(String resourceName, boolean clearIfManual) {
    	synchronized (this.icons) {  
	    	if (! this.icons.contains(resourceName)) {
		    	this.icons.add(resourceName);
		    	this.clearIfManual.add(clearIfManual);
		    	this.nodeChanged();
	    	}
    	}
    }
    
    public void removeCustomIcon(String resourceName) {
    	synchronized (this.icons) {  
	    	if (this.icons.contains(resourceName)) {
	    		int i = this.icons.indexOf(resourceName);
	    		this.icons.remove(i);
	    		this.clearIfManual.remove(i);
	    		this.nodeChanged();
	    	}
    	}
    }
    
    private void appendIcons(StringBuilder sb) {
    	if (calculateHighestAlert) {
    		calculateHighestAlert();
    	}
    	if (highestAlert != null) {
    		sb.append("&nbsp;<img src=\"");
    		sb.append(highestAlert.getIconUrl());
    		sb.append("\">&nbsp;");
    	}
    	if (justSpidered) {
        	sb.append("&nbsp;<img src=\"");
        	sb.append(Constant.class.getResource("/resource/icon/10/spider.png"));
        	sb.append("\">&nbsp;");
    	}
    	synchronized (this.icons) {  
	    	if (!this.icons.isEmpty()) {
	    		for(String icon : this.icons) {
	    			sb.append("&nbsp;<img src=\"");
	    			sb.append(Constant.class.getResource(icon));
	    			sb.append("\">&nbsp;");
	    		}
	    	}
    	}
    }

    /**
     * Calculates the highest alert.
     * <p>
     * After a call to this method the {@link #highestAlert} will have the highest alert (or {@code null} if none) and the flag
     * {@link #calculateHighestAlert} will have the value {@code false}.
     * 
     * @see #isHighestAlert(Alert)
     */
    private void calculateHighestAlert() {
        synchronized (alerts) {
            highestAlert = null;
            for (Alert alert : alerts) {
                if (isHighestAlert(alert)) {
                    highestAlert = alert;
                }
            }
            calculateHighestAlert = false;
        }
    }

    /**
     * Tells whether or not the given alert is the alert with highest risk than the current highest alert.
     * <p>
     * {@link Alert#CONFIDENCE_FALSE_POSITIVE False positive alerts} are ignored.
     *
     * @param alert the alert to check
     * @return {@code true} if it's the alert with highest risk, {@code false} otherwise.
     */
    private boolean isHighestAlert(Alert alert) {
        if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
            return false;
        }
        if (highestAlert == null) {
            return true;
        }
        return alert.getRisk() > highestAlert.getRisk();
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html><body>");
    	appendIcons(sb);
    	sb.append(StringEscapeUtils.escapeHtml(nodeName));
    	sb.append("</body></html>");
    	
    	return sb.toString();
    }
    
    public boolean isParentOf (String nodeName) {
    	if (nodeName == null) {
    		return false;
    	}
        return nodeName.compareTo(this.nodeName) < 0;
    }

    public String getNodeName() {
    	return this.nodeName;
    }

    public String getCleanNodeName() {
    	return getCleanNodeName(true);
    }

    public String getCleanNodeName(boolean specialNodesAsRegex) {
    	String name = this.getNodeName();
    	if (specialNodesAsRegex && this.isDataDriven()) {
    		// Non-greedy regex pattern 
			name = "(.+?)";

    	} else if (this.isLeaf()) {
    		int colonIndex = name.indexOf(":");
    		if (colonIndex > 0) {
    			// Strip the GET/POST etc off
    			name = name.substring(colonIndex+1);
    		}
    		int bracketIndex = name.lastIndexOf("(");
    		if (bracketIndex > 0) {
    			// Strip the param summary off
    			name = name.substring(0, bracketIndex);
    		}
    		int quesIndex = name.indexOf("?");
    		if (quesIndex > 0) {
    			// Strip the parameters off
    			name = name.substring(0, quesIndex);
    		}
    	}
    	return name;
    }

    public String getHierarchicNodeName() {
    	return getHierarchicNodeName(true);
    }

    public String getHierarchicNodeName(boolean specialNodesAsRegex) {
		if (hierarchicNodeName != null && specialNodesAsRegex) {
			// The regex version is used most frequently, so cache
			return hierarchicNodeName;
		}

    	if (this.isRoot()) {
    		hierarchicNodeName = "";
    	} else if (this.getParent().isRoot()) {
    		hierarchicNodeName = this.getNodeName();
    	} else {
    		String name = 
        			this.getParent().getHierarchicNodeName(specialNodesAsRegex) + "/" + 
        					this.getCleanNodeName(specialNodesAsRegex);
    		if (!specialNodesAsRegex) {
    			// Dont cache the non regex version
    			return name;
    		}
    		hierarchicNodeName = name;
    	}
    	return hierarchicNodeName;
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
            
        	if (this.justSpidered && (historyReference.getHistoryType() == HistoryReference.TYPE_PROXIED ||
        	        historyReference.getHistoryType() == HistoryReference.TYPE_ZAP_USER)) {
        		this.justSpidered = false;
        		this.nodeChanged();
        	}
			// we remove the icons of the node that has to be cleaned when manually visiting them
			if (!this.icons.isEmpty() && (historyReference.getHistoryType() == HistoryReference.TYPE_PROXIED ||
			        historyReference.getHistoryType() == HistoryReference.TYPE_ZAP_USER)) {
		    	synchronized (this.icons) {  
					for (int i = 0; i < this.clearIfManual.size(); ++i) {
						if (this.clearIfManual.get(i) && this.icons.size() > i) {
							this.icons.remove(i);
							this.clearIfManual.remove(i);
						}
					}
		    	}
        		this.nodeChanged();
    		}
            if (HistoryReference.TYPE_SCANNER == historyReference.getHistoryType()) {
                getPastHistoryReference().add(historyReference);
                return;
            }

            // above code commented as to always add all into past reference.  For use in scanner
            if (!getPastHistoryReference().contains(getHistoryReference())) {
                getPastHistoryReference().add(getHistoryReference());
            }
        }
        
        this.historyReference = historyReference;
        this.historyReference.setSiteNode(this);
    }    
    
    private void nodeChanged() {
    	if (this.siteMap == null || !View.isInitialised()) {
    		return;
    	}
        if (EventQueue.isDispatchThread()) {
        	nodeChangedEventHandler();
        } else {
            try {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	nodeChangedEventHandler();
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void nodeChangedEventHandler() {
		this.siteMap.nodeChanged(this);
    }
    
    public Vector<HistoryReference> getPastHistoryReference() {
        return pastHistoryList;
    }
    
    public boolean hasAlert(Alert alert) {
    	if (alert == null) {
    		throw new IllegalArgumentException("Alert must not be null");
    	}
    	return alerts.contains(alert);
    }
    
    public void addAlert(Alert alert) {
    	if (alert == null) {
    		throw new IllegalArgumentException("Alert must not be null");
    	}
    	if (!this.alerts.add(alert)) {
    		return;
    	}
    	if (isHighestAlert(alert)) {
    		highestAlert = alert;
    	}
    	if (this.getParent() != null) {
 			this.getParent().addAlert(alert);
    	}
    	if (this.siteMap != null) {
    		// Adding alert might affect the nodes visibility in a filtered tree
    		siteMap.applyFilter(this);
    	}
		this.nodeChanged();
    }
    
    public void updateAlert(Alert alert) {
    	if (alert == null) {
    		throw new IllegalArgumentException("Alert must not be null");
    	}
    	boolean updated = false;
    	synchronized (alerts) {
            for (Iterator<Alert> it = alerts.iterator(); it.hasNext();) {
                if (it.next().getAlertId() == alert.getAlertId()) {
                    it.remove();
                    updated = true;
                    this.alerts.add(alert);
                    setCalculateHighestAlertIfSameAlert(alert);
                    break;
                }
            }
        }

        if (updated) {
            if (this.getParent() != null) {
                this.getParent().updateAlert(alert);
            }
            if (this.siteMap != null) {
                // Updating an alert might affect the nodes visibility in a filtered tree
                siteMap.applyFilter(this);
            }
            this.nodeChanged();
        }
    }
    
    /**
     * Gets the alerts of the node.
     * <p>
     * The returned {@code List} is a copy of the internal collection.
     *
     * @return a new {@code List} containing the {@code Alert}s
     */
    public List<Alert> getAlerts() {
        synchronized (alerts) {
            return new ArrayList<>(alerts);
        }
    }
    
    private void clearChildAlert (Alert alert, SiteNode child) {
    	// Alerts are propagated up, which means when one is deleted we need to work out if it still
    	// is present in another child node
    	boolean removed = true;
    	alerts.remove(alert);
		if (this.getChildCount() > 0) {
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
		}
	 	if (removed) {
	 	    setCalculateHighestAlertIfSameAlert(alert);
	 	    nodeChanged();
	 	    if (this.getParent() != null) {
	 	        this.getParent().clearChildAlert(alert, this);
	 	    }
	 	}
    }

	public void deleteAlert(Alert alert) {
    	if (alert == null) {
    		throw new IllegalArgumentException("Alert must not be null");
    	}
		if (!alerts.remove(alert)) {
			return;
		}

		setCalculateHighestAlertIfSameAlert(alert);
		
		// Remove from parents, if not in siblings
	 	if (this.getParent() != null) {
	 		this.getParent().clearChildAlert(alert, this);
	 	}
    	if (this.siteMap != null) {
    		// Deleting alert might affect the nodes visibility in a filtered tree
    		siteMap.applyFilter(this);
    	}
		this.nodeChanged();
	}
	
    /**
     * Sets whether or not the highest alert needs to be calculated, based on the given alert.
     * <p>
     * The highest alert needs to be calculated if the given alert is the highest alert.
     *
     * @param alert the alert to check
     */
    private void setCalculateHighestAlertIfSameAlert(Alert alert) {
        if (highestAlert != null && highestAlert.getAlertId() == alert.getAlertId()) {
            calculateHighestAlert = true;
            highestAlert = null;
        }
    }

    public void deleteAlerts(List<Alert> alerts) {
        if (this.alerts.removeAll(alerts)) {
            // Remove from parents, if not in siblings
            if (this.getParent() != null) {
                this.getParent().clearChildAlerts(alerts);
            }
        	if (this.siteMap != null) {
        		// Deleting alerts might affect the nodes visibility in a filtered tree
        		siteMap.applyFilter(this);
        	}
        	calculateHighestAlert = true;
            this.nodeChanged();
        }
    }

    /**
     * Deletes all alerts of this node and all child nodes recursively.
     */
    public void deleteAllAlerts() {
        for(int i = 0; i < getChildCount(); i++) {
            ((SiteNode) getChildAt(i)).deleteAllAlerts();
        }

        if (!alerts.isEmpty()) {
            alerts.clear();
        	if (this.siteMap != null) {
        		// Deleting alert might affect the nodes visibility in a filtered tree
        		siteMap.applyFilter(this);
        	}
            nodeChanged();
        }
    }

    private void clearChildAlerts(List<Alert> alerts) {
        List<Alert> alertsToRemove = new ArrayList<>(alerts);
        if (this.getChildCount() > 0) {
            SiteNode c = (SiteNode) this.getFirstChild();
            while (c != null) {
                alertsToRemove.removeAll(c.alerts);
                c = (SiteNode) this.getChildAfter(c);
            }
        }
        boolean changed = this.alerts.removeAll(alertsToRemove);
        if (changed) {
            calculateHighestAlert = true;
            if (this.getParent() != null) {
                this.getParent().clearChildAlerts(alertsToRemove);
            }
            nodeChangedEventHandler();
        }
    }

	public boolean hasHistoryType (int type) {
		if (this.historyReference == null) {
			return false;
		}
		if (this.historyReference.getHistoryType() == type) {
			return true;
		}
		for (HistoryReference href : this.pastHistoryList) {
			if (href.getHistoryType() == type) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasJustHistoryType (int type) {
		if (this.historyReference == null) {
			return false;
		}
		if (this.historyReference.getHistoryType() != type) {
			return false;
		}
		for (HistoryReference href : this.pastHistoryList) {
			if (href.getHistoryType() != type) {
				return false;
			}
		}
		return true;
	}

	public boolean isIncludedInScope() {
		return isIncludedInScope;
	}

	public void setIncludedInScope(boolean isIncludedInScope, boolean applyToChildNodes) {
		this.isIncludedInScope = isIncludedInScope;
		if (siteMap != null) {
			// This could have affected its visibility
			siteMap.applyFilter(this);
		}
		this.nodeChanged();
		// Recurse down
		if (this.getChildCount() > 0 && applyToChildNodes) {
			SiteNode c = (SiteNode) this.getFirstChild();
    		while (c != null) {
    			c.setIncludedInScope(isIncludedInScope, applyToChildNodes);
    			c = (SiteNode) this.getChildAfter(c);
    		}
		}
	}

	public boolean isExcludedFromScope() {
		return isExcludedFromScope;
	}

	public void setExcludedFromScope(boolean isExcludedFromScope, boolean applyToChildNodes) {
		this.isExcludedFromScope = isExcludedFromScope;
		if (isExcludedFromScope) {
			this.isIncludedInScope = false;
		}
		if (siteMap != null) {
			// This could have affected its visibility
			siteMap.applyFilter(this);
		}
		this.nodeChanged();
		// Recurse down
		if (this.getChildCount() > 0 && applyToChildNodes) {
	    	SiteNode c = (SiteNode) this.getFirstChild();
	    	while (c != null) {
	    		c.setExcludedFromScope(isExcludedFromScope, applyToChildNodes);
	    		c = (SiteNode) this.getChildAfter(c);
	    	}
		}
	}
	
	@Override
	public void setParent(MutableTreeNode newParent) {
		if (newParent == this) {
			return;
		}
		super.setParent(newParent);
	}

    /**
     * Returns this node's parent or null if this node has no parent.
     *
     * @return  this node's parent SiteNode, or null if this node has no parent
     */
	@Override
    public SiteNode getParent() {
        return (SiteNode)super.getParent();
    }

	public boolean isFiltered() {
		return filtered;
	}

	protected void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	public boolean isDataDriven() {
		return dataDriven;
	}
}
