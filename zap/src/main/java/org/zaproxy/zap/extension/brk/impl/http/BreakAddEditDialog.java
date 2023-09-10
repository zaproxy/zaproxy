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
package org.zaproxy.zap.extension.brk.impl.http;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.brk.impl.http.HttpBreakpointMessage.Location;
import org.zaproxy.zap.extension.brk.impl.http.HttpBreakpointMessage.Match;
import org.zaproxy.zap.view.StandardFieldsDialog;

@SuppressWarnings("serial")
public class BreakAddEditDialog extends StandardFieldsDialog {

    private static final String FIELD_LOCATION = "brk.brkpoint.location.label";
    private static final String FIELD_MATCH = "brk.brkpoint.match.label";
    private static final String FIELD_PROTOCOL = "brk.brkpoint.protocol.label";
    private static final String FIELD_HOST = "brk.brkpoint.host.label";
    private static final String FIELD_PATH = "brk.brkpoint.path.label";
    private static final String FIELD_QUERY = "brk.brkpoint.query.label";
    private static final String FIELD_INVERSE = "brk.brkpoint.inverse.label";
    private static final String FIELD_IGNORECASE = "brk.brkpoint.ignorecase.label";
    private static final String FIELD_ON_REQUEST = "brk.brkpoint.request.label";
    private static final String FIELD_ON_RESPONSE = "brk.brkpoint.response.label";

    private static final long serialVersionUID = 1L;

    private HttpBreakpointsUiManagerInterface breakPointsManager;
    private boolean add = false;
    private HttpBreakpointMessage breakpoint;

    public BreakAddEditDialog(
            HttpBreakpointsUiManagerInterface breakPointsManager, Frame owner, Dimension dim) {
        super(owner, "brk.brkpoint.add.title", dim, true);
        this.breakPointsManager = breakPointsManager;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public void init(HttpBreakpointMessage breakpoint, boolean add) {
        this.add = add;
        this.breakpoint = breakpoint;

        this.removeAllFields();

        if (add) {
            this.setTitle(Constant.messages.getString("brk.brkpoint.add.title"));
        } else {
            this.setTitle(Constant.messages.getString("brk.brkpoint.edit.title"));
        }

        this.addComboField(FIELD_LOCATION, getLocations(), this.locToStr(breakpoint.getLocation()));
        this.addComboField(FIELD_MATCH, getMatches(), this.matchToStr(breakpoint.getMatch()));
        this.addTextField(FIELD_PROTOCOL, breakpoint.getProtocol());
        this.addTextField(FIELD_HOST, breakpoint.getHost());
        this.addTextField(FIELD_PATH, breakpoint.getPath());
        this.addTextField(FIELD_QUERY, breakpoint.getQuery());
        this.addCheckBoxField(FIELD_INVERSE, breakpoint.isInverse());
        this.addCheckBoxField(FIELD_IGNORECASE, breakpoint.isIgnoreCase());
        this.addCheckBoxField(FIELD_ON_REQUEST, breakpoint.isOnRequest());
        this.addCheckBoxField(FIELD_ON_RESPONSE, breakpoint.isOnResponse());

        this.addPadding();
    }

    private List<String> getLocations() {
        ArrayList<String> list = new ArrayList<>();
        for (Location loc : HttpBreakpointMessage.Location.values()) {
            list.add(this.locToStr(loc));
        }
        return list;
    }

    private String locToStr(Location loc) {
        return Constant.messages.getString("brk.brkpoint.location." + loc.name());
    }

    private Location strToLoc(String str) {
        for (Location loc : HttpBreakpointMessage.Location.values()) {
            if (this.locToStr(loc).equals(str)) {
                return loc;
            }
        }
        return null;
    }

    private List<String> getMatches() {
        ArrayList<String> list = new ArrayList<>();
        for (Match match : HttpBreakpointMessage.Match.values()) {
            list.add(this.matchToStr(match));
        }
        return list;
    }

    private String matchToStr(Match match) {
        return Constant.messages.getString("brk.brkpoint.match." + match.name());
    }

    private Match strToMatch(String str) {
        for (Match match : HttpBreakpointMessage.Match.values()) {
            if (this.matchToStr(match).equals(str)) {
                return match;
            }
        }
        return null;
    }

    @Override
    public void save() {
        HttpBreakpointMessage brk =
                new HttpBreakpointMessage(
                        getUrlStrFromFields(),
                        this.strToLoc(this.getStringValue(FIELD_LOCATION)),
                        this.strToMatch(this.getStringValue(FIELD_MATCH)),
                        this.getBoolValue(FIELD_INVERSE),
                        this.getBoolValue(FIELD_IGNORECASE),
                        this.getBoolValue(FIELD_ON_REQUEST),
                        this.getBoolValue(FIELD_ON_RESPONSE));

        if (add) {
            breakPointsManager.addBreakpoint(brk);
            dispose();
        } else {
            breakPointsManager.editBreakpoint(breakpoint, brk);
            breakpoint = null;
            dispose();
        }
    }

    private String getUrlStrFromFields() {
        StringBuilder sbFromFields =
                new StringBuilder(this.getStringValue(FIELD_PROTOCOL))
                        .append("://")
                        .append(this.getStringValue(FIELD_HOST))
                        .append(this.getStringValue(FIELD_PATH));
        String query = this.getStringValue(FIELD_QUERY);
        if (!query.isBlank()) {
            sbFromFields.append("?").append(query);
        }
        return sbFromFields.toString();
    }

    @Override
    public String validateFields() {
        if (this.isEmptyField(FIELD_PROTOCOL)) {
            return Constant.messages.getString("brk.brkpoint.error.noprotocol");
        }
        if (this.isEmptyField(FIELD_HOST)) {
            return Constant.messages.getString("brk.brkpoint.error.nohost");
        }

        String urlString = getUrlStrFromFields();
        if (Match.regex.equals(this.strToMatch(this.getStringValue(FIELD_MATCH)))) {
            try {
                Pattern.compile(urlString);
            } catch (Exception e) {
                return Constant.messages.getString("brk.brkpoint.error.regex");
            }
        }
        if (urlString.contains("#")
                && Location.url.equals(this.strToLoc(this.getStringValue(FIELD_LOCATION)))
                && Match.contains.equals(this.strToMatch(this.getStringValue(FIELD_MATCH)))) {
            return Constant.messages.getString("brk.brkpoint.warn.urlfragment");
        }
        return null;
    }

    @Override
    public void cancelPressed() {
        dispose();
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.addbreak";
    }
}
