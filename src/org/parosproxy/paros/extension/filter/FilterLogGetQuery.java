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
// ZAP: 2012/04/25 Added type arguments to generic types, removed unnecessary
// casts, removed unused variable and added @Override annotation to all
// appropriate methods.
// ZAP: 2012/07/29 Corrected init method and log errors
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2016/06/07 Use ZAP's home filter directory

package org.parosproxy.paros.extension.filter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;


public class FilterLogGetQuery extends FilterAdaptor {

    private static final String LOG_FILE = Paths.get(Constant.FOLDER_FILTER, "get.xls").toString();
    private static final String delim = "\t";   
    private static final String CRLF = "\r\n";
    private File outFile;		    
    private Pattern pSeparator	= Pattern.compile("([^=&]+)[=]([^=&]*)"); 
    private Matcher matcher2;
    private BufferedWriter writer = null;
    private long lastWriteTime = System.currentTimeMillis();
    
    private static final Logger logger = Logger.getLogger(FilterLogGetQuery.class);
    
    @Override
    public int getId() {
        return 20;
    }

    @Override
    public String getName() {
        return Constant.messages.getString("filter.loggets.name") + getLogFileName();
        
    }
    
    @Override
    public void init(Model model) {
     	outFile = new File(Constant.getZapHome(), getLogFileName());
    }

    protected String getLogFileName() {
        return LOG_FILE;
    }
    
    @Override
    public void onHttpRequestSend(HttpMessage httpMessage) {

        HttpRequestHeader reqHeader = httpMessage.getRequestHeader();
        
        if (reqHeader != null && reqHeader.isText() && !reqHeader.isImage()){
            if (reqHeader.getMethod().equalsIgnoreCase(HttpRequestHeader.GET)){
                try{
                    
                    URI uri = reqHeader.getURI();
                    
                    // ZAP: Removed unused variable (int pos).
                    
                    String firstline;
                    
                    URI newURI = (URI) uri.clone();
                    String query = newURI.getQuery();
                    if (query != null) {
                        newURI.setQuery(null);
                        firstline = newURI.toString();
                        // ZAP: Added type arguments.
                        Hashtable<String, String> param = parseParameter(query);
                        writeLogFile(firstline,param);
                    } else {
                        firstline = uri.toString();
                        writeLogFile(firstline,null);				
                    }
                    
                } catch (Exception aa){
                	logger.error(aa.getMessage(), aa);
                }
            }
        }
        
    }
    
    @Override
    public void onHttpResponseReceive(HttpMessage httpMessage) {
        
    }
    
    // ZAP: Added type arguments.
    protected synchronized void writeLogFile(String line, Hashtable<String, String> param){
        // write to default file
        try{
            
            if (getWriter() != null) {
                
                getWriter().write(line + CRLF);
            }
            
            if (param!=null){
                // ZAP: Added type argument.
                Enumeration<String> v = param.keys();
                while (v.hasMoreElements()) {
                    // ZAP: Removed unnecessary cast.
                    String name = v.nextElement();
                    // ZAP: Removed unnecessary cast.
                    String value = param.get(name);
                    getWriter().write(delim + name + delim + value + CRLF);		        		           
                }    		
            }

            lastWriteTime = System.currentTimeMillis();
            
        } catch(IOException e){
        	logger.error(e.getMessage(), e);
        }
        
    }
    
    // ZAP: Added type arguments.
    protected Hashtable<String, String> parseParameter(String param){
        // ZAP: Added type arguments.
        Hashtable<String, String> table = new Hashtable<>();
        
        try{	  
            matcher2 = pSeparator.matcher(param);
            while (matcher2.find()){
                // start of a request
                table.put(matcher2.group(1), matcher2.group(2));
                
            }
        } catch(Exception e){
        	logger.error(e.getMessage(), e);
        }
        return table;
        
    }
    
    @Override
    public synchronized void timer() {
        // 5s elapse and no more write.  close file.
        if (writer != null && System.currentTimeMillis() > lastWriteTime + 5000) {
            try {
                writer.close();
                writer = null;
            } catch (IOException e) {
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
