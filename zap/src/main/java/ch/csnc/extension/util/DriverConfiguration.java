/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Please note that this file was originally released under the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version
 * by Compass Security AG.
 *
 * As of October 2014 Compass Security AG granted the OWASP ZAP Project
 * permission to redistribute this code under the Apache License, Version 2.0.
 */
package ch.csnc.extension.util;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class DriverConfiguration {
    private File file = null;
    private URL url;

    private Vector<String> names;
    private Vector<String> paths;
    private Vector<Integer> slots;
    private Vector<Integer> slotListIndexes;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private EventListenerList eventListeners = new EventListenerList();
    private ChangeEvent changeEvent;

    public DriverConfiguration(URL url) {
        this.url = url;
        load();
    }

    public DriverConfiguration(File file) {
        this.file = file;
        load();
    }

    private void load() {
        names = new Vector<>();
        paths = new Vector<>();
        slots = new Vector<>();
        slotListIndexes = new Vector<>();

        try {
            ZapXmlConfiguration configuration =
                    file != null ? new ZapXmlConfiguration(file) : new ZapXmlConfiguration(url);
            List<HierarchicalConfiguration> drivers = configuration.configurationsAt("driver");
            for (HierarchicalConfiguration driver : drivers) {
                names.add(driver.getString("name", ""));
                paths.add(driver.getString("path", ""));
                slots.add(getInt(driver.getString("slot")));
                slotListIndexes.add(getInt(driver.getString("slotListIndex")));
            }

        } catch (ConfigurationException e) {
            logger.error("Failed to read the configuration from " + (file != null ? file : url), e);
        }
    }

    /**
     * Gets an integer from the given string.
     *
     * <p>If the given string is {@code null} or does not have an integer, zero is returned.
     *
     * @param string the string with the integer value
     * @return an integer
     */
    private int getInt(String string) {
        if (string != null) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                logger.error("Failed to extract an integer from: " + string);
            }
        }
        return 0;
    }

    public void write() {
        if (file == null) {
            fireStateChanged();
            return;
        }

        ZapXmlConfiguration configuration = new ZapXmlConfiguration();
        configuration.setRootElementName("driverConfiguration");

        for (int i = 0; i < names.size(); i++) {
            String baseKey = "driver(" + i + ").";
            configuration.setProperty(baseKey + "name", names.get(i));
            configuration.setProperty(baseKey + "path", paths.get(i));
            configuration.setProperty(baseKey + "slot", slots.get(i).toString());
            configuration.setProperty(baseKey + "slotListIndex", slotListIndexes.get(i).toString());
        }

        try {
            configuration.save(file);
        } catch (ConfigurationException e) {
            logger.error("Failed to save driver configuration to " + file, e);
        }

        fireStateChanged();
    }

    private void fireStateChanged() {
        Object[] listeners = eventListeners.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    public Vector<String> getNames() {
        return names;
    }

    public void setNames(Vector<String> names) {
        this.names = names;
    }

    public Vector<String> getPaths() {
        return paths;
    }

    public void setPaths(Vector<String> paths) {
        this.paths = paths;
    }

    public Vector<Integer> getSlots() {
        return slots;
    }

    public void setSlots(Vector<Integer> slots) {
        this.slots = slots;
    }

    public Vector<Integer> getSlotIndexes() {
        return slotListIndexes;
    }

    public void setSlotListIndexes(Vector<Integer> slotListIndexes) {
        this.slotListIndexes = slotListIndexes;
    }

    public void addChangeListener(ChangeListener listener) {
        eventListeners.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        eventListeners.remove(ChangeListener.class, listener);
    }
}
