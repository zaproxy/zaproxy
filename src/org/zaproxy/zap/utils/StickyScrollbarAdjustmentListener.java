/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.utils;

import java.awt.Adjustable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * When the scrollbar is at the bottom, then it keeps it there also in the
 * case when new items are inserted at the bottom. As a result you can
 * always see all items. It does not affect the position of the scrollbar
 * when it is not at the bottom.
 */
public class StickyScrollbarAdjustmentListener implements AdjustmentListener {
	private int previousMaximum;

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		Adjustable source = (Adjustable) e.getSource();

		if (source.getValue() + source.getVisibleAmount() == previousMaximum
				&& source.getMaximum() > previousMaximum) {
			// scrollbar is at previous position,
			// that was also the former maximum value

			// now content was added => scroll down
			source.setValue(source.getMaximum());
		}

		previousMaximum = source.getMaximum();
	}
}
