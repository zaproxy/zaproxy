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

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.DynamicLoader;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterFactory {

    private static Log log = LogFactory.getLog(FilterFactory.class);

    private DynamicLoader loader = null;
    private static TreeMap mapAllFilter = new TreeMap();
    private Vector listAllFilter = new Vector();

    public void loadAllFilter() {
        if (loader == null) {
            loader = new DynamicLoader(Constant.FOLDER_FILTER, "org.parosproxy.paros.extension.filter");
        }
        
        List listFilter = loader.getFilteredObject(Filter.class);

        synchronized (mapAllFilter) {
            
            mapAllFilter.clear();
            for (int i=0; i<listFilter.size(); i++) {
                Filter filter = (Filter) listFilter.get(i);
                filter.setEnabled(false);
                log.info("loaded filter " + filter.getName());
                mapAllFilter.put(new Integer(filter.getId()), filter);
               
            }
            Iterator iterator = mapAllFilter.values().iterator();
            while (iterator.hasNext()) {
                listAllFilter.add(iterator.next());
            }
        }
                
    }
    
    public List getAllFilter() {
        return listAllFilter;
    }
}
