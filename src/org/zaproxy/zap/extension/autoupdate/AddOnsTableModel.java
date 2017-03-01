/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP Development Team
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

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOn.AddOnRunRequirements;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.control.AddOnRunIssuesUtils;

/**
 * An {@code AbstractTableModel} for add-ons.
 */
public abstract class AddOnsTableModel extends AbstractTableModel {

    /**
     * The column in the table model that allows to get the {@code AddOnWrapper} of a given row.
     */
    public static final int COLUMN_ADD_ON_WRAPPER = -1;

    private static final long serialVersionUID = -5240438485136881299L;

    protected final Logger logger = Logger.getLogger(this.getClass());

    private final Comparator<AddOnWrapper> comparator;

    private final List<AddOnWrapper> wrappers;

    private final int progressColumn;

    private AddOnCollection addOnCollection;

    private AddOnSearcher addOnSeacher;

	/**
	 * @deprecated (2.5.0) Replaced by {@link #AddOnsTableModel(AddOnCollection, int)}. It will be removed in a future release.
	 */
    @Deprecated
    public AddOnsTableModel(Comparator<AddOnWrapper> comparator, AddOnCollection addOnCollection, int progressColumn) {
        super();

        this.comparator = comparator;
        this.wrappers = new ArrayList<>();
        this.progressColumn = progressColumn;

        this.addOnCollection = addOnCollection;
    }

    public AddOnsTableModel(AddOnCollection addOnCollection, int progressColumn) {
        super();

        this.comparator = null;
        this.wrappers = new ArrayList<>();
        this.progressColumn = progressColumn;

        this.addOnCollection = addOnCollection;
    }
    
    public void setAddOnCollection(AddOnCollection addOnCollection) {
        this.addOnCollection = addOnCollection;
    }

    protected List<AddOnWrapper> getAddOnWrappers() {
        return wrappers;
    }

    public Set<AddOn> getSelectedAddOns() {
        Set<AddOn> selectedAddOns = new HashSet<>();
        for (AddOnWrapper aow : getAddOnWrappers()) {
            if (aow.isEnabled()) {
                selectedAddOns.add(aow.getAddOn());
            }
        }
        return selectedAddOns;
    }

    public Set<AddOn> getDownloadingAddOns() {
        Set<AddOn> downloadingAddOns = new HashSet<>();
        for (AddOnWrapper aow : getAddOnWrappers()) {
            AddOn addOn = getAddOnForDownload(aow);
            if (AddOn.InstallationStatus.DOWNLOADING == addOn.getInstallationStatus()) {
                downloadingAddOns.add(addOn);
            }
        }
        return downloadingAddOns;
    }

    protected AddOn getAddOnForDownload(AddOnWrapper aow) {
        return aow.getAddOn();
    }

    protected void addAddOnWrapper(AddOn addOn, AddOnWrapper.Status status) {
        AddOnWrapper aow = createAddOnWrapper(addOn, status);
        int idx = 0;
    	if (comparator != null) {
    		for (; idx < getAddOnWrappers().size(); idx++) {
        		if (comparator.compare(aow, getAddOnWrappers().get(idx)) < 0) {
        			break;
        		}
                getAddOnWrappers().add(idx, aow);
        	}
        } else {
        	idx = getAddOnWrappers().size();
        	getAddOnWrappers().add(aow);
        }

        fireTableRowsInserted(idx, idx);

        refreshEntries();
    }

    protected void refreshEntries() {
        for (int idx = 0; idx < getAddOnWrappers().size(); idx++) {
            if (refreshEntry(getAddOnWrappers().get(idx), idx)) {
                fireTableRowsUpdated(idx, idx);
            }
        }
    }

    protected boolean refreshEntry(AddOnWrapper aow, int row) {
        boolean updateRow = false;
        if (aow.getInstallationStatus() != aow.getAddOn().getInstallationStatus()) {
            aow.setInstallationStatus(aow.getAddOn().getInstallationStatus());
            updateRow = true;
        }
        updateRow |= refreshRunningIssues(aow, row);
        return updateRow;
    }

    public void removeAddOn(AddOn addOn) {
        for (int idx = 0; idx < getAddOnWrappers().size(); idx++) {
            if (addOn.isSameAddOn(getAddOnWrappers().get(idx).getAddOn())) {
                getAddOnWrappers().remove(idx);
                fireTableRowsDeleted(idx, idx);

                refreshEntries();
                break;
            }
        }
    }

    public boolean notifyAddOnDownloading(AddOn addOn) {
        for (int idx = 0; idx < getAddOnWrappers().size(); idx++) {
            AddOnWrapper aow = getAddOnWrappers().get(idx);
            if (addOn.isSameAddOn(getAddOnForDownload(aow))) {
                aow.setInstallationStatus(AddOn.InstallationStatus.DOWNLOADING);
                aow.setEnabled(false);
                fireTableRowsUpdated(idx, idx);
                return true;
            }
        }
        return false;
    }

    public boolean notifyAddOnDownloadFailed(String url) {
        for (int idx = 0; idx < getAddOnWrappers().size(); idx++) {
            AddOnWrapper aow = getAddOnWrappers().get(idx);
            AddOn addOn = getAddOnForDownload(aow);
            if (hasSameUrl(addOn, url)) {
                setFailed(aow, addOn);

                fireTableRowsUpdated(idx, idx);
                return true;
            }
        }
        return false;
    }

