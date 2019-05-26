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
package org.zaproxy.zap.view;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;

/**
 * A {@code JSlider} which only allows values greater than zero and has the minimum value always set to zero. Setting a value
 * less than or equal to zero will change the current value to the value one.
 * <p>
 * The default values for minor and major tick spacings are 1 and 5, respectively. The tick and label painting and snap to ticks
 * are enabled by default.
 * </p>
 * 
 * @see JSlider
 */
public class PositiveValuesSlider extends JSlider {

    private static final long serialVersionUID = 1L;

    /**
     * Default value of minor tick spacing.
     */
    private static final int DEFAULT_MINOR_TICK_SPACING = 1;

    /**
     * Default value of major tick spacing.
     */
    private static final int DEFAULT_MAJOR_TICK_SPACING = 5;

    /**
     * Constructs a new {@code PositiveValuesSlider} with {@code max} as the maximum value allowed.
     * <p>
     * The current value will be set to the value one.
     * </p>
     * 
     * @param max the maximum value allowed
     * @throws IllegalArgumentException if {@code max} is lesser than or equal to zero.
     */
    public PositiveValuesSlider(int max) {
        this(1, max);
    }

    /**
     * Constructs a new {@code PositiveValuesSlider} with {@code value} as the current value and {@code max} as the maximum
     * value allowed.
     * <p>
     * If {@code value} is not greater than zero the value set as the current value will be the value one.
     * </p>
     * 
     * @param value the value that will be set as the current value (if greater than zero)
     * @param max the maximum value allowed
     * @throws IllegalArgumentException if {@code max} is lesser than or equal to zero.
     */
    public PositiveValuesSlider(int value, int max) {
        super(new PositiveValuesBoundedRangeModel(value, max));

        setMinorTickSpacing(DEFAULT_MINOR_TICK_SPACING);
        setMajorTickSpacing(DEFAULT_MAJOR_TICK_SPACING);

        setPaintTicks(true);
        setPaintLabels(true);
        setSnapToTicks(true);
        setPaintTrack(true);
    }

    @Override
    public void setMajorTickSpacing(int n) {
        // Set the label table to null to force the creation of (new) major tick labels,
        // if it's not called the major tick labels are not updated with the new values.
        setLabelTable(null);
        super.setMajorTickSpacing(n);
    }

    /**
     * Calling this method has <strong>no</strong> effect. It's using a custom {@code BoundedRangeModel}.
     */
    @Override
    public void setModel(BoundedRangeModel newModel) {
        if (!(newModel instanceof PositiveValuesBoundedRangeModel)) {
            return;
        }
        super.setModel(newModel);
    }

    /**
     * A BoundedRangeModel that allows only values greater than zero and has the minimum always set to zero.
     */
    private static class PositiveValuesBoundedRangeModel extends DefaultBoundedRangeModel {

        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new {@code PositiveValuesBoundedRangeModel} with {@code value} as the current value and {@code max} as
         * maximum value allowed.
         * 
         * <p>
         * If {@code value} is not greater than zero, the value set as the current value will be the value one.
         * </p>
         * 
         * @param value the value that will be set as the current value if greater than zero
         * @param max the maximum value allowed
         * @throws IllegalArgumentException if {@code max} is lesser than or equal to zero.
         */
        public PositiveValuesBoundedRangeModel(int value, int max) {
            super(getValueGreaterThanZero(value), 0, 0, max);
        }

        /**
         * Calling this method has <strong>no</strong> effect. The minimum is always zero.
         */
        @Override
        public void setMinimum(int n) {
        }

        /**
         * Sets the current value.
         * <p>
         * If {@code value} is not greater than zero the value set will be the value one.
         * </p>
         */
        @Override
        public void setValue(int value) {
            super.setValue(getValueGreaterThanZero(value));
        }

        /**
         * Returns a value greater than zero.
         * <p>
         * It the {@code value} is greater than zero its value is returned otherwise it's returned the value one.
         * </p>
         * 
         * @param value the value that will be checked
         * @return the {@code value} if greater than zero otherwise the value one
         */
        private static int getValueGreaterThanZero(int value) {
            return Math.max(value, 1);
        }
    }
}
