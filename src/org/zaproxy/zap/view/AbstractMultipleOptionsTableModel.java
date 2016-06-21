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
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.view;

import java.util.Iterator;
import javax.swing.event.TableModelEvent;

import org.zaproxy.zap.utils.Enableable;

public abstract class AbstractMultipleOptionsTableModel<E extends Enableable> extends AbstractMultipleOptionsBaseTableModel<E> {

    private static final long serialVersionUID = 1L;

    public AbstractMultipleOptionsTableModel() {
        super();
    }
    
    public void setAllEnabled(boolean enabled) {
        final int size = getElements().size();
        if (size > 0) {
            for (Iterator<E> it = getElements().iterator(); it.hasNext();) {
                it.next().setEnabled(enabled);
            }

            fireTableColumnUpdated(0, size-1, 0);
        }
    }
    
    public void fireTableColumnUpdated(int firstRow, int lastRow, int column) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow, column, TableModelEvent.UPDATE));
    }
}
