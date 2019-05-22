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
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods and
// removed unnecessary cast.
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2013/07/12 Issue 713: Add CWE and WASC numbers to issues

package org.parosproxy.paros.core.scanner;

import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * @deprecated No longer used/needed (FilePlugins were replaced with Forced Browse). It will be removed in a future release.
 */
@Deprecated
public abstract class AbstractDefaultFilePlugin extends AbstractHostPlugin {
    
    private static final Logger logger = Logger.getLogger(AbstractDefaultFilePlugin.class);
    
	private static final Pattern patternItems = Pattern.compile(",");
	private static final String[]	SPECIAL_TAG_LIST = {"@cgibin"};
	
	private static final String[]	TAG_REPLACE_LIST = {
		"cgi-bin,cgi-local,htbin,cgi,cgis,cgi-win,bin,scripts"
	};
    

    private URI baseURI = null;    
    private Vector<URI> listURI = new Vector<>();
		
	protected void addTest(String directories, String files) {
		String[] 	dirList = null,
					fileList = null;
		String		dir = "",
					file = "";

		directories = directories.trim();
		files = files.trim();
		for (int i=0; i<SPECIAL_TAG_LIST.length; i++) {
			directories = directories.replaceAll(SPECIAL_TAG_LIST[i], TAG_REPLACE_LIST[i]);
		}

		try {
			dirList = patternItems.split(directories);
			fileList = patternItems.split(files);
			for (int i=0; i<dirList.length; i++) {
				dir = dirList[i].trim();
				if (!dir.startsWith("/")) {
					dir = "/" + dir;
				}

				for (int j=0; j<fileList.length; j++) {
					file = fileList[j].trim();
					try {
					    URI uri = createURI(baseURI, dir, file);
					    listURI.add(uri);
					} catch (URIException eu) {
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private URI createURI(URI base, String dir, String file) throws URIException {
		if (!dir.startsWith("/")) {
			dir = "/" + dir;
		}
		
		if (!file.startsWith("/") && !dir.endsWith("/")) {
			file = "/" + file;
		}
				
		String path = dir + file;
		URI uri = new URI(base, path, true);
		return uri;
	}
    
    /**
     * @return Returns the baseURI.
     */
    public URI getBaseURI() {
        return baseURI;
    }
    /**
     * @return Returns the listURI.
     */
    public Vector<URI> getListURI() {
        return listURI;
    }

    @Override
    public void init() {
        baseURI = getBaseMsg().getRequestHeader().getURI();
	}

    @Override
    public void scan() {
        for (int i=0; i<getListURI().size() && !isStop(); i++) {
            // ZAP: Removed unnecessary cast.
            URI uri = getListURI().get(i);
            HttpMessage msg = getNewMsg();
            try {
                msg.getRequestHeader().setURI(uri);
                msg.getRequestBody().setLength(0);
                sendAndReceive(msg);
                if (isFileExist(msg)) {
                    bingo(Alert.RISK_MEDIUM, Alert.CONFIDENCE_LOW, uri.toString(), "", "", "", "", msg);
                }
            } catch (Exception e) {
            }
        }
    }
}
