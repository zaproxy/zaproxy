/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension;

import org.zaproxy.zap.control.AddOn;

/**
 * A listener that will be notified when the installation status of the add-ons changes.
 *
 * @since 2.5.0
 */
public interface AddOnInstallationStatusListener {

    /**
     * The status update.
     *
     * @since 2.15.0
     */
    public interface StatusUpdate {

        /** The status of the add-on. */
        public enum Status {
            INSTALL,
            INSTALLED,
            SOFT_UNINSTALL,
            SOFT_UNINSTALLED,
            UNINSTALL,
            UNINSTALLED,
        }

        /**
         * Tells whether or not the update is successful, that is, no errors occurred.
         *
         * @return {@code true} if the update is successful, {@code false} otherwise.
         */
        boolean isSuccessful();

        /**
         * Gets the status.
         *
         * @return the status, never {@code null}.
         */
        Status getStatus();

        /**
         * Gets the add-on.
         *
         * @return the add-on, never {@code null}.
         */
        AddOn getAddOn();
    }

    /**
     * Notifies of an add-on status update.
     *
     * @param statusUpdate the status update, never {@code null}.
     * @since 2.15.0
     */
    default void update(StatusUpdate statusUpdate) {
        switch (statusUpdate.getStatus()) {
            case INSTALLED:
                addOnInstalled(statusUpdate.getAddOn());
                break;

            case SOFT_UNINSTALLED:
                addOnSoftUninstalled(statusUpdate.getAddOn(), statusUpdate.isSuccessful());
                break;

            case UNINSTALLED:
                addOnUninstalled(statusUpdate.getAddOn(), statusUpdate.isSuccessful());
                break;

            default:
        }
    }

    /**
     * Notifies that the given add-on was installed.
     *
     * @param addOn the add-on that was installed, never {@code null}
     */
    @Deprecated(since = "2.15.0", forRemoval = true)
    default void addOnInstalled(AddOn addOn) {}

    /**
     * Notifies that the given add-on was soft uninstalled.
     *
     * <p>Soft uninstallation consists of uninstalling the Java classes ({@code Extension}s, {@code
     * Plugin}s, {@code PassiveScanner}s) of an add-on, called when the add-on must be temporarily
     * uninstalled for an update of a dependency.
     *
     * @param addOn the add-on that was soft uninstalled, never {@code null}
     * @param successfully if the soft uninstallation was successful, that is, no errors occurred
     *     while uninstalling it
     */
    @Deprecated(since = "2.15.0", forRemoval = true)
    default void addOnSoftUninstalled(AddOn addOn, boolean successfully) {}

    /**
     * Notifies that the given add-on was uninstalled.
     *
     * @param addOn the add-on that was uninstalled, never {@code null}
     * @param successfully if the uninstallation was successful, that is, no errors occurred while
     *     uninstalling it
     */
    @Deprecated(since = "2.15.0", forRemoval = true)
    default void addOnUninstalled(AddOn addOn, boolean successfully) {}
}
