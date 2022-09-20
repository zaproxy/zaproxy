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

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;

public class AutoUpdateAPI extends ApiImplementor {

    private static Logger log = LogManager.getLogger(AutoUpdateAPI.class);

    private static final String PREFIX = "autoupdate";
    private static final String ACTION_DOWNLOAD_LATEST_RELEASE = "downloadLatestRelease";
    private static final String ACTION_INSTALL_ADDON = "installAddon";
    private static final String ACTION_INSTALL_LOCAL_ADDON = "installLocalAddon";
    private static final String ACTION_UNINSTALL_ADDON = "uninstallAddon";
    private static final String VIEW_LATEST_VERSION_NUMBER = "latestVersionNumber";
    private static final String VIEW_IS_LATEST_VERSION = "isLatestVersion";
    private static final String VIEW_INSTALLED_ADDONS = "installedAddons";
    private static final String VIEW_LOCAL_ADDONS = "localAddons";
    private static final String VIEW_NEW_ADDONS = "newAddons";
    private static final String VIEW_UPDATED_ADDONS = "updatedAddons";
    private static final String VIEW_MARKETPLACE_ADDONS = "marketplaceAddons";
    private static final String PARAM_ID = "id";
    private static final String PARAM_FILE = "file";

    private ExtensionAutoUpdate extension;

    public AutoUpdateAPI(ExtensionAutoUpdate extension) {
        this.extension = extension;
        this.addApiAction(new ApiAction(ACTION_DOWNLOAD_LATEST_RELEASE));
        this.addApiAction(new ApiAction(ACTION_INSTALL_ADDON, new String[] {PARAM_ID}));
        if (Constant.isDevMode()) {
            this.addApiAction(new ApiAction(ACTION_INSTALL_LOCAL_ADDON, new String[] {PARAM_FILE}));
        }
        this.addApiAction(new ApiAction(ACTION_UNINSTALL_ADDON, new String[] {PARAM_ID}));
        this.addApiView(new ApiView(VIEW_LATEST_VERSION_NUMBER));
        this.addApiView(new ApiView(VIEW_IS_LATEST_VERSION));
        this.addApiView(new ApiView(VIEW_INSTALLED_ADDONS));
        this.addApiView(new ApiView(VIEW_LOCAL_ADDONS));
        this.addApiView(new ApiView(VIEW_NEW_ADDONS));
        this.addApiView(new ApiView(VIEW_UPDATED_ADDONS));
        this.addApiView(new ApiView(VIEW_MARKETPLACE_ADDONS));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        log.debug("handleApiAction {} {}", name, params);
        if (ACTION_DOWNLOAD_LATEST_RELEASE.equals(name)) {
            if (this.downloadLatestRelease()) {
                return ApiResponseElement.OK;
            } else {
                return ApiResponseElement.FAIL;
            }

        } else if (ACTION_INSTALL_ADDON.equals(name)) {
            String id = params.getString(PARAM_ID);
            AddOn ao = extension.getAddOn(id);
            if (ao == null) {
                throw new ApiException(Type.DOES_NOT_EXIST);
            } else {
                List<String> l = new ArrayList<>();
                l.add(id);
                String errorMessages = extension.installAddOns(l);
                if (errorMessages.length() == 0) {
                    return ApiResponseElement.OK;
                } else {
                    throw new ApiException(ApiException.Type.INTERNAL_ERROR, errorMessages);
                }
            }
        } else if (ACTION_INSTALL_LOCAL_ADDON.equals(name) && Constant.isDevMode()) {
            return extension.installLocalAddOnQuietly(createPath(params.getString(PARAM_FILE)))
                    ? ApiResponseElement.OK
                    : ApiResponseElement.FAIL;
        } else if (ACTION_UNINSTALL_ADDON.equals(name)) {
            String id = params.getString(PARAM_ID);
            AddOn ao = extension.getLocalVersionInfo().getAddOn(id);
            if (ao == null) {
                throw new ApiException(Type.DOES_NOT_EXIST);
            }
            if (ao.isMandatory()) {
                throw new ApiException(
                        Type.ILLEGAL_PARAMETER,
                        "The add-on can't be uninstalled, it is mandatory.");
            }
            List<String> l = new ArrayList<>();
            l.add(id);
            String errorMessages = extension.uninstallAddOns(l);
            if (errorMessages.length() == 0) {
                return ApiResponseElement.OK;
            } else {
                throw new ApiException(ApiException.Type.INTERNAL_ERROR, errorMessages);
            }
        } else {
            throw new ApiException(ApiException.Type.BAD_ACTION);
        }
    }

    private static Path createPath(String path) throws ApiException {
        try {
            return Paths.get(path);
        } catch (InvalidPathException e) {
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_FILE, e);
        }
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        ApiResponse result;
        if (VIEW_LATEST_VERSION_NUMBER.equals(name)) {
            result = new ApiResponseElement(name, this.getLatestVersionNumber());
        } else if (VIEW_IS_LATEST_VERSION.equals(name)) {
            result = new ApiResponseElement(name, Boolean.toString(this.isLatestVersion()));
        } else if (VIEW_INSTALLED_ADDONS.equals(name)) {
            result = createResponseList(name, extension.getInstalledAddOns(), true);
        } else if (VIEW_LOCAL_ADDONS.equals(name)) {
            result = createResponseList(name, extension.getLocalAddOns(), true);
        } else if (VIEW_NEW_ADDONS.equals(name)) {
            result = createResponseList(name, extension.getNewAddOns());
        } else if (VIEW_UPDATED_ADDONS.equals(name)) {
            result = createResponseList(name, extension.getUpdatedAddOns());
        } else if (VIEW_MARKETPLACE_ADDONS.equals(name)) {
            result = createResponseList(name, extension.getMarketplaceAddOns());
        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW);
        }
        return result;
    }

    private static ApiResponseList createResponseList(String name, List<AddOn> addOns) {
        return createResponseList(name, addOns, false);
    }

    private static ApiResponseList createResponseList(
            String name, List<AddOn> addOns, boolean localAddOns) {
        ApiResponseList response = new ApiResponseList(name);
        for (AddOn ao : addOns) {
            response.addItem(addonToSet(ao, localAddOns));
        }
        return response;
    }

    private static ApiResponseSet<String> addonToSet(AddOn ao, boolean localAddOn) {
        Map<String, String> map = new HashMap<>();
        map.put("id", ao.getId());
        map.put("name", ao.getName());
        map.put("author", ao.getAuthor());
        map.put("changes", ao.getChanges());
        map.put("description", ao.getDescription());
        map.put("hash", ObjectUtils.toString(ao.getHash()));
        map.put("infoUrl", ObjectUtils.toString(ao.getInfo()));
        map.put("repoUrl", ObjectUtils.toString(ao.getRepo()));
        map.put("sizeInBytes", String.valueOf(ao.getSize()));
        map.put("status", ao.getStatus().toString());
        map.put("url", ObjectUtils.toString(ao.getUrl()));
        map.put("version", ObjectUtils.toString(ao.getVersion()));
        map.put("installationStatus", ObjectUtils.toString(ao.getInstallationStatus()));
        if (localAddOn) {
            map.put("file", ao.getFile().toString());
            map.put("mandatory", String.valueOf(ao.isMandatory()));
        }
        return new ApiResponseSet<>("addon", map);
    }

    public String getLatestVersionNumber() {
        return extension.getLatestVersionNumber();
    }

    public boolean isLatestVersion() {
        return extension.isLatestVersion();
    }

    public boolean downloadLatestRelease() {
        return extension.downloadLatestRelease();
    }
}
