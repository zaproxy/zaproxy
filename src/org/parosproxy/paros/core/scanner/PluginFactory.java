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

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.DynamicLoader;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PluginFactory {

    private static Log log = LogFactory.getLog(PluginFactory.class);

    private static Vector listAllPlugin = new Vector();
    private static TreeMap mapAllPlugin = new TreeMap();
    private static TreeMap mapAllPluginOrderCodeName = new TreeMap();
    private static DynamicLoader loader = null;
    private Vector listPending = new Vector();
    private Vector listRunning = new Vector();
    private Vector listCompleted = new Vector();
    private int	totalPluginToRun = 0;
    
    /**
     * 
     */
    public PluginFactory() {
        super();
        Iterator iterator = null;
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
        if (loader == null) {
            loader = new DynamicLoader(Constant.FOLDER_PLUGIN, "org.parosproxy.paros.core.scanner.plugin");
        }
        List listTest = loader.getFilteredObject(AbstractPlugin.class);

        synchronized (mapAllPlugin) {
            
            mapAllPlugin.clear();
            for (int i=0; i<listTest.size(); i++) {
                Plugin plugin = (Plugin) listTest.get(i);
                plugin.setConfig(config);
                plugin.createParamIfNotExist();
                if (!plugin.isVisible()) {
                    continue;
                }
//                plugin.setEnabled(true);
                log.info("loaded plugin " + plugin.getName());
                mapAllPlugin.put(new Integer(plugin.getId()), plugin);
                mapAllPluginOrderCodeName.put(plugin.getCodeName(), plugin);
            }
            Iterator iterator = mapAllPlugin.values().iterator();
            while (iterator.hasNext()) {
                listAllPlugin.add(iterator.next());
            }
        }
                
    }
    
    public static List getAllPlugin() {
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
