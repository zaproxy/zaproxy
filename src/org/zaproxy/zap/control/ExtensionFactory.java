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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;

public class ExtensionFactory {

    private static Logger log = Logger.getLogger(ExtensionFactory.class);

    private static Vector<Extension> listAllExtension = new Vector<>();
    private static TreeMap<String, Extension> mapAllExtension = new TreeMap<>();
    private static TreeMap<Integer, Extension> mapOrderToExtension = new TreeMap<>();
    private static List<Extension> unorderedExtensions = new ArrayList<>();
    
    private static AddOnLoader addOnLoader = null;
    
    /**
     * 
     */
    public ExtensionFactory() {
        super();
    }
    
    public static AddOnLoader getAddOnLoader() {
    	if (addOnLoader == null) {
    		addOnLoader = new AddOnLoader(new File[] {
					new File(Constant.FOLDER_PLUGIN),
					new File(Constant.getZapHome(), Constant.FOLDER_PLUGIN)});
    	}
    	return addOnLoader;
    }
    
    public synchronized static void loadAllExtension(ExtensionLoader extensionLoader, Configuration config) {
       	List<Extension> listTest = getAddOnLoader().getImplementors("org.zaproxy.zap.extension", Extension.class);
		listTest.addAll(getAddOnLoader().getImplementors("org.parosproxy.paros.extension", Extension.class));

        synchronized (mapAllExtension) {
            
            mapAllExtension.clear();
            for (int i=0; i<listTest.size(); i++) {
                Extension extension = listTest.get(i);
                if (mapAllExtension.containsKey(extension.getName())) {
                	if (mapAllExtension.get(extension.getName()).getClass().equals(extension.getClass())) {
                		// Same name, same class so ignore
                    	log.error("Duplicate extension: " + extension.getName() + " " + 
                    			extension.getClass().getCanonicalName());
                		continue;
                	} else {
                		// Same name but different class, log but still load it
                    	log.error("Duplicate extension name: " + extension.getName() + " " + 
                    			extension.getClass().getCanonicalName() +
                    			" " + mapAllExtension.get(extension.getName()).getClass().getCanonicalName());
                	}
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
            	Integer order = iter.next();
            	Extension ext = mapOrderToExtension.get(order);
            	if (ext.isEnabled()) {
            		log.debug("Ordered extension " + order + " " + ext.getName());
            		extensionLoader.addExtension(ext);
            		loadMessages(ext);
            	}
            }
            // And then the unordered ones
            for (Extension ext : unorderedExtensions) {
            	if (ext.isEnabled()) {
            		log.debug("Unordered extension " + ext.getName());
            		extensionLoader.addExtension(ext);
            		loadMessages(ext);
            	}
            }
        }
    }
    
    private static void loadMessages(Extension ext) {
    	// Try to load a message bundle in the same package as the extension 
		String name = ext.getClass().getPackage().getName() + "." + Constant.MESSAGES_PREFIX;
		try {
			ResourceBundle msg = ResourceBundle.getBundle(name, Constant.getLocale(), ext.getClass().getClassLoader());
			ext.setMessages(msg);
			Constant.messages.addMessageBundle(ext.getI18nPrefix(), ext.getMessages());
		} catch (Exception e) {
			// Ignore - it will be using the standard message bundle
		}
    }
    
	public static List<Extension> getAllExtensions() {
        return listAllExtension;
    }
    
    public static Extension getExtension(String name) {
        Extension test = mapAllExtension.get(name);
        return test;
    }
    
}
