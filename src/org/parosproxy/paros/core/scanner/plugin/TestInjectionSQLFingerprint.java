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
public class TestInjectionSQLFingerprint extends AbstractAppParamPlugin {

    private static final int TIME_SPREAD = 15000;
	
//	private static final String MSSQL_DELAY_1 = "';waitfor%20delay%20'0:0:15';--";
//	private static final String MSSQL_DELAY_2 = ";waitfor%20delay%20'0:0:15';--";
//
//	private static final String SQL_BLIND_MS_INSERT = ");waitfor%20delay%20'0:0:15';--";
    
    private static final String MSSQL_DELAY_1 = "';waitfor delay '0:0:15';--";
    private static final String MSSQL_DELAY_2 = ";waitfor delay '0:0:15';--";

    private static final String SQL_BLIND_MS_INSERT = ");waitfor delay '0:0:15';--";

    private static final String SQL_BLIND_INSERT = ");--";
		
	private static final String SQL_CHECK_ERR = "'INJECTED_PARAM";		// invalid statement to trigger SQL exception. Make sure the pattern below does not appear here
	
	private static final Pattern patternErrorODBC1 = Pattern.compile("Microsoft OLE DB Provider for ODBC Drivers.*error", PATTERN_PARAM);
	private static final Pattern patternErrorODBC2 = Pattern.compile("ODBC.*Drivers.*error", PATTERN_PARAM);
	private static final Pattern patternErrorGeneric = Pattern.compile("JDBC|ODBC|not a valid MySQL|SQL", PATTERN_PARAM);
	private static final Pattern patternErrorODBCMSSQL = Pattern.compile("ODBC SQL Server Driver", PATTERN_PARAM);
	
