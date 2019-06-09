/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.parosproxy.paros.db;

/**
 * This interface was extracted from the previous Paros class of the same name. The Paros class that
 * implements this interface has been moved to the 'paros' sub package and prefixed with 'Paros'
 *
 * @author psiinon
 */
import java.util.List;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

public interface TableHistory extends DatabaseListener {

    RecordHistory read(int historyId) throws HttpMalformedHeaderException, DatabaseException;

    RecordHistory write(long sessionId, int histType, HttpMessage msg)
            throws HttpMalformedHeaderException, DatabaseException;

    /**
     * Gets all the history record IDs of the given session.
     *
     * @param sessionId the ID of session of the history records to be returned
     * @return a {@code List} with all the history IDs of the given session, never {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.3.0
     * @see #getHistoryIdsOfHistType(long, int...)
     */
    List<Integer> getHistoryIds(long sessionId) throws DatabaseException;

    /**
     * Gets the history record IDs of the given session starting at the specified historyId
     * (inclusive).
     *
     * @param sessionId the ID of session of the history records to be returned
     * @param startAtHistoryId filters historyIds with &ge; startAtHistoryId
     * @return a {@code List} with the history IDs of the given session starting at the specified
     *     historyId (inclusive), never {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.8.0
     */
    List<Integer> getHistoryIdsStartingAt(long sessionId, int startAtHistoryId)
            throws DatabaseException;

    /**
     * Gets all the history record IDs of the given session and with the given history types.
     *
     * @param sessionId the ID of session of the history records
     * @param histTypes the history types of the history records that should be returned
     * @return a {@code List} with all the history IDs of the given session and history types, never
     *     {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.3.0
     * @see #getHistoryIds(long)
     * @see #getHistoryIdsExceptOfHistType(long, int...)
     */
    List<Integer> getHistoryIdsOfHistType(long sessionId, int... histTypes)
            throws DatabaseException;

    /**
     * Gets the history record IDs of the given session and with the given history types starting at
     * the specified historyId (inclusive).
     *
     * @param sessionId the ID of session of the history records
     * @param startAtHistoryId filters historyIds with &ge; startAtHistoryId
     * @param histTypes the history types of the history records that should be returned
     * @return a {@code List} with all the history IDs of the given session and history types
     *     starting at the specified historyId (inclusive), never {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.8.0
     */
    List<Integer> getHistoryIdsOfHistTypeStartingAt(
            long sessionId, int startAtHistoryId, int... histTypes) throws DatabaseException;

    /**
     * Returns all the history record IDs of the given session except the ones with the given
     * history types. *
     *
     * @param sessionId the ID of session of the history records
     * @param histTypes the history types of the history records that should be excluded
     * @return a {@code List} with all the history IDs of the given session and history types, never
     *     {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.3.0
     * @see #getHistoryIdsOfHistType(long, int...)
     */
    List<Integer> getHistoryIdsExceptOfHistType(long sessionId, int... histTypes)
            throws DatabaseException;

    /**
     * Returns the history record IDs of the given session except the ones with the given history
     * types starting at the specified historyId (inclusive). *
     *
     * @param sessionId the ID of session of the history records
     * @param startAtHistoryId filters historyIds with &ge; startAtHistoryId
     * @param histTypes the history types of the history records that should be excluded
     * @return a {@code List} with all the history IDs of the given session and history types
     *     starting at the specified historyId (inclusive), never {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.8.0
     */
    List<Integer> getHistoryIdsExceptOfHistTypeStartingAt(
            long sessionId, int startAtHistoryId, int... histTypes) throws DatabaseException;

    List<Integer> getHistoryList(long sessionId, int histType, String filter, boolean isRequest)
            throws DatabaseException;

    void deleteHistorySession(long sessionId) throws DatabaseException;

    void deleteHistoryType(long sessionId, int historyType) throws DatabaseException;

    void delete(int historyId) throws DatabaseException;

    /**
     * Deletes from the database all the history records whose ID is in the list {@code ids}, in
     * batches of 1000 records.
     *
     * @param ids a {@code List} containing all the IDs of the history records to be deleted
     * @throws IllegalArgumentException if {@code ids} is null
     * @throws DatabaseException if an error occurred while deleting the history records
     * @since 2.0.0
     * @see #delete(List, int)
     */
    // ZAP: Added method.
    void delete(List<Integer> ids) throws DatabaseException;

    /**
     * Deletes from the database all the history records whose ID is in the list {@code ids}, in
     * batches of given {@code batchSize}.
     *
     * @param ids a {@code List} containing all the IDs of the history records to be deleted
     * @param batchSize the maximum size of records to delete in a single batch
     * @throws IllegalArgumentException if {@code ids} is null
     * @throws IllegalArgumentException if {@code batchSize} is not greater than zero
     * @throws DatabaseException if an error occurred while deleting the history records
     * @since 2.3.0
     */
    void delete(List<Integer> ids, int batchSize) throws DatabaseException;

    /**
     * Deletes all records whose history type was marked as temporary.
     *
     * @throws DatabaseException if an error occurred while deleting the temporary history records
     * @see HistoryReference#getTemporaryTypes()
     */
    void deleteTemporary() throws DatabaseException;

    boolean containsURI(long sessionId, int historyType, String method, String uri, byte[] body)
            throws DatabaseException;

    RecordHistory getHistoryCache(HistoryReference ref, HttpMessage reqMsg)
            throws DatabaseException, HttpMalformedHeaderException;

    void updateNote(int historyId, String note) throws DatabaseException;

    int lastIndex();
}
