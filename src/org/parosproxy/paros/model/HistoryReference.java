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
// ZAP: 2011/07/23 Added TYPE_FUZZER
// ZAP: 2011/12/04 Support deleting alerts
// ZAP: 2012/03/15 Changed the method getDisplay to use the class StringBuilder 
//      instead of StringBuffer.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/05/28 Added some JavaDoc
// ZAP: 2012/06/13 Optimized alerts related code
// ZAP: 2012/08/07 Deleted some not used Spider Related constants
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2012/02/26 Cache the response body length as part of Issue 539
// ZAP: 2013/08/07 Added TYPE_AUTHENTICATION
// ZAP: 2013/11/16 Issue 869: Differentiate proxied requests from (ZAP) user requests
// ZAP: 2013/11/16 Issue 892: Cache of response body length in HistoryReference might not be correct
// ZAP: 2014/04/10 Changed to use HttpMessageCachedData and expose the cached data
// ZAP: 2014/04/10 Issue 1042: Having significant issues opening a previous session
// ZAP: 2014/05/23 Issue 1209: Reliability becomes Confidence and add levels
// ZAP: 2014/06/10 Added TYPE_ACCESS_CONTROL
// ZAP: 2014/06/16 Issue 990: Allow to delete alerts through the API
// ZAP: 2014/08/14 Issue 1311: Differentiate temporary internal messages from temporary scanner messages

package org.parosproxy.paros.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
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
* It reads the whole http message from database when getHttpMessage() is called.
*/
public class HistoryReference {

   /**
    * Temporary type = not retrieved from history.  To be deleted.
    */
   public static final int TYPE_TEMPORARY = 0;

    /**
     * @deprecated Use {@link #TYPE_PROXIED} instead.
     * @see #TYPE_ZAP_USER
     */
    @Deprecated
    public static final int TYPE_MANUAL = 1;

    /**
     * A HTTP message that was proxied through ZAP.
     */
    public static final int TYPE_PROXIED = 1;

    /**
     * A HTTP message sent by the user from within ZAP, for example, using "Manual Request Editor" or "Resend" dialogues.
     */
    public static final int TYPE_ZAP_USER = 15;

   public static final int TYPE_SPIDER = 2;
   public static final int TYPE_SCANNER = 3;
   public static final int TYPE_HIDDEN = 6;
   // ZAP: Added TYPE_BRUTE_FORCE
   public static final int TYPE_BRUTE_FORCE = 7;
   public static final int TYPE_FUZZER = 8;
   // ZAP: Added TYPE_SPIDER_TASK for use in spider tasks
   public static final int TYPE_SPIDER_TASK = 9;
   // ZAP: Added TYPE_SPIDER_AJAX to use in spider ajax.
   public static final int TYPE_SPIDER_AJAX = 10;
   // ZAP: Added TYPE_AUTHENTICATION for use in authentication methods
   public static final int TYPE_AUTHENTICATION = 11;
   // ZAP: Added TYPE_ACCESS_CONTROL for use in access control testing methods
   public static final int TYPE_ACCESS_CONTROL = 13;
   public static final int TYPE_RESERVED_11 = 12;	// Reserved by Psiinon

    /**
     * A HTTP message sent by the (active) scanner which is set as temporary (i.e. deleted when the session is closed).
     */
    public static final int TYPE_SCANNER_TEMPORARY = 14;

   private static java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("##0.###");
	private static TableHistory staticTableHistory = null;
	// ZAP: Support for multiple tags
	private static TableTag staticTableTag = null;
	// ZAP: Support for loading alerts from db
	private static TableAlert staticTableAlert = null;
	
	private int historyId = 0;
	private int historyType = TYPE_PROXIED;
	private SiteNode siteNode = null;
    private String display = null;
    private long sessionId = 0;
    
    //ZAP: Support for specific icons
	private ArrayList<String> icons = null;
	private ArrayList<Boolean> clearIfManual = null;
	
	// ZAP: Support for linking Alerts to Hrefs
	private List<Alert> alerts;
	
	private List<String> tags = new ArrayList<>();
	private Boolean webSocketUpgrade = null;	// Deliberately a Boolean so we can initialise it from the msg

    private static Logger log = Logger.getLogger(HistoryReference.class);

    private HttpMessageCachedData httpMessageCachedData;

	/**
     * @return Returns the sessionId.
     */
    public long getSessionId() {
        return sessionId;
    }

