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
// ZAP: 2012/05/31 Changed to log the exceptions and one message in the method
// getNewConnection().
// ZAP: 2012/08/16 SHUTDOWN COMPACT old databases.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hsqldb.Server;


public class DatabaseServer {

    public static final int DEFAULT_SERVER_PORT = 9001;
    
    // ZAP: Added the class variable.
    private static final Logger logger = Logger.getLogger(DatabaseServer.class);
    
	//  change the url to reflect your preferred db location and name
	//  String url = "jdbc:hsqldb:hsql://localhost/yourtest";
	//String  mServerProps;
	String  mUrl;
	String  mUser     = "sa";
	String  mPassword = "";
	Server  mServer = null;
	Connection mConn = null;
	


	DatabaseServer(String dbname) throws ClassNotFoundException, Exception {
		start(dbname);
	}
	

    private void start(String dbname) throws ClassNotFoundException, Exception{
        // hsqldb only accept '/' as path;
        dbname = dbname.replaceAll("\\\\", "/");
        
    	// ZAP: Check if old database should be compacted
        boolean doCompact = false;
        File propsFile = new File(dbname + ".properties");
        if (propsFile.exists()) {
            Properties dbProps = new Properties();
            InputStream propsStream = null;
            try {
                propsStream = new FileInputStream(propsFile);
                dbProps.load(propsStream);
            } finally {
                try {
                    if (propsStream != null) {
                        propsStream.close();
                    }
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e.getMessage(), e);
                    }
                }
            }
            String version = (String) dbProps.get("version");
            if (version.charAt(0) < '2') {
                // got version < 2.0.0
                // => SHUTDOWN COMPACT database
                // and reconnect again
                doCompact = true;
            }
        }
        
        mUrl         = "jdbc:hsqldb:file:" + dbname;
        
        Class.forName("org.hsqldb.jdbcDriver");

        mConn = DriverManager.getConnection(mUrl, mUser, mPassword);
        
        // ZAP: If old database is in load => shutdown & reconnect
        if (doCompact) {
	    	shutdown(true);
            mConn = DriverManager.getConnection(mUrl, mUser, mPassword);
        }
    }
    
    
    void shutdown(boolean compact) throws SQLException {
        Connection conn = getSingletonConnection();
        //CallableStatement psCompact = mConn.prepareCall("SHUTDOWN COMPACT");
        CallableStatement psCompact = null;
        
        if (compact) {
            // db is not new and useful for future.  Compact it.
            psCompact = conn.prepareCall("SHUTDOWN COMPACT");

        } else {
            // new need to compact database.  just shutdown.
            psCompact = conn.prepareCall("SHUTDOWN");

        }
        
        psCompact.execute();
        psCompact.close();
        mConn.close();
        mConn = null;
    }
	
	
	public Connection getNewConnection() throws SQLException {
		
		
		Connection conn = null;
		for (int i=0; i<5; i++) {
			try {
				conn = DriverManager.getConnection(mUrl, mUser, mPassword);
				return conn;
			} catch (SQLException e) {
				// ZAP: Changed to log the exception.
			    logger.warn(e.getMessage(), e);
				if (i==4) {
					throw e;
				}
				// ZAP: Changed to log the message.
				logger.warn("Recovering " + i + " times.");
			}
			
			try {
				Thread.sleep(500);
				// ZAP: Changed to catch the InterruptedException.
			} catch (InterruptedException e) {
				// ZAP: Added the log.
				if (logger.isDebugEnabled()) {
					logger.debug(e.getMessage(), e);
				}
			}
		}
		return conn;
	}
	
	public Connection getSingletonConnection() throws SQLException {
		if (mConn == null) {
			mConn = getNewConnection();
		}
		
		return mConn;
	}
}
