/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2006 Chinotec Technologies Company
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
package org.parosproxy.paros.db.paros;

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class InMemoryTableHistory extends ParosAbstractTable implements TableHistory {

    private final AtomicInteger nextId = new AtomicInteger();
    private final InMemoryDb<Integer, HistoryItem> db = new InMemoryDb<>();

    private record HistoryItem(
            long sessionId,
            int histType,
            long timeSentMillis,
            int timeElapsedMillis,
            String method,
            String uri,
            int statusCode,
            String reqHeader,
            byte[] reqBody,
            String resHeader,
            byte[] resBody,
            String tag,
            String note,
            boolean responseFromTargetHost
    ) {
        public RecordHistory toRecordHistory(int historyId) throws HttpMalformedHeaderException {
            return new RecordHistory(
                    historyId,
                    histType, sessionId, timeSentMillis, timeElapsedMillis, reqHeader, reqBody, resHeader, resBody, tag, note, responseFromTargetHost
            );
        }
    }

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
    }

    @Override
    public RecordHistory read(int historyId)
            throws HttpMalformedHeaderException, DatabaseException {
        return db.get(historyId).toRecordHistory(historyId);
    }

    @Override
    public RecordHistory write(long sessionId, int histType, HttpMessage msg)
            throws HttpMalformedHeaderException, DatabaseException {

        try {
            String reqHeader = "";
            byte[] reqBody = new byte[0];
            String resHeader = "";
            byte[] resBody = reqBody;
            String method = "";
            String uri = "";
            int statusCode = 0;
            String note = msg.getNote();

            if (!msg.getRequestHeader().isEmpty()) {
                reqHeader = msg.getRequestHeader().toString();
                reqBody = msg.getRequestBody().getBytes();
                method = msg.getRequestHeader().getMethod();
                uri = msg.getRequestHeader().getURI().toString();
            }

            if (!msg.getResponseHeader().isEmpty()) {
                resHeader = msg.getResponseHeader().toString();
                resBody = msg.getResponseBody().getBytes();
                statusCode = msg.getResponseHeader().getStatusCode();
            }

            // return write(sessionId, histType, msg.getTimeSentMillis(),
            // msg.getTimeElapsedMillis(), method, uri, statusCode, reqHeader, reqBody, resHeader,
            // resBody, msg.getTag());
            return write(
                    sessionId,
                    histType,
                    msg.getTimeSentMillis(),
                    msg.getTimeElapsedMillis(),
                    method,
                    uri,
                    statusCode,
                    reqHeader,
                    reqBody,
                    resHeader,
                    resBody,
                    null,
                    note,
                    msg.isResponseFromTargetHost());
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private RecordHistory write(
            long sessionId,
            int histType,
            long timeSentMillis,
            int timeElapsedMillis,
            String method,
            String uri,
            int statusCode,
            String reqHeader,
            byte[] reqBody,
            String resHeader,
            byte[] resBody,
            String tag,
            String note,
            boolean responseFromTargetHost)
            throws HttpMalformedHeaderException, SQLException, DatabaseException {

        var item = new HistoryItem(
                sessionId, histType, timeSentMillis, timeElapsedMillis, method, uri, statusCode, reqHeader, reqBody, resHeader, resBody, tag, note, responseFromTargetHost);

        Integer id = nextId.incrementAndGet();

        db.put(id, item);

        return item.toRecordHistory(id);
    }

    /**
     * Gets all the history record IDs of the given session.
     *
     * @param sessionId the ID of session of the history records to be returned
     * @return a {@code List} with all the history IDs of the given session, never {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @see #getHistoryIdsOfHistType(long, int...)
     * @since 2.3.0
     */
    @Override
    public List<Integer> getHistoryIds(long sessionId) throws DatabaseException {
        return getHistoryIdsOfHistType(sessionId, null);
    }

    /**
     * Gets all the history record IDs of the given session and with the given history types.
     *
     * @param sessionId the ID of session of the history records
     * @param histTypes the history types of the history records that should be returned
     * @return a {@code List} with all the history IDs of the given session and history types, never
     * {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @see #getHistoryIds(long)
     * @since 2.3.0
     */
    @Override
    public List<Integer> getHistoryIdsOfHistType(long sessionId, int... histTypes)
            throws DatabaseException {
        return getHistoryIdsByParams(sessionId, 0, true, histTypes);
    }

    private List<Integer> getHistoryIdsByParams(
            long sessionId, int startAtHistoryId, boolean includeHistTypes, int... histTypes) {
        Predicate<HistoryItem> trueP = (item) -> true;

        Predicate<HistoryItem> sessionP = (item) -> item.sessionId == sessionId;

        boolean hasHistTypes = histTypes != null && histTypes.length > 0;

        Predicate<HistoryItem> histTypesP = (item) -> Set.of(List.of(histTypes)).contains(item.histType);
        Predicate<HistoryItem> typesP = hasHistTypes ? (includeHistTypes ? histTypesP : histTypesP.negate()) : trueP;

        Predicate<HistoryItem> startP = startAtHistoryId > 0 ?
                (item) -> item.sessionId > startAtHistoryId : trueP;


        Predicate<HistoryItem> want = sessionP.and(typesP).and(startP);

        List<Integer> results = new ArrayList<>();

        db.collect((id, item) -> {
            if (want.test(item)) {
                results.add(id);
            }
        });

        return results;
    }

    @Override
    public void deleteHistorySession(long sessionId) throws DatabaseException {
        db.remove((item) -> item.sessionId == sessionId);
    }

    @Override
    public void deleteHistoryType(long sessionId, int historyType) throws DatabaseException {
        db.remove((item) -> item.sessionId == sessionId && item.histType == historyType);
    }

    @Override
    public void delete(int historyId) throws DatabaseException {
        db.remove(historyId);
    }

    /**
     * Deletes from the database all the history records whose ID is in the list {@code ids}, in
     * batches of 1000 records.
     *
     * @param ids a {@code List} containing all the IDs of the history records to be deleted
     * @throws IllegalArgumentException if {@code ids} is null
     * @throws DatabaseException        if an error occurred while deleting the history records
     * @see #delete(List, int)
     * @since 2.0.0
     */
    // ZAP: Added method.
    @Override
    public void delete(List<Integer> ids) throws DatabaseException {
        delete(ids, 1000);
    }

    /**
     * Deletes from the database all the history records whose ID is in the list {@code ids}, in
     * batches of given {@code batchSize}.
     *
     * @param ids       a {@code List} containing all the IDs of the history records to be deleted
     * @param batchSize the maximum size of records to delete in a single batch
     * @throws IllegalArgumentException if {@code ids} is null
     * @throws IllegalArgumentException if {@code batchSize} is not greater than zero
     * @throws DatabaseException        if an error occurred while deleting the history records
     * @since 2.3.0
     */
    @Override
    public void delete(List<Integer> ids, int batchSize) throws DatabaseException {
        ids.forEach(db::remove);
    }

    /**
     * Deletes all records whose history type was marked as temporary (by calling {@code
     * setHistoryTypeTemporary(int)}).
     *
     * <p>By default the only temporary history types are {@code HistoryReference#TYPE_TEMPORARY}
     * and {@code HistoryReference#TYPE_SCANNER_TEMPORARY}.
     *
     * @see HistoryReference#getTemporaryTypes()
     */
    @Override
    public void deleteTemporary() {
        Set<Integer> types = HistoryReference.getTemporaryTypes();

        db.remove((item) -> types.contains(item.histType));
    }

    @Override
    public boolean containsURI(
            long sessionId, int historyType, String method, String uri, byte[] body) {
        throw new UnsupportedOperationException("james");
    }

    @Override
    public RecordHistory getHistoryCache(HistoryReference ref, HttpMessage reqMsg) {
        throw new UnsupportedOperationException("james");

    }

    @Override
    public void updateNote(int historyId, String note) {
        throw new UnsupportedOperationException("james");
    }

    @Override
    public int lastIndex() {
        return nextId.get();
    }
}
