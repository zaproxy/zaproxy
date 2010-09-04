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
package org.parosproxy.paros.extension.filter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterLogGetQuery extends FilterAdaptor {

    private static final String delim = "\t";   
    private static final String CRLF = "\r\n";
    private File outFile;		    
    private Pattern pSeparator	= Pattern.compile("([^=&]+)[=]([^=&]*)"); 
    private Matcher matcher2;
    private BufferedWriter writer = null;
    private long lastWriteTime = System.currentTimeMillis();
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.AbstractFilter#getId()
     */
    public int getId() {
        return 20;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.AbstractFilter#getName()
     */
    public String getName() {
        return "Log unique GET queries into file (" + getLogFileName() + ")";
        
    }

    public void init() {
     	outFile = new File(getLogFileName());
     	
    }

    protected String getLogFileName() {
        return "filter/get.xls";
    }
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpRequestSend(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpRequestSend(HttpMessage httpMessage) {

        HttpRequestHeader reqHeader = httpMessage.getRequestHeader();
        
        if (reqHeader != null && reqHeader.isText() && !reqHeader.isImage()){
            if (reqHeader.getMethod().equalsIgnoreCase(HttpRequestHeader.GET)){
                try{
                    
                    URI uri = reqHeader.getURI();
                    
                    int pos;
                    
                    String firstline;
                    
                    URI newURI = (URI) uri.clone();
                    String query = newURI.getQuery();
                    if (query != null) {
                        newURI.setQuery(null);
                        firstline = newURI.toString();
                        Hashtable param = parseParameter(query);
                        writeLogFile(firstline,param);
                    } else {
                        firstline = uri.toString();
                        writeLogFile(firstline,null);				
                    }
                    
                    
                    
                }catch(Exception aa){
                    aa.printStackTrace();
                }
            }
            
        }
        
    }
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpResponseReceive(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpResponseReceive(HttpMessage httpMessage) {
        
    }
    
    protected synchronized void writeLogFile(String line, Hashtable param){
        // write to default file
        try{
            
            if (getWriter() != null) {
                
                getWriter().write(line + CRLF);
            }
            
            if (param!=null){
                Enumeration v = param.keys();
                while (v.hasMoreElements()) {
                    String name = (String)v.nextElement();
                    String value = (String)param.get(name);
                    getWriter().write(delim + name + delim + value + CRLF);		        		           
                }    		
            }

            lastWriteTime = System.currentTimeMillis();
            
        }catch(IOException ae){
        }
        
    }
    
    protected Hashtable parseParameter(String param){
        Hashtable table = new Hashtable();
        
        try{	  
            matcher2 = pSeparator.matcher(param);
            while (matcher2.find()){
                // start of a request
                table.put(matcher2.group(1), matcher2.group(2));
                
            }
        }
        catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return table;
        
    }
    
    public synchronized void timer() {
        // 5s elapse and no more write.  close file.
        if (writer != null && System.currentTimeMillis() > lastWriteTime + 5000) {
            try {
                writer.close();
                writer = null;
            } catch (IOException e) {
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
