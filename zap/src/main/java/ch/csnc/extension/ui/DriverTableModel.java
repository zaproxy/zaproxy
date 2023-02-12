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
package ch.csnc.extension.ui;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
@SuppressWarnings("serial")
public class DriverTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -9114670362713975727L;

    private ch.csnc.extension.util.DriverConfiguration driverConfig;
    private Vector<String> names;
    private Vector<String> paths;
    private Vector<Integer> slots;
    private Vector<Integer> slotListIndexes;

    public DriverTableModel(ch.csnc.extension.util.DriverConfiguration driverConfig) {
        this.driverConfig = driverConfig;
        this.driverConfig.addChangeListener(e -> fireTableDataChanged());

        names = driverConfig.getNames();
        paths = driverConfig.getPaths();
        slots = driverConfig.getSlots();
        slotListIndexes = driverConfig.getSlotIndexes();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public int getRowCount() {
        return names.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return names.get(row);
        }
        if (column == 1) {
            return paths.get(row);
        }
        if (column == 2) {
            return slots.get(row);
        }
        if (column == 3) {
            return slotListIndexes.get(row);
        }

        return "";
    }

    /*default*/ int getPreferredWith(int column) {
        if (column == 0) {
            return 75;
        }
        if (column == 1) {
            return 300;
        }
        if (column == 2) {
            return 15;
        }
        if (column == 3) {
            return 15;
        }
        return 0;
    }

    /* default */ void addDriver(String name, String path, int slot, int slotListIndex) {
        names.add(name);
        paths.add(path);
        slots.add(slot);
        slotListIndexes.add(slotListIndex);

        updateConfiguration();
    }

    /* default */ void deleteDriver(int index) {
        names.remove(index);
        paths.remove(index);
        slots.remove(index);
        slotListIndexes.remove(index);

        updateConfiguration();
    }

    private void updateConfiguration() {
        driverConfig.setNames(names);
        driverConfig.setPaths(paths);
        driverConfig.setSlots(slots);
        driverConfig.setSlotListIndexes(slotListIndexes);
        driverConfig.write();
    }

    @Override
    public String getColumnName(int columnNumber) {
        if (columnNumber == 0) {
            return "Name";
        } else if (columnNumber == 1) {
            return "Path";
        } else if (columnNumber == 2) {
            return "Slot";
        } else if (columnNumber == 3) {
            return "SlotListIndex";
        } else {
            throw new IllegalArgumentException("Invalid column number: " + columnNumber);
        }
    }
}
