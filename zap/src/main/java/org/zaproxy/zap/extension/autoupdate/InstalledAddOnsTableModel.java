/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOn.AddOnRunRequirements;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.extension.autoupdate.AddOnWrapper.Status;

@SuppressWarnings("serial")
public class InstalledAddOnsTableModel extends AddOnsTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        "", // Column for warning of running issues (e.g. incorrect Java version, missing
        // dependency...)
        Constant.messages.getString("cfu.table.header.name"),
        Constant.messages.getString("cfu.table.header.version"),
        Constant.messages.getString("cfu.table.header.desc"),
        Constant.messages.getString("cfu.table.header.update"),
        ""
    };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private AddOnCollection availableAddOns;

    public InstalledAddOnsTableModel(AddOnCollection installedAddOns) {
        super(installedAddOns, 3);

        for (AddOn addOn : installedAddOns.getAddOns()) {
            addAddOnWrapper(addOn, null);
        }
    }

    public void setAvailableAddOns(AddOnCollection availableAddOns) {
        this.availableAddOns = availableAddOns;
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
        if (columnIndex == 0 || columnIndex == 5) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AddOnWrapper aow = getAddOnWrapper(rowIndex);
        switch (columnIndex) {
            case COLUMN_ADD_ON_WRAPPER:
                return aow;
            case 0:
                return aow.hasRunningIssues();
            case 1:
                return aow.getAddOn().getName();
            case 2:
                return aow.getAddOn().getVersion();
            case 3:
                return aow.getAddOn().getDescription();
            case 4:
                int progress = aow.getProgress();
                if (aow.isFailed()) {
                    return Constant.messages.getString("cfu.table.label.failed");
                } else if (AddOn.InstallationStatus.UNINSTALLATION_FAILED
                                == aow.getInstallationStatus()
                        || AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED
                                == aow.getInstallationStatus()) {
                    return Constant.messages.getString("cfu.table.label.restartRequired");
                } else if (progress > 0) {
                    return progress + "%";
                } else if (AddOnWrapper.Status.newVersion == aow.getStatus()) {
                    return Constant.messages.getString("cfu.table.label.update");
                } else {
                    return "";
                }
            case 5:
                return getAddOnWrapper(rowIndex).isEnabled();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 5) {
            return;
        }

        AddOnWrapper addOnWrapper = getAddOnWrapper(rowIndex);
        if (addOnWrapper.getAddOn().isMandatory()) {
            return;
        }

        if (AddOn.InstallationStatus.UNINSTALLATION_FAILED == addOnWrapper.getInstallationStatus()
                || AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED
                        == addOnWrapper.getInstallationStatus()) {
            return;
        }

        if (aValue instanceof Boolean) {
            if (addOnWrapper.getInstallationStatus() != AddOn.InstallationStatus.DOWNLOADING) {
                addOnWrapper.setEnabled((Boolean) aValue);
                this.fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex != 5) {
            return false;
        }

        AddOnWrapper addOnWrapper = getAddOnWrapper(rowIndex);
        if (addOnWrapper.getAddOn().isMandatory()) {
            return false;
        }

        if (AddOn.InstallationStatus.UNINSTALLATION_FAILED == addOnWrapper.getInstallationStatus()
                || AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED
                        == addOnWrapper.getInstallationStatus()) {
            return false;
        }

        if (addOnWrapper.getInstallationStatus() != AddOn.InstallationStatus.DOWNLOADING) {
            // Its the 'enabled' checkbox, and no download is in progress
            return true;
        }
        return false;
    }

    public boolean canUpdateSelected() {
        boolean enable = false;
        for (AddOnWrapper addon : getAddOnWrappers()) {
            if (addon.isEnabled()) {
                if (Status.newVersion == addon.getStatus()) {
                    enable = true;
                } else {
                    return false;
                }
            }
        }
        return enable;
    }

    public boolean canUninstallSelected() {
        boolean enable = false;
        for (AddOnWrapper addon : getAddOnWrappers()) {
            if (addon.isEnabled()) {
                return true;
            }
        }
        return enable;
    }

    public void addOrRefreshAddOn(AddOn addOn) {
        for (int idx = 0; idx < getAddOnWrappers().size(); idx++) {
            AddOnWrapper aow = getAddOnWrappers().get(idx);
            if (addOn.isSameAddOn(getAddOnForDownload(aow))) {
                aow.setAddOn(addOn);
                fireTableRowsUpdated(idx, idx);

                refreshEntries();
                return;
            }
        }

        addAddOnWrapper(addOn, null);
    }

    @Override
    protected boolean refreshEntry(AddOnWrapper aow, int row) {
        boolean changed = super.refreshEntry(aow, row);

        if (aow.getAddOnUpdate() != null) {
            changed |= refreshUpdateIssues(aow);
        }

        return changed;
    }

    @Override
    protected AddOnWrapper createAddOnWrapper(AddOn addOn, Status status) {
        AddOnWrapper aow = super.createAddOnWrapper(addOn, status);
        if (availableAddOns != null) {
            AddOn possibleUpdate = availableAddOns.getAddOn(addOn.getId());
            if (possibleUpdate != null && possibleUpdate.isUpdateTo(aow.getAddOn())) {
                aow.setAddOnUpdate(possibleUpdate);
                refreshUpdateIssues(aow);
            }
        }

        return aow;
    }

    private boolean refreshUpdateIssues(AddOnWrapper aow) {
        AddOnRunRequirements reqs =
                aow.getAddOnUpdate().calculateRunRequirements(availableAddOns.getAddOns());
        String issues = getAddOnRunningIssues(reqs);
        aow.setUpdateIssues(issues, !reqs.hasExtensionsWithRunningIssues());
        return !issues.isEmpty();
    }

    public Set<AddOn> getSelectedUpdates() {
        Set<AddOn> selectedAddOns = new HashSet<>();
        for (AddOnWrapper aow : getAddOnWrappers()) {
            if (aow.isEnabled() && aow.getAddOnUpdate() != null) {
                selectedAddOns.add(aow.getAddOnUpdate());
            }
        }
        return selectedAddOns;
    }

    public Set<AddOn> getAllUpdates() {
        Set<AddOn> selectedAddOns = new HashSet<>();
        for (AddOnWrapper aow : getAddOnWrappers()) {
            if (aow.getAddOnUpdate() != null) {
                selectedAddOns.add(aow.getAddOnUpdate());
            }
        }
        return selectedAddOns;
    }

    @Override
    protected AddOn getAddOnForDownload(AddOnWrapper aow) {
        if (aow.getAddOnUpdate() == null) {
            return aow.getAddOn();
        }
        return aow.getAddOnUpdate();
    }

    public List<AddOn> updateEntries() {
        if (availableAddOns == null) {
            return Collections.emptyList();
        }

        List<AddOn> addOnsNotInstalled = new ArrayList<>(availableAddOns.getAddOns());
        for (int i = 0; i < getAddOnWrappers().size(); i++) {
            boolean fireRowUpdate = false;
            boolean fireCellUpdate = false;

            AddOnWrapper aow = getAddOnWrappers().get(i);
            removeAddOn(addOnsNotInstalled, aow.getAddOn().getId());

            AddOn addOn = availableAddOns.getAddOn(aow.getAddOn().getId());
            if (addOn != null && addOn.isUpdateTo(aow.getAddOn())) {
                aow.setAddOnUpdate(addOn);
                refreshUpdateIssues(aow);
                fireRowUpdate = true;
            } else {
                aow.setStatus(null);
                fireCellUpdate = true;
            }

            if (aow.hasRunningIssues()) {
                fireRowUpdate = refreshRunningIssues(aow, i);
            }

            if (fireRowUpdate) {
                fireTableRowsUpdated(i, i);
            } else if (fireCellUpdate) {
                fireTableCellUpdated(i, 3);
            }
        }
        return addOnsNotInstalled;
    }

    public static void removeAddOn(List<AddOn> addOns, String id) {
        for (Iterator<AddOn> it = addOns.iterator(); it.hasNext(); ) {
            AddOn addOn = it.next();
            if (addOn.getId().equals(id)) {
                it.remove();
                return;
            }
        }
    }

    @Override
    protected void restoreInstallationStatusFailedDownload(AddOnWrapper aow) {
        aow.setInstallationStatus(AddOn.InstallationStatus.INSTALLED);
    }

    @Override
    protected AddOn getMissingAddOn(String addOnId) {
        AddOn addOn = super.getMissingAddOn(addOnId);
        if (addOn != null) {
            return addOn;
        }

        if (availableAddOns != null) {
            return availableAddOns.getAddOn(addOnId);
        }
        return null;
    }
}
