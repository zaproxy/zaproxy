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
// ZAP: 2015/04/02 Issue 1582: Low memory option

package org.parosproxy.paros.db;


/**
 * This interface was extracted from the previous Paros class of the same name.
 * The Paros class that implements this interface has been moved to the 'paros' sub package and prefixed with 'Paros'
 * @author psiinon
 */
public interface Database {
	
	/**
	 * The default HyperSQL Db: http://hsqldb.org/
	 */
	public static final String DB_TYPE_HSQLDB	= "hsqldb";

	DatabaseServer getDatabaseServer();

	TableHistory getTableHistory();

	/**
	 * @return Returns the tableSession.
	 */
	TableSession getTableSession();

	void addDatabaseListener(DatabaseListener listener);

	// ZAP: Changed parameter's type from SpiderListener to DatabaseListener.
	void removeDatabaseListener(DatabaseListener listener);

	void open(String path) throws ClassNotFoundException, Exception;

	/**
	 * Closes the database. If the parameter {@code compact} is {@code true},
	 * the database will be compacted, compacting the database ensures a minimal
	 * space disk usage but it will also take longer to close. Any necessary
	 * cleanups are performed prior to closing the database (the cleanup removes
	 * the temporary {@code HistoryRefernece}s).
	 * 
	 * <p>
	 * <b>Note:</b> Calling this method has the same effects as calling the
	 * method {@link #close(boolean, boolean)} with the parameter
	 * {@code cleanup} as {@code true}.
	 * </p>
	 * 
	 * @param compact
	 *            {@code true} if the database should be compacted,
	 *            {@code false} otherwise.
	 * @see org.parosproxy.paros.model.HistoryReference
	 */
	// ZAP: Added JavaDoc.
	void close(boolean compact);

	/**
	 * Permanently delete the specified session
	 * @param sessionName
	 */
	void deleteSession(String sessionName);

	/**
	 * Closes the database. If the parameter {@code compact} is {@code true},
	 * the database will be compacted, compacting the database ensures a minimal
	 * space disk usage but it will also take longer to close. If the parameter
	 * {@code cleanup} is {@code true} any necessary cleanups are performed
	 * prior to closing the database (the cleanup removes the temporary
	 * {@code HistoryRefernece}s.)
	 * 
	 * @param compact
	 *            {@code true} if the database should be compacted,
	 *            {@code false} otherwise.
	 * @param cleanup
	 *            {@code true} if any necessary cleanups should be performed,
	 *            {@code false} otherwise.
	 * @see org.parosproxy.paros.model.HistoryReference
	 */
	// ZAP: Added method. Note: any change made to this method must have the
	// ZAP comment as the content was moved from the paros method close(boolean).
	void close(boolean compact, boolean cleanup);

	/**
	 * @return Returns the tableAlert.
	 */
	TableAlert getTableAlert();

	/**
	 * @param tableAlert The tableAlert to set.
	 */
	void setTableAlert(TableAlert tableAlert);

	/**
	 * @return Returns the tableScan.
	 */
	TableScan getTableScan();

	/**
	 * @param tableScan The tableScan to set.
	 */
	void setTableScan(TableScan tableScan);

	TableTag getTableTag();

	void setTableTag(TableTag tableTag);

	// ZAP: Added method.
	TableSessionUrl getTableSessionUrl();

	// ZAP: Added method.
	void setTableSessionUrl(TableSessionUrl tableSessionUrl);

	// ZAP: Added method.
	TableParam getTableParam();

	TableContext getTableContext();

	TableStructure getTableStructure();

	/**
	 * The type of the database - eg {@value #DB_TYPE_HSQLDB}
	 * @return
	 */
	String getType();

	/**
	 * Discards the history associated with the given session ID, called when a session is not to be saved (and a new one is
	 * about to be created).
	 * <p>
	 * Implementations might opt to do nothing, for example, if the database is file based (HSQLDB) and those files are deleted
	 * if the session is not saved.
	 *
	 * @param sessionId the ID of the session
	 * @throws DatabaseException If an error occurred while discarding the history of the session.
	 * @since 2.5.0
	 */
	void discardSession(long sessionId) throws DatabaseException;

}