    public HistoryReference(int historyId) throws HttpMalformedHeaderException, SQLException {
		RecordHistory history = null;
		this.icons =  new ArrayList<>();
		this.clearIfManual = new ArrayList<>();
		history = staticTableHistory.read(historyId);
		if (history == null) {
			throw new HttpMalformedHeaderException();
		}
		HttpMessage msg = history.getHttpMessage();
 	   	// ZAP: Support for multiple tags
		List<RecordTag> rtags = staticTableTag.getTagsForHistoryID(historyId);
		for (RecordTag rtag : rtags) {
			this.tags.add(rtag.getTag());
		}

		
		build(history.getSessionId(), history.getHistoryId(), history.getHistoryType(), msg);
	}
	
	public HistoryReference(Session session, int historyType, HttpMessage msg) throws HttpMalformedHeaderException, SQLException {
		
		RecordHistory history = null;	
		this.icons =  new ArrayList<>();
		this.clearIfManual = new ArrayList<>();
		history = staticTableHistory.write(session.getSessionId(), historyType, msg);		
		build(session.getSessionId(), history.getHistoryId(), history.getHistoryType(), msg);
		// ZAP: Init HttpMessage HistoryReference field
		msg.setHistoryRef(this);
		List <RecordTag> rtags = staticTableTag.getTagsForHistoryID(historyId);
		for (RecordTag rtag : rtags) {
			this.tags.add(rtag.getTag());
		}
		
		// ZAP: Support for loading the alerts from the db
		List<RecordAlert> alerts = staticTableAlert.getAlertsBySourceHistoryId(historyId);
		for (RecordAlert alert: alerts) {
			this.addAlert(new Alert(alert, this));
		}
	}
	
	
	/**
	 * 
	 * @return whether the icon has to be cleaned when being manually visited or not.
	 */
	public ArrayList<Boolean> getClearIfManual() {
		return this.clearIfManual;
	}
	
	
	/**
	 * 
	 * @return The icon's string path (i.e. /resource/icon/16/xx.png)
	 */
	public ArrayList<String> getCustomIcons(){
		return this.icons;
	}
	
	/**
	 * 
	 * @param i the icon's URL (i.e. /resource/icon/16/xx.png)
	 * @param c if the icon has to be cleaned when the node is manually visited
	 */
	public void setCustomIcon(String i, boolean c){
		this.icons.add(i);
		this.clearIfManual.add(c);
	}
	
	
	
	private void build(long sessionId, int historyId, int historyType, HttpMessage msg) {
	    this.sessionId = sessionId;
	    this.historyId = historyId;
		this.historyType = historyType;
		if (historyType == TYPE_PROXIED || historyType == TYPE_ZAP_USER) {
		    this.display = getDisplay(msg);
		}
		// ZAP: Init HttpMessage HistoryReference field
		msg.setHistoryRef(this);
		
		// Cache info commonly used so that we dont need to keep reading the HttpMessage from the db. 
		httpMessageCachedData = new HttpMessageCachedData(msg);
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

	/**
	 * Gets the corresponding http message from the database. Try to minimise calls to this method as much as possible.
	 * But also dont cache the HttpMessage either as this can significantly increase ZAP's memory usage.
	 * 
	 * @return the http message
	 * @throws HttpMalformedHeaderException the http malformed header exception
	 * @throws SQLException the sQL exception
	 */
	public HttpMessage getHttpMessage() throws HttpMalformedHeaderException, SQLException {
		// fetch complete message
		RecordHistory history = staticTableHistory.read(historyId);
		if (history == null) {
			throw new HttpMalformedHeaderException("No history reference for id " + historyId + " type=" + historyType);
		}
		// ZAP: Init HttpMessage HistoryReference field
		history.getHttpMessage().setHistoryRef(this);
		return history.getHttpMessage();
	}
	
	public URI getURI() {
		return httpMessageCachedData.getUri();
	}
	
	@Override
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
        	   log.error(e.getMessage(), e);
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
	   StringBuilder sb = new StringBuilder(Integer.toString(historyId));
	   sb.append(' ');
       sb.append(msg.getRequestHeader().getPrimeHeader());
       if (!msg.getResponseHeader().isEmpty()) {
           sb.append(" \t=> ").append(msg.getResponseHeader().getPrimeHeader());
           sb.append("\t [").append(decimalFormat.format(msg.getTimeElapsedMillis()/1000.0)).append(" s]");
       }
       
       return sb.toString();
       
   }
   
   	// ZAP: Support for multiple tags
   	public void addTag(String tag) {
   		try {
   			staticTableTag.insert(historyId, tag);
   			this.tags.add(tag);
   		} catch (SQLException e) {
   			log.error(e.getMessage(), e);
   		}
   	}
   
