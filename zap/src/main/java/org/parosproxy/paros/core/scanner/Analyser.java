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
// ZAP: 2012/03/15 Changed the method getPathRegex to use the class StringBuilder
// instead of StringBuffer.
// ZAP: 2012/04/25 Removed unnecessary casts.
// ZAP: 2012/05/04 Catch CloneNotSupportedException whenever an Uri is cloned,
// 		as introduced with version 3.1 of HttpClient
// ZAP: 2012/07/30 Issue 43: Added support for Scope
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/03/23 Issue 1021: OutOutOfMemoryError while running the active scanner
// ZAP: 2014/06/26 Added the possibility to count the available nodes that can be scanned
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2015/04/02 Issue 321: Support multiple databases and Issue 1582: Low memory option
// ZAP: 2016/01/26 Fixed findbugs warning
// ZAP: 2016/04/21 Allow to obtain the number of requests sent during the analysis
// ZAP: 2016/06/10 Honour scan's scope when following redirections
// ZAP: 2016/09/20 JavaDoc tweaks
// ZAP: 2017/03/27 Use HttpRequestConfig.
// ZAP: 2017/06/15 Allow to obtain the running time of the analysis.
// ZAP: 2017/12/29 Rely on HostProcess to validate the redirections.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/07/26 Remove null check in sendAndReceive(HttpMessage). (LGTM Issue)
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/06/05 Remove usage of HttpException.
// ZAP: 2022/06/09 Quote the query component used in the regular expression.
// ZAP: 2022/06/09 Use Analyser in more circumstances.
// ZAP: 2022/09/07 Remove unnecessary comments and address SonarLint issues
// ZAP: 2022/09/08 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.core.scanner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.model.StructuralNode;

public class Analyser {

    private static final Logger logger = LogManager.getLogger(Analyser.class);

    /** remove HTML HEAD as this may contain expiry time which dynamic changes */
    private static final String PATTERN_REMOVE_HEADER = "(?m)(?i)(?s)<HEAD>.*?</HEAD>";

    private static final Pattern PATTERN_NOT_FOUND =
            Pattern.compile(
                    "(\\bnot\\b(found|exist))|(\\b404\\berror\\b)|(\\berror\\b404\\b)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final String PATTERN_THREE_SEGMENT_TIME = "\\s[012]\\d:[0-5]\\d:[0-5]\\d\\s";

    private static Random staticRandomGenerator = new Random();
    private static final String[] staticSuffixList = {
        ".cfm", ".jsp", ".php", ".asp", ".aspx", ".dll", ".exe", ".pl"
    };

    private HttpSender httpSender = null;
    private TreeMap<String, SampleResponse> mapVisited = new TreeMap<>();
    private boolean isStop = false;

    private StopWatch stopWatch;
    private boolean stopWatchStarted;

    private int delayInMs;

    /**
     * The count of requests sent (and received) during the analysis.
     *
     * @see #sendAndReceive(HttpMessage)
     * @see #getRequestCount()
     */
    private int requestCount;

    HostProcess parent = null;

    public Analyser() {}

    public Analyser(HttpSender httpSender, HostProcess parent) {
        this.httpSender = httpSender;
        this.parent = parent;
        this.stopWatch = new StopWatch();
    }

    public boolean isStop() {
        return isStop;
    }

    public void stop() {
        isStop = true;
    }

    public void start(StructuralNode node) {
        if (stopWatchStarted) {
            stopWatch.resume();
        } else {
            stopWatch.start();
            stopWatchStarted = true;
        }

        try {
            inOrderAnalyse(new HashSet<>(), node);
        } finally {
            stopWatch.suspend();
        }
    }

    private void addAnalysedHost(URI uri, HttpMessage msg, int errorIndicator) {
        try {
            mapVisited.put(uri.toString(), new SampleResponse(msg, errorIndicator));

        } catch (HttpMalformedHeaderException | DatabaseException e) {
            logger.error("Failed to persist the message: {}", e.getMessage(), e);
        }
    }

    /**
     * Analyse a single folder entity. Results are stored into mAnalysedEntityTable.
     *
     * @param node the node that will be analysed
     * @throws Exception if an error occurred while analysing the node (for example, failed to send
     *     the message)
     */
    private void analyse(StructuralNode node) throws Exception {
        // if analysed already, return; move to host part
        if (node.getHistoryReference() == null) {
            logger.debug("Node being analysed has no History Reference");
            return;
        }

        if (!parent.nodeInScope(node.getName())) {
            logger.debug("Node being analysed is out of scope");
            return;
        }

        HttpMessage baseMsg = node.getHistoryReference().getHttpMessage();
        URI baseUri = (URI) baseMsg.getRequestHeader().getURI().clone();
        logger.debug("Analysing {}", baseUri);

        baseUri.setQuery(null);

        // already exist one.  no need to test
        if (mapVisited.get(baseUri.toString()) != null) {
            logger.debug("Skipping: This node has already been analysed");
            return;
        }

        String path = getRandomPathSuffix(node, baseUri);
        HttpMessage msg = baseMsg.cloneRequest();

        URI uri = (URI) baseUri.clone();
        uri.setPath(path);
        msg.getRequestHeader().setURI(uri);

        logger.debug("Sending first analyse request {}", uri);
        sendAndReceive(msg);

        // standard RFC response, no further check is needed
        if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.NOT_FOUND) {
            addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_RFC);
            logger.debug("Analysis determined standard 404 handling");
            return;
        }

