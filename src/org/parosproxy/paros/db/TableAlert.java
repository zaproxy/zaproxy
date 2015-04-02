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
package org.parosproxy.paros.db;

/**
 * This interface was extracted from the previous Paros class of the same name.
 * The Paros class that implements this interface has been moved to the 'paros' sub package and prefixed with 'Paros'
 * @author psiinon
 */

import java.util.List;
import java.util.Vector;


public interface TableAlert extends DatabaseListener {

	RecordAlert read(int alertId) throws DatabaseException;

	RecordAlert write(int scanId, int pluginId, String alert,
			int risk, int confidence, String description, String uri,
			String param, String attack, String otherInfo, String solution,
			String reference, String evidence, int cweId, int wascId,
			int historyId, int sourceHistoryId) throws DatabaseException;

	Vector<Integer> getAlertListBySession(long sessionId)
			throws DatabaseException;


	void deleteAlert(int alertId) throws DatabaseException;

	int deleteAllAlerts() throws DatabaseException;

	void update(int alertId, String alert, int risk,
			int confidence, String description, String uri, String param,
			String attack, String otherInfo, String solution, String reference,
			String evidence, int cweId, int wascId, int sourceHistoryId)
			throws DatabaseException;

	void updateHistoryIds(int alertId, int historyId,
			int sourceHistoryId) throws DatabaseException;

	List<RecordAlert> getAlertsBySourceHistoryId(int historyId)
			throws DatabaseException;

	Vector<Integer> getAlertList() throws DatabaseException;
}