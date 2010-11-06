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
package org.parosproxy.paros.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.RecordTag;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.db.TableTag;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


/**
* 
* This class abstracts a reference to a http message stored in database.
* It read the whole http message from database when getHttpMessage() is called.
*/
public class HistoryReference {

   /**
    * Temporary type = not retrieved from history.  To be deleted.
    */
   public static final int TYPE_TEMPORARY = 0;
   public static final int TYPE_MANUAL = 1;
   public static final int TYPE_SPIDER = 2;
   public static final int TYPE_SCANNER = 3;
   public static final int TYPE_SPIDER_SEED = 4;
   public static final int TYPE_SPIDER_VISITED = 5;
   public static final int TYPE_HIDDEN = 6;
   // ZAP: Added TYPE_BRUTE_FORCE
   public static final int TYPE_BRUTE_FORCE = 7;
   
   // -ve means unsaved message;
   public static final int TYPE_SPIDER_UNSAVE = -TYPE_SPIDER;

   private static java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("##0.###");
	private static TableHistory staticTableHistory = null;
	// ZAP: Support for multiple tags
	private static TableTag staticTableTag = null;
	// ZAP: Support for loading alerts from db
	private static TableAlert staticTableAlert = null;
	
	private int historyId = 0;
	private int historyType = TYPE_MANUAL;
	private SiteNode siteNode = null;
    private String display = null;
    private long sessionId = 0;
	
	// ZAP: Support for linking Alerts to Hrefs
	private List<Alert> alerts = new ArrayList<Alert>();

	/**
     * @return Returns the sessionId.
     */
    public long getSessionId() {
        return sessionId;
    }

    public HistoryReference(int historyId) throws HttpMalformedHeaderException, SQLException {
		RecordHistory history = null;		
		history = staticTableHistory.read(historyId);
		HttpMessage msg = history.getHttpMessage();
 	   	// ZAP: Support for multiple tags
		List<RecordTag> tags = staticTableTag.getTagsForHistoryID(historyId);
		for (RecordTag tr : tags) {
			msg.addTag(tr.getTag());
		}
		
		build(history.getSessionId(), history.getHistoryId(), history.getHistoryType(), msg);

		// ZAP: Support for loading the alerts from the db
		List<RecordAlert> alerts = staticTableAlert.getAlertsBySourceHistoryId(historyId);
		for (RecordAlert alert: alerts) {
			this.addAlert(new Alert(alert, this));
		}
	}
	
	public HistoryReference(Session session, int historyType, HttpMessage msg) throws HttpMalformedHeaderException, SQLException {
		
		RecordHistory history = null;		
		history = staticTableHistory.write(session.getSessionId(), historyType, msg);		
		build(session.getSessionId(), history.getHistoryId(), history.getHistoryType(), msg);
		// ZAP: Init HttpMessage HistoryReference field
		msg.setHistoryRef(this);
		// ZAP: Support for multiple tags
		for (String tag : msg.getTags()) {
			this.addTag(tag);
		}
		// ZAP: Support for loading the alerts from the db
		List<RecordAlert> alerts = staticTableAlert.getAlertsBySourceHistoryId(historyId);
		for (RecordAlert alert: alerts) {
			this.addAlert(new Alert(alert, this));
		}
	}

	private void build(long sessionId, int historyId, int historyType, HttpMessage msg) {
	    this.sessionId = sessionId;
	    this.historyId = historyId;
		this.historyType = historyType;
		if (historyType == TYPE_MANUAL) {
		    this.display = getDisplay(msg);
		}
		// ZAP: Init HttpMessage HistoryReference field
		msg.setHistoryRef(this);
	}
	
	public static void setTableHistory(TableHistory tableHistory) {
		staticTableHistory = tableHistory;
	}
	public static void setTableTag(TableTag tableTag) {
		staticTableTag = tableTag;
	}
	public static void setTableAlert(TableAlert tableAlert) {
		staticTableAlert = tableAlert;
	}
	/**
	 * @return Returns the historyId.
	 */
	public int getHistoryId() {
		return historyId;
	}

