/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.DynamicLoader;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;

public class ExtensionFactory {

    private static Logger log = Logger.getLogger(ExtensionFactory.class);

    private static Vector<Extension> listAllExtension = new Vector<Extension>();
    private static TreeMap<String, Extension> mapAllExtension = new TreeMap<String, Extension>();
    private static TreeMap<Integer, Extension> mapOrderToExtension = new TreeMap<Integer, Extension>();
    private static List<Extension> unorderedExtensions = new ArrayList<Extension>();
    private static DynamicLoader parosLoader = null;
    private static DynamicLoader zapLoader = null;
    
    /**
     * 
     */
    public ExtensionFactory() {
        super();
    }
    
    public synchronized static void loadAllExtension(ExtensionLoader extensionLoader, Configuration config) {
        if (zapLoader == null) {
        	zapLoader = new DynamicLoader(Constant.FOLDER_EXTENSION, "org.zaproxy.zap.extension", true);
        }
        if (parosLoader == null) {
        	parosLoader = new DynamicLoader(Constant.FOLDER_EXTENSION, "org.parosproxy.paros.extension", true);
        }
        List<Object> listTest = parosLoader.getFilteredObject(Extension.class);
        listTest.addAll(zapLoader.getFilteredObject(Extension.class));

        synchronized (mapAllExtension) {
            
            mapAllExtension.clear();
            for (int i=0; i<listTest.size(); i++) {
                Extension extension = (Extension) listTest.get(i);
                if (mapAllExtension.containsKey(extension.getName())) {
                	log.error("Duplicate extension name: " + extension.getName() + " " + 
                			extension.getClass().getCanonicalName() +
                			" " + mapAllExtension.get(extension.getName()).getClass().getCanonicalName());
                	//continue;
                }
                if (extension.isDepreciated()) {
                	log.debug("Depreciated extension " + extension.getName());
                	continue;
                }
                extension.setEnabled(config.getBoolean("ext." + extension.getName(), true));
                
                listAllExtension.add(extension);
                mapAllExtension.put(extension.getName(), extension);
                
                int order = extension.getOrder();
                if (order == 0) {
                	unorderedExtensions.add(extension);
                } else if (mapOrderToExtension.containsKey(order)) {
                	log.error("Duplicate order " + order + " " + 
                			mapOrderToExtension.get(order).getName() + "/" + mapOrderToExtension.get(order).getClass().getCanonicalName() + 
                			" already registered, " +
                			extension.getName() + "/" +extension.getClass().getCanonicalName() +
                			" will be added as an unordered extension");
                	unorderedExtensions.add(extension);
                } else {
                	mapOrderToExtension.put(order, extension);
                }

            }
            // Add the ordered extensions
            Iterator<Integer> iter = mapOrderToExtension.keySet().iterator();
            while (iter.hasNext()) {
            	int order = iter.next();
            	Extension ext = mapOrderToExtension.get(order);
            	if (ext.isEnabled()) {
            		log.debug("Ordered extension " + order + " " + ext.getName());
            		extensionLoader.addExtension(ext);
            	}
            }
            // And then the unordered ones
            for (Extension ext : unorderedExtensions) {
            	if (ext.isEnabled()) {
            		log.debug("Unordered extension " + ext.getName());
            		extensionLoader.addExtension(ext);
            	}
            }
        }
    }
    
	public static List<Extension> getAllExtensions() {
        return listAllExtension;
    }
    
    public static Extension getExtension(int id) {
        Extension test = (Extension) mapAllExtension.get(new Integer(id));
        return test;
    }
    
}
