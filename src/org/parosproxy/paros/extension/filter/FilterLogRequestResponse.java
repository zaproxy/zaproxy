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
// ZAP: 2011/04/16 i18n
// ZAP: 2012/03/15 Changed the method onHttpResponseReceive to use the class StringBuilder 
// instead of StringBuffer.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2016/06/07 Use ZAP's home filter directory

package org.parosproxy.paros.extension.filter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;


public class FilterLogRequestResponse extends FilterAdaptor {

    private static final String logFile = Paths.get(Constant.FOLDER_FILTER, "message.txt").toString();
    private static final String delim = "====================================";   
    private static final String CRLF = "\r\n";
    private File outFile = new File(Constant.getZapHome(), logFile);
    private BufferedWriter writer = null;
    private long lastWriteTime = System.currentTimeMillis();
    private int counter = 1;
    
    // ZAP Added logger
    private Logger logger = Logger.getLogger(FilterLogRequestResponse.class);
    
    @Override
    public int getId() {
        return 40;
    }

    @Override
    public String getName() {
        return Constant.messages.getString("filter.logreqresp.name") + logFile;
    }

    @Override
    public void onHttpRequestSend(HttpMessage httpMessage) {

    }

    @Override
    public void onHttpResponseReceive(HttpMessage httpMessage) {

        if (!httpMessage.getRequestHeader().isText() || httpMessage.getRequestHeader().isImage() || httpMessage.getResponseHeader().isImage()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(httpMessage.getRequestHeader().toString());
        sb.append(httpMessage.getRequestBody().toString()).append(CRLF);
        
        if (!httpMessage.getResponseHeader().isEmpty()) {
            sb.append(httpMessage.getResponseHeader().toString());
            sb.append(httpMessage.getResponseBody().toString()).append(CRLF);
        }
        
        writeLogFile(sb.toString());
    }
    
    private synchronized void writeLogFile(String line) {
        try{
            
            if (getWriter() != null) {
                getWriter().write("===== " + counter + " " + delim + CRLF);
                getWriter().write(line + CRLF);
                counter++;
            }
            // avoid close file frequently
            //getWriter().close();
        }catch(Exception e){
        	// ZAP: Log the exception
        	logger.error(e.getMessage(), e);
        }
        
    }
    
    @Override
    public synchronized void timer() {
        if (writer != null && System.currentTimeMillis() > lastWriteTime + 5000) {
            // 5s elapse and no more write.  close file.

            try {
                writer.close();
                writer = null;
            } catch (IOException e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            }            
        }
    }
    
    private synchronized BufferedWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new BufferedWriter(new FileWriter(outFile,true));            
        }
        return writer;
    }
    
    

}