    private void setFailed(AddOnWrapper aow, AddOn addOn) {
        aow.setFailed(true);
        restoreInstallationStatusFailedDownload(aow);
        addOn.setInstallationStatus(AddOn.InstallationStatus.AVAILABLE);
    }

    private static boolean hasSameUrl(AddOn addOn, String url) {
        URL addOnUrl = addOn.getUrl();
        if (addOnUrl == null) {
            return false;
        }
        return addOnUrl.toString().equals(url);
    }

    public boolean notifyAddOnFailedUninstallation(AddOn addOn) {
        for (int idx = 0; idx < getAddOnWrappers().size(); idx++) {
            AddOnWrapper aow = getAddOnWrappers().get(idx);
            if (addOn.isSameAddOn(getAddOnForDownload(aow))) {
                aow.setInstallationStatus(AddOn.InstallationStatus.UNINSTALLATION_FAILED);
                aow.setEnabled(false);
                fireTableRowsUpdated(idx, idx);
                return true;
            }
        }
        return false;
    }

    protected AddOnWrapper getAddOnWrapper(int rowIndex) {
        return this.getAddOnWrappers().get(rowIndex);
    }

    public void updateDownloadsProgresses(ExtensionAutoUpdate extension) {
        final List<Integer> rows = new ArrayList<>(getAddOnWrappers().size());
        for (int idx = 0; idx < getAddOnWrappers().size(); idx++) {
            AddOnWrapper aow = getAddOnWrappers().get(idx);

            AddOn addOn = getAddOnForDownload(aow);
            if (AddOn.InstallationStatus.DOWNLOADING == addOn.getInstallationStatus()) {
                URL url = addOn.getUrl();
                try {
                    int progress = extension.getDownloadProgressPercent(url);
                    if (progress > 0) {
                        aow.setProgress(progress);
                        rows.add(Integer.valueOf(idx));
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Error on " + url, e);
                    }
                    setFailed(aow, addOn);
                    try {
                        final int row = idx;
                        EventQueue.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                fireTableRowsUpdated(row, row);
                            }
                        });
                    } catch (InvocationTargetException | InterruptedException ignore) {
                    }
                }
            }
        }

        if (!rows.isEmpty()) {
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        for (Integer row : rows) {
                            fireTableCellUpdated(row.intValue(), progressColumn);
                        }
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to update all the progresses: ", e);
                }
            }
        }
    }

    protected abstract void restoreInstallationStatusFailedDownload(AddOnWrapper aow);

    protected void fireTableColumnUpdated(int firstRow, int lastRow, int column) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow, column, TableModelEvent.UPDATE));
    }

    protected AddOnWrapper createAddOnWrapper(AddOn addOn, AddOnWrapper.Status status) {
        if (addOnCollection == null) {
            return new AddOnWrapper(addOn, status);
        }
        return new AddOnWrapper(
                addOn,
                status,
                getAddOnRunningIssues(addOn.calculateRunRequirements(addOnCollection.getAddOns())));
    }

    protected boolean refreshRunningIssues(AddOnWrapper aow, int row) {
        AddOnRunRequirements reqs = aow.getAddOn().calculateRunRequirements(addOnCollection.getAddOns());
        String issues = getAddOnRunningIssues(reqs);
        aow.setRunningIssues(issues, !reqs.hasExtensionsWithRunningIssues());

        return !issues.isEmpty();
    }

    protected String getAddOnRunningIssues(AddOnRunRequirements reqs) {
        List<String> extractedIssues = AddOnRunIssuesUtils.getUiRunningIssues(reqs, getAddOnSearcher());
        if (extractedIssues.isEmpty()) {
            List<String> extensionsIssues = AddOnRunIssuesUtils.getUiExtensionsRunningIssues(reqs, getAddOnSearcher());
            if (!extensionsIssues.isEmpty()) {
                return getHtmlFromIssues(
                        Constant.messages.getString("cfu.warn.addon.with.extensions.with.missing.requirements"),
                        extensionsIssues);
            }
            return "";
        }
        return getHtmlFromIssues(Constant.messages.getString("cfu.warn.addon.with.missing.requirements"), extractedIssues);
    }

    private static String getHtmlFromIssues(String title, List<String> issues) {
        StringBuilder strBuilder = new StringBuilder(150);
        Encoder encoder = new Encoder();
        strBuilder.append("<html><strong>").append(encoder.getHTMLString(title)).append("</strong><ul>");
        for (String issue : issues) {
            strBuilder.append("<li>").append(encoder.getHTMLString(issue)).append("</li>");
        }
        strBuilder.append("</ul></html>");
        return strBuilder.toString();
    }

    protected AddOn getMissingAddOn(String addOnId) {
        return addOnCollection.getAddOn(addOnId);
    }

    private AddOnSearcher getAddOnSearcher() {
        if (addOnSeacher == null) {
            addOnSeacher = new AddOnSearcher();
        }
        return addOnSeacher;
    }

    private class AddOnSearcher implements AddOnRunIssuesUtils.AddOnSearcher {

        @Override
        public AddOn searchAddOn(String id) {
            return getMissingAddOn(id);
        }
    }
}