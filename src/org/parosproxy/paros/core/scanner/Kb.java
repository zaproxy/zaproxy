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
// ZAP: 2012/04/25 Added type arguments to generic types, removed variables, 
// added logger and other minor changes.
// ZAP: 2012/05/04 Catch CloneNotSupportedException whenever an Uri is cloned,
//              as introduced with version 3.1 of HttpClient
// ZAP: 2016/09/20 JavaDoc tweaks

package org.parosproxy.paros.core.scanner;

import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

/**
 *
 * Knowledge base records the properties or result found during a scan.
 * It is mainly used to share result among plugin when dependency arise.
 * 
 * There are 2 types of Kb:
 * 1. key = name.  result = value.  This represents kb applicable over the entire host.
 * 2. key = url (path without query) and name.  result = value.  This represents kb applicable for specific path only.
 */
public class Kb {

    // ZAP: Added logger.
    private static final Logger logger = Logger.getLogger(Kb.class);

    // KB related
    // ZAP: Added the type arguments.
    private TreeMap<String, Object> mapKb = new TreeMap<>();
    // ZAP: Added the type arguments.
    private TreeMap<String, TreeMap<String, Object>> mapURI = new TreeMap<>();
    
	/**
	 * Get a list of the values matching the key.
	 * @param key the key for the knowledge base list entry
	 * @return null if there is no previous values.
	 */
	// ZAP: Added the type argument.
	public synchronized Vector<Object> getList(String key) {
	    return getList(mapKb, key);
	    
	}
	
	/**
	 * Add the key value pair to KB.  Only unique value will be added to KB.
	 * 
	 * @param key the key for the knowledge base entry
	 * @param value the value of the new entry
	 */
	public synchronized void add(String key, Object value) {
	    add(mapKb, key, value);
	}
	
	public synchronized Object get(String key) {
	    // ZAP: Added the type argument.
	    Vector<Object> v = getList(key);
	    if (v == null || v.size() == 0) {
	        return null;
	        
	    }
	    
	    return v.get(0);
	}
    

	/**
	 * Get the first item in KB matching the key as a String.
	 * @param key the key for the knowledge base entry
	 * @return the entry, or {@code null} if not a {@code String} or does not exist
	 */
	public String getString(String key) {
	    Object obj = get(key);
	    if (obj != null && obj instanceof String) {
		    return (String) obj;
	        
		}
	    return null;
	    
	}
		
	public boolean getBoolean(String key) {
	    Object obj = get(key);
	    if (obj != null && obj instanceof Boolean) {
	        return ((Boolean) obj).booleanValue();
	        
	    }
	    return false;
	    
	}


	public synchronized void add(URI uri, String key, Object value) {
    	// ZAP: catch CloneNotSupportedException as introduced with version 3.1 of HttpClient
	    try {
			uri = (URI) uri.clone();
		} catch (CloneNotSupportedException e1) {
			return;
		}
	    
	    // ZAP: Removed variable (TreeMap map).
	    try {
            uri.setQuery(null);
        } catch (URIException e) {
            // ZAP: Added logging.
            logger.error(e.getMessage(), e);
            return;
        }
	    // ZAP: Moved to after the try catch block.
	    String uriKey = uri.toString();
	    // ZAP: Added the type arguments.
	    TreeMap<String, Object> map = mapURI.get(uriKey);
	    if (map == null) {
	        // ZAP: Added the type argument.
	        map = new TreeMap<>();
	        mapURI.put(uriKey, map);
	    } // ZAP: Removed else branch.
	    
	    add(map, key, value);
	}
	
	public synchronized Vector<Object> getList(URI uri, String key) {
    	// ZAP: catch CloneNotSupportedException as introduced with version 3.1 of HttpClient
	    try {
			uri = (URI) uri.clone();
		} catch (CloneNotSupportedException e1) {
			return null;
		}
	    
	    // ZAP: Removed variable (TreeMap map).
	    try {
            uri.setQuery(null);
        } catch (URIException e) {
            // ZAP: Added logging.
            logger.error(e.getMessage(), e);
            return null;
        }
        // ZAP: Moved to after the try catch block.
	    String uriKey = uri.toString();
	    // ZAP: Added the type argument and removed the instanceof.
        TreeMap<String, Object> map = mapURI.get(uriKey);
	    if (map == null) {
	        return null;
	    } // ZAP: Removed else branch.
	    
	    return getList(map, key);
	}
	
	public synchronized Object get(URI uri, String key) {
	    // ZAP: Added the type argument.
	    Vector<Object> v = getList(uri, key);
	    if (v == null || v.size() == 0) {
	        return null;
	    }
	    
	    return v.get(0);
	}
	public String getString(URI uri, String key) {
	    Object obj = get(uri, key);
	    if (obj != null && obj instanceof String) {
	        return (String) obj;
	    }
	    return null;
	}
	
	public boolean getBoolean(URI uri, String key) {
	    Object obj = get(uri, key);
	    if (obj != null && obj instanceof Boolean) {
	        return ((Boolean) obj).booleanValue();
	    }
	    return false;
	    
	}
	
	/**
	 * Generic method for adding into a map
	 * @param map the map of the knowledge base entries
	 * @param key the key for the knowledge base entry
	 * @param value the value of the entry
	 */
	// ZAP: Added the type arguments.
	private void add(TreeMap<String, Object> map, String key, Object value) {
		// ZAP: Added the type argument.
	    Vector<Object> v = getList(map, key);
	    if (v == null) {
	    	// ZAP: Added the type argument.
	        v = new Vector<>();
	        synchronized (map) {
	            map.put(key, v);
	        }
	    }
	    if (!v.contains(value)) {
	        v.add(value);
	    }

	}

	/**
	 * Generic method for getting values out of a map
	 * @param map the map of the knowledge base entries
	 * @param key the key for the knowledge base entry
	 * @return the values of the entry, might be {@code null}
	 */
	// ZAP: Added the type arguments and @SuppressWarnings annotation.
	@SuppressWarnings("unchecked")
	private Vector<Object> getList(TreeMap<String, Object> map, String key) {
	    Object obj = null;
	    synchronized (map) {
	        obj = map.get(key);
	    }
	    
	    if (obj != null && obj instanceof Vector) {
	    	// ZAP: Added the type argument.
	        return (Vector<Object>) obj;
	    }
	    return null;
	    
	}
	
}
