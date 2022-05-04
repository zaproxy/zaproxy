/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.autoupdate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.view.StatusUI;

public class UninstalledAddOnsTableModel extends AddOnsTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        "", // Column for warning of running issues (e.g. incorrect Java version, missing
        // dependency...)
        Constant.messages.getString("cfu.table.header.status"),
        Constant.messages.getString("cfu.table.header.name"),
        Constant.messages.getString("cfu.table.header.desc"),
        Constant.messages.getString("cfu.table.header.update"),
        ""
    };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    public UninstalledAddOnsTableModel(AddOnCollection installedAddOns) {
        super(installedAddOns, 4);
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public int getRowCount() {
        return getAddOnWrappers().size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) { // Icon
            return Icon.class;
        } else if (columnIndex == 1) { // Status
            return StatusUI.class;
        } else if (columnIndex == 5) { // update
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case COLUMN_ADD_ON_WRAPPER:
                return getAddOnWrapper(rowIndex);
            case 0:
                return getAddOnWrapper(rowIndex).hasRunningIssues();
            case 1:
                return View.getSingleton()
                        .getStatusUI(getAddOnWrapper(rowIndex).getAddOn().getStatus());
            case 2:
                return getAddOnWrapper(rowIndex).getAddOn().getName();
            case 3:
                return getAddOnWrapper(rowIndex).getAddOn().getDescription();
            case 4:
                int progress = getAddOnWrapper(rowIndex).getProgress();
                if (getAddOnWrapper(rowIndex).isFailed()) {
                    return Constant.messages.getString("cfu.download.failed");
                } else if (progress > 0) {
                    return progress + "%";
                } else if (AddOnWrapper.Status.newAddon == getAddOnWrapper(rowIndex).getStatus()) {
                    return Constant.messages.getString("cfu.table.label.new");
                } else {
                    // TODO change to date ??
                    return getAddOnWrapper(rowIndex).getAddOn().getVersion();
                }
            case 5:
                return getAddOnWrapper(rowIndex).isEnabled();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 5
                && getAddOnWrapper(rowIndex).getInstallationStatus()
                        != AddOn.InstallationStatus.DOWNLOADING) {
            if (aValue instanceof Boolean) {
                getAddOnWrapper(rowIndex).setEnabled((Boolean) aValue);
                this.fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 5
                && getAddOnWrapper(rowIndex).getInstallationStatus()
                        != AddOn.InstallationStatus.DOWNLOADING) {
            // Its the 'enabled' checkbox, and no download is in progress
            return true;
        }
        return false;
    }

    public boolean canIinstallSelected() {
        boolean enable = false;
        for (AddOnWrapper addon : getAddOnWrappers()) {
            if (addon.isEnabled()) {
                return true;
            }
        }
        return enable;
    }

    public Set<AddOn> getAvailableAddOns() {
        Set<AddOn> addOns = new HashSet<>();
        for (AddOnWrapper aow : getAddOnWrappers()) {
            if (aow.getInstallationStatus() == AddOn.InstallationStatus.AVAILABLE) {
                addOns.add(aow.getAddOn());
            }
        }
        return addOns;
    }

    public boolean hasAvailableAddOns() {
        for (AddOnWrapper aow : getAddOnWrappers()) {
            if (aow.getInstallationStatus() == AddOn.InstallationStatus.AVAILABLE) {
                return true;
            }
        }
        return false;
    }

    public void addAddOn(AddOn addOn) {
        addAddOnWrapper(addOn, null);
    }

    public void setAddOns(List<AddOn> addOnsNotInstalled, AddOnCollection olderAddOns) {
        if (!getAddOnWrappers().isEmpty()) {
            int rows = getAddOnWrappers().size();
            getAddOnWrappers().clear();
            fireTableRowsDeleted(0, rows - 1);
        }

        for (AddOn addOn : addOnsNotInstalled) {
            AddOnWrapper.Status status = null;
            if (olderAddOns != null && olderAddOns.getAddOn(addOn.getId()) == null) {
                // Not in the previous set
                status = AddOnWrapper.Status.newAddon;
            }
            addAddOnWrapper(addOn, status);
        }
    }

    @Override
    protected void restoreInstallationStatusFailedDownload(AddOnWrapper aow) {
        aow.setInstallationStatus(AddOn.InstallationStatus.AVAILABLE);
    }
}
