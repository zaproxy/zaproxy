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
package org.zaproxy.zap.extension.stats;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.utils.Stats;
import org.zaproxy.zap.utils.XMLStringUtil;

public class StatsAPI extends ApiImplementor {

    private static final String PREFIX = "stats";

    private static final String ACTION_CLEAR_STATS = "clearStats";

    private static final String VIEW_STATS = "stats";
    private static final String VIEW_SITE_STATS = "siteStats";
    private static final String VIEW_ALL_SITES_STATS = "allSitesStats";

    private static final String PARAM_KEY_PREFIX = "keyPrefix";
    private static final String PARAM_SITE = "site";

    private ExtensionStats extension;

    public StatsAPI(ExtensionStats extension) {
        this.extension = extension;
        this.addApiAction(new ApiAction(ACTION_CLEAR_STATS, null, new String[] {PARAM_KEY_PREFIX}));

        this.addApiView(new ApiView(VIEW_STATS, null, new String[] {PARAM_KEY_PREFIX}));
        this.addApiView(new ApiView(VIEW_ALL_SITES_STATS, null, new String[] {PARAM_KEY_PREFIX}));
        this.addApiView(
                new ApiView(
                        VIEW_SITE_STATS,
                        new String[] {PARAM_SITE},
                        new String[] {PARAM_KEY_PREFIX}));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        if (ACTION_CLEAR_STATS.equals(name)) {
            Stats.clear(this.getParam(params, PARAM_KEY_PREFIX, ""));
            return ApiResponseElement.OK;

        } else {
            throw new ApiException(ApiException.Type.BAD_ACTION);
        }
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        ApiResponse result = null;
        InMemoryStats memStats = extension.getInMemoryStats();
        if (memStats == null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST);
        }

        if (VIEW_STATS.equals(name)) {
            Map<String, String> map = new TreeMap<>();

            for (Entry<String, Long> stat :
                    memStats.getStats(this.getParam(params, PARAM_KEY_PREFIX, "")).entrySet()) {
                map.put(stat.getKey(), stat.getValue().toString());
            }
            result = new ApiResponseSet<>(name, map);

        } else if (VIEW_ALL_SITES_STATS.equals(name)) {
            result = new ApiResponseList(name);
            for (Entry<String, Map<String, Long>> stats :
                    memStats.getAllSiteStats(this.getParam(params, PARAM_KEY_PREFIX, ""))
                            .entrySet()) {
                ((ApiResponseList) result)
                        .addItem(new SiteStatsApiResponse(stats.getKey(), stats.getValue()));
            }

        } else if (VIEW_SITE_STATS.equals(name)) {
            String site = params.getString(PARAM_SITE);
            URI siteURI;

            try {
                siteURI = new URI(site, true);
                site = SessionStructure.getHostName(siteURI);
            } catch (Exception e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_SITE);
            }
            String scheme = siteURI.getScheme();
            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_SITE);
            }

            result =
                    new SiteStatsApiResponse(
                            site,
                            memStats.getSiteStats(
                                    site, this.getParam(params, PARAM_KEY_PREFIX, "")));

        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW);
        }
        return result;
    }

    private static class SiteStatsApiResponse extends ApiResponseList {

        private String site;
        private Map<String, Long> stats;

        public SiteStatsApiResponse(String site, Map<String, Long> stats) {
            super("statistics");
            this.site = site;
            this.stats = new TreeMap<>(stats);
            this.addItem(new ApiResponseSet<>(site, this.stats));
        }

        @Override
        public void toXML(Document doc, Element parent) {
            parent.setAttribute("type", "list");
            Element els = doc.createElement("site");
            Text texts = doc.createTextNode(XMLStringUtil.escapeControlChrs(this.site));
            els.appendChild(texts);
            parent.appendChild(els);

            for (Entry<String, Long> stat : this.stats.entrySet()) {
                Element el = doc.createElement("statistic");
                el.setAttribute("type", "set");

                Element elk = doc.createElement("key");
                Text textk = doc.createTextNode(XMLStringUtil.escapeControlChrs(stat.getKey()));
                elk.appendChild(textk);
                el.appendChild(elk);

                Element elv = doc.createElement("value");
                Text textv =
                        doc.createTextNode(
                                XMLStringUtil.escapeControlChrs(stat.getValue().toString()));
                elv.appendChild(textv);
                el.appendChild(elv);

                parent.appendChild(el);
            }
        }

        @Override
        public JSON toJSON() {
            JSONObject jo = new JSONObject();
            JSONArray array = new JSONArray();
            for (ApiResponse resp : this.getItems()) {
                if (resp instanceof ApiResponseElement) {
                    array.add(((ApiResponseElement) resp).getValue());
                } else {
                    array.add(resp.toJSON());
                }
            }
            // Use the site name instead of 'statistics'
            jo.put(this.site, array);
            return jo;
        }
    }
}