        if (HttpStatusCode.isRedirection(msg.getResponseHeader().getStatusCode())) {
            addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_REDIRECT);
            logger.debug("Analysis determined error page redirect handling");
            return;
        }

        if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
            addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_NON_RFC);
            logger.debug("Analysis determined non-RFC error page handling");
            return;
        }

        HttpMessage msg2 = baseMsg.cloneRequest();
        URI uri2 = msg2.getRequestHeader().getURI();
        String path2 = getRandomPathSuffix(node, uri2);
        uri2 = (URI) baseUri.clone();
        uri2.setPath(path2);
        msg2.getRequestHeader().setURI(uri2);
        logger.debug("Sending second analyse request {}", uri2);
        sendAndReceive(msg2);

        // remove HTML HEAD as this may contain expiry time which dynamic changes
        String resBody1 = msg.getResponseBody().toString().replaceAll(PATTERN_REMOVE_HEADER, "");
        String resBody2 = msg2.getResponseBody().toString().replaceAll(PATTERN_REMOVE_HEADER, "");

        // check if page is static.  If so, remember this static page
        if (resBody1.equals(resBody2)) {
            msg.getResponseBody().setBody(resBody1);
            addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_STATIC);
            logger.debug("Analysis determined static error page handling");
            return;
        }

        // else check if page is dynamic but deterministic
        resBody1 =
                resBody1.replaceAll(getPathRegex(uri), "")
                        .replaceAll(PATTERN_THREE_SEGMENT_TIME, "");
        resBody2 =
                resBody2.replaceAll(getPathRegex(uri2), "")
                        .replaceAll(PATTERN_THREE_SEGMENT_TIME, "");
        if (resBody1.equals(resBody2)) {
            msg.getResponseBody().setBody(resBody1);
            addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_DYNAMIC_BUT_DETERMINISTIC);
            logger.debug("Analysis determined dynamic but deterministic error page handling");
            return;
        }

        // else mark app "undeterministic".
        addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_UNDETERMINISTIC);
        logger.debug("Analysis fell all the way through to non-deterministic error page handling");
    }

    /**
     * Get a suffix from the children which exists in staticSuffixList. An option is provided to
     * check recursively. Note that the immediate children are always checked first before further
     * recursive check is done.
     *
     * @param node the node used to obtain the suffix
     * @param performRecursiveCheck True = get recursively the suffix from all the children.
     * @return The suffix ".xxx" is returned. If there is no suffix found, an empty string is
     *     returned.
     */
    private String getChildSuffix(StructuralNode node, boolean performRecursiveCheck) {

        String resultSuffix = "";
        String suffix = null;
        StructuralNode child = null;
        try {

            for (int i = 0; i < staticSuffixList.length; i++) {
                suffix = staticSuffixList[i];
                Iterator<StructuralNode> iter = node.getChildIterator();
                while (iter.hasNext()) {
                    child = iter.next();
                    try {
                        if (child.getURI().getPath().endsWith(suffix)) {
                            return suffix;
                        }
                    } catch (Exception e) {
                        // Nothing to do
                    }
                }
            }

            if (performRecursiveCheck) {
                Iterator<StructuralNode> iter = node.getChildIterator();
                while (iter.hasNext()) {
                    child = iter.next();
                    resultSuffix = getChildSuffix(child, performRecursiveCheck);
                    if (!resultSuffix.equals("")) {
                        return resultSuffix;
                    }
                }
            }

        } catch (Exception e) {
            // Nothing to do
        }

        return resultSuffix;
    }

    static String getPathRegex(URI uri) throws URIException {
        URI newUri;
        try {
            newUri = (URI) uri.clone();

        } catch (CloneNotSupportedException e) {
            throw new URIException(e.getMessage());
        }

        String query = newUri.getQuery();
        StringBuilder sb = new StringBuilder(100);

        // case should be sensitive sb.append("(?i)");
        newUri.setQuery(null);

        sb.append(newUri.toString());
        if (query != null) {
            String queryPattern = "(\\?" + Pattern.quote(query) + ")?";
            sb.append(queryPattern);
        }

        return sb.toString();
    }

    /**
     * Get a random path relative to the current entity. Whenever possible, use a suffix exist in
     * the children according to a priority of staticSuffixList.
     *
     * @param node the node used to construct the random path
     * @param uri The uri of the current entity.
     * @return A random path (e.g. /folder1/folder2/1234567.chm) relative the entity.
     * @throws URIException if unable to decode the path of the given URI
     */
    private String getRandomPathSuffix(StructuralNode node, URI uri) throws URIException {
        String resultSuffix = getChildSuffix(node, true);

        String path = "";
        path = (uri.getPath() == null) ? "" : uri.getPath();
        path = path + (path.endsWith("/") ? "" : "/") + Long.toString(getRndPositiveLong());
        path = path + resultSuffix;

        return path;
    }

    /*
     * Return a random positive long value
     * Long.MIN_VALUE cannot be converted into a positive number by Math.abs
     */
    private long getRndPositiveLong() {
        long rnd = Long.MIN_VALUE;
        while (rnd == Long.MIN_VALUE) {
            rnd = staticRandomGenerator.nextLong();
        }
        return Math.abs(rnd);
    }

    /**
     * Analyse node (should be a folder unless it is host level) in-order.
     *
     * @param traversedNodes the nodes that were traversed during the analysis.
     * @param node the node to analyse
     * @return the number of nodes available at this layer
     */
    private int inOrderAnalyse(Set<StructuralNode> traversedNodes, StructuralNode node) {
        if (isStop || node == null) {
            return 0;
        }

        int subtreeNodes = 0;

        if (traversedNodes.contains(node)) {
            return 0;
        }
        traversedNodes.add(node);

        try {
            if (!node.isRoot()) {
                if (!node.isLeaf() || node.isLeaf() && node.getParent().isRoot()) {
                    logger.debug(
                            "Node being analysed isn't a leaf or is a leaf whose parent is root");
                    analyse(node);
                } else if (node.isLeaf() && !node.getParent().isRoot()) {
                    logger.debug("Node being analysed is a leaf whose parent is not root");
                    inOrderAnalyse(traversedNodes, node.getParent());
                } else {
                    return 1;
                }
            }

        } catch (Exception e) {
            // Nothing to do
        }

        Iterator<StructuralNode> iter = node.getChildIterator();
        logger.debug("Iterating children of {} with URI {}", node.getName(), node.getURI());
        while (iter.hasNext()) {
            subtreeNodes += inOrderAnalyse(traversedNodes, iter.next());
        }

        return subtreeNodes + 1;
    }

    public boolean isFileExist(HttpMessage msg) {

        if (msg.getResponseHeader().isEmpty()) {
            return false;
        }

        // RFC
        if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.NOT_FOUND) {
            return false;
        }

        URI uri = null;
        String sUri = null;
        try {
            uri = (URI) msg.getRequestHeader().getURI().clone();

            // strip off last part of path - use folder only
            uri.setQuery(null);
            String path = uri.getPath();
            path = path.replaceAll("/[^/]*$", "");
            uri.setPath(path);

        } catch (Exception e) {
            // Nothing to do
        } finally {
            if (uri != null) {
                sUri = uri.toString();
            }
        }

        // get sample with same relative path position when possible.
        // if not exist, use the host only
        SampleResponse sample = mapVisited.get(sUri);
        if (sample == null) {
            try {
                uri.setPath(null);

            } catch (URIException e2) {
                // Nothing to do
            }

            String sHostOnly = uri.toString();

            sample = mapVisited.get(sHostOnly);
        }

        // check if any analysed result.
        if (sample == null) {
            return msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK;
        }

        // check for redirect response.  If redirect to same location, then file does not exist
        if (HttpStatusCode.isRedirection(msg.getResponseHeader().getStatusCode())) {
            try {
                if (sample.getMessage().getResponseHeader().getStatusCode()
                        == msg.getResponseHeader().getStatusCode()) {
                    String location = msg.getResponseHeader().getHeader(HttpHeader.LOCATION);
                    if (location != null
                            && location.equals(
                                    sample.getMessage()
                                            .getResponseHeader()
                                            .getHeader(HttpHeader.LOCATION))) {
                        return false;
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return true;
        }

        // Not success code
        if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
            return false;
        }

        // remain only OK response here
        // nothing more to determine.  Check for possible not found page pattern.
        Matcher matcher = PATTERN_NOT_FOUND.matcher(msg.getResponseBody().toString());
        if (matcher.find()) {
            return false;
        }

        // static response
        String body = msg.getResponseBody().toString().replaceAll(PATTERN_REMOVE_HEADER, "");
        if (sample.getErrorPageType() == SampleResponse.ERROR_PAGE_STATIC) {
            try {
                if (sample.getMessage().getResponseBody().toString().equals(body)) {
                    return false;
                }

            } catch (HttpMalformedHeaderException | DatabaseException e) {
                logger.error("Failed to read the message: {}", e.getMessage(), e);
            }
            return true;
        }

        uri = msg.getRequestHeader().getURI();
        try {
            if (sample.getErrorPageType() == SampleResponse.ERROR_PAGE_DYNAMIC_BUT_DETERMINISTIC) {
                body =
                        msg.getResponseBody()
                                .toString()
                                .replaceAll(getPathRegex(uri), "")
                                .replaceAll(PATTERN_THREE_SEGMENT_TIME, "");
                return !sample.getMessage().getResponseBody().toString().equals(body);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return true;
    }

    private void sendAndReceive(HttpMessage msg) throws IOException {
        if (this.getDelayInMs() > 0) {
            try {
                Thread.sleep(this.getDelayInMs());

            } catch (InterruptedException e) {
                // Ignore
            }
        }

        httpSender.sendAndReceive(msg, parent.getRedirectRequestConfig());
        requestCount++;
        parent.notifyNewMessage(msg);
    }

    public int getDelayInMs() {
        return delayInMs;
    }

    public void setDelayInMs(int delayInMs) {
        this.delayInMs = delayInMs;
    }

    /**
     * Gets the request count, sent (and received) during the analysis.
     *
     * @return the request count
     * @since 2.5.0
     */
    public int getRequestCount() {
        return requestCount;
    }

    /**
     * Gets the running time, in milliseconds, of the analyser.
     *
     * @return the running time of the analyser.
     * @since 2.7.0
     */
    public long getRunningTime() {
        return stopWatch.getTime();
    }
}
