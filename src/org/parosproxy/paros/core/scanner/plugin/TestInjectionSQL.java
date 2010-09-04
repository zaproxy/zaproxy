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
public class TestInjectionSQL extends AbstractAppParamPlugin {

    private static final String[] dependency = {"TestInjectionSQLFingerprint"};
    
    private static final int TIME_SPREAD = 15000;

//	private static final String SQL_OR_1 = "%20OR%201=1;--";
//	private static final String SQL_OR_2 = "'%20OR%201=1;--";
//	private static final String SQL_OR_3 = "'%20OR%20'1'='1";
//	
//	private static final String SQL_DELAY_1 = "';waitfor%20delay%20'0:0:15';--";
//	private static final String SQL_DELAY_2 = ";waitfor%20delay%20'0:0:15';--";
//
//	private static final String SQL_BLIND_MS_INSERT = ");waitfor%20delay%20'0:0:15';--";
//	private static final String SQL_BLIND_INSERT = ");--";
//	
//	private static final String SQL_AND_1 = "%20AND%201=1";		// true statement for comparison
//	private static final String SQL_AND_1_ERR = "%20AND%201=2";	// always false stmt for comparison
//
//	private static final String SQL_AND_2 = "'%20AND%20'1'='1";		// true statement
//	private static final String SQL_AND_2_ERR = "'%20AND%20'1'='2";	// always false statement for comparison

//    private static final String SQL_OR_1 = " OR 1=1;--";
//    private static final String SQL_OR_2 = "' OR 1=1;--";
//    private static final String SQL_OR_3 = "' OR '1'='1";
    
    private static final String SQL_DELAY_1 = "';waitfor delay '0:0:15';--";
    private static final String SQL_DELAY_2 = ";waitfor delay '0:0:15';--";

    private static final String SQL_BLIND_MS_INSERT = ");waitfor delay '0:0:15';--";
//    private static final String SQL_BLIND_INSERT = ");--";
    
//    private static final String SQL_AND_1 = " AND 1=1";     // true statement for comparison
//    private static final String SQL_AND_1_ERR = " AND 1=2"; // always false stmt for comparison
//
//    private static final String SQL_AND_2 = "' AND '1'='1";     // true statement
//    private static final String SQL_AND_2_ERR = "' AND '1'='2"; // always false statement for comparison
    
    private static final String[] SQL_AND = {
        " AND 1=1",
        "' AND '1'='1",
        "\" AND \"1\"=\"1",
    };  // always true statement for comparison
    
    private static final String[] SQL_AND_ERR = {
        " AND 1=2",
        "' AND '1'='2",
        "\" AND \"1\"=\"2"
    }; // always false statement for comparison
    
    private static final String[] SQL_OR = {
        " OR 1=1",
        "' OR '1'='1",
        "\" OR \"1\"=\"1",
    };  // always true statement for comparison if no output is returned from AND

    private static final String SQL_CHECK_ERR = "'INJECTED_PARAM";		// invalid statement to trigger SQL exception. Make sure the pattern below does not appear here
	
	private static final Pattern patternErrorODBC1 = Pattern.compile("Microsoft OLE DB Provider for ODBC Drivers.*error", PATTERN_PARAM);
	private static final Pattern patternErrorODBC2 = Pattern.compile("ODBC.*Drivers.*error", PATTERN_PARAM);
	private static final Pattern patternErrorGeneric = Pattern.compile("JDBC|ODBC|not a valid MySQL|SQL", PATTERN_PARAM);
	private static final Pattern patternErrorODBCMSSQL = Pattern.compile("ODBC SQL Server Driver", PATTERN_PARAM);
	
