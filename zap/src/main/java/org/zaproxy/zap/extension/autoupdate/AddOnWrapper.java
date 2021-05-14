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

import org.apache.commons.lang.Validate;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.Enableable;

public class AddOnWrapper extends Enableable {

    public enum Status {
        newAddon,
        newVersion
    }

    private AddOn addOn = null;
    private AddOn.InstallationStatus installationStatus;
    private AddOn addOnUpdate;
    private Status status = null;
    private int progress = 0;
    private boolean failed = false;

    /**
     * The issues that prevent this add-on, or its extensions, from being run, contents in HTML
     * format.
     */
    private String runningIssues;

    /**
     * Flag that indicates if the running issues are caused by the add-on. If not are caused by its
     * extensions.
     */
    private boolean addOnRunningIssues;

    /**
     * The issues that prevent this add-on, or its extensions, from being run after updating,
     * contents in HTML format.
     */
    private String updateIssues;

    /**
     * Flag that indicates if the update issues are caused by the add-on. If not are caused by its
     * extensions.
     */
    private boolean addOnUpdateIssues;

    public AddOnWrapper(AddOn addOn, Status status) {
        this(addOn, status, "");
    }

    /**
     * Creates an {@code AddOnWrapper} with the given {@code addOn}, {@code status} and {@code
     * runningIssues}.
     *
     * @param addOn the add-on
     * @param status the status of the add-on
     * @param runningIssues a String containing the issues that prevents the add-on from being run,
     *     in HTML format
     * @throws IllegalArgumentException if {@code runningIssues} is null.
     * @since 2.4.0
     */
    public AddOnWrapper(AddOn addOn, Status status, String runningIssues) {
        Validate.notNull(runningIssues, "Parameter runningIssues must not be null.");

        this.addOn = addOn;
        this.installationStatus = addOn.getInstallationStatus();
        this.status = status;
        this.runningIssues = runningIssues;
        this.updateIssues = "";
    }

    public AddOn getAddOn() {
        return addOn;
    }

    public Status getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public void setAddOn(AddOn addOn) {
        this.addOn = addOn;
        this.installationStatus = addOn.getInstallationStatus();
        addOnUpdate = null;
        progress = 0;
        status = null;
        failed = false;
        setEnabled(false);
    }

    /**
     * Gets the installation status of the wrapper.
     *
     * @return the installation status of the wrapper.
     * @since 2.4.0
     * @see #setInstallationStatus(org.zaproxy.zap.control.AddOn.InstallationStatus)
     */
    public AddOn.InstallationStatus getInstallationStatus() {
        return installationStatus;
    }

    /**
     * Sets the installation status of the wrapper.
     *
     * <p>The status might not be the same as the wrapped add-on, when the add-on is being, for
     * example, downloaded and then installed. This allows to properly report the state of the
     * add-on until a final status was achieved.
     *
     * @param installationStatus the new installation status
     * @since 2.4.0
     * @see #getInstallationStatus()
     */
    public void setInstallationStatus(AddOn.InstallationStatus installationStatus) {
        this.installationStatus = installationStatus;
    }

    /**
     * Sets the newer version of the wrapped add-on.
     *
     * <p>The status is updated to {@code newVersion}.
     *
     * @param addOnUpdate the newer version or {@code null} if none
     * @since 2.4.0
     * @see #getAddOnUpdate()
     */
    public void setAddOnUpdate(AddOn addOnUpdate) {
        this.addOnUpdate = addOnUpdate;
        setStatus(AddOnWrapper.Status.newVersion);
    }

