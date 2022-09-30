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
// ZAP: 2012/04/25 Removed unnecessary casts, changed to use the method
// Integer.valueOf and added logging of exception.
// ZAP: 2012/11/20 Issue 419: Restructure jar loading code
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons
// ZAP: 2013/01/19 Issue 460 Add support for a scan progress dialog
// ZAP: 2013/01/25 Catch any exceptions thrown when loading plugins to allow ZAP to still start
// ZAP: 2013/03/18 Issue 564: Active scanner can hang if dependencies used
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2014/01/16 Added skip support functions and changed obsolete collections
// ZAP: 2014/02/12 Issue 1030: Load and save scan policies
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/05/20 Issue 377: Unfulfilled dependencies hang the active scan
// ZAP: 2014/11/19 Issue 1412: Manage scan policies
// ZAP: 2014/11/19 Issue 1412: Init scan rule status (quality) from add-on
// ZAP: 2015/01/04 Issue 1484: NullPointerException during uninstallation of an add-on with active
// scanners
// ZAP: 2015/01/04 Issue 1486: Add-on components leak
// ZAP: 2015/07/25 Do not log error if the duplicated scanner is (apparently) a newer/older version
// ZAP: 2015/08/19 Issue 1785: Plugin enabled even if dependencies are not, "hangs" active scan
// ZAP: 2015/11/02 Issue 1969: Issues with installation of scanners
// ZAP: 2015/12/21 Issue 2112: Wrong policy on active Scan
// ZAP: 2016/01/26 Fixed findbugs warning
// ZAP: 2016/05/04 Use existing Plugin instances when setting them as completed
// ZAP: 2016/06/27 Reduce log level when loading the plugins
// ZAP: 2016/06/29 Do not log when cloning PluginFactory
// ZAP: 2016/07/25 Fix to correct handling of lists in plugins
// ZAP: 2017/06/20 Allow to obtain a Plugin by ID.
// ZAP: 2017/07/05 Log an error if the Plugin does not have a defined ID.
// ZAP: 2017/07/12 Order dependencies before dependent plugins (Issue 3154) and tweak status
// comparison.
// ZAP: 2017/10/05 Replace usage of Class.newInstance (deprecated in Java 9).
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/05/14 Remove redundant type arguments.
// ZAP: 2022/02/03 Removed loadedPlugin and unloadedPlugin.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.zap.control.CoreFunctionality;
import org.zaproxy.zap.control.ExtensionFactory;

public class PluginFactory {

    private static Logger log = LogManager.getLogger(PluginFactory.class);
    private static List<AbstractPlugin> loadedPlugins = null;
    private static Map<Integer, Plugin> mapLoadedPlugins;

    private List<Plugin> listAllPlugin = new ArrayList<>();
    private LinkedHashMap<Integer, Plugin> mapAllPlugin =
            new LinkedHashMap<>(); // insertion-ordered
    private LinkedHashMap<String, Plugin> mapAllPluginOrderCodeName =
            new LinkedHashMap<>(); // insertion-ordered
    private List<Plugin> listPending = new ArrayList<>();
    private List<Plugin> listRunning = new ArrayList<>();
    private List<Plugin> listCompleted = new ArrayList<>();
    private int totalPluginToRun = 0;
    private boolean init = false;
    private Configuration config;

    public PluginFactory() {
        super();
        HierarchicalConfiguration configuration = new HierarchicalConfiguration();
        configuration.setDelimiterParsingDisabled(true);
        config = configuration;
    }

    private static synchronized void initPlugins() {
        if (loadedPlugins == null) {
            init(true);
        }
    }

    // Helper method to ease tests.
    static void init(boolean includeAddOns) {
        loadedPlugins = new ArrayList<>(CoreFunctionality.getBuiltInActiveScanRules());
        if (includeAddOns) {
            loadedPlugins.addAll(ExtensionFactory.getAddOnLoader().getActiveScanRules());
        }
        // sort by the criteria below.
        Collections.sort(loadedPlugins, riskComparator);

        mapLoadedPlugins = new HashMap<>();
        for (Plugin plugin : loadedPlugins) {
            checkPluginId(plugin);
            mapLoadedPlugins.put(plugin.getId(), plugin);
        }
    }

