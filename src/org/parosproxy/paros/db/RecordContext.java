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

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RecordContext {

	public static final int TYPE_NAME = 1;
	public static final int TYPE_DESCRIPTION = 2;
	public static final int TYPE_INCLUDE = 3;
	public static final int TYPE_EXCLUDE = 4;
	public static final int TYPE_IN_SCOPE = 5;
	
	public static final int TYPE_AUTH_LOGIN_URL = 101;
	public static final int TYPE_AUTH_LOGIN_POST_DATA = 102;
	public static final int TYPE_AUTH_LOGIN_INDICATOR = 103;
	public static final int TYPE_AUTH_LOGOUT_URL = 104;
	public static final int TYPE_AUTH_LOGOUT_POST_DATA = 105;
	public static final int TYPE_AUTH_LOGOUT_INDICATOR = 106;

    private long dataId = 0;
    private int contextId = 0;
    private int type = 0;
    private String data = "";

	public RecordContext(long dataId, int contextId, int type, String data) {
		super();
		this.contextId = contextId;
		this.dataId = dataId;
		this.type = type;
		this.data = data;
	}

	public int getContextId() {
		return contextId;
	}

	public void setContextId(int contextId) {
		this.contextId = contextId;
	}

	public long getDataId() {
		return dataId;
	}

	public void setDataId(long dataId) {
		this.dataId = dataId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}


}
