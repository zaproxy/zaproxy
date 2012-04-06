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
package org.parosproxy.paros.core.scanner;

import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

/**
 *
 * Knowledge base records the properties or result found during a scan.
 * It is mainly used to share result among plugin when dependency arise.
 * 
 * There are 2 types of Kb:
 * 1. key = name.  result = value.  This represents kb applicable over the entire host.
 * 2. key = name and url (path without query).  result = value.  This represents kb applicable for specific path only.
 */
public class Kb {

    // KB related
    private TreeMap mapKb = new TreeMap();
    private TreeMap mapURI = new TreeMap();
    
	/**
	 * Get a list of the values matching the key.
	 * @param key
	 * @return null if there is no previous values.
	 */
	public synchronized Vector getList(String key) {
	    return getList(mapKb, key);
	    
	}
	
	/**
	 * Add the key value pair to KB.  Only unique value will be added to KB.
	 * 
	 * @param key
	 * @param value
	 */
	public synchronized void add(String key, Object value) {
	    add(mapKb, key, value);
	}
	
	public synchronized Object get(String key) {
	    Vector v = getList(key);
	    if (v == null || v.size() == 0) {
	        return null;
	        
	    } else {
	        return v.get(0);
	    }
	}
    

	/**
	 * Get the first item in KB matching the key as a String.
	 * @param key
	 * @return null if not found or the object is not a String.
	 */
	/**
	 * @param key
	 * @return
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
	    uri = (URI) uri.clone();
	    String uriKey = uri.toString();
	    TreeMap map = null;
	    try {
            uri.setQuery(null);
        } catch (URIException e) {
            return;
        }
	    Object obj = mapURI.get(uriKey);
	    if (obj == null) {
	        map = new TreeMap();
	        mapURI.put(uriKey, map);
	    } else {
	        map = (TreeMap) obj;
	    }
	    
	    add(map, key, value);
	}
	
	public synchronized Vector getList(URI uri, String key) {
	    uri = (URI) uri.clone();
	    String uriKey = uri.toString();
	    TreeMap map = null;
	    try {
            uri.setQuery(null);
        } catch (URIException e) {
            return null;
        }

        Object obj = mapURI.get(uriKey);
	    if (obj != null && obj instanceof TreeMap) {
	        map = (TreeMap) obj;
	    } else {
	        return null;
	    }
	    return getList(map, key);
	}
	
	public synchronized Object get(URI uri, String key) {
	    Vector v = getList(uri, key);
	    if (v == null || v.size() == 0) {
	        return null;
	        
	    } else {
	        return v.get(0);
	    }
	    
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
	 * @param map
	 * @param key
	 * @param value
	 */
	private void add(TreeMap map, String key, Object value) {
	    Vector v = getList(map, key);
	    if (v == null) {
	        v = new Vector();
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
	 * @param map
	 * @param key
	 * @param value
	 */
	private Vector getList(TreeMap map, String key) {
	    Vector result = null;
	    Object obj = null;
	    synchronized (map) {
	        obj = map.get(key);
	    }
	    
	    if (obj != null && obj instanceof Vector) {
	        return (Vector) obj;
	    }
	    return null;
	    
	}
	
}
