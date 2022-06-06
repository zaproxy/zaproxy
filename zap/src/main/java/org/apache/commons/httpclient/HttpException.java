/*
 * HeadURL: https://svn.apache.org/repos/asf/httpcomponents/oac.hc3x/trunk/src/java/org/apache/commons/httpclient/HttpException.java
 * Revision: 608014
 * Date: 2008-01-02 05:48:53 +0000 (Wed, 02 Jan 2008)
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient;

import java.io.IOException;

/*
 * Forked class, for compatibility.
 *
 * Changes:
 *  - Remove cause and reason, no longer needed.
 */
/**
 * Signals that an HTTP or HttpClient exception has occurred.
 *
 * @author Laura Werner
 *
 * @version Revision: 608014 $ $Date: 2008-01-02 05:48:53 +0000 (Wed, 02 Jan 2008)
 * @deprecated (2.12.0)
 */
@Deprecated
public class HttpException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new HttpException with a <tt>null</tt> detail message.
     */
    public HttpException() {
        super();
    }

    /**
     * Creates a new HttpException with the specified detail message.
     *
     * @param message the exception detail message
     */
    public HttpException(String message) {
        super(message);
    }

    /**
     * Creates a new HttpException with the specified detail message and cause.
     *
     * @param message the exception detail message
     * @param cause the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     * if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     *
     * @since 3.0
     */
    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