    /**
     * Gets newer version of the wrapped add-on. Might be {@code null} if none.
     *
     * @return the newer version or {@code null} if none
     * @since 2.4.0
     * @see #setAddOnUpdate(AddOn)
     */
    public AddOn getAddOnUpdate() {
        return addOnUpdate;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    /**
     * Tells whether or not the running issues are caused by the add-on.
     *
     * @return {@code true} if the running issues are caused by the add-on, {@code false} if caused
     *     by its extensions.
     * @since 2.4.0
     * @see #hasRunningIssues()
     */
    public boolean isAddOnRunningIssues() {
        return addOnRunningIssues;
    }

    /**
     * Tells whether or not the wrapped add-on or its extensions have issues that prevent them from
     * being run.
     *
     * @return {@code true} if the add-on or its extensions have issues that prevent them from being
     *     run, {@code false} otherwise
     * @since 2.4.0
     * @see #isAddOnRunningIssues()
     * @see #getRunningIssues()
     */
    public boolean hasRunningIssues() {
        return !runningIssues.isEmpty();
    }

    /**
     * Gets the issues that the wrapped add-on or its extensions might have that prevent them from
     * being run. Might be empty if there's no issues.
     *
     * <p>The contents are in HTML.
     *
     * @return the issues of the add-on or its extensions that prevent them from being run, empty if
     *     there's no issues.
     * @since 2.4.0
     * @see #hasRunningIssues()
     */
    public String getRunningIssues() {
        return runningIssues;
    }

    /**
     * Sets the issues that the wrapped add-on or its extensions might have that prevent them from
     * being run.
     *
     * <p>The contents should be in HTML.
     *
     * @param runningIssues the running issues of the add-on or its extensions, empty if there's no
     *     issues.
     * @param addOnIssues {@code true} if the issues are caused by the add-on, {@code false} if are
     *     caused by the extensions
     * @since 2.4.0
     * @see #getRunningIssues()
     */
    public void setRunningIssues(String runningIssues, boolean addOnIssues) {
        Validate.notNull(runningIssues, "Parameter runningIssues must not be null.");

        this.runningIssues = runningIssues;
        this.addOnRunningIssues = addOnIssues;
    }

    /**
     * Tells whether or not the running issues, after updating, are caused by the add-on.
     *
     * @return {@code true} if the running issues, after updating, are caused by the add-on, {@code
     *     false} if caused by its extensions.
     * @since 2.4.0
     * @see #hasUpdateIssues()
     */
    public boolean isAddOnUpdateIssues() {
        return addOnUpdateIssues;
    }

    /**
     * Tells whether or not the wrapped add-on or its extensions have issues that prevents them from
     * being run after updating.
     *
     * @return {@code true} if the add-on or its extensions have issues that prevents them from
     *     being run after updating, {@code false} otherwise
     * @since 2.4.0
     * @see #isAddOnUpdateIssues()
     * @see #getUpdateIssues()
     */
    public boolean hasUpdateIssues() {
        return !updateIssues.isEmpty();
    }

    /**
     * Gets the issues that the wrapped add-on or its extensions might have that prevent them from
     * being run after updating. Might be empty if there's no issues.
     *
     * <p>The contents are in HTML.
     *
     * @return the issues of the add-on or its extensions that prevent them from being run after
     *     updating, empty if there's no issues.
     * @since 2.4.0
     * @see #hasUpdateIssues()
     */
    public String getUpdateIssues() {
        return updateIssues;
    }

    /**
     * Sets the issues that the newer version of the wrapped add-on or its extensions might have
     * that prevents them from being run.
     *
     * <p>The contents should be in HTML.
     *
     * @param updateIssues the running issues of the add-on or its extensions, empty if there's no
     *     issues.
     * @param addOnIssues {@code true} if the issues are caused by the add-on, {@code false} if are
     *     caused by the extensions
     * @since 2.4.0
     * @see #getUpdateIssues()
     */
    public void setUpdateIssues(String updateIssues, boolean addOnIssues) {
        Validate.notNull(updateIssues, "Parameter updateIssues must not be null.");

        this.updateIssues = updateIssues;
        this.addOnUpdateIssues = addOnIssues;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + ((addOn == null) ? 0 : addOn.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AddOnWrapper other = (AddOnWrapper) obj;
        if (addOn == null) {
            if (other.addOn != null) {
                return false;
            }
        } else if (!addOn.equals(other.addOn)) {
            return false;
        }
        return true;
    }
}