   	public void deleteTag(String tag) {
   		try {
   			staticTableTag.delete(historyId, tag);
   			this.tags.remove(tag);
   		} catch (SQLException e) {
   			log.error(e.getMessage(), e);
   		}
   	}

	public List<String> getTags() {
		return this.tags;
	}
   
   // ZAP: Added setNote method to HistoryReference
   public void setNote(String note) {
       try {
           staticTableHistory.updateNote(historyId, note);
           httpMessageCachedData.setNote(note != null && note.length() > 0);
       } catch (SQLException e) {
           log.error(e.getMessage(), e);
       }
       
   }
   
   public void loadAlerts() {
		// ZAP: Support for loading the alerts from the db
		List<RecordAlert> alerts;
		try {
			alerts = staticTableAlert.getAlertsBySourceHistoryId(historyId);
			for (RecordAlert alert: alerts) {
				this.addAlert(new Alert(alert, this));
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
   }
   
   public synchronized boolean addAlert(Alert alert) {
	   //If this is the first alert
	   if (alerts == null) {
		   alerts = new ArrayList<>(1);
	   }
	   
	   boolean add = true;
	   
	   for (Alert a : alerts) {
		   if (alert.equals(a)) {
			   // We've already recorded it
				add = false;
		   }
	   }
	   if (add) {
		   this.alerts.add(alert);
		   alert.setHistoryRef(this);
	   }
	   // Try to add to the SiteHNode anyway - that will also check if its already added
	   if (this.siteNode != null) {
		   siteNode.addAlert(alert);
	   } else {
	   }
	   return add;
   }
   
   public synchronized void updateAlert(Alert alert) {
	   //If there are no alerts yet
	   if (alerts == null) {
		   return;
	   }
	   
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
   
   public synchronized void deleteAlert(Alert alert) {
	   if (alerts != null) {
		   alerts.remove(alert);
	   }
   }
   
    public synchronized void deleteAllAlerts() {
        if (alerts != null) {
            alerts.clear();
        }
    }

   public int getHighestAlert() {
	   int i = -1;
	   //If there are no alerts
	   if(alerts==null)
		   return i;
	   for (Alert a : alerts) {
		   if (a.getConfidence() != Alert.FALSE_POSITIVE && a.getRisk() > i) {
			   i = a.getRisk();
		   }
	   }
	   
	   return i;
   }
   
   	/**
	 * Gets the alerts. If alerts where never added, an empty list will be returned. This list
	 * should be used as "read-only", not modifying content in the {@link HistoryReference} through
	 * it.
	 * 
	 * @return the alerts
	 */
   public List<Alert> getAlerts() {
	   if (alerts != null) {
		   return this.alerts;
	   } else {
		   return Collections.emptyList();
	   }
   }

	public String getMethod() {
		return httpMessageCachedData.getMethod();
	}

	public int getStatusCode() {
		return httpMessageCachedData.getStatusCode();
	}

	public String getReason() {
		return httpMessageCachedData.getReason();
	}

	public int getRtt() {
		return httpMessageCachedData.getRtt();
	}

	public void setTags(Vector<String> tags) {
		this.tags = tags;
	}
	
	public boolean hasNote() {
		return httpMessageCachedData.hasNote();
	}

	public long getTimeSentMillis() {
		return httpMessageCachedData.getTimeSentMillis();
	}

	public long getTimeReceivedMillis() {
		return httpMessageCachedData.getTimeReceivedMillis();
	}

	public boolean isWebSocketUpgrade() {
		if (webSocketUpgrade == null) {
			try {
				webSocketUpgrade = this.getHttpMessage().isWebSocketUpgrade();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				webSocketUpgrade = false;
			}
		}
		return webSocketUpgrade;
	}

	public int getRequestHeaderLength() {
		return httpMessageCachedData.getRequestHeaderLength();
	}

	public int getRequestBodyLength() {
		return httpMessageCachedData.getRequestBodyLength();
	}

	public int getResponseHeaderLength() {
		return httpMessageCachedData.getResponseHeaderLength();
	}

	public int getResponseBodyLength() {
		return httpMessageCachedData.getResponseBodyLength();
	} 
	
	public String getRequestBody() {
		String requestBody = httpMessageCachedData.getRequestBody();
		if (requestBody == null) {
			try {
				requestBody = getHttpMessage().getRequestBody().toString();
				httpMessageCachedData.setRequestBody(requestBody);
			} catch (HttpMalformedHeaderException | SQLException e) {
				log.error("Failed to reload request body from database with history ID: " + historyId, e);
				requestBody = "";
			}
		}
		return requestBody;
	}
}
