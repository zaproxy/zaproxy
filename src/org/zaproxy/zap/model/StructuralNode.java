/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
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
package org.zaproxy.zap.model;

import java.util.Iterator;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;

public interface StructuralNode {

	StructuralNode getParent() throws DatabaseException;
	
	Iterator<StructuralNode> getChildIterator();
	
	long getChildNodeCount() throws DatabaseException;
	
	HistoryReference getHistoryReference();
	
	String getName();
	
	String getRegexPattern() throws DatabaseException;

	String getRegexPattern(boolean incChildren) throws DatabaseException;

	URI getURI();
	
	boolean isRoot();
	
	boolean isLeaf();
	
	boolean isDataDriven();
	
	boolean isSameAs (StructuralNode node);
}