	//private String mResBodyNormal 	= "";		// normal response for comparison
	private String mResBodyError	= "";	// error response for comparison



    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getId()
     */
    public int getId() {
        return 40000;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getName()
     */
    public String getName() {
        return "SQL Injection Fingerprinting";
    }


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDescription()
     */
    public String getDescription() {
        String msg = "SQL injection may be possible.";
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
        String msg = "Do not trust client side input even if there is client side validation.  In general, "
            + "<ul>" 
            + "<li>If the input string is numeric, type check it.</li>"
            + "<li>If the application used JDBC, use PreparedStatement or CallableStatement with parameters passed by '?'</li>"
            + "<li>If the application used ASP, use ADO Command Objects with strong type checking and parameterized query.</li>"
            + "<li>If stored procedure or bind variables can be used, use it for parameter passing into query.  Do not just concatenate string into query in the stored procedure!</li>"
            + "<li>Do not create dynamic SQL query by simple string concatentation.</li>"
            + "<li>Use minimum database user privilege for the application.  This does not eliminate SQL injection but minimize its damage.  "
            + "Eg if the application require reading one table only, grant such access to the application.  Avoid using 'sa' or 'db-owner'.</li>"
            + "</ul>";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getReference()
     */
    public String getReference() {
        String msg = "<ul><li>The OWASP guide at http://www.owasp.org/documentation/guide</li>"
            + "<li>http://www.sqlsecurity.com/DesktopDefault.aspx?tabid=23</li>"
            + "<li>http://www.spidynamics.com/whitepapers/WhitepaperSQLInjection.pdf</li>"
            + "<li>For Oracle database, refer to http://www.integrigy.com/info/IntegrigyIntrotoSQLInjectionAttacks.pdf</li></ul>";
      return msg;
            
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractTest#init()
     */
    public void init() {


    }

    public void scan(HttpMessage baseMsg, String param, String value) {
        try {
            scanMSSQL(baseMsg, param, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractAppParamTest#scan(com.proofsecure.paros.network.HttpMessage, java.lang.String, java.lang.String)
     */
    public void scanMSSQL(HttpMessage baseMsg, String param, String value) throws HttpException, IOException {

		String bingoQuery = null;
		String displayURI = null;
		String newQuery = null;
		
		String resBodyAND = null;
		String resBodyANDErr = null;
		
		int pos = 0;
		long defaultTimeUsed = 0;
		long timeUsed = 0;
		long lastTime = 0;

		HttpMessage msg = getNewMsg();
		
		// always try normal query first
//		lastTime = System.currentTimeMillis();
//		sendAndReceive(msg);
//		defaultTimeUsed = System.currentTimeMillis() - lastTime;
//		if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
//			return;
//		}
//
//		mResBodyNormal = msg.getResponseBody().toString();
		
		// 2nd try an always error SQL query

		newQuery = setParameter(msg, param, value+SQL_CHECK_ERR);
		lastTime = System.currentTimeMillis();
		sendAndReceive(msg);
		defaultTimeUsed = System.currentTimeMillis() - lastTime;
		mResBodyError	= msg.getResponseBody().toString();
		
		if (checkResult(msg, newQuery)) {
		    return;
		}
		
		// try BLIND SQL SELECT using timing 
		newQuery = setParameter(msg, param, value + MSSQL_DELAY_1);
		lastTime = System.currentTimeMillis();
		sendAndReceive(msg);
		timeUsed = System.currentTimeMillis() - lastTime;
				
		if (checkMSTimeResult(msg, newQuery, defaultTimeUsed, timeUsed)) {
		    return;
		}

		newQuery = setParameter(msg, param, value + MSSQL_DELAY_2);
		lastTime = System.currentTimeMillis();
		sendAndReceive(msg);
		timeUsed = System.currentTimeMillis() - lastTime;
				
		if (checkMSTimeResult(msg, newQuery, defaultTimeUsed, timeUsed)) {
		    return;
		    
		}

		// try BLIND MSSQL INSERT using timing
		
		testMSBlindINSERT(msg, param, value);
		
	}
	
	private void testMSBlindINSERT(HttpMessage msg, String param, String value) throws HttpException, IOException {

	    String bingoQuery = null;
		String displayURI = null;
		String newQuery = null;
		
		String resBody = null;
		String resBodyErr = null;
		
		int pos = 0;
		long defaultTimeUsed = 0;

		int TRY_COUNT = 5;
		StringBuffer sbInsertValue = null;
		

		// try insert param using INSERT and timing
		sbInsertValue = new StringBuffer();
		for (int i=0; i<TRY_COUNT; i++) {
			// guess at most 10 parameters.
			
			if (i>0) {
				sbInsertValue.append(",'0'");
			}
			
			// try INSERT
			newQuery = setParameter(msg, param, value + "'" + sbInsertValue.toString() + SQL_BLIND_MS_INSERT);

			sendAndReceive(msg);

			if (checkMSTimeResult(msg, newQuery, defaultTimeUsed, msg.getTimeElapsedMillis())) {
				return;
			}

			// no need to try following if not a value integer
			try {
				long tmp = Long.parseLong(value);
			} catch (NumberFormatException e) {
				continue;
			}
			newQuery = setParameter(msg, param, value + sbInsertValue.toString() + SQL_BLIND_MS_INSERT);
			sendAndReceive(msg);
		
			if (checkMSTimeResult(msg, newQuery, defaultTimeUsed, msg.getTimeElapsedMillis())) {
				return;
			}
			
		}

	}


	private boolean checkResult(HttpMessage msg, String query) {

	    StringBuffer sb = new StringBuffer();
	    boolean isSqlError = false;
		if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK
			&& !HttpStatusCode.isServerError(msg.getResponseHeader().getStatusCode())) {
		    return false;
		}

		
		if (matchBodyPattern(msg, patternErrorODBC1, sb)
				|| matchBodyPattern(msg, patternErrorODBC2, sb)) {
			// check for ODBC error.  Almost certain.
		    
		    isSqlError = true;
		    
		} else if (matchBodyPattern(msg, patternErrorGeneric, sb)) {
			// check for other sql error (JDBC) etc.  Suspicious.
			bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, query, sb.toString(), msg);
			isSqlError = true;
			
		}

		if (!isSqlError) {
		    return false;
		}
		
		if (matchBodyPattern(msg, patternErrorODBCMSSQL, sb)) {
		    getKb().add(msg.getRequestHeader().getURI(), "sql/mssql", new Boolean(true));
		    return true;
		}

		return true;
		
	}

	
	private boolean checkMSTimeResult(HttpMessage msg, String query, long defaultTimeUsed, long timeUsed) {

		if (timeUsed > defaultTimeUsed + TIME_SPREAD - 500) {		
			// allow 500ms discrepancy
		    getKb().add(msg.getRequestHeader().getURI(), "sql/mssql", new Boolean(true));
		    return true;
		}			
		return false;
	}

}
