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

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestObsoleteFileExtended extends AbstractAppPlugin {

    private final static String[] staticSuffixList = {
            ".OLD", ".Old",
            ".BAK", ".Bak",
            ".java",
            ".INC", ".Inc"
            //			,".class"
            //			,".backup", ".BACKUP", ".Backup",
            //			".pl", ".c", ".cpp"
    };
    
    private static final String[] staticAppendixList = {
            "~"
    };
    
    private static final Pattern patternNotFound = Pattern.compile("(\\bNot\\sfound\\b)|(\\b404\\b)", PATTERN_PARAM);
    
    public int getId() {
        return 00005;
    }
    
    public String getName() {
        return "Obsolete file extended check";
    }
    
    
    public String[] getDependency() {
        return null;
    }
    
    public String getDescription() {
        return "Miscellenous include files, backup, unused or obsolete files exist as indicated.  If these files contain program source, information such as server logic or ODBC/JDBC user ID and passwords may be revealed since these file extension may not be processed by the web server.";
    }
    
    public String getSolution() {
        return "Remove backup, unused or obsolete files.  For include files, carefully choose the suffix to prevent information disclosure.";
    }
    
    public String getReference() {
        return "";
    }
    public int getCategory() {
        return 0;
    }
    
    public void init() {
        
    }
    
    public void scan() {
        for (int i=0; i<staticSuffixList.length; i++) {
            try {
                testSuffix(staticSuffixList[i], false);
                testSuffix(staticSuffixList[i], true);
                
            } catch (IOException e) {
            }
        }
        
        for (int i=0; i<staticAppendixList.length; i++) {
            try {
                testSuffix(staticAppendixList[i], false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     Test existence of obsolete file with the suffix.
     @param suffix suffix to run scan with.
     @param	replaceSuffix true = replace the suffix for checking.  false = append the suffix.
     */
    private void testSuffix(String suffix, boolean replaceSuffix) throws IOException {
        HttpMessage msg = getNewMsg();
        URI uri = msg.getRequestHeader().getURI();
        String 	path 	= uri.getPath();
        
        if (path == null || path.equals("")) {
            return;
        }
        
        if (replaceSuffix) {
            int pos = path.lastIndexOf(".");
            if (pos > -1) {
                path = path.substring(0, pos);
            }
        }
        
        path = path + suffix;
        
        uri.setPath(path);
        msg.getRequestHeader().setURI(uri);
        
        sendAndReceive(msg);
        
        if (!isFileExist(msg)) {
            return;
        }    		
        
		bingo(Alert.RISK_LOW, Alert.WARNING, uri.toString(), "", "", msg);

        
    }
}
