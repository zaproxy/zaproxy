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
// ZAP: 2011/08/30 Support for scanner levels
// ZAP: 2012/04/23 Changed the method loadAllPlugin to reflect the changes made
// in the method DynamicLoader.getFilteredObject(Class).

package org.parosproxy.paros.core.scanner;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.DynamicLoader;

public class PluginFactory {

    private static Logger log = Logger.getLogger(PluginFactory.class);

    private static Vector<Plugin> listAllPlugin = new Vector<Plugin>();
    private static TreeMap<Integer, Plugin> mapAllPlugin = new TreeMap<Integer, Plugin>();
    private static TreeMap<String, Plugin> mapAllPluginOrderCodeName = new TreeMap<String, Plugin>();
    private static DynamicLoader parosLoader = null;
    private static DynamicLoader zapLoader = null;
    private Vector<Plugin> listPending = new Vector<Plugin>();
    private Vector<Plugin> listRunning = new Vector<Plugin>();
    private Vector<Plugin> listCompleted = new Vector<Plugin>();
    private int	totalPluginToRun = 0;
    
    /**
     * 
     */
    public PluginFactory() {
        super();
        Iterator<Plugin> iterator = null;
        Plugin plugin = null;
        synchronized (mapAllPlugin) {

            // pass 1 - enable all plugin's dependency
            iterator = mapAllPlugin.values().iterator();
            while (iterator.hasNext()) {
                plugin = (Plugin) iterator.next();
                if (plugin.isEnabled()) {
                    enableDependency(plugin);
                }
            }

            // pass 2 - put enabled dependency in listPending
            iterator = mapAllPlugin.values().iterator();
            while (iterator.hasNext()) {
                plugin = (Plugin) iterator.next();
                if (plugin.isEnabled()) {
                    listPending.add(plugin);
                }
            }
            totalPluginToRun = listPending.size();
        }
    }
    
    private void enableDependency(Plugin plugin) {
        
        String[] dependency = plugin.getDependency();
        if (dependency == null) {
            return;
        }
        for (int i=0; i<dependency.length; i++) {
            try {
                Object obj = mapAllPluginOrderCodeName.get(dependency[i]);
                if (obj == null) continue;
                Plugin p = (Plugin) obj;
                p.setEnabled(true);
                enableDependency(p);

            } catch (Exception e) {}
        } 
        
    }

    public synchronized static void loadAllPlugin(Configuration config) {
        if (parosLoader == null) {
        	parosLoader = new DynamicLoader(Constant.FOLDER_PLUGIN, "org.parosproxy.paros.core.scanner.plugin");
        }
        if (zapLoader == null) {
        	zapLoader = new DynamicLoader(Constant.FOLDER_PLUGIN, "org.zaproxy.zap.scanner.plugin");
        }
        List<AbstractPlugin> listTest = parosLoader.getFilteredObject(AbstractPlugin.class);
        listTest.addAll(zapLoader.getFilteredObject(AbstractPlugin.class));

        synchronized (mapAllPlugin) {
            
            mapAllPlugin.clear();
            for (int i=0; i<listTest.size(); i++) {
                Plugin plugin = listTest.get(i);
                plugin.setConfig(config);
                plugin.createParamIfNotExist();
                if (!plugin.isVisible()) {
					log.info("Plugin " + plugin.getName() + " not visible");
                    continue;
                }
                if (plugin.isDepreciated()) {
                	// ZAP: ignore all depreciated plugins
					log.info("Plugin " + plugin.getName() + " depricated");
                	continue;
                }
                log.info("loaded plugin " + plugin.getName());
                if (mapAllPlugin.get(new Integer(plugin.getId())) != null) {
                	System.out.println("Duplicate id " + plugin.getName() + " " +
                			mapAllPlugin.get(new Integer(plugin.getId())).getName());
                }
                mapAllPlugin.put(new Integer(plugin.getId()), plugin);
                mapAllPluginOrderCodeName.put(plugin.getCodeName(), plugin);
            }
            Iterator<Plugin> iterator = mapAllPlugin.values().iterator();
            while (iterator.hasNext()) {
                listAllPlugin.add(iterator.next());
            }
        }
                
    }
    
    public static List<Plugin> getAllPlugin() {
        return listAllPlugin;
    }
    
    public static Plugin getPlugin(int id) {
        Plugin test = (Plugin) mapAllPlugin.get(new Integer(id));
        return test;
    }
    
    public static void setAllPluginEnabled(boolean enabled) {
        for (int i=0; i<listAllPlugin.size(); i++) {
            Plugin plugin = (Plugin) listAllPlugin.get(i);
            plugin.setEnabled(enabled);
        }
    }
    
    synchronized boolean existPluginToRun() {
        if (probeNextPlugin() != null) {
            return true;
        }

        // no test ready to run.  still exist test if due to dependency
        if (!listPending.isEmpty() || !listRunning.isEmpty()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get next test ready to be run.  Null = none.
     * Test dependendent on others will not be obtained.
     * @return
     */
    private Plugin probeNextPlugin() {
        Plugin plugin = null;
        int i=0;
        while (plugin == null && i<listPending.size()) {
            plugin = (Plugin) listPending.get(i);
            if (isAllDependencyCompleted(plugin)) {
                return plugin;
            }
            i++;
        }
        return null;        
    }
    
    /**
     * Get next plugin ready to be run without any dependency outstanding.
     * @return new instance of next plugin to be run.
     */
    synchronized Plugin nextPlugin() {
        Plugin plugin = null;

        plugin = probeNextPlugin();
        if (plugin == null) {
            return null;
        }
        listPending.remove(plugin);
        listRunning.add(plugin);
        
        return plugin;
    }
    
    private boolean isAllDependencyCompleted(Plugin plugin) {
        
        // note the plugin object checked may not be the exact plugin object stored in the completed list.
        // but the comparison is basing on pluginId (see equals method) so it will work.
        String[] dependency = plugin.getDependency();
        if (dependency == null) {
            return true;
        }
        synchronized(listCompleted) {
            for (int i=0; i<dependency.length; i++) {
                boolean isFound = false;
                for (int j=0; j<listCompleted.size() && !isFound; j++) {
                    Plugin completed = (Plugin) listCompleted.get(j);
                    if (completed.getCodeName().equalsIgnoreCase(dependency[i])) {
                        isFound = true;
                    }
                }
                
                if (!isFound) {
                    return false;
                }
            }
            
        }
        return true;
    }

    synchronized void setRunningPluginCompleted(Plugin plugin) {
        listRunning.remove(plugin);
        listCompleted.add(plugin);
    }
    
    int totalPluginToRun() {
        return totalPluginToRun;
    }
    
    int totalPluginCompleted() {
        return listCompleted.size();
    }
}
