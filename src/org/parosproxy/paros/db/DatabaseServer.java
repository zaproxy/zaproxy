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

package org.parosproxy.paros.db;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.hsqldb.Server;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DatabaseServer {

    public static final int DEFAULT_SERVER_PORT = 9001;
    
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
	
//	private void start(String dbname) throws ClassNotFoundException, Exception{
//	    // hsqldb only accept '/' as path;
//	    dbname = dbname.replaceAll("\\\\", "/");
//	    String serverProps1 = "database.0=file:" + dbname;
//
//	    int port = DEFAULT_SERVER_PORT;
//	    
//	    mUrl         = "jdbc:hsqldb:hsql://localhost";
//		mServer      = new Server();
//        mServer.setAddress("localhost");
//			
//		mServer.putPropertiesFromString(serverProps1);
//		
//		mServer.setLogWriter(null);
//		mServer.setErrWriter(null);
//		Class.forName("org.hsqldb.jdbcDriver");
//
//		// try at most 10 times if there is a server port
//		
//		for (int i=0; i<10;i++) {
//		    ServerSocket socket = null;
//		    try {
//		        socket = new ServerSocket(port);
//		        socket.close();
//		        mServer.setPort(port);
//		        break;
//		    } catch (IOException e) {
//		        if (port < 65535) {
//		            port++;
//		        }
//		        
//		    }
//		}
//		    
//	    mServer.start();
//        if (mServer.getPort() != 9001) {
//            mUrl = mUrl + ":" + mServer.getPort();
//            //System.out.println("db server port:" + mServer.getPort());
//            
//        }
//
//		
//	}

    private void start(String dbname) throws ClassNotFoundException, Exception{
        // hsqldb only accept '/' as path;
        dbname = dbname.replaceAll("\\\\", "/");
        
        mUrl         = "jdbc:hsqldb:file:" + dbname;
        
        Class.forName("org.hsqldb.jdbcDriver");

        mConn = DriverManager.getConnection(mUrl, mUser, mPassword);
        

        
    }
    
//	void shutdown(boolean compact) throws SQLException {
//		Connection conn = getSingletonConnection();
//		//CallableStatement psCompact = mConn.prepareCall("SHUTDOWN COMPACT");
//		CallableStatement psCompact = null;
//		
//		if (compact) {
//		    // db is not new and useful for future.  Compact it.
//		    psCompact = conn.prepareCall("SHUTDOWN COMPACT");
//
//		} else {
//		    // new need to compact database.  just shutdown.
//		    psCompact = conn.prepareCall("SHUTDOWN");
//
//		}
//		
//		psCompact.execute();
//		mServer.signalCloseAllServerConnections();
//		mServer.stop();
//
//		boolean retry = true;
//		for (int i=0; i<20 && retry; i++) {
//		    try {
//		        mServer.checkRunning(false);
//		        retry = false;
//		    } catch (RuntimeException e) {
//		        try {
//		            Thread.sleep(200);
//		        } catch (InterruptedException e1) {
//		            e1.printStackTrace();
//		        }
//		    }
//		}
//		mServer = null;
//
//	}
    
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
				e.printStackTrace();
				if (i==4) {
					throw e;
				}
				System.out.println("Recovering " + i + " times.");
			}
			
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				
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
