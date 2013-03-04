/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

public class RecordTag {

    private long tagId = 0;
    private long historyId = 0;
    private String tag = "";

    
    /**
     * @param tagId
     * @param historyId
     * @param tag
     */
    public RecordTag(long tagId, long historyId, String tag) {
        super();
        setTagId(tagId);
        setHistoryId(historyId);
        setTag(tag);
    }
    /**
     * @return Returns the tagId.
     */
    public long getTagId() {
        return tagId;
    }
    /**
     * @param tagId The tagId to set.
     */
    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
	public long getHistoryId() {
		return historyId;
	}
	public void setHistoryId(long historyId) {
		this.historyId = historyId;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
}
