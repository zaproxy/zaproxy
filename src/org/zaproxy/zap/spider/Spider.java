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

import java.net.CookieManager;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.spider.ExtensionSpider;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.spider.filters.DefaultFetchFilter;
import org.zaproxy.zap.spider.filters.DefaultParseFilter;
import org.zaproxy.zap.spider.filters.FetchFilter;
import org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus;
import org.zaproxy.zap.spider.filters.ParseFilter;
import org.zaproxy.zap.spider.parser.SpiderParser;
import org.zaproxy.zap.users.User;

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
	private List<SpiderListener> listeners;

	/** If the spider is currently paused. */
	private volatile boolean paused;

	/** The the spider is currently stopped. */
	private volatile boolean stopped;

	/** The pause lock, used for locking access to the "paused" variable. */
	private ReentrantLock pauseLock = new ReentrantLock();

	/** The controller that manages the spidering process. */
	private SpiderController controller;

	/**
	 * The condition that is used for the threads in the pool to wait on, when the Spider crawling
	 * is paused. When the Spider is resumed, all the waiting threads are awakened.
	 */
	private Condition pausedCondition = pauseLock.newCondition();

	/** The thread pool for spider workers. */
	private ExecutorService threadPool;

	/** The default fetch filter. */
	private DefaultFetchFilter defaultFetchFilter;
	
	/** The seed list. */
	private LinkedHashSet<URI> seedList;
	
	/** The extension. */
	private ExtensionSpider extension;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(Spider.class);

	/** The HTTP sender used to effectively send the data. */
	private HttpSender httpSender;

	/** The count of the tasks finished. */
	private int tasksDoneCount;

	/** The total count of all the submitted tasks. */
	private int tasksTotalCount;

	/** The cookie manager. */
	private CookieManager cookieManager;
	
	/** The scan context. If null, the scan is not performed in a context. */
	private Context scanContext;

	/** The scan user. */
	private User scanUser;
	
	/** The time the scan was started */
	private long timeStarted;

	/**
	 * The initialized marks if the spidering process is completely started. It solves the problem
	 * when the first task is processed and the process is finished before the other seeds are
	 * added.
	 */
	private boolean initialized;

	/**	we do not want to recurse into an SVN folder, or a subfolder of an SVN folder, if one was created from a previous Spider run */
	private static final Pattern svnUrlPattern = Pattern.compile("\\.svn/"); //case sensitive

	/**	we do not want to recurse into a Git folder, or a subfolder of a Git folder, if one was created from a previous Spider run */
	private static final Pattern gitUrlPattern = Pattern.compile("\\.git/"); //case sensitive

	/**
	 * Instantiates a new spider.
	 *
	 * @param extension the extension
	 * @param spiderParam the spider param
	 * @param connectionParam the connection param
	 * @param model the model
	 * @param scanContext if a scan context is set, only URIs within the context are fetched and processed
	 */
	public Spider(ExtensionSpider extension, SpiderParam spiderParam, ConnectionParam connectionParam,
			Model model, Context scanContext) {
		super();
		log.info("Spider initializing...");
		this.spiderParam = spiderParam;
		this.connectionParam = connectionParam;
		this.model = model;
		this.controller = new SpiderController(this, extension.getCustomParsers());
		this.listeners = new LinkedList<>();
		this.seedList = new LinkedHashSet<>();
		this.cookieManager = new CookieManager();
		this.scanContext = scanContext;
		this.extension = extension;
		
		init();
	}

	/**
	 * Initialize the spider.
	 */
	private void init() {
		this.paused = false;
		this.stopped = true;
		this.tasksDoneCount = 0;
		this.tasksTotalCount = 0;
		this.initialized = false;

		// Add a default fetch filter and any custom ones
		defaultFetchFilter = new DefaultFetchFilter();
		this.addFetchFilter(defaultFetchFilter);
		
		for (FetchFilter filter : extension.getCustomFetchFilters()) {
			this.addFetchFilter(filter);
		}

		// Add a default parse filter and any custom ones
		this.addParseFilter(new DefaultParseFilter());
		for (ParseFilter filter : extension.getCustomParseFilters())
			this.addParseFilter(filter);
		
		// Add the scan context, if any
		defaultFetchFilter.setScanContext(this.scanContext);
		defaultFetchFilter.setDomainsAlwaysInScope(spiderParam.getDomainsAlwaysInScopeEnabled());

	}

	/* SPIDER Related */
	/**
	 * Adds a new seed for the Spider.
	 * 
	 * @param msg the message used for seed. The request URI is used from the Request Header
	 */
	public void addSeed(HttpMessage msg) {
		URI uri = msg.getRequestHeader().getURI();
		addSeed(uri);
	}

	/**
	 * Adds a new seed for the Spider.
	 * 
	 * @param uri the uri
	 */
	public void addSeed(URI uri) {
		// Update the scope of the spidering process
		String host = null;

		try {
			host = uri.getHost();
			defaultFetchFilter.addScopeRegex(host);
		} catch (URIException e) {
			log.error("There was an error while adding seed value: " + uri, e);
			return;
		}
		// Add the seed to the list -- it will be added to the task list only when the spider is
		// started
		this.seedList.add(uri);
		// Add the appropriate 'robots.txt' as a seed
		if (getSpiderParam().isParseRobotsTxt()) {
			try {
				// Build the URI of the robots.txt file
				URI robotsUri;
				// If the port is not 80 or 443, add it to the URI
				if (uri.getPort() == 80 || uri.getPort() == 443) {
					robotsUri = new URI(uri.getScheme() + "://" + host + "/robots.txt", true);
				} else {
					robotsUri = new URI(uri.getScheme() + "://" + host + ":" + uri.getPort() + "/robots.txt",
							true);
				}
				this.seedList.add(robotsUri);
			} catch (Exception e) {
				log.warn("Error while creating URI for robots.txt file for site " + uri, e);
			}
		}
		// Add the appropriate 'sitemap.xml' as a seed
		if (getSpiderParam().isParseSitemapXml()) {
			try {
				// Build the URI of the sitemap.xml file
				URI sitemapUri;
				// If the port is not 80 or 443, add it to the URI
				if (uri.getPort() == 80 || uri.getPort() == 443) {
					sitemapUri = new URI(uri.getScheme() + "://" + host + "/sitemap.xml", true);
				} else {
					sitemapUri = new URI(uri.getScheme() + "://" + host + ":" + uri.getPort() + "/sitemap.xml",
							true);
				}
				this.seedList.add(sitemapUri);
			} catch (Exception e) {
				log.warn("Error while creating URI for sitemap.xml file for site " + uri, e);
			}
		}
		// And add '.svn/entries' as a seed, for SVN based spidering
		if (getSpiderParam().isParseSVNEntries()) {
			try {
				URI svnEntriesURI1, svnEntriesURI2;
				// If the port is not 80 or 443, add it to the URI
				// SVN entries can exist in multiple directories, so make sure to add in the full path.
				String fullpath = uri.getPath();
				String name = uri.getName();				
				if (fullpath==null) fullpath="";
				if (name==null) name="";
				
				String pathminusfilename = fullpath.substring( 0, fullpath.lastIndexOf(name));
				if (pathminusfilename.equals("")) pathminusfilename="/";
				
				//if it's not an svn folder, add the seeds.
				Matcher matcherSvnUrl = svnUrlPattern.matcher(pathminusfilename);
				if (! matcherSvnUrl.find()) {
					if (uri.getPort() == 80 || uri.getPort() == 443) {
						svnEntriesURI1 = new URI(uri.getScheme() + "://" + host + pathminusfilename + ".svn/entries", true);
						svnEntriesURI2 = new URI(uri.getScheme() + "://" + host + pathminusfilename + ".svn/wc.db", true);
					} else {
						svnEntriesURI1 = new URI(uri.getScheme() + "://" + host + ":" + uri.getPort() + pathminusfilename + ".svn/entries", true);
						svnEntriesURI2 = new URI(uri.getScheme() + "://" + host + ":" + uri.getPort() + pathminusfilename + ".svn/wc.db", true);
					}
					this.seedList.add(svnEntriesURI1);
					this.seedList.add(svnEntriesURI2);
				}
			} catch (Exception e) {
				log.warn("Error while creating a seed URI for the SVN files for site " + uri, e);
			}
		}

		// And add '.git/index' as a seed, for Git based spidering
		if (getSpiderParam().isParseGit()) {
			try {
				URI gitEntriesURI;
				// If the port is not 80 or 443, add it to the URI
				// Make sure to add in the full path.
				String fullpath = uri.getPath();
				String name = uri.getName();
				if (fullpath==null) fullpath="";
				if (name==null) name="";
				
				String pathminusfilename = fullpath.substring( 0, fullpath.lastIndexOf(name));
				if (pathminusfilename.equals("")) pathminusfilename="/";
				
				//if it's not in a Git folder, add the seed.
				Matcher matcherGitUrl = gitUrlPattern.matcher(pathminusfilename);
				if (! matcherGitUrl.find()) {
					if (uri.getPort() == 80 || uri.getPort() == 443) {
						gitEntriesURI = new URI(uri.getScheme() + "://" + host + pathminusfilename + ".git/index", true);
					} else {
						gitEntriesURI = new URI(uri.getScheme() + "://" + host + ":" + uri.getPort() + pathminusfilename + ".git/index", true);
					}
					this.seedList.add(gitEntriesURI);					
				}
			} catch (Exception e) {
				log.warn("Error while creating a seed URI for the Git files for site " + uri, e);
			}
		}

	}

	/**
	 * Sets the exclude list which contains a List of strings, defining the uris that should be
	 * excluded.
	 * 
	 * @param excludeList the new exclude list
	 */
	public void setExcludeList(List<String> excludeList) {
		log.debug("New Exclude list: " + excludeList);
		defaultFetchFilter.setExcludeRegexes(excludeList);
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
	 * Adds a new parse filter to the spider.
	 * 
	 * @param filter the filter
	 */
	public void addParseFilter(ParseFilter filter) {
		controller.addParseFilter(filter);
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

	protected ConnectionParam getConnectionParam() {
		return connectionParam;
	}

	/**
	 * Gets the controller.
	 * 
	 * @return the controller
	 */
	protected SpiderController getController() {
		return controller;
	}

	/**
	 * Gets the cookie manager.
	 * 
	 * @return the cookie manager
	 */
	protected CookieManager getCookieManager() {
		return cookieManager;
	}

	/**
	 * Gets the model.
	 * 
	 * @return the model
	 */
	protected Model getModel() {
		return this.model;
	}

	/**
	 * Submit a new task to the spidering task pool.
	 * 
	 * @param task the task
	 */
	protected synchronized void submitTask(SpiderTask task) {
		if (isStopped()) {
			log.debug("Submitting task skipped (" + task + ") as the Spider process is stopped.");
			return;
		}
		if (isTerminated()) {
			log.debug("Submitting task skipped (" + task + ") as the Spider process is terminated.");
			return;
		}
		this.tasksTotalCount++;
		try {
			this.threadPool.execute(task);
		} catch (RejectedExecutionException e) {
			if (log.isDebugEnabled()) {
				log.debug("Submitted task was rejected (" + task + "), spider state: [stopped=" + isStopped() + ", terminated="
						+ isTerminated() + "].");
			}
		}
	}

	/* SPIDER PROCESS maintenance - pause, resume, shutdown, etc. */

	/**
	 * Starts the Spider crawling.
	 */
	public void start() {

		log.info("Starting spider...");
		
		this.timeStarted = System.currentTimeMillis();

		fetchFilterSeeds();

		// Check if seeds are available, otherwise the Spider will start, but will not have any
		// seeds and will not stop.
		if (seedList == null || seedList.isEmpty()) {
			log.warn("No seeds available for the Spider. Cancelling scan...");
			notifyListenersSpiderComplete(false);
			notifyListenersSpiderProgress(100, 0, 0);
			return;
		}

		if (scanUser != null)
			log.info("Scan will be performed from the point of view of User: " + scanUser.getName());

		this.controller.init();
		this.stopped = false;
		this.paused = false;
		this.initialized = false;

		// Initialize the thread pool
		this.threadPool = Executors.newFixedThreadPool(spiderParam.getThreadCount());

		// Initialize the HTTP sender
		httpSender = new HttpSender(connectionParam, true, HttpSender.SPIDER_INITIATOR);
		// Do not follow redirections because the request is not updated, the redirections will be
		// handled manually.
		httpSender.setFollowRedirect(false);

		// Add the seeds
		for (URI uri : seedList) {
			if (log.isDebugEnabled()) {
				log.debug("Adding seed for spider: " + uri);
			}
			controller.addSeed(uri, HttpRequestHeader.GET);
		}
		// Mark the process as completely initialized
		initialized = true;
	}

	/**
	 * Filters the seed list using the current fetch filters, preventing any non-valid seed from being accessed.
	 * 
	 * @see #seedList
	 * @see FetchFilter
	 * @see SpiderController#getFetchFilters()
	 * @since 2.5.0
	 */
	private void fetchFilterSeeds() {
		if (seedList == null || seedList.isEmpty()) {
			return;
		}

		for (Iterator<URI> it = seedList.iterator(); it.hasNext();) {
			URI seed = it.next();
			for (FetchFilter filter : controller.getFetchFilters()) {
				FetchStatus filterReason = filter.checkFilter(seed);
				if (filterReason != FetchStatus.VALID) {
					if (log.isDebugEnabled()) {
						log.debug("Seed: " + seed + " was filtered with reason: " + filterReason);
					}
					it.remove();
					break;
				}
			}
		}
	}

	/**
	 * Stops the Spider crawling. Must not be called from any of the threads in the thread pool.
	 */
	public void stop() {
		if (stopped) {
			return;
		}
		this.stopped = true;
		log.info("Stopping spidering process by request.");
		
		if (this.paused) {
			// Have to resume first or we get a deadlock
			this.resume();
		}
		
		// Issue the shutdown command
		this.threadPool.shutdown();
		try {
			if (!this.threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
				log.warn("Failed to await for all spider threads to stop in the given time (2s)...");
				for (Runnable task : this.threadPool.shutdownNow()) {
					((SpiderTask) task).cleanup();
				}
			}
		} catch (InterruptedException ignore) {
			log.warn("Interrupted while awaiting for all spider threads to stop...");
		}
		if (httpSender != null) {
			this.getHttpSender().shutdown();
			httpSender = null;
		}

		// Notify the controller to clean up memory
		controller.reset();
		this.threadPool = null;

		// Notify the listeners -- in the meanwhile
		notifyListenersSpiderComplete(false);
	}

	/**
	 * The Spidering process is complete.
	 */
	private void complete() {
		if (stopped) {
			return;
		}

		log.info("Spidering process is complete. Shutting down...");
		this.stopped = true;
		if (httpSender != null) {
			this.getHttpSender().shutdown();
			httpSender = null;
		}

		// Notify the controller to clean up memory
		controller.reset();

		// Issue the shutdown command on a separate thread, as the current thread is most likely one
		// from the pool
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (threadPool != null) {
					threadPool.shutdown();
				}
				// Notify the listeners -- in the meanwhile
				notifyListenersSpiderComplete(true);
				controller.reset();
				threadPool = null;
			}
		}).start();
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
	 * Sets the spider so it will scan from the point of view of a user.
	 *
	 * @param user the user to be scanned as
	 */
	public void setScanAsUser(User user) {
		this.scanUser = user;
	}
	
	/**
	 * Gets the user that will be used in the scanning.
	 *
	 * @return the scan user
	 */
	protected User getScanUser(){
		return this.scanUser;
	}

	
	
	/**
	 * This method is run by each thread in the Thread Pool before the task execution. Particularly,
	 * it checks if the Spidering process is paused and, if it is, it waits on the corresponding
	 * condition for the process to be resumed. Called from the SpiderTask.
	 */
	protected void preTaskExecution() {
		checkPauseAndWait();
	}

	/**
	 * This method is run by Threads in the ThreadPool and checks if the scan is paused and, if it
	 * is, waits until it's unpaused.
	 */
	protected void checkPauseAndWait() {
		pauseLock.lock();
		try {
			while (paused && ! stopped) {
				pausedCondition.await();
			}
		} catch (InterruptedException e) {
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * This method is run by each thread in the Thread Pool after the task execution. Particularly,
	 * it notifies the listeners of the progress and checks if the scan is complete. Called from the
	 * SpiderTask.
	 */
	protected synchronized void postTaskExecution() {
		if (stopped) {
			// Stopped, so don't count the task(s) as done.
			// (worker threads call this method even if the task was not really executed.)
			return;
		}
		tasksDoneCount++;
		int percentageComplete = tasksDoneCount * 100 / tasksTotalCount;

		// Compute the progress and notify the listeners
		this.notifyListenersSpiderProgress(percentageComplete, tasksDoneCount, tasksTotalCount - tasksDoneCount);

		// Check for ending conditions
		if (tasksDoneCount == tasksTotalCount && initialized) {
			this.complete();
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
		if (! stopped && this.spiderParam.getMaxDuration() > 0) {
			// Check to see if the scan has exceeded the specified maxDuration
			if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - this.timeStarted) > 
					this.spiderParam.getMaxDuration()) {
				log.info("Spidering process has exceeded maxDuration of " + this.spiderParam.getMaxDuration() + " minute(s)");
				this.complete();
			}
		}
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
	protected synchronized void notifyListenersSpiderProgress(int percentageComplete, int numberCrawled,
			int numberToCrawl) {
		for (SpiderListener l : listeners) {
			l.spiderProgress(percentageComplete, numberCrawled, numberToCrawl);
		}
	}

	/**
	 * Notifies the listeners regarding a found uri.
	 * 
	 * @param uri the uri
	 * @param method the method used for fetching the resource
	 * @param status the {@link FetchStatus} stating if this uri will be processed, and, if not,
	 *            stating the reason of the filtering
	 */
	protected synchronized void notifyListenersFoundURI(String uri, String method, FetchStatus status) {
		for (SpiderListener l : listeners) {
			l.foundURI(uri, method, status);
		}
	}

	/**
	 * Notifies the listeners regarding a read uri.
	 * 
	 * @param msg the message
	 */
	protected synchronized void notifyListenersReadURI(HttpMessage msg) {
		for (SpiderListener l : listeners) {
			l.readURI(msg);
		}
	}

	/**
	 * Notifies the listeners that the spider is complete.
	 * 
	 * @param successful {@code true} if the spider completed successfully (e.g. was not stopped), {@code false} otherwise
	 */
	protected synchronized void notifyListenersSpiderComplete(boolean successful) {
		for (SpiderListener l : listeners) {
			l.spiderComplete(successful);
		}
	}

	public void addCustomParser(SpiderParser sp) {
		this.controller.addSpiderParser(sp);
	}

}
