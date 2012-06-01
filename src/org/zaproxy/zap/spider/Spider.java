/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.spider;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.Sleep;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.spider.filters.DefaultFetchFilter;
import org.zaproxy.zap.spider.filters.FetchFilter;

/**
 * The Class Spider.
 */
public class Spider {

	/** The spider parameters. */
	private SpiderParam spiderParam;

	/** The connection parameters. */
	private ConnectionParam connectionParam;

	/** The model. */
	private Model model;

	/** The listeners for Spider related events. */
	private LinkedList<SpiderListener> listeners;

	/** If the spider is currently paused. */
	private boolean paused;

	/** The the spider is currently stopped. */
	private boolean stopped;

	/** The pause lock, used for locking access to the "paused" vairable */
	private ReentrantLock pauseLock = new ReentrantLock();

	/** The seed values. */
	private LinkedList<String> seeds;

	/** The controller that manages the spidering process. */
	private SpiderController controller;

	/**
	 * The condition that is used for the threads in the pool to wait on, when the Spider crawling
	 * is paused. When the Spider is resumed, all the waiting threads are awakened.
	 */
	private Condition pausedCondition = pauseLock.newCondition();

	/** The thread pool for spider workers. */
	ExecutorService threadPool;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(Spider.class);

	/** The http sender. */
	private HttpSender httpSender;

	/**
	 * Instantiates a new spider.
	 * 
	 * @param spiderParam the spider param
	 * @param connectionParam the connection param
	 * @param model the model
	 */
	public Spider(SpiderParam spiderParam, ConnectionParam connectionParam, Model model) {
		super();
		log.info("Spider initializing...");
		this.spiderParam = spiderParam;
		this.connectionParam = connectionParam;
		this.model = model;
		this.threadPool = Executors.newFixedThreadPool(spiderParam.getThreadCount());
		this.controller = new SpiderController(this, connectionParam);
		this.listeners = new LinkedList<SpiderListener>();

		init();
	}

	/**
	 * Initialize the spider.
	 */
	private void init() {
		this.paused = false;
		this.stopped = true;

		// Add a default fetch filter
		this.addFetchFilter(new DefaultFetchFilter());
	}

	/* SPIDER Related */
	/**
	 * Adds a new seed for the Spider.
	 * 
	 * @param msg the message used for seed.
	 */
	public void addSeed(HttpMessage msg) {
		// TODO Auto-generated method stub
	}

	/**
	 * Sets the exclude list which contains a List of strings, defining the uris that should be
	 * excluded.
	 * 
	 * @param excludeList the new exclude list
	 */
	public void setExcludeList(List<String> excludeList) {
		// TODO Auto-generated method stub

	}

	/**
	 * Adds a new fetch filter to the spider.
	 * 
	 * @param filter the filter
	 */
	public void addFetchFilter(FetchFilter filter) {
		controller.addFetchFilter(filter);
	}

	/**
	 * Gets the http sender. Can be called from the SpiderTask.
	 * 
	 * @return the http sender
	 */
	protected HttpSender getHttpSender() {
		return httpSender;
	}

	/**
	 * Gets the spider parameters. Can be called from the SpiderTask.
	 * 
	 * @return the spider parameters
	 */
	protected SpiderParam getSpiderParam() {
		return spiderParam;
	}

	/**
	 * Submit a new task to the spidering task pool.
	 * 
	 * @param task the task
	 */
	protected synchronized void submitTask(SpiderTask task) {
		if (isStopped() || isTerminated()) {
			log.debug("Submit task skipped (" + task + ") as the Spider process is stopped.");
			return;
		}
		this.threadPool.execute(task);
	}

	/* SPIDER PROCESS maintenance - pause, resume, shutdown, etc. */

	/**
	 * Starts the Spider crawling.
	 */
	public void start() {

		log.info("Spider started.");
		this.stopped = false;
		this.paused = false;

		// Initialize the HTTP sender
		httpSender = new HttpSender(connectionParam, true);
		// Do not follow redirections because the request is not updated, the redirections will be
		// handled manually.
		httpSender.setFollowRedirect(false);

		// Feed the seed to the thread pool
		controller.resourceFound(null, "http://www.feedrz.com:80/contact.jsp");
	}

	/**
	 * Stops the Spider crawling.
	 */
	public void stop() {

	}

	/**
	 * Pauses the Spider crawling.
	 */
	public void pause() {
		pauseLock.lock();
		try {
			paused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * Resumes the Spider crawling.
	 */
	public void resume() {
		pauseLock.lock();
		try {
			paused = false;
			// Wake up all threads that are currently paused
			pausedCondition.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * This method is run by each thread in the Thread Pool before the task execution. Particularly,
	 * it checks if the Spidering process is paused and, if it is, it waits on the corresponding
	 * condition for the process to be resumed. Called from the SpiderTask.
	 */
	protected void beforeTaskExecution() {
		pauseLock.lock();
		try {
			while (paused)
				pausedCondition.await();
		} catch (InterruptedException e) {
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * Checks if is paused.
	 * 
	 * @return true, if is paused
	 */
	public boolean isPaused() {
		return this.paused;
	}

	/**
	 * Checks if is stopped, i.e. a shutdown was issued or it is not running.
	 * 
	 * @return true, if is stopped
	 */
	public boolean isStopped() {
		return stopped;
	}

	/**
	 * Checks if is terminated.
	 * 
	 * @return true, if is terminated
	 */
	public boolean isTerminated() {
		return threadPool.isTerminated();
	}

	/**
	 * Shutdown the Spider process.
	 */
	public void shutdown() {
		this.stopped = true;
		threadPool.shutdown();
	}

	/**
	 * Issues a shutdown of the Spider process and waits for all the current tasks to finish.
	 */
	public void shutdownAndWait() {
		this.stopped = true;
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* LISTENERS SECTION */

	/**
	 * Adds a new spider listener.
	 * 
	 * @param listener the listener
	 */
	public void addSpiderListener(SpiderListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes a spider listener.
	 * 
	 * @param listener the listener
	 */
	public void removeSpiderListener(SpiderListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Notifies all the listeners regarding the spider progress.
	 * 
	 * @param percentageComplete the percentage complete
	 * @param numberCrawled the number of pages crawled
	 * @param numberToCrawl the number of pages left to crawl
	 */
	protected void notifyListenersSpiderProgress(int percentageComplete, int numberCrawled, int numberToCrawl) {
		for (SpiderListener l : listeners)
			l.spiderProgress(percentageComplete, numberCrawled, numberToCrawl);
	}

	/**
	 * Notifies the listeners regarding a found uri.
	 * 
	 * @param msg the message
	 * @param isSkipped the is skipped
	 */
	protected void notifyListenersFoundURI(HttpMessage msg, boolean isSkipped) {
		for (SpiderListener l : listeners)
			l.foundURI(msg, isSkipped);
	}

	/**
	 * Notifies the listeners regarding a read uri.
	 * 
	 * @param msg the msg
	 */
	protected void notifyListenersReadURI(HttpMessage msg) {
		for (SpiderListener l : listeners)
			l.readURI(msg);
	}

	/**
	 * Notifies the listeners that the spider is complete.
	 */
	private void notifyListenersSpiderComplete() {
		for (SpiderListener l : listeners)
			l.spiderComplete();
	}

}
