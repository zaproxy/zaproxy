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
package org.zaproxy.zap.control;

import java.io.File;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public class AddOnCollection {

    public enum Platform {
        daily,
        windows,
        linux,
        mac
    }

    private static final Logger logger = LogManager.getLogger(AddOnCollection.class);
    private ZapRelease zapRelease = null;
    private List<AddOn> addOns = new ArrayList<>();
    private File downloadDir = new File(Constant.FOLDER_LOCAL_PLUGIN);
    private Platform platform;

    public AddOnCollection(ZapXmlConfiguration config, Platform platform) {
        this(config, platform, true);
    }

    public AddOnCollection(
            ZapXmlConfiguration config,
            Platform platform,
            boolean allowAddOnsWithDependencyIssues) {
        this.platform = platform;
        this.load(config);

        if (!allowAddOnsWithDependencyIssues) {
            List<AddOn> checkedAddOns = new ArrayList<>(addOns);
            List<AddOn> runnableAddOns = new ArrayList<>(addOns.size());
            while (!checkedAddOns.isEmpty()) {
                AddOn addOn = checkedAddOns.remove(0);
                // Shouldn't happen but make sure to not show add-ons that wouldn't run, or one of
                // its extensions because of dependency issues.
                AddOn.AddOnRunRequirements requirements =
                        addOn.calculateInstallRequirements(addOns);
                if (requirements.hasDependencyIssue()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Ignoring add-on {} because of dependency issue: {}",
                                addOn.getName(),
                                AddOnRunIssuesUtils.getDependencyIssue(requirements));
                    }
                    if (AddOn.AddOnRunRequirements.DependencyIssue.CYCLIC
                            == requirements.getDependencyIssue()) {
                        @SuppressWarnings("unchecked")
                        Set<AddOn> cyclicChain =
                                (Set<AddOn>) requirements.getDependencyIssueDetails().get(0);
                        checkedAddOns.removeAll(cyclicChain);
                    }
                } else if (requirements.hasExtensionsWithRunningIssues()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Ignoring add-on {} because of dependency issue in an extension: {}",
                                addOn.getName(),
                                AddOnRunIssuesUtils.getDependencyIssue(requirements));
                    }
                } else {
                    runnableAddOns.add(addOn);
                }
            }
            addOns = runnableAddOns;
        }
    }

    private void load(ZapXmlConfiguration config) {
        try {
            // See if theres a ZAP release defined
            String version = config.getString("core.version");
            if (Platform.daily.equals(platform)) {
                // Daily releases take precedence even if running on Kali as they will have been
                // manually installed
                version = config.getString("core.daily-version", version);
            } else if (Constant.isKali()) {
                version = config.getString("core.kali-version", version);
            }
            if (version != null && version.length() > 0) {
                String relUrlStr = config.getString("core.relnotes-url", null);
                URL relUrl = null;
                if (relUrlStr != null) {
                    relUrl = new URL(relUrlStr);
                }

                String platformPrefix = "core." + platform.name() + ".";
                this.zapRelease =
                        new ZapRelease(
                                version,
                                new URL(config.getString(platformPrefix + "url")),
                                config.getString(platformPrefix + "file"),
                                config.getLong(platformPrefix + "size"),
                                config.getString("core.relnotes"),
                                relUrl,
                                config.getString(platformPrefix + "hash"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            // And then load the addons
            String[] addOnIds = config.getStringArray("addon");
            for (String id : addOnIds) {
                logger.debug("Found add-on {}", id);

                AddOn ao;
                try {
                    ao = new AddOn(id, downloadDir, config.configurationAt("addon_" + id));
                    ao.setInstallationStatus(AddOn.InstallationStatus.AVAILABLE);
                } catch (Exception e) {
                    logger.warn("Failed to create add-on for {}", id, e);
                    continue;
                }
                if (ao.canLoadInCurrentVersion()) {
                    // Ignore ones that dont apply to this version
                    this.addOns.add(ao);
                } else {
                    logger.debug("Ignoring add-on {} can't load in this version", ao.getName());
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public AddOnCollection(File[] dirs) {
        if (dirs != null) {
            for (File dir : dirs) {
                try {
                    this.addDirectory(dir);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void addDirectory(File dir) throws Exception {
        if (dir == null) {
            logger.error("Null directory supplied");
            return;
        }
        if (!dir.exists()) {
            logger.warn(
                    "Skipping enumeration of add-ons, the directory does not exist: {}",
                    dir.getAbsolutePath());
            return;
        }
        if (!dir.isDirectory()) {
            logger.warn("Not a directory: {}", dir.getAbsolutePath());
            return;
        }

        // Load the addons
        try (DirectoryStream<Path> addOnFiles =
                Files.newDirectoryStream(dir.toPath(), "*" + AddOn.FILE_EXTENSION)) {
            for (Path addOnFile : addOnFiles) {
                AddOn.createAddOn(addOnFile)
                        .ifPresent(
                                ao -> {
                                    boolean add = true;
                                    for (AddOn addOn : addOns) {
                                        if (ao.isSameAddOn(addOn)) {
                                            if (ao.isUpdateTo(addOn)) {
                                                if (ao.canLoadInCurrentVersion()) {
                                                    // Replace in situ so we're not changing a list
                                                    // we're iterating through
                                                    logger.debug(
                                                            "Add-on {} version {} superseded by {}",
                                                            addOn.getId(),
                                                            addOn.getVersion(),
                                                            ao.getVersion());
                                                    addOns.remove(addOn);
                                                } else {
                                                    logger.debug(
                                                            "Ignoring newer add-on {} version {} because of ZAP version constraints; Not before={} Not from={} Current Version={}",
                                                            ao.getId(),
                                                            ao.getVersion(),
                                                            ao.getNotBeforeVersion(),
                                                            ao.getNotFromVersion(),
                                                            Constant.PROGRAM_VERSION);
                                                    add = false;
                                                }
                                            } else {
                                                // Same or older version, don't include
                                                logger.debug(
                                                        "Add-on {} version {} not latest.",
                                                        ao.getId(),
                                                        ao.getVersion());
                                                add = false;
                                            }
                                            break;
                                        }
                                    }
                                    if (add) {
                                        logger.debug(
                                                "Found add-on {} version {}",
                                                ao.getId(),
                                                ao.getVersion());
                                        this.addOns.add(ao);
                                    }
                                });
            }
        }
    }

    /**
     * Gets all add-ons of this add-on collection.
     *
     * @return a {@code List} with all add-ons of the collection
     * @see #getInstalledAddOns()
     */
    public List<AddOn> getAddOns() {
        return this.addOns;
    }

    /**
     * Gets all installed add-ons of this add-on collection, that is, the add-ons whose installation
     * status is {@code INSTALLED}.
     *
     * @return a {@code List} with all installed add-ons of the collection
     * @see #getAddOns()
     * @see AddOn.InstallationStatus#INSTALLED
     */
    public List<AddOn> getInstalledAddOns() {
        List<AddOn> installedAddOns = new ArrayList<>(addOns.size());
        for (AddOn addOn : addOns) {
            if (AddOn.InstallationStatus.INSTALLED == addOn.getInstallationStatus()) {
                installedAddOns.add(addOn);
            }
        }
        return installedAddOns;
    }

    public AddOn getAddOn(String id) {
        for (AddOn addOn : addOns) {
            if (addOn.getId().equals(id)) {
                return addOn;
            }
        }
        return null;
    }

    /**
     * Returns a list of addons from the supplied collection that are newer than the equivalent ones
     * in this collection
     *
     * @param aoc the collection to compare with
     * @return a list of addons from the supplied collection that are newer than the equivalent ones
     *     in this collection
     */
    public List<AddOn> getUpdatedAddOns(AddOnCollection aoc) {
        List<AddOn> updatedAddOns = new ArrayList<>();

        for (AddOn ao : aoc.getAddOns()) {
            for (AddOn addOn : addOns) {
                try {
                    if (ao.isSameAddOn(addOn) && ao.isUpdateTo(addOn)) {
                        // Its an update to one in this collection
                        updatedAddOns.add(ao);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return updatedAddOns;
    }

    /**
     * Returns a list of addons from the supplied collection that are newer than the equivalent ones
     * in this collection
     *
     * @param aoc the collection to compare with
     * @return a list of addons from the supplied collection that are newer than the equivalent ones
     *     in this collection
     */
    public List<AddOn> getNewAddOns(AddOnCollection aoc) {
        List<AddOn> newAddOns = new ArrayList<>();

        for (AddOn ao : aoc.getAddOns()) {
            boolean isNew = true;
            for (AddOn addOn : addOns) {
                try {
                    if (ao.isSameAddOn(addOn)) {
                        isNew = false;
                        break;
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (isNew) {
                newAddOns.add(ao);
            }
        }
        return newAddOns;
    }

    public ZapRelease getZapRelease() {
        return zapRelease;
    }

    public boolean includesAddOn(String id) {
        boolean inc = false;
        for (AddOn addOn : addOns) {
            if (addOn.getId().equals(id)) {
                return true;
            }
        }
        return inc;
    }

    public boolean addAddOn(AddOn ao) {
        if (this.includesAddOn(ao.getId())) {
            return false;
        }
        this.addOns.add(ao);
        return true;
    }

    public boolean removeAddOn(AddOn ao) {
        for (AddOn addOn : addOns) {
            if (addOn.getId().equals(ao.getId())) {
                addOns.remove(addOn);
                return true;
            }
        }
        return false;
    }
}