	private String mResBodyNormal 	= "";		// normal response for comparison
	private String mResBodyError	= "";	// error response for comparison



    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getId()
     */
    public int getId() {
        return 40030;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getName()
     */
    public String getName() {
        return "SQL Injection";
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
        String msg = "SQL injection is possible.  User parameters submitted will be formulated into a SQL query for database processing.  "
            + "If the query is built by simple 'string concatenation', it is possible to modify the meaning of the query by carefully crafting the parameters.  "
            + "Depending on the access right and type of database used, tampered query can be used to retrieve sensitive information from the database or execute arbitrary code.  " 
            + "MS SQL and PostGreSQL, which supports multiple statements, may be exploited if the database access right is more powerful.\r\n"
            + "This can occur in URL query strings, POST paramters or even cookies.  Currently check on cookie is not supported by Paros.  "
            + "You should check SQL injection manually as well as some blind SQL injection areas cannot be discovered by this check.";
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
		String resBodyANDErr = null;
		String resBodyOR = null;

		long defaultTimeUsed = 0;

		HttpMessage msg = getNewMsg();
		
		// always try normal query first
		sendAndReceive(msg);
		defaultTimeUsed = msg.getTimeElapsedMillis();
		if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
			return;
		}

		mResBodyNormal = msg.getResponseBody().toString();
		
		// 2nd try an always error SQL query

		newQuery = setParameter(msg, param, value+SQL_CHECK_ERR);
		sendAndReceive(msg);
		mResBodyError	= msg.getResponseBody().toString();
		if (checkANDResult(msg, newQuery)) {
			return;
		}


		// blind sql injections
        
        for (int i=0; i<SQL_AND.length;i++) {
            bingoQuery = setParameter(msg, param, value+SQL_AND[i]);
            sendAndReceive(msg);
            
            displayURI = msg.getRequestHeader().getURI().toString();
            
            if (checkANDResult(msg, bingoQuery)) {
                return;
            }
            
            if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
                
                resBodyAND = stripOff(msg.getResponseBody().toString(), SQL_AND[i]);
                
                if (resBodyAND.compareTo(mResBodyNormal) == 0) {
                    
                    newQuery = setParameter(msg, param, value + SQL_AND_ERR[i]);
                    sendAndReceive(msg);
                    resBodyANDErr = stripOff(msg.getResponseBody().toString(), SQL_AND_ERR[i]);
                    
                    // build a always false AND query.  Result should be different to prove the SQL works.
                    if (resBodyANDErr.compareTo(mResBodyNormal) != 0) {
                        getKb().add(msg.getRequestHeader().getURI(), "sql/and", new Boolean(true));
                        bingo(Alert.RISK_HIGH, Alert.WARNING, displayURI, bingoQuery, "", msg);
                        return;
                    } else {
                        // OR check is used to figure out if there is any diffrence if a AND query return nothing
                        newQuery = setParameter(msg, param, value + SQL_OR[i]);
                        sendAndReceive(msg);
                        resBodyOR = stripOff(msg.getResponseBody().toString(), SQL_OR[i]);
                        
                        if (resBodyOR.compareTo(mResBodyNormal) != 0) {
                            getKb().add(msg.getRequestHeader().getURI(), "sql/or", new Boolean(true));
                            bingo(Alert.RISK_HIGH, Alert.WARNING, displayURI, newQuery, "", msg);
                            return;    
                        }
                    }
                }
                
            }
        }

		if (getKb().getBoolean(msg.getRequestHeader().getURI(), "sql/mssql")) {
		    return;
		}
		
		// try BLIND SQL SELECT using timing 
		newQuery = setParameter(msg, param, value + SQL_DELAY_1);
		sendAndReceive(msg);
				
		if (checkTimeResult(msg, newQuery, defaultTimeUsed, msg.getTimeElapsedMillis())) {
			return;
		}

		newQuery = setParameter(msg, param, value + SQL_DELAY_2);
		sendAndReceive(msg);
				
		if (checkTimeResult(msg, newQuery, defaultTimeUsed, msg.getTimeElapsedMillis())) {
			return;
		}

		// try BLIND MSSQL INSERT using timing
		
