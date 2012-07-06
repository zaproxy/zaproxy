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

package org.parosproxy.paros.extension.option;

import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the database configurations saved in the configuration file.
 * <p>
 * It allows to change, programmatically, the following database options:
 * <ul>
 * <li>Compact - allows to compact the database on exit.</li>
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
     * The compact option, whether the database should be compacted on exit.
     * Default is {@code false}.
     * 
     * @see org.parosproxy.paros.db.Database#close(boolean)
     */
    private boolean compactDatabase;

    public DatabaseParam() {
        super();
        
        compactDatabase = false;
    }

    /**
     * Parses the database options.
     * <p>
     * The following database options are parsed:
     * <ul>
     * <li>Compact - allows to compact the database on exit.</li>
     * </ul>
     * </p>
     */
    @Override
    protected void parse() {
        compactDatabase = getConfig().getBoolean(PARAM_COMPACT_DATABASE, compactDatabase);
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

}
