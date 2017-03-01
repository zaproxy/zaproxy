/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The OWASP ZAP Development Team
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

package org.zaproxy.zap.db.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.parosproxy.paros.db.DatabaseServer;


public class SqlDatabaseServer implements DatabaseServer {

    public static final int DEFAULT_SERVER_PORT = 9001;
    
    private static final Logger logger = Logger.getLogger(SqlDatabaseServer.class);
    
	private String  dbUrl = null;
	private String  dbUser = null;
	private String  dbPassword = null;
	private Server  dbServer = null;
	private Connection dbConn = null;

	SqlDatabaseServer(String dbname) throws ClassNotFoundException, Exception {
		start(dbname);
	}
	

    private void start(String dbname) throws ClassNotFoundException, Exception{
        this.setDbUrl(DbSQL.getSingleton().getDbUrl());
        this.setDbUser(DbSQL.getSingleton().getDbUser());
        this.setDbPassword(DbSQL.getSingleton().getDbPassword());
    }
    
    
    void shutdown(boolean compact) throws SQLException {
    	if (dbConn != null) {
    		dbConn.close();
    		dbConn = null;
    	}
    }
	
	
	public Connection getNewConnection() throws SQLException {
		Connection conn = null;
		for (int i=0; i<5; i++) {
			try {
				conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
				return conn;
			} catch (SQLException e) {
			    logger.warn(e.getMessage(), e);
				if (i==4) {
					throw e;
				}
				logger.warn("Recovering " + i + " times.");
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				if (logger.isDebugEnabled()) {
					logger.debug(e.getMessage(), e);
				}
			}
		}
		return conn;
	}
	
	public Connection getSingletonConnection() throws SQLException {
		if (dbConn == null) {
			dbConn = getNewConnection();
		}
		
		return dbConn;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public Server getDbServer() {
		return dbServer;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public void setDbServer(Server dbServer) {
		this.dbServer = dbServer;
	}
	
}
