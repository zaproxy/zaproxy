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
package org.zaproxy.zap.spider.filters;

import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A FetchFilter is used to filter which resources should be fetched and processed by the Spider and
 * which shouldn't. This filter is applied before adding the resource in the processing queue.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public abstract class FetchFilter {

    /**
     * The FetchStatus enum is used as the status returned by a FetchFilter, stating if the uri is
     * accepted or, if not, why was it not accepted.
     */
    public enum FetchStatus {
        /** The uri is VALID. */
        VALID,
        /** The uri is VALID and is a seed. * */
        SEED,
        /** The uri is out of context. */
        OUT_OF_CONTEXT,
        /** The uri is out of scope. */
        OUT_OF_SCOPE,
        /** The uri has an illegal protocol. */
        ILLEGAL_PROTOCOL,
        /** The The uri is skipped because of user rules. */
        USER_RULES
    }

    /**
     * The Constant log.
     *
     * @deprecated (2.10.0) Use {@link #getLogger()} instead.
     */
    @Deprecated
    protected static final org.apache.log4j.Logger log =
            org.apache.log4j.Logger.getLogger(FetchFilter.class);

    private final Logger logger = LogManager.getLogger(getClass());

    /**
     * Gets the logger.
     *
     * @return the logger, never {@code null}.
     * @since 2.10.0
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Checks if the uri must be ignored and not processed and return the filter status.
     *
     * @param uri the uri to be processed
     * @return the fetch status, stating if the uri is accepted or, if not, why was it not accepted.
     */
    public abstract FetchStatus checkFilter(URI uri);
}