		testBlindINSERT(msg, param, value);
		
	}
	
	private void testBlindINSERT(HttpMessage msg, String param, String value) throws HttpException, IOException {

	    String bingoQuery = null;
		String displayURI = null;
		String newQuery = null;
		
		String resBody = null;
		String resBodyErr = null;
		
		int pos = 0;
		long defaultTimeUsed = 0;

		int TRY_COUNT = 5;
		StringBuffer sbInsertValue = new StringBuffer();

		/*	below code is useless because insert can be detected by next section.
			If not, it's likely non MS-SQL and no multiple statement is allowed.
		// try insert using comparison

		for (int i=0; i<TRY_COUNT; i++) {
			// guess at most TRY_COUNT times.
			
			if (i>0) {
				sbInsertValue.append(",'0'");
			} else {
				
				// check if a known to be error response returned a page different from a normal response
				// if so, allow testing to proceed
				newQuery = insertQuery(query, insertPos, paramKey + "=" + paramValue
					+ "'" + SQL_BLIND_INSERT);
				createMessage(isBody, newQuery);
				sendAndReceive();
				resBody = getResponseBody().toString();
				if (resBody.compareTo(mResBodyNormal) == 0) {
					break;
				}
	
				try {
					long tmp = Long.parseLong(paramValue);
					newQuery = insertQuery(query, insertPos, paramKey + "=" + paramValue
						+ SQL_BLIND_INSERT);
					createMessage(isBody, newQuery);
					sendAndReceive();
					resBody = getResponseBody().toString();
					if (resBody.compareTo(mResBodyNormal) == 0) {
						break;
					}

				} catch (NumberFormatException e) {
				}
				
				// do not test for no length added case
				continue;
			}
				
			// try INSERT lengthened.  If for some return returned normal response,
			// that is a sign of success
			newQuery = insertQuery(query, insertPos, paramKey + "=" + paramValue
				+ "'" + sbInsertValue.toString() + SQL_BLIND_INSERT);
			createMessage(isBody, newQuery);
			lastTime = System.currentTimeMillis();
			System.out.println(newQuery);

			sendAndReceive();
			timeUsed = System.currentTimeMillis() - lastTime;

			resBody = getResponseBody().toString();
			if (resBody.compareTo(mResBodyNormal) == 0) {
				bingo(20001, AlertItem.RISK_HIGH, AlertItem.WARNING, displayURI, newQuery, "");
				return;
				
			}

			// no need to try following if not a value integer
			try {
				long tmp = Long.parseLong(paramValue);
			} catch (NumberFormatException e) {
				continue;
			}
			newQuery = insertQuery(query, insertPos, paramKey + "=" + paramValue
				+ sbInsertValue.toString() + SQL_BLIND_INSERT);
			System.out.println(newQuery);
			createMessage(isBody, newQuery);
			lastTime = System.currentTimeMillis();
			sendAndReceive();
			timeUsed = System.currentTimeMillis() - lastTime;
		
			if (resBody.compareTo(mResBodyNormal) == 0) {
				bingo(20001, AlertItem.RISK_HIGH, AlertItem.WARNING, displayURI, newQuery, "");
				return;
				
			}
			
		}
		*/		
		

		// try insert param using INSERT and timing
		sbInsertValue = new StringBuffer();
		for (int i=0; i<TRY_COUNT; i++) {
			
			if (i>0) {
				sbInsertValue.append(",'0'");
			}
			
			// try INSERT
			newQuery = setParameter(msg, param, value + "'" + sbInsertValue.toString() + SQL_BLIND_MS_INSERT);

			sendAndReceive(msg);

			if (checkTimeResult(msg, newQuery, defaultTimeUsed, msg.getTimeElapsedMillis())) {
			    getKb().add(msg.getRequestHeader().getURI(), "sql/mssql", new Boolean(true));
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
		
			if (checkTimeResult(msg, newQuery, defaultTimeUsed, msg.getTimeElapsedMillis())) {
			    getKb().add(msg.getRequestHeader().getURI(), "sql/mssql", new Boolean(true));			    
				return;
			}
			
		}

	}


	private boolean checkANDResult(HttpMessage msg, String query) {

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
			bingo(Alert.RISK_HIGH, Alert.WARNING, null, query, sb.toString(), msg);
			return true;
		} else if (matchBodyPattern(msg, patternErrorGeneric, sb)) {
			// check for other sql error (JDBC) etc.  Suspicious.
			bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, query, sb.toString(), msg);
			return true;
		}
		
		return false;
		
	}

	
	private boolean checkTimeResult(HttpMessage msg, String query, long defaultTimeUsed, long timeUsed) {

		boolean result = checkANDResult(msg, query);
		if (result) {
			return result;
		}


		if (timeUsed > defaultTimeUsed + TIME_SPREAD - 500) {		
			// allow 500ms discrepancy
			bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, query, "", msg);
			return true;
		}			
		return false;
	}

}
