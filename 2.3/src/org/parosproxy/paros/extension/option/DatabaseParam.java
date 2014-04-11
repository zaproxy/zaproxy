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
// ZAP: 2014/03/27 Issue 1072: Allow the request and response body sizes to be user-specifiable as far as possible

package org.parosproxy.paros.extension.option;

import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the database configurations saved in the configuration file.
 * <p>
 * It allows to change, programmatically, the following database options:
 * <ul>
 * <li>Compact - allows the database to be compacted on exit.</li>
 * <li>Request Body Size - the size of the request body in the 'History' database table.</li>
 * <li>Response Body Size - the size of the response body in the 'History' database table.</li>
 * </ul>
 * </p>
 */
public class DatabaseParam extends AbstractParam {

    /**
     * The base configuration key for all database configurations.
     */
    private static final String PARAM_BASE_KEY = "database";
    
    /**
     * The configuration key for the compact option.
     */
    private static final String PARAM_COMPACT_DATABASE = PARAM_BASE_KEY + ".compact";
    
    /**
     * The configuration key for the request body size.
     */
    private static final String PARAM_REQUEST_BODY_SIZE = PARAM_BASE_KEY + ".request.bodysize";
    
    /**
     * The configuration key for the response body size.
     */
    private static final String PARAM_RESPONSE_BODY_SIZE = PARAM_BASE_KEY + ".response.bodysize";
    
    /**
     * The compact option, whether the database should be compacted on exit.
     * Default is {@code false}.
     * 
     * @see org.parosproxy.paros.db.Database#close(boolean)
     */
    private boolean compactDatabase;
    
    /**
     * The request body size in the history table.
     * Default is 16777216.
     * 
     */    
    private int requestbodysize;

    /**
     * The response body size in the history table.
     * Default is 16777216.
     * 
     */    
    private int responsebodysize;

    public DatabaseParam() {
        super();
        
        compactDatabase = false;
        requestbodysize = 16777216;
		responsebodysize = 16777216;
    }

    /**
     * Parses the database options.
     * <p>
     * The following database options are parsed:
     * <ul>
     * <li>Compact - allows the database to be compacted on exit.</li>
     * <li>Request Body Size - the size of the request body in the 'History' database table.</li>
     * <li>Response Body Size - the size of the response body in the 'History' database table.</li>
     * </ul>
     * </p>
     */
    @Override
    protected void parse() {
        compactDatabase = getConfig().getBoolean(PARAM_COMPACT_DATABASE, compactDatabase);
        requestbodysize = getConfig().getInt(PARAM_REQUEST_BODY_SIZE, requestbodysize);
        responsebodysize = getConfig().getInt(PARAM_RESPONSE_BODY_SIZE, responsebodysize);
    }

    /**
     * Tells whether the database should be compacted on exit or not.
     * 
     * @return {@code true} if the database should be compacted on exit,
     *         {@code false} otherwise
     * @see #setCompactDatabase(boolean)
     */
    public boolean isCompactDatabase() {
        return compactDatabase;
    }

    /**
     * Sets whether the database should be compacted on exit or not.
     * 
     * @param compactDatabase
     *            {@code true} if the database should be compacted on exit,
     *            {@code false} otherwise
     * @see #isCompactDatabase()
     * @see org.parosproxy.paros.db.Database#close(boolean)
     */
    public void setCompactDatabase(boolean compactDatabase) {
        this.compactDatabase = compactDatabase;
        getConfig().setProperty(PARAM_COMPACT_DATABASE, Boolean.valueOf(compactDatabase));
    }

    /**
     * Gets the request body size
     * 
     * @return the size of the request body, specified in bytes
     */
    public int getRequestBodySize() {
        return requestbodysize;
    }

    /**
     * Sets the request body size
     * 
     * @param requestbodysize the request body size
     */
    public void setRequestBodySize(int requestbodysize) {
        this.requestbodysize = requestbodysize;
        getConfig().setProperty(PARAM_REQUEST_BODY_SIZE, Integer.valueOf(requestbodysize));
    }

    /**
     * Gets the response body size
     * 
     * @return the size of the response body, specified in bytes
     */
    public int getResponseBodySize() {
        return responsebodysize;
    }

    /**
     * Sets the response body size
     * 
     * @param responsebodysize the response body size
     */
    public void setResponseBodySize(int responsebodysize) {
        this.responsebodysize = responsebodysize;
        getConfig().setProperty(PARAM_RESPONSE_BODY_SIZE, Integer.valueOf(responsebodysize));
    }

}
