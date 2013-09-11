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
// ZAP: 2012/04/25 Added type arguments to the generic types, removed an 
// unnecessary cast and changed to use the method Integer.valueOf.
// ZAP: 2012/11/20 Issue 419: Restructure jar loading code
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.extension.filter;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.zaproxy.zap.control.ExtensionFactory;


public class FilterFactory {

    private static Logger log = Logger.getLogger(FilterFactory.class);

    // ZAP: Added the type arguments.
    private static TreeMap<Integer, Filter> mapAllFilter = new TreeMap<>();
    // ZAP: Added the type arguments.
    private Vector<Filter> listAllFilter = new Vector<>();

    public void loadAllFilter() {
       	List<Filter> listFilter = ExtensionFactory.getAddOnLoader().getImplementors(
       					"org.parosproxy.paros.extension.filter", Filter.class);

        synchronized (mapAllFilter) {
            
            mapAllFilter.clear();
            for (int i=0; i<listFilter.size(); i++) {
            	// ZAP: Removed unnecessary cast.
                Filter filter = listFilter.get(i);
                filter.setEnabled(false);
                log.info("loaded filter " + filter.getName());
                // ZAP: Changed to use Integer.valueOf.
                mapAllFilter.put(Integer.valueOf(filter.getId()), filter);
               
            }
            // ZAP: Added the type argument.
            Iterator<Filter> iterator = mapAllFilter.values().iterator();
            while (iterator.hasNext()) {
                listAllFilter.add(iterator.next());
            }
        }
                
    }
    
    // ZAP: Added the type argument.
    public List<Filter> getAllFilter() {
        return listAllFilter;
    }
}