    private static void checkPluginId(Plugin plugin) {
        if (plugin.getId() == -1) {
            log.error(
                    "The active scan rule [{}] does not have a defined ID.",
                    plugin.getClass().getCanonicalName());
        }
    }

    /**
     * Gets the {@code Plugin} with the given ID.
     *
     * @param id the ID of the plugin.
     * @return the {@code Plugin}, or {@code null} if not found (e.g. not installed).
     * @since 2.7.0
     */
    public static Plugin getLoadedPlugin(int id) {
        initPlugins();
        return mapLoadedPlugins.get(id);
    }

    private static List<AbstractPlugin> getLoadedPlugins() {
        if (loadedPlugins == null) {
            initPlugins();
        }
        return loadedPlugins;
    }

    /**
     * Tells whether or not the given {@code plugin} was already loaded.
     *
     * @param plugin the plugin that will be checked
     * @return {@code true} if the plugin was already loaded, {@code false} otherwise
     * @since 2.4.3
     */
    public static boolean isPluginLoaded(AbstractPlugin plugin) {
        if (loadedPlugins == null) {
            return false;
        }
        return isPluginLoadedImpl(plugin);
    }

    private static boolean isPluginLoadedImpl(AbstractPlugin plugin) {
        for (AbstractPlugin otherPlugin : getLoadedPlugins()) {
            if (otherPlugin == plugin) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the given loaded {@code plugin} to the {@code PluginFactory}. Loaded plugins, are used
     * by the active scanner, if enabled.
     *
     * <p>Call to this method has not effect it the {@code plugin} was already added.
     *
     * @param plugin the plugin that should be loaded
     * @since 2.4.0
     * @see #isPluginLoaded(AbstractPlugin)
     */
    public static void loadedPlugin(AbstractPlugin plugin) {
        if (!isPluginLoadedImpl(plugin)) {
            checkPluginId(plugin);
            getLoadedPlugins().add(plugin);
            mapLoadedPlugins.put(plugin.getId(), plugin);
            Collections.sort(loadedPlugins, riskComparator);
        }
    }

    public static void unloadedPlugin(AbstractPlugin plugin) {
        if (loadedPlugins == null) {
            return;
        }
        for (Iterator<AbstractPlugin> it = getLoadedPlugins().iterator(); it.hasNext(); ) {
            if (it.next() == plugin) {
                it.remove();
                mapLoadedPlugins.remove(plugin.getId());
                return;
            }
        }
    }

    // now order the list by the highest risk thrown, in descending order (to execute the more
    // critical checks first)
    private static final Comparator<AbstractPlugin> riskComparator =
            new Comparator<AbstractPlugin>() {
                @Override
                public int compare(AbstractPlugin e1, AbstractPlugin e2) {
                    // Run stable plugins first
                    int res = e1.getStatus().compareTo(e2.getStatus());
                    if (res != 0) {
                        return -res;
                    }

                    if (e1.getRisk() > e2.getRisk()) {
                        // High Risk alerts are checked before low risk alerts
                        return -1;

                    } else if (e1.getRisk() < e2.getRisk()) {
                        return 1;

                    } else {
                        // need to look at a secondary factor (the Id of the plugin) to decide. Run
                        // older plugins first, followed by newer plugins
                        if (e1.getId() < e2.getId()) {
                            // log numbered (older) plugins are run before newer plugins
                            return -1;

                        } else if (e1.getId() > e2.getId()) {
                            return 1;

                        } else {
                            return 0;
                        }
                    }
                }
            };

    public void reset() {
        Iterator<Plugin> iterator;
        Plugin plugin;
        synchronized (mapAllPlugin) {
            this.listPending.clear();
            this.listRunning.clear();
            this.listCompleted.clear();

            // pass 1 - enable all plugin's dependency
            iterator = mapAllPlugin.values().iterator();
            while (iterator.hasNext()) {
                // ZAP: Removed unnecessary cast.
                plugin = iterator.next();
                if (plugin.isEnabled()) {
                    enableDependency(plugin);
                }
            }

            // pass 2 - put enabled dependency in listPending
            iterator = mapAllPlugin.values().iterator();
            while (iterator.hasNext()) {
                // ZAP: Removed unnecessary cast.
                plugin = iterator.next();
                if (plugin.isEnabled()) {
                    addAllDependencies(plugin, listPending);
                    if (!listPending.contains(plugin)) {
                        listPending.add(plugin);
                    }
                }
            }

            totalPluginToRun = listPending.size();
        }
        this.init = true;
    }

    private void enableDependency(Plugin plugin) {

        String[] dependency = plugin.getDependency();
        if (dependency == null || dependency.length == 0) {
            return;
        }

        List<Plugin> dependencies = new ArrayList<>(dependency.length);
        if (addAllDependencies(plugin, dependencies)) {
            for (Plugin dep : dependencies) {
                if (!dep.isEnabled()) {
                    dep.setEnabled(true);
                }
            }
        } else {
            plugin.setEnabled(false);
            plugin.setAlertThreshold(Plugin.AlertThreshold.OFF);
            log.warn(
                    "Disabled scanner '{}' because of unfulfilled dependencies.", plugin.getName());
        }
    }

    public boolean hasAllDependenciesAvailable(Plugin plugin) {
        List<Plugin> deps = new ArrayList<>();
        return addAllDependencies(plugin, deps);
    }

    public boolean addAllDependencies(Plugin plugin, List<Plugin> to) {
        String[] dependencies = plugin.getDependency();
        if (dependencies == null || dependencies.length == 0) {
            return true;
        }

        boolean allDepsAvailable = true;
        List<String> deps = new ArrayList<>(Arrays.asList(dependencies));
        for (String dependency : deps) {
            Plugin pluginDep = mapAllPluginOrderCodeName.get(dependency);
            if (pluginDep == null) {
                allDepsAvailable = false;
            } else if (!to.contains(pluginDep)) {
                allDepsAvailable &= addAllDependencies(pluginDep, to);
                to.add(pluginDep);
            }
        }
        return allDepsAvailable;
    }

    public List<Plugin> getDependentPlugins(Plugin plugin) {
        List<Plugin> dependentPlugins = new ArrayList<>();
        addDependentPlugins(plugin.getCodeName(), dependentPlugins);
        return dependentPlugins;
    }

    private void addDependentPlugins(String pluginName, List<Plugin> to) {
        for (Plugin plugin : listAllPlugin) {
            String[] dependencies = plugin.getDependency();
            if (dependencies != null && dependencies.length != 0) {
                if (Arrays.asList(dependencies).contains(pluginName) && !to.contains(plugin)) {
                    to.add(plugin);
                    addDependentPlugins(plugin.getCodeName(), to);
                }
            }
        }
    }

    public List<Plugin> getDependencies(Plugin plugin) {
        String[] dependencies = plugin.getDependency();
        if (dependencies == null || dependencies.length == 0) {
            return Collections.emptyList();
        }

        List<String> deps = new ArrayList<>(Arrays.asList(dependencies));
        List<Plugin> depsPlugins = new ArrayList<>(deps.size());
        for (String dependency : deps) {
            Plugin pluginDep = mapAllPluginOrderCodeName.get(dependency);
            if (pluginDep != null) {
                depsPlugins.add(pluginDep);
            }
        }
        return depsPlugins;
    }

    /** @param config */
    public synchronized void loadAllPlugin(Configuration config) {
        log.debug("loadAllPlugin");
        this.config = config;

        // mapAllPlugin is ordered by insertion order, so the ordering of plugins in listTest is
        // used
        // when mapAllPlugin is iterated
        synchronized (mapAllPlugin) {
            mapAllPlugin.clear();
            listAllPlugin.clear();
            mapAllPluginOrderCodeName.clear();

            for (int i = 0; i < getLoadedPlugins().size(); i++) {
                // ZAP: Removed unnecessary cast.
                try {
                    Plugin loadedPlugin = getLoadedPlugins().get(i);
                    if (!loadedPlugin.isVisible()) {
                        log.info("Plugin {} not visible", loadedPlugin.getName());
                        continue;
                    }

                    if (loadedPlugin.isDepreciated()) {
                        // ZAP: ignore all depreciated plugins
                        log.info("Plugin {} deprecated", loadedPlugin.getName());
                        continue;
                    }

                    if (!canAddPlugin(mapAllPlugin, loadedPlugin)) {
                        continue;
                    }

                    Plugin plugin = createNewPlugin(loadedPlugin, config);
                    log.debug(
                            "loaded plugin {} with: Threshold={} Strength={}",
                            plugin.getName(),
                            plugin.getAlertThreshold().name(),
                            plugin.getAttackStrength());

                    // ZAP: Changed to use the method Integer.valueOf.
                    mapAllPlugin.put(plugin.getId(), plugin);
                    mapAllPluginOrderCodeName.put(plugin.getCodeName(), plugin);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            Iterator<Plugin> iterator = mapAllPlugin.values().iterator();
            while (iterator.hasNext()) {
                listAllPlugin.add(iterator.next());
            }
        }
    }

    private static Plugin createNewPlugin(Plugin plugin, Configuration config)
            throws ReflectiveOperationException {
        Plugin newPlugin = plugin.getClass().getDeclaredConstructor().newInstance();
        newPlugin.setConfig(new BaseConfiguration());
        plugin.cloneInto(newPlugin);

        newPlugin.setConfig(config);
        newPlugin.createParamIfNotExist();
        newPlugin.loadFrom(config);
        return newPlugin;
    }

    private static boolean canAddPlugin(Map<Integer, Plugin> plugins, Plugin plugin) {
        Plugin existingPlugin = plugins.get(plugin.getId());
        if (existingPlugin == null) {
            return true;
        }

        // Check if it has also the same name, might be the same scanner but a newer/older version
        if (existingPlugin.getName().equals(plugin.getName())) {
            if (existingPlugin.getStatus().compareTo(plugin.getStatus()) > 0) {
                log.info(
                        "Ignoring (apparently) less stable scanner version, id={}, ExistingPlugin[Status={}, Class={}], LessStablePlugin[Status={}, Class={}]",
                        plugin.getId(),
                        existingPlugin.getStatus(),
                        existingPlugin.getClass().getCanonicalName(),
                        plugin.getStatus(),
                        plugin.getClass().getCanonicalName());
                return false;
            }

            if (existingPlugin.getStatus() != plugin.getStatus()) {
                log.info(
                        "Replacing existing scanner with (apparently) stabler version, id={}, ExistingPlugin[Status={}, Class={}], StablerPlugin[Status={}, Class={}]",
                        plugin.getId(),
                        existingPlugin.getStatus(),
                        existingPlugin.getClass().getCanonicalName(),
                        plugin.getStatus(),
                        plugin.getClass().getCanonicalName());
                return true;
            }
        }

        log.error(
                "Duplicate id {} {} {}",
                plugin.getId(),
                plugin.getClass().getCanonicalName(),
                existingPlugin.getClass().getCanonicalName());
        return true;
    }

    public synchronized void loadFrom(PluginFactory pf) {
        log.debug("loadFrom {}", pf.listAllPlugin.size());
        for (Plugin plugin : pf.listAllPlugin) {
            Plugin p = this.mapAllPlugin.get(plugin.getId());
            if (p != null) {
                plugin.cloneInto(p);
            }
        }
    }

    public List<Plugin> getAllPlugin() {
        return listAllPlugin;
    }

    @Override
    public PluginFactory clone() {
        PluginFactory clone = new PluginFactory();
        Plugin pluginCopy;
        for (Plugin plugin : listAllPlugin) {
            try {
                pluginCopy = plugin.getClass().getDeclaredConstructor().newInstance();
                pluginCopy.setConfig(clone.config);
                plugin.cloneInto(pluginCopy);
                clone.addPlugin(pluginCopy);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return clone;
    }

    public boolean addPlugin(String name) {
        try {
            Class<?> c = ExtensionFactory.getAddOnLoader().loadClass(name);
            Plugin plugin = (AbstractPlugin) c.getDeclaredConstructor().newInstance();

            boolean duplicatedId = mapAllPlugin.get(plugin.getId()) != null;
            if (this.addPlugin(plugin)) {
                log.info("loaded plugin {}", plugin.getName());
                if (duplicatedId) {
                    log.error(
                            "Duplicate id {} {}",
                            plugin.getName(),
                            mapAllPlugin.get(plugin.getId()).getName());
                }
                return true;
            }

            if (!plugin.isVisible()) {
                log.info("Plugin {} not visible", plugin.getName());
                return false;
            }

            if (plugin.isDepreciated()) {
                log.info("Plugin {} deprecated", plugin.getName());
                return false;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private boolean addPlugin(Plugin plugin) {
        listAllPlugin.add(plugin);
        plugin.setConfig(this.config);

        plugin.createParamIfNotExist();
        if (!plugin.isVisible()) {
            return false;
        }

        if (plugin.isDepreciated()) {
            return false;
        }

        mapAllPlugin.put(plugin.getId(), plugin);
        mapAllPluginOrderCodeName.put(plugin.getCodeName(), plugin);

        return true;
    }

    public boolean removePlugin(String className) {
        for (int i = 0; i < listAllPlugin.size(); i++) {
            Plugin plugin = listAllPlugin.get(i);
            if (plugin.getClass().getName().equals(className)) {
                listAllPlugin.remove(plugin);
                mapAllPlugin.remove(plugin.getId());
                mapAllPluginOrderCodeName.remove(plugin.getCodeName());
                return true;
            }
        }

        return false;
    }

    public Plugin getPlugin(int id) {
        // ZAP: Removed unnecessary cast and changed to use the method
        // Integer.valueOf.
        return mapAllPlugin.get(id);
    }

    public void setAllPluginEnabled(boolean enabled) {
        for (int i = 0; i < listAllPlugin.size(); i++) {
            // ZAP: Removed unnecessary cast.
            Plugin plugin = listAllPlugin.get(i);
            plugin.setEnabled(enabled);
        }
    }

    synchronized boolean existPluginToRun() {
        if (!init) {
            this.reset();
        }
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
     * Get next test ready to be run. Null = none. Test dependent on others will not be obtained.
     *
     * @return
     */
    private Plugin probeNextPlugin() {
        Plugin plugin = null;
        int i = 0;
        while (i < listPending.size()) {
            // ZAP: Removed unnecessary cast.
            plugin = listPending.get(i);
            if (isAllDependencyCompleted(plugin)) {
                return plugin;
            }
            i++;
        }

        return null;
    }

    /**
     * Get next plugin ready to be run without any dependency outstanding.
     *
     * @return new instance of next plugin to be run.
     */
    synchronized Plugin nextPlugin() {
        if (!init) {
            this.reset();
        }

        Plugin plugin = probeNextPlugin();
        if (plugin == null) {
            return null;
        }

        listPending.remove(plugin);
        plugin.setTimeStarted();
        listRunning.add(plugin);

        return plugin;
    }

    private boolean isAllDependencyCompleted(Plugin plugin) {
        // note the plugin object checked may not be the exact plugin object stored in the completed
        // list.
        // but the comparison is basing on pluginId (see equals method) so it will work.
        String[] dependency = plugin.getDependency();
        if (dependency == null || dependency.length == 0) {
            return true;
        }

        synchronized (listCompleted) {
            for (int i = 0; i < dependency.length; i++) {
                boolean isFound = false;
                for (int j = 0; j < listCompleted.size() && !isFound; j++) {
                    // ZAP: Removed unnecessary cast.
                    Plugin completed = listCompleted.get(j);
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

    public void saveTo(Configuration conf) throws ConfigurationException {
        for (Plugin plugin : listAllPlugin) {
            plugin.saveTo(conf);
        }
    }

    public void loadFrom(Configuration config) throws ConfigurationException {
        for (Plugin plugin : listAllPlugin) {
            plugin.loadFrom(config);
        }
    }

    synchronized void setRunningPluginCompleted(Plugin plugin) {
        if (listRunning.remove(plugin)) {
            Plugin completedPlugin = mapAllPlugin.get(plugin.getId());
            listCompleted.add(completedPlugin);
            completedPlugin.setTimeFinished();
        }
    }

    boolean isRunning(Plugin plugin) {
        return listRunning.contains(plugin);
    }

    int totalPluginToRun() {
        return totalPluginToRun;
    }

    int totalPluginCompleted() {
        return listCompleted.size();
    }

    List<Plugin> getPending() {
        return this.listPending;
    }

    List<Plugin> getRunning() {
        return this.listRunning;
    }

    List<Plugin> getCompleted() {
        return this.listCompleted;
    }

    public int getEnabledPluginCount() {
        int count = 0;
        for (Plugin plugin : listAllPlugin) {
            if (plugin.isEnabled()) {
                count++;
            }
        }
        return count;
    }
}
