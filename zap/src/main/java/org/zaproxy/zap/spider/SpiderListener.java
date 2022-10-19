/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.spider;

/**
 * The listener interface for receiving spider related events. The class that is interested in
 * processing a spider event implements this interface, and the object created with that class is
 * registered with a component using the component's <code>addSpiderListener</code> method. When the
 * spider event occurs, that object's appropriate method is invoked.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public interface SpiderListener {

    /**
     * Event triggered when the Spider progress has changed.
     *
     * @param percentageComplete the percentage complete
     * @param numberCrawled the number of pages crawled
     * @param numberToCrawl the number of pages left to crawl
     */
    void spiderProgress(int percentageComplete, int numberCrawled, int numberToCrawl);

    /**
     * Event triggered when a new uri was found. The <code>status</code> parameter says if the URI
     * was skipped according to any skip rule or it was processed.
     *
     * @param uri the uri
     * @param method the method used for accessing the uri
     * @param status the {@code FetchStatus} stating if this uri will be processed, and, if not,
     *     stating the reason of the filtering
     */
    void foundURI(
            String uri,
            String method,
            org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status);

    /**
     * Notifies that a new {@link SpiderTask}'s result is available.
     *
     * @param spiderTaskResult the result of the spider task.
     */
    void notifySpiderTaskResult(SpiderTaskResult spiderTaskResult);

    /**
     * Event triggered when the spider is finished. This event is triggered either when the spider
     * has completed scanning a website, in which case the parameter <code>successful</code> is
     * <code>true</code> , either when it was stopped by an user in which case the <code>successful
     * </code> parameter is false.
     *
     * @param successful
     *     <ul>
     *       <li>true - when the spider has scanned a website completely and finished by its own
     *       <li>false - when the spider was stopped by the user
     *     </ul>
     */
    void spiderComplete(boolean successful);
}
