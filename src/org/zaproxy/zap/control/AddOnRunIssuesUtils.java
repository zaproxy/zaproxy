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
package org.zaproxy.zap.control;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTree;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;

/**
 * An utility/helper class that extract textual representations of running issues of add-ons and show warning messages of
 * add-ons that can not be run.
 *
 * @since 2.4.0
 */
public final class AddOnRunIssuesUtils {

    private static final Logger LOGGER = Logger.getLogger(AddOnRunIssuesUtils.class);

    private AddOnRunIssuesUtils() {
    }

    /**
     * Shows a warning dialogue with the add-ons and its corresponding running issues or the issues if is extensions.
     * <p>
     * The dialogue is composed with the given {@code message} and a tree in which are shown the add-ons and its issues as child
     * nodes of the add-ons.
     *
     * @param message the main message shown in the dialogue
     * @param availableAddOns the add-ons that are available, used to create and check the running issues
     * @param addOnsNotRunnable the add-ons with running issues that will be shown in the tree
     */
    public static void showWarningMessageAddOnsNotRunnable(
            String message,
            AddOnCollection availableAddOns,
            Collection<AddOn> addOnsNotRunnable) {

        Object[] msgs = {
                message,
                createScrollableTreeAddOnsNotRunnable(
                        availableAddOns,
                        addOnsNotRunnable.toArray(new AddOn[addOnsNotRunnable.size()])) };

        JOptionPane.showMessageDialog(
                View.getSingleton().getMainFrame(),
                msgs,
                Constant.PROGRAM_NAME,
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Creates a scrollable tree with the given add-ons as root nodes and its issues as child nodes.
     *
     * @param availableAddOns the add-ons that are available, used to create check the running issues
     * @param addOnsNotRunnable the add-ons with running issues that will be shown in the tree
     * @return the tree wrapper in a {@code JSCrollPane}
     */
    private static JScrollPane createScrollableTreeAddOnsNotRunnable(
            final AddOnCollection availableAddOns,
            AddOn... addOnsNotRunnable) {
        AddOnSearcher addOnSearcher = new AddOnSearcher() {

            @Override
            public AddOn searchAddOn(String id) {
                return availableAddOns.getAddOn(id);
            }
        };

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("");
        for (AddOn addOn : addOnsNotRunnable) {
            DefaultMutableTreeNode addOnNode = new DefaultMutableTreeNode(addOn.getName());
            AddOn.AddOnRunRequirements requirements = addOn.calculateRunRequirements(availableAddOns.getAddOns());
            List<String> issues = getUiRunningIssues(requirements, addOnSearcher);

            if (issues.isEmpty()) {
                issues.addAll(getUiExtensionsRunningIssues(requirements, addOnSearcher));
            }

            for (String issue : issues) {
                addOnNode.add(new DefaultMutableTreeNode(issue));
            }
            rootNode.add(addOnNode);
        }

        JXTree tree = new JXTree(new DefaultTreeModel(rootNode));
        tree.setVisibleRowCount(5);
        tree.setEditable(false);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.expandAll();

        return new JScrollPane(tree);
    }

    /**
     * Shows a confirmation dialogue (yes-no question), with the given message at the top, followed by a tree in which is shown
     * the add-on with its corresponding running issues and, at the bottom before the yes and no buttons, the given question.
     *
     * @param message the main message shown in the dialogue
     * @param question the questions shown at the bottom of the dialogue, before the buttons
     * @param availableAddOns the add-ons that are available, used to create check the running issues
     * @param addOnNotRunnable the add-on with running issues that will be shown in the tree
     * @return {@code true} if it is confirmed, {@code false} otherwise
     */
    public static boolean askConfirmationAddOnNotRunnable(
            String message,
            String question,
            AddOnCollection availableAddOns,
            AddOn addOnNotRunnable) {
        Object[] msgs = { message, createScrollableTreeAddOnsNotRunnable(availableAddOns, addOnNotRunnable), question };

        return JOptionPane.showConfirmDialog(
                View.getSingleton().getMainFrame(),
                msgs,
                Constant.PROGRAM_NAME,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /**
     * Returns the textual representations of the running issues (Java version and dependency), if any.
     * <p>
     * The messages are internationalised thus suitable for UI components.
     *
     * @param requirements the run requirements of the add-on
     * @param addOnSearcher the class responsible for searching add-ons with a given id, used to search for add-ons that are
     *            missing for the add-on
     * @return a {@code List} containing all the running issues of the add-on, empty if none
     * @see #getRunningIssues(AddOn.BaseRunRequirements)
     * @see #getUiExtensionsRunningIssues(AddOn.AddOnRunRequirements, AddOnSearcher)
     */
    public static List<String> getUiRunningIssues(AddOn.BaseRunRequirements requirements, AddOnSearcher addOnSearcher) {
        List<String> issues = new ArrayList<>(2);
        if (requirements.isNewerJavaVersionRequired()) {
            if (requirements.getAddOn() != requirements.getAddOnMinimumJavaVersion()) {
                issues.add(MessageFormat.format(
                        Constant.messages.getString("cfu.warn.addon.with.missing.requirements.javaversion.dependency"),
                        requirements.getMinimumJavaVersion(),
                        (SystemUtils.JAVA_VERSION == null
                                ? Constant.messages.getString("cfu.warn.unknownJavaVersion")
                                : SystemUtils.JAVA_VERSION),
                        requirements.getAddOnMinimumJavaVersion().getName()));
            } else {
                issues.add(MessageFormat.format(
                        Constant.messages.getString("cfu.warn.addon.with.missing.requirements.javaversion"),
                        requirements.getMinimumJavaVersion(),
                        (SystemUtils.JAVA_VERSION == null
                                ? Constant.messages.getString("cfu.warn.unknownJavaVersion")
                                : SystemUtils.JAVA_VERSION)));
            }
        }

        if (requirements.hasDependencyIssue()) {
            List<Object> issueDetails = requirements.getDependencyIssueDetails();
            AddOn addOn;
            String message = null;
            switch (requirements.getDependencyIssue()) {
            case CYCLIC:
                message = Constant.messages.getString("cfu.warn.addon.with.missing.requirements.addon.id");
                break;
            case OLDER_VERSION:
                // Do not set a message, the state is already reported as requiring an update.
                break;
            case MISSING:
                String addOnId = (String) issueDetails.get(0);
                if (addOnSearcher != null) {
                    addOn = addOnSearcher.searchAddOn(addOnId);
                } else {
                    addOn = null;
                }
                if (addOn == null) {
                    message = MessageFormat.format(
                            Constant.messages.getString("cfu.warn.addon.with.missing.requirements.addon.id"),
                            addOnId);

                } else {
                    message = MessageFormat.format(
                            Constant.messages.getString("cfu.warn.addon.with.missing.requirements.addon"),
                            addOn.getName());
                }
                break;
            case PACKAGE_VERSION_NOT_BEFORE:
                addOn = (AddOn) issueDetails.get(0);
                message = MessageFormat.format(
                        Constant.messages.getString("cfu.warn.addon.with.missing.requirements.addon.version.notBefore"),
                        addOn.getName(),
                        issueDetails.get(1),
                        Integer.valueOf(addOn.getFileVersion()));
                break;
            case PACKAGE_VERSION_NOT_FROM:
                addOn = (AddOn) issueDetails.get(0);
                message = MessageFormat.format(
                        Constant.messages.getString("cfu.warn.addon.with.missing.requirements.addon.version.notAfter"),
                        addOn.getName(),
                        issueDetails.get(1),
                        Integer.valueOf(addOn.getFileVersion()));
                break;
            case VERSION:
                addOn = (AddOn) issueDetails.get(0);
                if (addOn.getVersion() == null) {
                    message = MessageFormat.format(
                            Constant.messages.getString("cfu.warn.addon.with.missing.requirements.addon.semver.notAvailable"),
                            addOn.getName(),
                            issueDetails.get(1));
                } else {
                    message = MessageFormat.format(
                            Constant.messages.getString("cfu.warn.addon.with.missing.requirements.addon.semver"),
                            addOn.getName(),
                            issueDetails.get(1),
                            addOn.getVersion());
                }
                break;
            default:
                message = Constant.messages.getString("cfu.warn.addon.with.missing.requirements.unknown");
                LOGGER.warn("Failed to handle dependency issue with name \"" + requirements.getDependencyIssue().name()
                        + "\" and details: " + issueDetails);
                break;
            }

            if (message != null) {
                issues.add(message);
            }
        }
        return issues;
    }

    /**
     * Returns the textual representations of the running issues (Java version and dependency) of the extensions of hte add-on,
     * if any.
     * <p>
     * The messages are internationalised thus suitable for UI components.
     *
     * @param requirements the run requirements of the add-on, whose extensions' run requirements will be used
     * @param addOnSearcher the class responsible for searching add-ons with a given id, used to search for add-ons that are
     *            missing for the add-on
     * @return a {@code List} containing all the running issues of the add-on, empty if none
     * @see #getRunningIssues(AddOn.BaseRunRequirements)
     * @see #getUiExtensionsRunningIssues(AddOn.AddOnRunRequirements, AddOnSearcher)
     */
    public static List<String> getUiExtensionsRunningIssues(AddOn.AddOnRunRequirements requirements, AddOnSearcher addOnSearcher) {
        if (!requirements.hasExtensionsWithRunningIssues()) {
            return new ArrayList<>(0);
        }

        List<String> issues = new ArrayList<>(10);
        for (AddOn.ExtensionRunRequirements extReqs : requirements.getExtensionRequirements()) {
            issues.addAll(getUiRunningIssues(extReqs, addOnSearcher));
        }
        return issues;
    }

    /**
     * Returns the textual representations of the running issues (Java version and dependency), if any.
     * <p>
     * The messages are not internationalised, should be used only for logging and non UI uses.
     *
     * @param requirements the run requirements of the add-on or extension
     * @return a {@code List} containing all the running issues of the add-on or extension, empty if none
     * @see #getUiRunningIssues(AddOn.BaseRunRequirements, AddOnSearcher)
     * @see #getUiExtensionsRunningIssues(AddOn.AddOnRunRequirements, AddOnSearcher)
     */
    public static List<String> getRunningIssues(AddOn.BaseRunRequirements requirements) {
        List<String> issues = new ArrayList<>(2);
        String issue = getJavaVersionIssue(requirements);
        if (issue != null) {
            issues.add(issue);
        }
        issue = getDependencyIssue(requirements);
        if (issue != null) {
            issues.add(issue);
        }
        return issues;
    }

    /**
     * Returns the textual representation of the issues that prevent the extensions of the add-on from being run, if any.
     * <p>
     * The messages are not internationalised, should be used only for logging and non UI uses.
     *
     * @param requirements the run requirements of the add-on whose extensions' run requirements will be used
     * @return a {@code String} representing the running issue, {@code null} if none.
     * @see AddOn.AddOnRunRequirements#getExtensionRequirements()
     */
    public static List<String> getExtensionsRunningIssues(AddOn.AddOnRunRequirements requirements) {
        if (!requirements.hasExtensionsWithRunningIssues()) {
            return new ArrayList<>(0);
        }

        List<String> issues = new ArrayList<>(10);
        for (AddOn.ExtensionRunRequirements extReqs : requirements.getExtensionRequirements()) {
            issues.addAll(getRunningIssues(extReqs));
        }
        return issues;
    }

    /**
     * Returns the textual representation of the Java version issue that prevents the add-on or extension from being run, if
     * any.
     * <p>
     * The message is not internationalised, should be used only for logging and non UI uses.
     *
     * @param requirements the run requirements of the add-on or extension
     * @return a {@code String} representing the running issue, {@code null} if none.
     */
    public static String getJavaVersionIssue(AddOn.BaseRunRequirements requirements) {
        if (!requirements.isNewerJavaVersionRequired()) {
            return null;
        }

        if (requirements.getAddOn() != requirements.getAddOnMinimumJavaVersion()) {
            return MessageFormat.format(
                    "Minimum Java version: {0} (\"{1}\" add-on)",
                    requirements.getMinimumJavaVersion(),
                    requirements.getAddOnMinimumJavaVersion().getName());
        }
        return MessageFormat.format("Minimum Java version: {0}", requirements.getMinimumJavaVersion());
    }

    /**
     * Returns the textual representation of the issue that prevents the add-on or extension from being run, if any.
     * <p>
     * The messages are not internationalised, should be used only for logging and non UI uses.
     *
     * @param requirements the run requirements of the add-on or extension
     * @return a {@code String} representing the running issue, {@code null} if none.
     */
    public static String getDependencyIssue(AddOn.BaseRunRequirements requirements) {
        if (!requirements.hasDependencyIssue()) {
            return null;
        }

        List<Object> issueDetails = requirements.getDependencyIssueDetails();
        switch (requirements.getDependencyIssue()) {
        case CYCLIC:
            return "Cyclic dependency with: " + issueDetails.get(0);
        case OLDER_VERSION:
            return "Older version still installed: " + issueDetails.get(0);
        case MISSING:
            String addOnId = (String) issueDetails.get(0);
            return MessageFormat.format("Add-On with ID \"{0}\"", addOnId);
        case PACKAGE_VERSION_NOT_BEFORE:
            AddOn addOn = (AddOn) issueDetails.get(0);
            return MessageFormat.format(
                    "Add-on \"{0}\" with version not before {1} (found version {2})",
                    addOn.getName(),
                    issueDetails.get(1),
                    Integer.valueOf(addOn.getFileVersion()));
        case PACKAGE_VERSION_NOT_FROM:
            addOn = (AddOn) issueDetails.get(0);
            return MessageFormat.format(
                    "Add-on \"{0}\" with version not after {1} (found version {2})",
                    addOn.getName(),
                    issueDetails.get(1),
                    Integer.valueOf(addOn.getFileVersion()));
        case VERSION:
            addOn = (AddOn) issueDetails.get(0);
            if (addOn.getVersion() == null) {
                return MessageFormat.format(
                        "Add-on \"{0}\" with semantic version >= {1} (found no semantic version)",
                        addOn.getName(),
                        issueDetails.get(1));
            }
            return MessageFormat.format(
                    "Add-on \"{0}\" with semantic version >= {1} (found version {2})",
                    addOn.getName(),
                    issueDetails.get(1),
                    addOn.getVersion());
        default:
            LOGGER.warn("Failed to handle dependency issue with name \"" + requirements.getDependencyIssue().name()
                    + "\" and details: " + issueDetails);
            return null;
        }
    }

    /**
     * Interface to search for add-ons with a given id.
     * <p>
     * Used to search for missing add-ons as reported by run requirements of an add-on.
     * 
     * @see AddOn.BaseRunRequirements.DependencyIssue#MISSING
     */
    public static interface AddOnSearcher {

        /**
         * Returns an add-on with the given id, if none is found returns {@code null}.
         *
         * @param id the id of the add-on
         * @return the add-on, or {@code null} if not found
         */
        AddOn searchAddOn(String id);
    }
}
