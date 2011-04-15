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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.FileXML;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.xml.sax.SAXException;


public class Session extends FileXML {
	
    // ZAP: Added logger
    private static Log log = LogFactory.getLog(Session.class);

	private static final String ROOT = "session";
	
	private static final String SESSION_DESC = "sessionDesc";
	private static final String SESSION_ID = "sessionId";
	private static final String SESSION_NAME = "sessionName";
	
	private static final String[] PATH_SESSION_DESC = {ROOT, SESSION_DESC};	
	private static final String[] PATH_SESSION_ID = {ROOT, SESSION_ID};
	private static final String[] PATH_SESSION_NAME = {ROOT, SESSION_NAME};


	// other runtime members
	private Model model = null;
	private String fileName = "";
	private String sessionDesc = "";
	
	// parameters in XML
	private long sessionId = 0;
	private String sessionName = "";
	private SiteMap siteTree = null;
	
	/**
	 * Constructor for the current session.  The current system time will be used as the session ID.
	 * @param sessionId
	 */
	public Session(Model model) {
		super(ROOT);

		/*try {
			parseFile("xml/untitledsession.xml");
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		// add session variable here
		setSessionId(System.currentTimeMillis());
		setSessionName(Constant.messages.getString("session.untitled"));
		setSessionDesc("");
		
		// create default object
		this.siteTree = SiteMap.createTree(model);
		
		this.model = model;
		
	}
	
	public void discard() {
	    try {
	        model.getDb().getTableHistory().deleteHistorySession(getSessionId());
        } catch (SQLException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
	}
	
	
    /**
     * @return Returns the sessionDesc.
     */
    public String getSessionDesc() {
        return sessionDesc;
    }
	
	/**
	 * @return Returns the sessionId.
	 */
	public long getSessionId() {
		return sessionId;
	}
	/**
	 * @return Returns the name.
	 */
	public String getSessionName() {
		return sessionName;
	}
    /**
     * @return Returns the siteTree.
     */
    public SiteMap getSiteTree() {
        return siteTree;
    }
	
	

    /**
     * @return Returns the newState.
     */
    public boolean isNewState() {
        return fileName.equals("");
    }

    
    public void open(final File file, final SessionListener callback) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Exception thrownException = null;
                try {
                    open(file.getAbsolutePath());
                } catch (Exception e) {
                    thrownException = e;
                }
                if (callback != null) {
                    callback.sessionOpened(file, thrownException);
                }
            }
        });
        t.setPriority(Thread.NORM_PRIORITY-2);
        t.start();
    }

	public void open(String fileName) throws SQLException, SAXException, IOException, Exception {

		readAndParseFile(fileName);
		model.getDb().close(false);
		model.getDb().open(fileName);
		this.fileName = fileName;
		
		//historyList.removeAllElements();

		SiteNode newRoot = new SiteNode(Constant.messages.getString("tab.sites"));
		siteTree.setRoot(newRoot);

		// update history reference
		List<Integer> list = model.getDb().getTableHistory().getHistoryList(getSessionId(), HistoryReference.TYPE_MANUAL);
		HistoryReference historyRef = null;
		
		for (int i=0; i<list.size(); i++) {
			int historyId = ((Integer) list.get(i)).intValue();

			try {
				historyRef = new HistoryReference(historyId);
				getSiteTree().addPath(historyRef);
				// ZAP: Load alerts from db
				historyRef.loadAlerts();

				if (i % 100 == 99) Thread.yield();
			} catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
			};
			
		}
		
		// update siteTree reference
		list = model.getDb().getTableHistory().getHistoryList(getSessionId(), HistoryReference.TYPE_SPIDER);
		
		for (int i=0; i<list.size(); i++) {
			int historyId = ((Integer) list.get(i)).intValue();

			try {
				historyRef = new HistoryReference(historyId);
				getSiteTree().addPath(historyRef);

				if (i % 100 == 99) Thread.yield();

			} catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
			};
			
			
		}
		
		// ZAP: expand root
	    View.getSingleton().getSiteTreePanel().expandRoot();

		System.gc();
		
	}
	
	protected void parse() throws Exception {
	    
	    long tempSessionId = 0;
	    String tempSessionName = "";
	    String tempSessionDesc = "";
	    
	    // use temp variable to check.  Exception will be flagged if any error.
		tempSessionId = Long.parseLong(getValue(SESSION_ID));
		tempSessionName = getValue(SESSION_NAME);
		tempSessionName = getValue(SESSION_NAME);

		// set member variable after here
		sessionId = tempSessionId;
		sessionName = tempSessionName;
		sessionDesc = tempSessionDesc;
		


	}

	/**
	 * Asynchronous call to save a session.
	 * @param fileName
	 * @param callback
	 */
    public void save(final String fileName, final SessionListener callback) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Exception thrownException = null;
                try {
                    save(fileName);
                } catch (Exception e) {
                	// ZAP: Log exceptions
                	log.warn(e.getMessage(), e);
                    thrownException = e;
                }
                if (callback != null) {
                    callback.sessionSaved(thrownException);
                }
            }
        });
        t.setPriority(Thread.NORM_PRIORITY-2);
        t.start();
    }
    
    /**
     * Synchronous call to save a session.
     * @param fileName
     * @throws Exception
     */
	public void save(String fileName) throws Exception {
	    saveFile(fileName);
		if (isNewState()) {
		    model.moveSessionDb(fileName);
		} else {
		    if (!this.fileName.equals(fileName)) {
		        // copy file to new fileName
		        model.copySessionDb(this.fileName, fileName);
		    }
		}
	    this.fileName = fileName;
		
		synchronized (siteTree) {
		    saveSiteTree((SiteNode) siteTree.getRoot());
		}
		
		model.getDb().getTableSession().update(getSessionId(), getSessionName());

	}
	
    /**
     * @param sessionDesc The sessionDesc to set.
     */
    public void setSessionDesc(String sessionDesc) {
        this.sessionDesc = sessionDesc;
		setValue(PATH_SESSION_DESC, sessionDesc);
    }
	
	/**
	 * @param sessionId The sessionId to set.
	 */
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
		//setText(SESSION_ID, Long.toString(sessionId));
		setValue(PATH_SESSION_ID, Long.toString(sessionId));

	}
	/**
	 * @param name The name to set.
	 */
	public void setSessionName(String name) {
		this.sessionName = name;
		//setText(SESSION_NAME, name);
		setValue(PATH_SESSION_NAME, name);
		
	}

    
    public String getFileName() {
        return fileName;
    }
    
    private void saveSiteTree(SiteNode node) {
        HttpMessage msg = null;

        if (!node.isRoot()) {
            if (node.getHistoryReference().getHistoryType() < 0) {
                // -ve means to be saved
                saveNodeMsg(msg);
            }
        }
        
        for (int i=0; i<node.getChildCount(); i++) {
            try {
                saveSiteTree((SiteNode) node.getChildAt(i));
            } catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
            }
        }
        
    }
    
    private void saveNodeMsg(HttpMessage msg) {
        // nothing need to be done
    }
    
    public String getSessionFolder() {
        String result = "";
        if (fileName.equals("")) {
//            result = Constant.FOLDER_SESSION;
            result = Constant.getInstance().FOLDER_SESSION;
        } else {
            File file = new File(fileName);
            result = file.getParent();
        }
        return result;
    }

}
