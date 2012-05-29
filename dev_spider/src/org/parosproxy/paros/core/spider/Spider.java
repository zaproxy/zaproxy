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
// ZAP: 2012/01/09 Changed to not follow redirections.
// ZAP: 2012/03/15 Changed the methods isInVisitedLink and isQueued to use 
// getRequestBody().getBytes() instead of getRequestBody().toString().
// ZAP: 2012/04/25 Removed unnecessary casts.
// ZAP: 2012/05/02 Changed the first letter of the method spiderProgress
// to lower case.
// ZAP: 2012/05/29 Changed to use the new SpiderParam


package org.parosproxy.paros.core.spider;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.spider.SpiderParam;




/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Spider {
    
    private static Logger log = Logger.getLogger(Spider.class);

    private HttpSender httpSender = null;
    private Vector<SpiderListener> listenerList = new Vector<SpiderListener>();
    private ConnectionParam connectionParam = null;
    private Vector<QueueItem> queue = new Vector<QueueItem>();
    private SpiderThread[] spiderThread = null;
    private SpiderParam spiderParam = null;
    private HashSet<String> seedHostNameSet = new HashSet<String>();
    private int[] depthQueueCount = null;
    private Session session = null;
    private Model model = null;
    private boolean isStop = false;
    private TreeSet<String> visitedGetMethod = null;
    private TreeSet<String> queuedGetMethod = null;
    private Vector<QueueItem> visitedPostMethod = new Vector<QueueItem>();
    private List<Pattern> excludeUrls = null;
    
    public Spider(SpiderParam spiderParam, ConnectionParam param, Model model) {
        this.connectionParam = param;
        this.spiderParam = spiderParam;
        this.model = model;
        this.session = model.getSession();
        spiderThread = new SpiderThread[spiderParam.getThread()];
        depthQueueCount = new int[spiderParam.getMaxDepth()+1];
        visitedGetMethod = new TreeSet<String>();
        queuedGetMethod = new TreeSet<String>();
    }

    public void addSpiderListener(SpiderListener listener) {
        listenerList.add(listener);     
    }
    
    public void addSeed(URI uri) {
        
        HttpMessage msg;
        try {
            msg = new HttpMessage(uri);
            addSeed(msg);
        } catch (HttpMalformedHeaderException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }

    }
    
    public synchronized void addSeed(HttpMessage msg) {
        URI uri = msg.getRequestHeader().getURI();
        String hostName = null;
        int port = 80;
        
        try {
            log.info("seeding " + msg.getRequestHeader().getURI().toString());

            hostName = uri.getHost();
            port = uri.getPort();
            if (port > 0) {
                hostName = hostName + ":" + Integer.toString(port);
            }
            
            seedHostNameSet.add(hostName);
            addQueue(msg, 0);

        } catch (URIException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
        
    }
    
    public boolean addQueue(HttpMessage msg, int depth) {

        QueueItem item = null;

        
        if (depth > spiderParam.getMaxDepth() || isInVisitedLink(msg)) {
            return false;
        }

        synchronized(queue) {        
            
            try {
                // no need to add if in queue already


//              comment code to use db directly
//              for (int i=0; i<queue.size() && !isStop; i++) {
//                    item = (QueueItem) (queue.get(i));
//                    if (item.getMessage().equals(msg)) {
//                        return;
//                    }
//                }

                if (isQueued(msg)) {
                    return false;
                }
                item = new QueueItem(session, HistoryReference.TYPE_SPIDER_SEED, msg);
            } catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
                return false;
            }
            
            item.setDepth(depth);
            
            if (queue.size() > 0) {
                // add to queue according to depth
                for (int i=queue.size()-1; i>=0; i--) {    
                    // ZAP: Removed unnecessary cast.
                    QueueItem poll = queue.get(i);
                    if (item.getDepth() >= poll.getDepth()) {
                        if (i+1 <= queue.size()-1) {
                            queue.add(i+1, item);
                            break;
                        } else {
                            queue.add(item);
                            break;
                        }
                    } else if (i==0) {
                        queue.add(0, item);
                        break;
                    }
                }
            } else {
                queue.add(item);
            }
            
            if (depth < depthQueueCount.length) {
                depthQueueCount[depth]++;
            }
        }

        if (queue.size() % 50 == 0) {
            queue.trimToSize();
        }

        return true;
    }
    
    void checkIfAllThreadCompleted() {
        for (int i=0; i<spiderThread.length; i++) {
            if (!spiderThread[i].isCompleted()) {
                // cannot check thread alive here because child issued this check.  They
                // will not stop yet.
                return;
            }
        }
        
        // all thread finished running
        notifyListenerSpiderComplete();
        
    }



    /**
     * @return Returns the httpSender.
     */
    public HttpSender getHttpSender() {
        return httpSender;
    }

    /**
     * @return Returns the queue.
     */
    public Vector<QueueItem> getQueue() {
        return queue;
    }
    
    private void notifyListenerFoundURI(HttpMessage msg, boolean isSkip) {
        SpiderListener listener = null;
        for (int i=0;i<listenerList.size();i++) {
            // ZAP: Removed unnecessary cast.
            listener = listenerList.get(i);
            listener.foundURI(msg, isSkip);
        }

    }
    
    private void notifyListenerSpiderComplete() {
        SpiderListener listener = null;
        
        notifyListenerSpiderProgress(null, 100);

        
        for (int i=0;i<listenerList.size();i++) {
            // ZAP: Removed unnecessary cast.
            listener = listenerList.get(i);
            listener.spiderComplete();          
        }
        log.info("Spider completed");
        getHttpSender().shutdown();
        isStop = true;
    }

    // ZAP: Changed the first letter to lower case.
    synchronized void spiderProgress(QueueItem item) {
        int scale = 100/(spiderParam.getMaxDepth()+1);
        int percentage = scale * item.getDepth();

        int outstanding= 0;
        for (int i=0;i<queue.size();i++) {
            // ZAP: Removed unnecessary cast.
            QueueItem poll= queue.get(i);
            if (poll != null) {
                if (poll.getDepth() <= item.getDepth()) {
                    outstanding++;
                }
            }
        }
        
        percentage += scale *(depthQueueCount[item.getDepth()]-outstanding)/depthQueueCount[item.getDepth()];

        try {
            notifyListenerSpiderProgress(item.getMessage().getRequestHeader().getURI(), percentage);
            Thread.sleep(100);
        } catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
    }
    
    private void notifyListenerSpiderProgress(URI uri, int percentageComplete) {
        SpiderListener listener = null;

        for (int i=0;i<listenerList.size();i++) {
            // ZAP: Removed unnecessary cast.
            listener = listenerList.get(i);
            listener.spiderProgress(uri, percentageComplete, visitedPostMethod.size() + visitedGetMethod.size(), queue.size());
        }
    }

    private void notifyListenerReadURI(HttpMessage msg) {
        SpiderListener listener = null;

        log.info("crawled " + msg.getRequestHeader().getURI().toString());

        for (int i=0;i<listenerList.size();i++) {
            // ZAP: Removed unnecessary cast.
            listener = listenerList.get(i);
            listener.readURI(msg);
        }

    }
    
    public void removeSpiderListener(SpiderListener listener) {
        listenerList.remove(listener);
    }
    
    /**
     * @return Returns the spiderParam.
     */
    public SpiderParam getSpiderParam() {
        return spiderParam;
    }
    /**
     * @param spiderParam The spiderParam to set.
     */
    public void setSpiderParam(SpiderParam spiderParam) {
        this.spiderParam = spiderParam;
    }

    
    public void start() {
        log.info("spider started.");
        isStop = false;
        httpSender = new HttpSender(connectionParam, true);
        // ZAP: Do not follow redirections because the request
        // is not updated, the redirections will be handled manually.
        httpSender.setFollowRedirect(false);

        for (int i=0; i<spiderThread.length; i++) {
            if (spiderThread[i] != null && spiderThread[i].isAlive()) {
                spiderThread[i].setStop(true);
            }

            spiderThread[i] = new SpiderThread(this);        
            spiderThread[i].start();
            
        }
    }
    
    public void stop() {
        for (int i=0; i<spiderThread.length; i++) {
            spiderThread[i].setStop(true);
            try {
                spiderThread[i].join(2000);
            } catch (InterruptedException e) {
            }
        }
        // ZAP: Shutdown sender after stopping threads - otherwise loads of exceptions 
        getHttpSender().shutdown();
        log.info("spider stopped.");
        isStop = true;
                    
    }

    // ZAP: Support pause and resume
    public void pause() {
        for (int i=0; i<spiderThread.length; i++) {
            spiderThread[i].setResume(false);
            spiderThread[i].setPause(true);
        }
    }
    public void resume() {
        for (int i=0; i<spiderThread.length; i++) {
            spiderThread[i].setPause(false);
            spiderThread[i].setResume(true);
        }
    }
    
    public boolean isSeedScope(URI uri) {
        String hostName = null;
        try {
            hostName = uri.getHost();
            if (uri.getPort() > 0) {
                hostName = hostName + ":" + uri.getPort();
            }

            // ZAP: Removed unnecessary cast..
            String[] hostList = seedHostNameSet.toArray(new String[0]);
            for (int i=0; i<hostList.length; i++) {
                if (hostList[i] == null) continue;
                if (hostName.endsWith(hostList[i])) {
                    return true;
                }
            }

        } catch (URIException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
        return false;
    }

    void foundURI(HttpMessage msg, int currentDepth) {
        try {

            if (isNeglectFound(msg)) {
                // treat even post as GET to avoid duplicate
                msg.getRequestHeader().setMethod(HttpRequestHeader.GET);
                if (!isInVisitedLink(msg)) {
                    notifyListenerFoundURI(msg, true);
                    addVisitedLink(msg);
                }
            } else {
                if (addQueue(msg, currentDepth)) {
                    notifyListenerFoundURI(msg, false);
                }
            }
        } catch (URIException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
            
    }
    
    void readURI(HttpMessage msg) {
        notifyListenerReadURI(msg);
    }

    boolean isInVisitedLink(HttpMessage msg) {

        if (msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.GET)) {
            return isVisitedGetMethod(msg);
        }
        
        try {
            if (model.getDb().getTableHistory().containsURI(session.getSessionId(), HistoryReference.TYPE_SPIDER_VISITED,
                    msg.getRequestHeader().getMethod(), msg.getRequestHeader().getURI().toString(), msg.getRequestBody().getBytes())) {
                return true;
            }
        } catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
        
        return false;
    }
    
    void addVisitedLink(HttpMessage msg) {

        if (msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.GET)) {
            synchronized(visitedGetMethod) {
                visitedGetMethod.add(msg.getRequestHeader().getURI().toString());
            }
            return;
        }

        QueueItem item = null;
        try {
            item = new QueueItem(session, HistoryReference.TYPE_SPIDER_VISITED, msg);
        } catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
            return;
        }

        synchronized(visitedPostMethod) {
            visitedPostMethod.add(item);
        }
    }
    
    boolean isQueued(HttpMessage msg) {
        
        try {
            return model.getDb().getTableHistory().containsURI(session.getSessionId(), HistoryReference.TYPE_SPIDER_SEED,
                    msg.getRequestHeader().getMethod(), msg.getRequestHeader().getURI().toString(), msg.getRequestBody().getBytes());
        } catch (SQLException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * Check buffer before searching for visited link from disk.
     * @param msg
     * @return
     */
    private boolean isVisitedGetMethod(HttpMessage msg) {

        String uri = msg.getRequestHeader().getURI().toString();
        synchronized(visitedGetMethod) {

            if (visitedGetMethod.contains(uri)) {
                return true;
            }
            
        }
        return false;
    }
    
    /**
     * Check if URL is to be neglected if: - not HTTP protocol - outside
     * host domain - irrelevant file suffix (eg gif, jpg) - visited before
     * URL queried by this method will be marked visited.
     * @throws URIException
     * 
     */
    private boolean isNeglectFound(HttpMessage msg) throws URIException {
        URI uri = msg.getRequestHeader().getURI();
        
        // check correct protocol
        if (!uri.getScheme().equalsIgnoreCase("HTTP") && !uri.getScheme().equalsIgnoreCase("HTTPS")) {
            return true;
        }

        // compare if in seed's domain or inside session domain scope
        String hostName = uri.getHost().toUpperCase();
        if (!isSeedScope(uri)) {
            if (!getSpiderParam().isInScope(hostName)) {
                return true;
            }
        }
        // check if its one of the explicitly excluded ones
        if (this.excludeUrl(uri)) {
        	return true;
        }
        
        return false;
        
    }
    
    public boolean isStop() {
        return isStop;
    }
    
    boolean isAllThreadEmptyQueue() {
        for (int i=0; i<spiderThread.length; i++) {
            if (!spiderThread[i].isEmptyQueue()) {
                return false;
            }
        }
        return true;
    }

	public void setExcludeList(List<String> urls) {
		if (urls != null) {
		    excludeUrls = new ArrayList<Pattern>(urls.size());
		    for (String url : urls) {
				Pattern p = Pattern.compile(url, Pattern.CASE_INSENSITIVE);
				excludeUrls.add(p);
		    }
		} else {
			excludeUrls = new ArrayList<Pattern>(0);
		}
	}
	
	public boolean excludeUrl(URI uri) {
		boolean ignore = false;
		if (excludeUrls != null) {
			String uriString = uri.toString();
			for (Pattern p : excludeUrls) {
				if (p.matcher(uriString).matches()) {
					ignore = true;
					if (log.isDebugEnabled()) {
						log.debug("URL excluded: " + uriString + " Regex: " + p.pattern());
					}
					break;
				}
			}
		} 

		return ignore;
	}
}