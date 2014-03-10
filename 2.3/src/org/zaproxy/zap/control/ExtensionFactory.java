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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.zaproxy.zap.extension.help.ExtensionHelp;

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
					new File(Constant.getZapInstall(), Constant.FOLDER_PLUGIN),
					new File(Constant.getZapHome(), Constant.FOLDER_PLUGIN)});
    	}
    	return addOnLoader;
    }

    public static synchronized void loadAllExtension(ExtensionLoader extensionLoader, Configuration config) {
       	List<Extension> listExts = getAddOnLoader().getImplementors("org.zaproxy.zap.extension", Extension.class);
		listExts.addAll(getAddOnLoader().getImplementors("org.parosproxy.paros.extension", Extension.class));

        synchronized (mapAllExtension) {
            
            mapAllExtension.clear();
            for (int i=0; i<listExts.size(); i++) {
                Extension extension = listExts.get(i);
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
            	}
            	loadMessagesAndAddExtension(extensionLoader, ext);
            }
            // And then the unordered ones
            for (Extension ext : unorderedExtensions) {
            	if (ext.isEnabled()) {
            		log.debug("Unordered extension " + ext.getName());
            	}
            	loadMessagesAndAddExtension(extensionLoader, ext);
            }
        }
    }

    /**
     * Loads the messages of the {@code extension} and, if enabled, adds it to the {@code extensionLoader} and loads the
     * extension's help set.
     * 
     * @param extensionLoader the extension loader
     * @param extension the extension
     * @see #loadMessages(Extension)
     * @see ExtensionLoader#addExtension(Extension)
     * @see #intitializeHelpSet(Extension)
     */
    private static void loadMessagesAndAddExtension(ExtensionLoader extensionLoader, Extension extension) {
        loadMessages(extension);
        if (extension.isEnabled()) {
            extensionLoader.addExtension(extension);
            intitializeHelpSet(extension);
        }
    }

    public static synchronized List<Extension> loadAddOnExtensions(ExtensionLoader extensionLoader, Configuration config, AddOn addOn) {
       	List<Extension> listExts = getAddOnLoader().getImplementors(addOn, "org.zaproxy.zap.extension", Extension.class);
		listExts.addAll(getAddOnLoader().getImplementors(addOn, "org.parosproxy.paros.extension", Extension.class));

        synchronized (mapAllExtension) {
            
            for (Extension extension : listExts) {
                if (mapAllExtension.containsKey(extension.getName())) {
                	if (mapAllExtension.get(extension.getName()).getClass().equals(extension.getClass())) {
                		// Same name, same class cant currently replace exts already loaded
                    	log.debug("Duplicate extension: " + extension.getName() + " " + 
                    			extension.getClass().getCanonicalName());
                        extension.setEnabled(false);
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
                // Order actually irrelevant when adding an addon;)
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
            for (Extension ext : listExts) {
            	if (ext.isEnabled()) {
            		log.debug("Adding new extension " + ext.getName());
            	}
            	loadMessagesAndAddExtension(extensionLoader, ext);
            }
        }
        return listExts;
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

    /**
	 * If there are help files within the extension, they are loaded and merged
	 * with existing help files.
	 */
    private static void intitializeHelpSet(Extension ext) {
		URL helpSetUrl = getExtensionHelpSetUrl(ext);
		if (helpSetUrl != null) {
			try {
				log.debug("Load help files for extension '" + ext.getName() + "' and merge with core help.");
				HelpSet extHs = new HelpSet(ext.getClass().getClassLoader(), helpSetUrl);
				HelpBroker hb = ExtensionHelp.getHelpBroker();
				hb.getHelpSet().add(extHs);
			} catch (HelpSetException e) {
				log.error("An error occured while adding help file of extension '" + ext.getName() + "': " + e.getMessage(), e);
			}
		}
	}
    
    private static URL getExtensionHelpSetUrl(Extension extension) {
        String helpSetLocation = extension.getClass().getPackage().getName().replaceAll("\\.", "/") + "/resource/help/helpset.hs";
        return HelpSet.findHelpSet(extension.getClass().getClassLoader(), helpSetLocation);
    }
    
	public static List<Extension> getAllExtensions() {
        return listAllExtension;
    }
    
    public static Extension getExtension(String name) {
        Extension test = mapAllExtension.get(name);
        return test;
    }
    
    public static void unloadAddOnExtension(Extension extension) {
        synchronized (mapAllExtension) {
            unloadMessages(extension);
            unloadHelpSet(extension);
            
            mapAllExtension.remove(extension.getName());
            listAllExtension.remove(extension);
            boolean isUnordered = true;
            for (Iterator<Extension> it = mapOrderToExtension.values().iterator(); it.hasNext();) {
                if (it.next() == extension) {
                    it.remove();
                    isUnordered = false;
                    break;
                }
            }
            if (isUnordered) {
                unorderedExtensions.remove(extension);
            }
        }
    }
    
    private static void unloadMessages(Extension extension) {
        ResourceBundle msg = extension.getMessages();
        if (msg != null) {
            Constant.messages.removeMessageBundle(extension.getI18nPrefix());
        }
    }
    
    private static void unloadHelpSet(Extension ext) {
        URL helpSetUrl = getExtensionHelpSetUrl(ext);
        if (helpSetUrl != null) {
            HelpSet baseHelpSet = ExtensionHelp.getHelpBroker().getHelpSet();
            Enumeration<?> helpSets = baseHelpSet.getHelpSets();
            while (helpSets.hasMoreElements()) {
                HelpSet extensionHelpSet = (HelpSet) helpSets.nextElement();
                if (helpSetUrl.equals(extensionHelpSet.getHelpSetURL())) {
                    baseHelpSet.remove(extensionHelpSet);
                    break;
                }
            }
        }
    }
    
}