	public HttpMessage getHttpMessage() throws HttpMalformedHeaderException, SQLException {
		// fetch complete message
		RecordHistory history = staticTableHistory.read(historyId);
		if (history == null) {
			return null;
		}
		if (history.getHttpMessage() != null) {
			// ZAP: Support for multiple tags
			List <RecordTag> tags = staticTableTag.getTagsForHistoryID(historyId);
			for (RecordTag tag : tags) {
				history.getHttpMessage().addTag(tag.getTag());
			}
		}
		// ZAP: Init HttpMessage HistoryReference field
		history.getHttpMessage().setHistoryRef(this);
		return history.getHttpMessage();
	}
	
	public String toString() {

        if (display != null) {
            return display;
        }
        
	    HttpMessage msg = null;
	    try {
	        msg = getHttpMessage();
            display = getDisplay(msg);	        
	    } catch (HttpMalformedHeaderException e1) {
	        display = "";
	    } catch (SQLException e) {
	        display = "";
	    }
        return display;
	}
	
   /**
    * @return Returns the historyType.
    */
   public int getHistoryType() {
       return historyType;
   }
   
   /**
    * Delete this HistoryReference from database
    *
    */
   public void delete() {
       if (historyId > 0) {
           try {
        	   // ZAP: Support for multiple tags
               staticTableTag.deleteTagsForHistoryID(historyId);
               staticTableHistory.delete(historyId);
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
   }
   
   
   /**
    * @return Returns the siteNode.
    */
   public SiteNode getSiteNode() {
       return siteNode;
   }
   /**
    * @param siteNode The siteNode to set.
    */
   public void setSiteNode(SiteNode siteNode) {
       this.siteNode = siteNode;
   }
   
   private String getDisplay(HttpMessage msg) {
       StringBuffer sb = new StringBuffer(Integer.toString(historyId) + " ");
       sb.append(msg.getRequestHeader().getPrimeHeader());
       if (!msg.getResponseHeader().isEmpty()) {
           sb.append(" \t=> " + msg.getResponseHeader().getPrimeHeader());
           String diffTimeString = "\t [" + decimalFormat.format((double) (msg.getTimeElapsedMillis()/1000.0)) + " s]";
           sb.append(diffTimeString);
       }
       
       return sb.toString();
       
   }
   
   // ZAP: Support for multiple tags
   public void addTag(String tag) {
       try {
           staticTableTag.insert(historyId, tag);
       } catch (SQLException e) {
           e.printStackTrace();
       }
   }
   
   public void deleteTag(String tag) {
       try {
           staticTableTag.delete(historyId, tag);
       } catch (SQLException e) {
           e.printStackTrace();
       }
   }
   
   public List<String> getTags() {
	   List <String> tags = new ArrayList<String>();
       try {
           List<RecordTag> rTags = staticTableTag.getTagsForHistoryID(historyId);
           for (RecordTag rTag : rTags) {
        	   tags.add(rTag.getTag());
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return tags;
   }
   
   // ZAP: Added setNote method to HistoryReference
   public void setNote(String note) {
       try {
           staticTableHistory.updateNote(historyId, note);
       } catch (SQLException e) {
           e.printStackTrace();
       }
       
   }
   
   public synchronized void addAlert(Alert alert) {
	   for (Alert a : alerts) {
		   if (a.equals(alert)) {
			   // We've already recorded it
			   return;
		   }
	   }
	   this.alerts.add(alert);
	   if (this.siteNode != null) {
		   siteNode.addAlert(alert);
	   }
   }
   
   public synchronized void updateAlert(Alert alert) {
	   for (Alert a : alerts) {
		   if (a.getAlertId() == alert.getAlertId()) {
			   // Have to use the alertId instead of 'equals' as any of the
			   // other params might have changed
			   this.alerts.remove(a);
			   this.alerts.add(alert);
			   if (this.siteNode != null) {
				   siteNode.updateAlert(alert);
			   }
			   return;
		   }
	   }
   }
   
   public int getHighestAlert() {
	   int i = Alert.RISK_INFO;
	   for (Alert a : alerts) {
		   if (a.getRisk() > i) {
			   i = a.getRisk();
		   }
	   }
	   
	   return i;
   }
   
   public List<Alert> getAlerts() {
	   return this.alerts;
   }
}
