/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
 */
package org.zaproxy.zap.extension.pscan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.StatusUI;

@SuppressWarnings("serial")
public class PolicyPassiveScanTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] columnNames = {
        Constant.messages.getString("ascan.policy.table.testname"),
        Constant.messages.getString("ascan.policy.table.threshold"),
        Constant.messages.getString("ascan.policy.table.status")
    };

    private static final int STATUS_COLUMN_IDX = 2;

    private List<ScannerWrapper> listScanners = new ArrayList<>();
    private Map<String, String> i18nToStr = null;

    public PolicyPassiveScanTableModel() {}

    /** @param scanner */
    public void addScanner(PluginPassiveScanner scanner) {
        listScanners.add(
                new ScannerWrapper(scanner, View.getSingleton().getStatusUI(scanner.getStatus())));
        fireTableDataChanged();
    }
    /*
    protected List<PluginPassiveScanner> getScanners() {
    	return null; // this.listScanners;
    }
    */

    public void persistChanges() {
        for (ScannerWrapper ss : this.listScanners) {
            ss.persistChanges();
        }
    }

    public void reset() {
        for (ScannerWrapper ss : this.listScanners) {
            ss.reset();
        }
    }

    public void applyThreshold(AlertThreshold threshold, String status) {
        if (listScanners.isEmpty()) {
            return;
        }

        for (ScannerWrapper ss : this.listScanners) {
            if (status.equals(ss.getStatus().toString())) {
                ss.setThreshold(threshold);
            }
        }
        this.fireTableRowsUpdated(0, getRowCount() - 1);
    }

    public void applyThresholdToAll(AlertThreshold threshold) {
        if (listScanners.isEmpty()) {
            return;
        }

        for (ScannerWrapper ss : this.listScanners) {
            ss.setThreshold(threshold);
        }
        this.fireTableRowsUpdated(0, getRowCount() - 1);
    }

    /**
     * Removes the given {@code scanner} from this table model.
     *
     * @param scanner the scanner that will be removed from the model
     */
    public void removeScanner(PluginPassiveScanner scanner) {
        for (int i = 0; i < listScanners.size(); i++) {
            if (scanner.equals(listScanners.get(i).getScanner())) {
                listScanners.remove(i);
                fireTableRowsDeleted(i, i);
                break;
            }
        }
    }

    /**
     * @param c
     * @return
     */
    @Override
    public Class<?> getColumnClass(int c) {
        if (c == STATUS_COLUMN_IDX) {
            return StatusUI.class;
        }
        return String.class;
    }

    /**
     * @param col
     * @return
     */
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return true;
        }
        return false;
    }

    /**
     * @param value
     * @param row
     * @param col
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        ScannerWrapper test = listScanners.get(row);

        switch (col) {
            case 0:
                break;

            case 1:
                AlertThreshold af = AlertThreshold.valueOf(i18nToStr((String) value));
                test.setThreshold(af);
                fireTableCellUpdated(row, col);
                break;
        }
    }

    private String strToI18n(String str) {
        // I18n's threshold and strength enums
        return Constant.messages.getString("ascan.policy.level." + str.toLowerCase(Locale.ROOT));
    }

    private String i18nToStr(String str) {
        // Converts to i18n'ed names back to the enum names
        if (i18nToStr == null) {
            i18nToStr = new HashMap<>();
            for (AlertThreshold at : AlertThreshold.values()) {
                i18nToStr.put(this.strToI18n(at.name()), at.name());
            }
        }

        return i18nToStr.get(str);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        if (listScanners == null) {
            return 0;
        }
        return listScanners.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        ScannerWrapper test = listScanners.get(row);
        Object result = null;
        switch (col) {
            case 0: // Plugin/Rule Name Column
                result = test.getName();
                break;

            case 1: // Threshold Column
                result = strToI18n(test.getThreshold().name());
                break;

            case STATUS_COLUMN_IDX:
                result = test.getStatus();
                break;

            default:
                result = "";
        }
        return result;
    }

    /**
     * Inner class which maintains any changes made so they can be undone if cancelled
     *
     * @author simon
     */
    private static class ScannerWrapper {
        private final PluginPassiveScanner scanner;
        private final StatusUI status;
        private AlertThreshold threshold;

        public ScannerWrapper(PluginPassiveScanner scanner, StatusUI status) {
            this.scanner = scanner;
            this.status = status;
            reset();
        }

        public PluginPassiveScanner getScanner() {
            return scanner;
        }

        public void reset() {
            this.threshold = scanner.getAlertThreshold();
        }

        public void persistChanges() {
            this.scanner.setAlertThreshold(threshold);
            this.scanner.setEnabled(!AlertThreshold.OFF.equals(threshold));
            this.scanner.save();
        }

        public String getName() {
            return scanner.getDisplayName();
        }

        public AlertThreshold getThreshold() {
            return threshold;
        }

        public void setThreshold(AlertThreshold threshold) {
            this.threshold = threshold;
        }

        public StatusUI getStatus() {
            return status;
        }
    }
}
