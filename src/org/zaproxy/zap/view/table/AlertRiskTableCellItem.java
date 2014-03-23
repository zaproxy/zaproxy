/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.view.table;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;

/**
 * The representation of an {@code Alert}'s risk in a table cell, has the risk's name (displayed in the cell) and value (used
 * for comparison).
 * 
 * @see #getItemForRisk(int)
 * @see Alert
 * @see Alert#RISK_INFO
 * @see Alert#RISK_LOW
 * @see Alert#RISK_MEDIUM
 * @see Alert#RISK_HIGH
 */
public class AlertRiskTableCellItem implements Comparable<AlertRiskTableCellItem> {

    private static final Map<Integer, AlertRiskTableCellItem> values;

    static {
        Map<Integer, AlertRiskTableCellItem> temp = new HashMap<>();

        AlertRiskTableCellItem alertCellItem = new AlertRiskTableCellItem(
                Alert.RISK_INFO,
                Constant.messages.getString("view.href.table.cell.alert.risk.label.info"));
        temp.put(Integer.valueOf(alertCellItem.getRisk()), alertCellItem);

        alertCellItem = new AlertRiskTableCellItem(
                Alert.RISK_LOW,
                Constant.messages.getString("view.href.table.cell.alert.risk.label.low"));
        temp.put(Integer.valueOf(alertCellItem.getRisk()), alertCellItem);

        alertCellItem = new AlertRiskTableCellItem(
                Alert.RISK_MEDIUM,
                Constant.messages.getString("view.href.table.cell.alert.risk.label.medium"));
        temp.put(Integer.valueOf(alertCellItem.getRisk()), alertCellItem);

        alertCellItem = new AlertRiskTableCellItem(
                Alert.RISK_HIGH,
                Constant.messages.getString("view.href.table.cell.alert.risk.label.high"));
        temp.put(Integer.valueOf(alertCellItem.getRisk()), alertCellItem);

        values = Collections.unmodifiableMap(temp);
    }

    /**
     * Represents a non {@code Alert}'s risk. It has an empty name and value {@literal -1}.
     * <p>
     * Should be used when there's no alerts therefore no risk.
     * </p>
     * 
     * @see Alert#RISK_INFO
     * @see Alert#RISK_LOW
     * @see Alert#RISK_MEDIUM
     * @see Alert#RISK_HIGH
     */
    public static final AlertRiskTableCellItem NO_RISK_CELL_ITEM = new AlertRiskTableCellItem(-1, "");

    /**
     * Represents an undefined {@code Alert}'s risk. It has the name as {@literal undefined} and value {@literal -2}.
     * 
     * @see Alert#RISK_INFO
     * @see Alert#RISK_LOW
     * @see Alert#RISK_MEDIUM
     * @see Alert#RISK_HIGH
     */
    public static final AlertRiskTableCellItem UNDEFINED_RISK_CELL_ITEM = new AlertRiskTableCellItem(
            -2,
            Constant.messages.getString("view.href.table.cell.alert.risk.label.undefined"));

    private final int risk;

    private final String description;

    private AlertRiskTableCellItem(final int risk, final String description) {
        super();
        this.risk = risk;
        this.description = description;
    }

    public int getRisk() {
        return risk;
    }

    @Override
    public int hashCode() {
        return 31 + risk;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        AlertRiskTableCellItem other = (AlertRiskTableCellItem) object;
        if (risk != other.risk) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(AlertRiskTableCellItem o) {
        final int otherRisk = o.risk;
        if (risk < otherRisk) {
            return -1;
        } else if (risk > otherRisk) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * Returns an {@code AlertTableCellItem} for the given {@code risk}. If the risk is {@literal -1} the returned
     * {@code AlertTableCellItem} will be {@code NO_RISK_CELL_ITEM}.
     * <p>
     * If the given {@code risk} is not {@literal -1} but it's not one of the risks defined an {@code UNDEFINED_RISK_CELL_ITEM}
     * will be returned.
     * 
     * @param risk the risk of the alert
     * @return the {@code AlertTableCellItem} for the given {@code risk}.
     * @see #NO_RISK_CELL_ITEM
     * @see #UNDEFINED_RISK_CELL_ITEM
     */
    public static AlertRiskTableCellItem getItemForRisk(final int risk) {
        if (risk == -1) {
            return NO_RISK_CELL_ITEM;
        }

        AlertRiskTableCellItem alertCelLItem = values.get(Integer.valueOf(risk));
        if (alertCelLItem == null) {
            return UNDEFINED_RISK_CELL_ITEM;
        }
        return alertCelLItem;
    }
}
