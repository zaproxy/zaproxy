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

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpResponseHeader;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterDetectSetCookie extends FilterAdaptor {

    private static final String DELIM = "\t";   
    private static final String CRLF = "\r\n";
    
    private	Pattern pattern = Pattern.compile("^ *"+ "Set-[Cc]ookie" + " *: *([^\\r\\n]*)" + "\\r\\n", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private Matcher matcher = null;
    private Vector result = null; 

    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.AbstractFilter#getId()
     */
    public int getId() {
        return 110;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.AbstractFilter#getName()
     */
    public String getName() {
        return "Detect and alert 'Set-cookie' attempt in HTTP response for modification.";
        
    }

    public void init() {
     	
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpRequestSend(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpRequestSend(HttpMessage msg) {
       }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.filter.FilterAdaptor#onHttpResponseReceive(org.parosproxy.paros.network.HttpMessage)
     */
    public void onHttpResponseReceive(HttpMessage msg) {
        HttpResponseHeader resHeader = msg.getResponseHeader();
        if (resHeader == null || resHeader.isEmpty()) {
            return;
        }
        
        if (resHeader.getHeader("Set-cookie") != null) {
            String content = resHeader.toString();
            matcher = pattern.matcher(content);
            result = new Vector();
            while (matcher.find()){
                String cookie = matcher.group(1);
                if (cookie != null){
                    
                    // Modal dialog with OK/cancel and a text field
                    getView().getMainFrame().toFront();
                    String text = JOptionPane.showInputDialog(getView().getMainFrame(), "Accept the following cookie (Ok=Accept, Cancel=Reject)?", cookie);
                    
                    // text == null means cancel pressed.  If cancel, cookie rejected 
                    if ((text != null && !text.equals(""))){            
                        resHeader.setHeader("Set-cookie", null);          
                        result.add(text);
                    } else if (text==null) {
                        resHeader.setHeader("Set-cookie", null);
                    }
                }
            }
            
            if (result.size() >0){
                content = matcher.replaceAll("");
                
                for (int i = 0;i< result.size(); i++){
                    content += "Set-Cookie: " + result.get(i).toString() + "\r\n";
                }
                try{
                    //	resHeader = new HttpResponseHeader(content);
                    resHeader.setMessage(content);
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            
            result.clear();  		
            
        }
    }
}
    

