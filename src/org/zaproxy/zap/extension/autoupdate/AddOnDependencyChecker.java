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

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;

/**
 * Helper class that checks the dependency changes when an add-on, or several add-ons, are installed, uninstalled or updated.
 * <p>
 * It also allows to confirm with the user the resulting changes.
 * 
 * @since 2.4.0
 */
class AddOnDependencyChecker {

    private final AddOnCollection installedAddOns;
    private final AddOnCollection availableAddOns;

    public AddOnDependencyChecker(AddOnCollection installedAddOns, AddOnCollection availableAddOns) {
        this.installedAddOns = installedAddOns;
        this.availableAddOns = availableAddOns;
    }

    private static boolean contains(Collection<AddOn> addOns, AddOn addOn) {
        for (AddOn ao : addOns) {
            if (addOn.isSameAddOn(ao)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the changes required to install the given add-on.
     * <p>
     * It might require updating, installing or uninstalling other add-ons depending on the dependencies of the affected
     * add-ons.
     *
     * @param addOn the add-on that would be installed
     * @return the resulting changes with the add-ons that need to be updated, installed or uninstalled
     */
    public AddOnChangesResult calculateInstallChanges(AddOn addOn) {
        Set<AddOn> addOns = new HashSet<>();
        addOns.add(addOn);

        return calculateInstallChanges(addOns);
    }

    /**
     * Calculates the changes required to install the given add-ons.
     * <p>
     * It might require updating, installing or uninstalling other add-ons depending on the dependencies of the affected
     * add-ons.
     *
     * @param selectedAddOns the add-ons that would be installed
     * @return the resulting changes with the add-ons that need to be updated, installed or uninstalled
     */
    public AddOnChangesResult calculateInstallChanges(Set<AddOn> selectedAddOns) {
        return calculateChanges(selectedAddOns, false);
    }

    /**
     * Asks the user for confirmation of install changes.
     *
     * @param parent the parent component of the confirmation dialogue
     * @param changes the changes of the installation
     * @return {@code true} if the user accept the changes, {@code false} otherwise
     */
    public boolean confirmInstallChanges(Component parent, AddOnChangesResult changes) {
        return confirmChanges(parent, changes, false);
    }

    private boolean addDependencies(
            AddOn addOn,
            Set<AddOn> selectedAddOns,
            Set<AddOn> oldVersions,
            Set<AddOn> newVersions,
            Set<AddOn> installs) {
        AddOn.AddOnRunRequirements requirements = addOn.calculateRunRequirements(availableAddOns.getAddOns());

        for (AddOn dep : requirements.getDependencies()) {
            if (selectedAddOns.contains(dep)) {
                continue;
            }

            AddOn installed = installedAddOns.getAddOn(dep.getId());
            if (installed == null) {
                if (AddOn.InstallationStatus.AVAILABLE == availableAddOns.getAddOn(dep.getId()).getInstallationStatus()) {
                    installs.add(dep);
                }
            } else if (!addOn.dependsOn(installed)) {
                if (AddOn.InstallationStatus.AVAILABLE == availableAddOns.getAddOn(dep.getId()).getInstallationStatus()) {
                    oldVersions.add(installed);
                    newVersions.add(dep);
                }
            }
        }

        return requirements.isNewerJavaVersionRequired();
    }

    private boolean confirmChanges(Component parent, AddOnChangesResult changesResult, boolean updating) {
        Set<AddOn> selectedAddOnsJavaIssue = new HashSet<>();
        for (AddOn addOn : changesResult.getSelectedAddOns()) {
            if (!addOn.canRunInCurrentJavaVersion()) {
                selectedAddOnsJavaIssue.add(addOn);
            }
        }

        String question;

        Set<AddOn> installs = new HashSet<>(changesResult.getInstalls());
        Set<AddOn> updates = new HashSet<>(changesResult.getNewVersions());

        Set<AddOn> dependents = getDependents(updates, changesResult.getUninstalls());

        if (updating) {
            question = Constant.messages.getString("cfu.confirmation.dialogue.message.continueWithUpdate");
            updates.removeAll(changesResult.getSelectedAddOns());
        } else {
            question = Constant.messages.getString("cfu.confirmation.dialogue.message.continueWithInstallation");
            installs.removeAll(changesResult.getSelectedAddOns());
        }

        if (changesResult.getUninstalls().isEmpty() && updates.isEmpty() && installs.isEmpty() && dependents.isEmpty()
                && changesResult.getOptionalAddOns().isEmpty() && changesResult.getSoftUnloadExtensions().isEmpty()
                && changesResult.getUnloadExtensions().isEmpty()) {
            // No other changes required.
            if (selectedAddOnsJavaIssue.isEmpty()) {
                // No need to ask for confirmation.
                return true;
            }

            int size = changesResult.getSelectedAddOns().size();
            if (size == 1) {
                String baseMessage = MessageFormat.format(
                        Constant.messages.getString("cfu.confirmation.dialogue.message.selectedAddOnNewerJavaVersion"),
                        changesResult.getSelectedAddOns().iterator().next().getMinimumJavaVersion());
                return JOptionPane.showConfirmDialog(
                        parent,
                        baseMessage + question,
                        Constant.PROGRAM_NAME,
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            }

            String mainMessage;
            if (selectedAddOnsJavaIssue.size() == size) {
                mainMessage = Constant.messages.getString("cfu.confirmation.dialogue.message.addOnsNewerJavaVersion");
            } else {
                mainMessage = Constant.messages.getString("cfu.confirmation.dialogue.message.someSelectedAddOnsNewerJavaVersion");
            }

            JLabel label = new JLabel(
                    Constant.messages.getString("cfu.confirmation.dialogue.message.warnAddOnsNotRunJavaVersion"),
                    ManageAddOnsDialog.ICON_ADD_ON_ISSUES,
                    SwingConstants.LEADING);

            Object[] msgs = {
                    mainMessage,
                    createScrollableTable(new AddOnTableModel(selectedAddOnsJavaIssue, selectedAddOnsJavaIssue.size())),
                    label,
                    question };

            return JOptionPane.showConfirmDialog(parent, msgs, Constant.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        panel.add(tabs);

        int issues = selectedAddOnsJavaIssue.size();

        if (!selectedAddOnsJavaIssue.isEmpty()) {
            tabs.add(
                    Constant.messages.getString("cfu.confirmation.dialogue.tab.header.selectedAddOns"),
                    createScrollableTable(new AddOnTableModel(selectedAddOnsJavaIssue, selectedAddOnsJavaIssue.size())));
        }

        if (!changesResult.getUninstalls().isEmpty()) {
            tabs.add(
                    Constant.messages.getString("cfu.confirmation.dialogue.tab.header.uninstallations"),
                    createScrollableTable(new AddOnTableModel(changesResult.getUninstalls(), false)));
        }

        if (!updates.isEmpty()) {
            AddOnTableModel model = new AddOnTableModel(updates);
            issues += model.getMinimumJavaVersionIssues();
            tabs.add(Constant.messages.getString("cfu.confirmation.dialogue.tab.header.updats"), createScrollableTable(model));
        }

        if (!installs.isEmpty()) {
            AddOnTableModel model = new AddOnTableModel(installs);
            issues += model.getMinimumJavaVersionIssues();
            tabs.add(
                    Constant.messages.getString("cfu.confirmation.dialogue.tab.header.installations"),
                    createScrollableTable(model));
        }

        if (!dependents.isEmpty()) {
            AddOnTableModel model = new AddOnTableModel(dependents);
            issues += model.getMinimumJavaVersionIssues();
            tabs.add(
                    Constant.messages.getString("cfu.confirmation.dialogue.tab.header.softUninstalls"),
                    createScrollableTable(model));
        }

        SelectableAddOnTableModel optionalAddOnsTableModel = null;
        if (!changesResult.getOptionalAddOns().isEmpty()) {
            optionalAddOnsTableModel = new SelectableAddOnTableModel(changesResult.getOptionalAddOns());
            issues += optionalAddOnsTableModel.getMinimumJavaVersionIssues();
            tabs.add(
                    Constant.messages.getString("cfu.confirmation.dialogue.tab.header.optionalAddOns"),
                    createScrollableTable(optionalAddOnsTableModel));
        }

        if (!changesResult.getUnloadExtensions().isEmpty()) {
            ExtensionsTableModel model = new ExtensionsTableModel(changesResult.getUnloadExtensions());
            tabs.add(
                    Constant.messages.getString("cfu.confirmation.dialogue.tab.header.extensionUnloads"),
                    createScrollableTable(model));
        }

        if (!changesResult.getSoftUnloadExtensions().isEmpty()) {
            ExtensionsTableModel model = new ExtensionsTableModel(changesResult.getSoftUnloadExtensions());
            tabs.add(
                    Constant.messages.getString("cfu.confirmation.dialogue.tab.header.extensionSoftUnloads"),
                    createScrollableTable(model));
        }

        List<Object> optionPaneContents = new ArrayList<>();

        if (!changesResult.getOptionalAddOns().isEmpty() && changesResult.getUninstalls().isEmpty() && updates.isEmpty()
                && installs.isEmpty() && dependents.isEmpty() && changesResult.getSoftUnloadExtensions().isEmpty()
                && changesResult.getUnloadExtensions().isEmpty()) {
            optionPaneContents.add(Constant.messages.getString("cfu.confirmation.dialogue.message.suggestedChanges"));
        } else if (!changesResult.getOptionalAddOns().isEmpty()) {
            optionPaneContents.add(Constant.messages.getString("cfu.confirmation.dialogue.message.requiredSuggestedChanges"));
        } else {
            optionPaneContents.add(Constant.messages.getString("cfu.confirmation.dialogue.message.requiredChanges"));
        }

        optionPaneContents.add(panel);

        if (issues != 0) {
            String message;
            if (selectedAddOnsJavaIssue.size() == issues) {
                if (selectedAddOnsJavaIssue.size() == changesResult.getSelectedAddOns().size()) {
                    message = Constant.messages.getString("cfu.confirmation.dialogue.message.selectedAddOnsNewerJavaVersion");
                } else {
                    message = Constant.messages.getString("cfu.confirmation.dialogue.message.someUnnamedSelectedAddOnsNewerJavaVersion");
                }
            } else if (issues == 1) {
                message = Constant.messages.getString("cfu.confirmation.dialogue.message.addOnNewerJavaVersion");
            } else {
                message = Constant.messages.getString("cfu.confirmation.dialogue.message.someAddOnsNewerJavaVersion");
            }

            JLabel label = new JLabel(
                    Constant.messages.getString("cfu.confirmation.dialogue.message.warnUnknownNumberAddOnsNotRunJavaVersion"),
                    ManageAddOnsDialog.ICON_ADD_ON_ISSUES,
                    SwingConstants.LEADING);

            optionPaneContents.add(message);
            optionPaneContents.add(label);
        }

        optionPaneContents.add(question);

        boolean confirmed = JOptionPane.showConfirmDialog(
                parent,
                optionPaneContents.toArray(),
                Constant.PROGRAM_NAME,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

        if (confirmed && optionalAddOnsTableModel != null) {
            changesResult.getInstalls().addAll(optionalAddOnsTableModel.getSelectedAddOns());
        }

        return confirmed;
    }

    private Set<AddOn> getDependents(Set<AddOn> updates, Set<AddOn> ignoreAddOns) {
        Set<AddOn> dependents = new HashSet<>();
        for (AddOn update : updates) {
            addDependents(dependents, update, ignoreAddOns);
        }
        return dependents;
    }

    private void addDependents(Set<AddOn> dependents, AddOn addOn, Set<AddOn> ignoreAddOns) {
        for (AddOn availableAddOn : installedAddOns.getInstalledAddOns()) {
            if (!ignoreAddOns.contains(availableAddOn) && availableAddOn.dependsOn(addOn)
                    && dependents.contains(availableAddOn)) {
                dependents.add(availableAddOn);
                addDependents(dependents, availableAddOn, ignoreAddOns);
            }
        }
    }

    private static JScrollPane createScrollableTable(TableModel model) {
        JXTable table = new JXTable(model);
        table.setColumnControlVisible(true);
        table.setVisibleRowCount(Math.min(model.getRowCount() + 1, 5));
        table.packAll();
        return new JScrollPane(table);
    }

    /**
     * Calculates the changes required to update the given add-on.
     * <p>
     * It might require updating, installing or uninstalling other add-ons depending on the dependencies of the affected
     * add-ons.
     *
     * @param addOn the add-on that would be updated
     * @return the resulting changes with the add-ons that need to be updated, installed or uninstalled
     * @since 2.4.3
     */
    public AddOnChangesResult calculateUpdateChanges(AddOn addOn) {
        Set<AddOn> addOns = new HashSet<>();
        addOns.add(addOn);
        return calculateUpdateChanges(addOns);
    }

    /**
     * Calculates the changes required to update the given add-ons.
     * <p>
     * It might require updating, installing or uninstalling other add-ons depending on the dependencies of the affected
     * add-ons.
     *
     * @param addOns the add-ons that would be updated
     * @return the resulting changes with the add-ons that need to be updated, installed or uninstalled
     */
    public AddOnChangesResult calculateUpdateChanges(Set<AddOn> addOns) {
        return calculateChanges(addOns, true);
    }

    /**
     * Asks the user for confirmation of update changes.
     *
     * @param parent the parent component of the confirmation dialogue
     * @param changes the changes of the update
     * @return {@code true} if the user accept the changes, {@code false} otherwise
     */
    public boolean confirmUpdateChanges(Component parent, AddOnChangesResult changes) {
        return confirmChanges(parent, changes, true);
    }

    private AddOnChangesResult calculateChanges(Set<AddOn> selectedAddOns, boolean updating) {
        Set<AddOn> oldVersions = new HashSet<>();
        Set<AddOn> uninstalls = new HashSet<>();
        Set<AddOn> newVersions = new HashSet<>();
        Set<AddOn> installs = new HashSet<>();

        if (updating) {
            for (AddOn update : selectedAddOns) {
                AddOn oldVersion = installedAddOns.getAddOn(update.getId());
                oldVersions.add(oldVersion);
            }
        }

        boolean newerJavaVersion = false;
        for (AddOn addOn : selectedAddOns) {
            newerJavaVersion |= addDependencies(addOn, selectedAddOns, oldVersions, newVersions, installs);
        }

        Set<AddOn> remainingInstalledAddOns = new HashSet<>();
        for (AddOn addOn : installedAddOns.getAddOns()) {
            if (!contains(selectedAddOns, addOn) && !contains(newVersions, addOn)) {
                remainingInstalledAddOns.add(addOn);
            }
        }

        Set<AddOn> expectedInstalledAddOns = new HashSet<>(remainingInstalledAddOns);
        expectedInstalledAddOns.addAll(selectedAddOns);
        expectedInstalledAddOns.addAll(installs);
        expectedInstalledAddOns.addAll(newVersions);

        for (AddOn addOn : remainingInstalledAddOns) {
            if (addOn.calculateRunRequirements(expectedInstalledAddOns).hasDependencyIssue()) {
                uninstalls.add(addOn);
            }
        }

        for (Iterator<AddOn> it = uninstalls.iterator(); it.hasNext();) {
            AddOn addOn = it.next();
            AddOn addOnUpdate = availableAddOns.getAddOn(addOn.getId());
            if (addOnUpdate != null && !addOnUpdate.equals(addOn)) {
                it.remove();
                oldVersions.add(addOn);
                newVersions.add(addOnUpdate);
                newerJavaVersion |= addDependencies(addOnUpdate, selectedAddOns, oldVersions, newVersions, installs);
            }
        }

        for (Iterator<AddOn> it = uninstalls.iterator(); it.hasNext();) {
            AddOn addOn = it.next();
            if (contains(installs, addOn)
                    || contains(newVersions, addOn)
                    || (addOn.calculateRunRequirements(installedAddOns.getAddOns()).hasDependencyIssue() && !containsAny(
                            addOn.getIdsAddOnDependencies(),
                            uninstalls))) {
                it.remove();
            }
        }

        if (updating) {
            newVersions.addAll(selectedAddOns);
        } else {
            installs.addAll(selectedAddOns);
        }

        expectedInstalledAddOns = new HashSet<>(remainingInstalledAddOns);
        expectedInstalledAddOns.removeAll(uninstalls);
        expectedInstalledAddOns.removeAll(oldVersions);
        expectedInstalledAddOns.addAll(installs);
        expectedInstalledAddOns.addAll(newVersions);

        Set<Extension> unloadExtensions = new HashSet<>();
        Set<Extension> softUnloadExtensions = new HashSet<>();
        Set<AddOn> optionalAddOns = new HashSet<>();
        for (AddOn addOn : expectedInstalledAddOns) {
            List<String> extensionsWithDeps = addOn.getExtensionsWithDeps();
            for (Extension extension : addOn.getLoadedExtensionsWithDeps()) {
                AddOn.AddOnRunRequirements requirements = addOn.calculateExtensionRunRequirements(
                        extension,
                        expectedInstalledAddOns);
                AddOn.ExtensionRunRequirements extReqs = requirements.getExtensionRequirements().get(0);
                if (!extReqs.isRunnable()) {
                    unloadExtensions.add(extension);
                } else if (CollectionUtils.containsAny(extReqs.getDependencies(), oldVersions)) {
                    softUnloadExtensions.add(extension);
                }
                extensionsWithDeps.remove(extReqs.getClassname());
            }

            for (String classname : extensionsWithDeps) {
                AddOn.AddOnRunRequirements requirements = addOn.calculateExtensionRunRequirements(
                        classname,
                        availableAddOns.getAddOns());
                AddOn.ExtensionRunRequirements extReqs = requirements.getExtensionRequirements().get(0);
                if (extReqs.isRunnable()) {
                    optionalAddOns.addAll(extReqs.getDependencies());
                }
            }
        }

        optionalAddOns.removeAll(installs);
        optionalAddOns.removeAll(newVersions);
        optionalAddOns.removeAll(remainingInstalledAddOns);

        return new AddOnChangesResult(
                selectedAddOns,
                oldVersions,
                uninstalls,
                newVersions,
                installs,
                newerJavaVersion,
                optionalAddOns,
                unloadExtensions,
                softUnloadExtensions);
    }

    /**
     * Calculates the changes required to uninstall the given add-ons.
     * <p>
     * It might require uninstalling other add-ons depending on the dependencies of the affected add-ons.
     *
     * @param selectedAddOns the add-ons that would be uninstalled
     * @return the resulting changes with the add-ons that need to be uninstalled
     */
    public UninstallationResult calculateUninstallChanges(Set<AddOn> selectedAddOns) {
        List<AddOn> remainingAddOns = new ArrayList<>(installedAddOns.getAddOns());
        remainingAddOns.removeAll(selectedAddOns);

        Set<AddOn> uninstallations = new HashSet<>();
        List<AddOn> addOnsToCheck = new ArrayList<>(remainingAddOns);
        while (!addOnsToCheck.isEmpty()) {
            AddOn addOn = addOnsToCheck.remove(0);
            AddOn.AddOnRunRequirements requirements = addOn.calculateRunRequirements(remainingAddOns);

            if (!requirements.hasDependencyIssue()) {
                addOnsToCheck.removeAll(requirements.getDependencies());
            } else if (AddOn.InstallationStatus.UNINSTALLATION_FAILED != addOn.getInstallationStatus()) {
                uninstallations.add(addOn);
            }
        }

        for (Iterator<AddOn> it = uninstallations.iterator(); it.hasNext();) {
            AddOn addOn = it.next();
            if (addOn.calculateRunRequirements(installedAddOns.getAddOns()).hasDependencyIssue()
                    && !containsAny(addOn.getIdsAddOnDependencies(), uninstallations)) {
                it.remove();
            }
        }

        remainingAddOns.removeAll(uninstallations);
        Set<Extension> extensions = new HashSet<>();
        for (AddOn addOn : remainingAddOns) {
            if (addOn.hasExtensionsWithDeps()) {
                for (Extension ext : addOn.getLoadedExtensions()) {
                    AddOn.AddOnRunRequirements requirements = addOn.calculateExtensionRunRequirements(ext, remainingAddOns);
                    if (!requirements.getExtensionRequirements().isEmpty()) {
                        AddOn.ExtensionRunRequirements extReqs = requirements.getExtensionRequirements().get(0);
                        if (!extReqs.isRunnable()) {
                            extensions.add(ext);
                        }
                    }
                }
            }
        }

        uninstallations.addAll(selectedAddOns);
        return new UninstallationResult(selectedAddOns, uninstallations, extensions);
    }

    private boolean containsAny(List<String> addOnIds, Collection<AddOn> addOns) {
        for (String id : addOnIds) {
            for (AddOn addOn : addOns) {
                if (id.equals(addOn.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Asks the user for confirmation of uninstall changes.
     * <p>
     * User will also be warned about add-ons that are being uninstalled which are required by add-ons being downloaded.
     *
     * @param parent the parent component of the confirmation dialogue
     * @param result the calculation result of the uninstallation
     * @param addOnsBeingDownloaded the add-ons being downloaded to check if depend on the add-ons being uninstalled
     * @return {@code true} if the user accept the changes, {@code false} otherwise
     */
    public boolean confirmUninstallChanges(Component parent, UninstallationResult result, Set<AddOn> addOnsBeingDownloaded) {

        Set<AddOn> forcedUninstallations = new HashSet<>(result.getUninstallations());
        forcedUninstallations.removeAll(result.getSelectedAddOns());

        boolean dependencyDownloadFound = false;
        for (AddOn addOnDownloading : addOnsBeingDownloaded) {
            if (containsAny(addOnDownloading.getIdsAddOnDependencies(), forcedUninstallations)) {
                dependencyDownloadFound = true;
                break;
            }
        }

        if (!dependencyDownloadFound) {
            for (AddOn addOnDownloading : addOnsBeingDownloaded) {
                if (containsAny(addOnDownloading.getIdsAddOnDependencies(), result.getSelectedAddOns())) {
                    dependencyDownloadFound = true;
                    break;
                }
            }
        }

        if (dependencyDownloadFound) {
            if (JOptionPane.showConfirmDialog(
                    parent,
                    new Object[] {
                            Constant.messages.getString("cfu.confirmation.dialogue.message.uninstallsRequiredByAddOnsDownloading"),
                            Constant.messages.getString("cfu.confirmation.dialogue.message.continueWithUninstallation") },
                    Constant.PROGRAM_NAME,
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        if (forcedUninstallations.isEmpty() && result.getExtensions().isEmpty()) {
            return JOptionPane.showConfirmDialog(
                    parent,
                    Constant.messages.getString("cfu.uninstall.confirm"),
                    Constant.PROGRAM_NAME,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        if (result.getExtensions().isEmpty()) {
            return JOptionPane.showConfirmDialog(
                    parent,
                    new Object[] {
                            Constant.messages.getString("cfu.uninstall.dependentAddOns.confirm"),
                            createScrollableTable(new AddOnTableModel(forcedUninstallations, false)),
                            Constant.messages.getString("cfu.confirmation.dialogue.message.continueWithUninstallation") },
                    Constant.PROGRAM_NAME,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        if (forcedUninstallations.isEmpty()) {
            return JOptionPane.showConfirmDialog(
                    parent,
                    new Object[] {
                            Constant.messages.getString("cfu.uninstall.dependentExtensions.confirm"),
                            createScrollableTable(new ExtensionsTableModel(result.getExtensions())),
                            Constant.messages.getString("cfu.confirmation.dialogue.message.continueWithUninstallation") },
                    Constant.PROGRAM_NAME,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        panel.add(tabs);

        tabs.add(
                Constant.messages.getString("cfu.confirmation.dialogue.tab.header.uninstallations"),
                createScrollableTable(new AddOnTableModel(forcedUninstallations, false)));

        tabs.add(
                Constant.messages.getString("cfu.confirmation.dialogue.tab.header.extensionUnloads"),
                createScrollableTable(new ExtensionsTableModel(result.getExtensions())));

        List<Object> optionPaneContents = new ArrayList<>();
        optionPaneContents.add(Constant.messages.getString("cfu.uninstall.dependentAddonsAndExtensions.confirm"));
        optionPaneContents.add(panel);
        optionPaneContents.add(Constant.messages.getString("cfu.confirmation.dialogue.message.continueWithUninstallation"));

        return JOptionPane.showConfirmDialog(
                parent,
                optionPaneContents.toArray(),
                Constant.PROGRAM_NAME,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /**
     * The result of an installation or update of add-on(s).
     * <p>
     * Contains all add-ons that need to be installed, updated and uninstalled as the result of the changes.
     */
    public static class AddOnChangesResult {

        private final Set<AddOn> selectedAddOns;
        private final Set<AddOn> oldVersions;
        private final Set<AddOn> uninstalls;
        private final Set<AddOn> newVersions;
        private final Set<AddOn> installs;
        private final boolean newerJavaVersion;
        private final Set<AddOn> optionalAddOns;
        private final Set<Extension> unloadExtensions;
        private final Set<Extension> softUnloadExtensions;

        private AddOnChangesResult(
                Set<AddOn> selectedAddOns,
                Set<AddOn> oldVersions,
                Set<AddOn> uninstalls,
                Set<AddOn> newVersions,
                Set<AddOn> installs,
                boolean newerJavaVersion,
                Set<AddOn> optionalAddOns,
                Set<Extension> unloadExtensions,
                Set<Extension> softUnloadExtensions) {
            this.selectedAddOns = selectedAddOns;
            this.oldVersions = oldVersions;
            this.uninstalls = uninstalls;
            this.newVersions = newVersions;
            this.installs = installs;
            this.newerJavaVersion = newerJavaVersion;
            this.optionalAddOns = optionalAddOns;
            this.unloadExtensions = unloadExtensions;
            this.softUnloadExtensions = softUnloadExtensions;
        }

        /**
         * Gets the add-ons selected for installation or updated.
         *
         * @return the add-ons selected for installation or updated
         */
        public Set<AddOn> getSelectedAddOns() {
            return selectedAddOns;
        }

        /**
         * Gets old versions of the add-ons that need to be updated as result of the changes.
         *
         * @return the old versions of the add-ons that need to be updated
         */
        public Set<AddOn> getOldVersions() {
            return oldVersions;
        }

        /**
         * Gets the add-ons that need to be uninstalled as result of the changes.
         *
         * @return the the add-ons that need to be uninstalled
         */
        public Set<AddOn> getUninstalls() {
            return uninstalls;
        }

        /**
         * Gets the new versions of add-ons that need to be updated as result of the changes.
         *
         * @return the new versions of the add-ons
         */
        public Set<AddOn> getNewVersions() {
            return newVersions;
        }

        /**
         * Gets the add-ons that need to be installed as result of the changes.
         *
         * @return the add-ons that need to be installed
         * @see #getNewVersions()
         */
        public Set<AddOn> getInstalls() {
            return installs;
        }

        /**
         * Tells whether or not a newer Java version is required by any of add-ons as result of the changes.
         *
         * @return {@code true} if a newer Java version is required by any of the add-ons, {@code false} otherwise
         */
        public boolean isNewerJavaVersionRequired() {
            return newerJavaVersion;
        }

        /**
         * Gets the optional add-ons.
         *
         * @return the optional add-ons
         */
        public Set<AddOn> getOptionalAddOns() {
            return optionalAddOns;
        }

        /**
         * Gets the extensions that have to be unloaded as result of the changes.
         *
         * @return the extensions that have to be unloaded
         */
        public Set<Extension> getUnloadExtensions() {
            return unloadExtensions;
        }

        /**
         * Gets the extensions that have to be soft unloaded as result of the changes. The extension will be unloaded and then
         * loaded once the dependency is updated-
         *
         * @return the extensions that have to be soft unloaded
         */
        public Set<Extension> getSoftUnloadExtensions() {
            return softUnloadExtensions;
        }

        /**
         * Add the contents from the specified results 
         * @param result
         */
		public void addResults(AddOnChangesResult result) {
	        selectedAddOns.addAll(result.getSelectedAddOns());
	        oldVersions.addAll(result.oldVersions);
	        uninstalls.addAll(result.getUninstalls());
	        newVersions.addAll(result.getNewVersions());
	        installs.addAll(result.getInstalls());
	        optionalAddOns.addAll(result.getOptionalAddOns());
	        unloadExtensions.addAll(result.getUnloadExtensions());
	        softUnloadExtensions.addAll(result.getSoftUnloadExtensions());
		}
    }

    /**
     * The result of an uninstallation of add-on(s).
     * <p>
     * Contains all add-ons that need to be uninstalled as the result of an uninstallation or several.
     */
    public static class UninstallationResult {

        private final Set<AddOn> selectedAddOns;
        private final Set<AddOn> uninstallations;
        private final Set<Extension> extensions;

        private UninstallationResult(Set<AddOn> selectedAddOns, Set<AddOn> uninstallations, Set<Extension> extensions) {
            this.selectedAddOns = selectedAddOns;
            this.uninstallations = uninstallations;
            this.extensions = extensions;
        }

        /**
         * Gets the add-ons selected for uninstallation.
         *
         * @return the add-ons selected for uninstallation
         */
        public Set<AddOn> getSelectedAddOns() {
            return selectedAddOns;
        }

        /**
         * Gets all the add-ons (selected and dependencies) that need to be uninstalled as result of the uninstallations.
         *
         * @return all the add-ons that need to be uninstalled
         */
        public Set<AddOn> getUninstallations() {
            return uninstallations;
        }

        /**
         * Gets the extensions that are affected by the changes, but not its add-on.
         *
         * @return the extensions affected by the changes
         */
        public Set<Extension> getExtensions() {
            return extensions;
        }
    }

    private static class AddOnTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 5446781970087315105L;

        private static final String[] COLUMNS = {
                Constant.messages.getString("cfu.generic.table.header.addOn"),
                Constant.messages.getString("cfu.generic.table.header.version"),
                Constant.messages.getString("cfu.generic.table.header.minimumJavaVersion") };

        private final List<AddOn> addOns;
        private final int columnCount;
        private final int issues;

        public AddOnTableModel(Collection<AddOn> addOns) {
            this(addOns, true);
        }

        public AddOnTableModel(Collection<AddOn> addOns, boolean checkMinimumJavaVersion) {
            this.addOns = new ArrayList<>(addOns);

            int count = 0;
            if (checkMinimumJavaVersion) {
                for (AddOn addOn : addOns) {
                    if (!addOn.canRunInCurrentJavaVersion()) {
                        ++count;
                    }
                }
            }
            issues = count;
            columnCount = issues != 0 ? COLUMNS.length : COLUMNS.length - 1;
        }

        public AddOnTableModel(Collection<AddOn> addOns, int numberOfIssues) {
            this.addOns = new ArrayList<>(addOns);
            issues = numberOfIssues;
            columnCount = numberOfIssues != 0 ? COLUMNS.length : COLUMNS.length - 1;
        }

        public int getMinimumJavaVersionIssues() {
            return issues;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public int getColumnCount() {
            return columnCount;
        }

        @Override
        public int getRowCount() {
            return addOns.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AddOn addOn = getAddOn(rowIndex);
            switch (columnIndex) {
            case 0:
                return addOn.getName();
            case 1:
                return Integer.valueOf(addOn.getFileVersion());
            case 2:
                return addOn.getMinimumJavaVersion();
            default:
                return "";
            }
        }

        protected AddOn getAddOn(int rowIndex) {
            return addOns.get(rowIndex);
        }
    }

    private static class SelectableAddOnTableModel extends AddOnTableModel {

        private static final long serialVersionUID = 2337381848530495407L;

        private final Boolean[] selections;

        public SelectableAddOnTableModel(Collection<AddOn> addOns) {
            super(addOns, true);

            selections = new Boolean[addOns.size()];
            for (int i = 0; i < selections.length; i++) {
                selections[i] = Boolean.FALSE;
            }
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "";
            }
            return super.getColumnName(column - 1);
        }

        @Override
        public int getColumnCount() {
            return super.getColumnCount() + 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            }
            return super.getColumnClass(columnIndex - 1);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return selections[rowIndex];
            }
            return super.getValueAt(rowIndex, columnIndex - 1);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0 && aValue instanceof Boolean) {
                selections[rowIndex] = (Boolean) aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        public List<AddOn> getSelectedAddOns() {
            List<AddOn> selectedAddOns = new ArrayList<>(selections.length);
            for (int i = 0; i < selections.length; i++) {
                if (selections[i].booleanValue()) {
                    selectedAddOns.add(getAddOn(i));
                }
            }
            return selectedAddOns;
        }
    }

    private static class ExtensionsTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 5446781970087315105L;

        private static final String[] COLUMNS = { Constant.messages.getString("cfu.generic.table.header.extension") };

        private final List<Extension> extensions;

        public ExtensionsTableModel(Collection<Extension> extensions) {
            this.extensions = new ArrayList<>(extensions);
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public int getRowCount() {
            return extensions.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Extension extension = extensions.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return extension.getName();
            default:
                return "";
            }
        }
    }
}
