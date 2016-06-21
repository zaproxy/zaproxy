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
package org.zaproxy.zap.view;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@code AbstractMultipleOptionsBaseTableModel}.
 */
public class AbstractMultipleOptionsBaseTableModelUnitTest extends TableModelTestUtils {

    @Test(expected = Exception.class)
    public void shouldFailToGetNonExistingElement() {
        // Given
        AbstractMultipleOptionsBaseTableModel<Object> tableModel = new MultipleOptionsBaseTableModelImpl();
        // When
        tableModel.getElement(1);
        // Then = Exception
    }

    @Test
    public void shouldAddElement() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        AbstractMultipleOptionsBaseTableModel<Object> tableModel = new MultipleOptionsBaseTableModelImpl();
        tableModel.addTableModelListener(listener);
        Object element = new Object();
        // When
        tableModel.addElement(element);
        // Then
        assertThat(tableModel.getElements().size(), is(equalTo(1)));
        assertThat(tableModel.getElements().get(0), is(equalTo(element)));
        assertThat(tableModel.getElement(0), is(equalTo(element)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isRowInserted(0), is(equalTo(true)));
    }

    @Test(expected = Exception.class)
    public void shouldFailToModifyNonExistingElement() {
        // Given
        AbstractMultipleOptionsBaseTableModel<Object> tableModel = new MultipleOptionsBaseTableModelImpl();
        // When
        tableModel.modifyElement(1, new Object());
        // Then = Exception
    }

    @Test
    public void shouldModifyElement() {
        // Given
        List<Object> elements = new ArrayList<>();
        elements.add(new Object());
        AbstractMultipleOptionsBaseTableModel<Object> tableModel = new MultipleOptionsBaseTableModelImpl(elements);
        TestTableModelListener listener = createTestTableModelListener();
        tableModel.addTableModelListener(listener);
        Object element = new Object();
        // When
        tableModel.modifyElement(0, element);
        // Then
        assertThat(tableModel.getElements().size(), is(equalTo(1)));
        assertThat(tableModel.getElements().get(0), is(equalTo(element)));
        assertThat(tableModel.getElement(0), is(equalTo(element)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isRowUpdated(0), is(equalTo(true)));
    }

    @Test(expected = Exception.class)
    public void shouldFailToRemoveNonExistingElement() {
        // Given
        AbstractMultipleOptionsBaseTableModel<Object> tableModel = new MultipleOptionsBaseTableModelImpl();
        // When
        tableModel.removeElement(1);
        // Then = Exception
    }

    @Test
    public void shouldRemoveElement() {
        // Given
        List<Object> elements = new ArrayList<>();
        Object element = new Object();
        elements.add(element);
        elements.add(new Object());
        AbstractMultipleOptionsBaseTableModel<Object> tableModel = new MultipleOptionsBaseTableModelImpl(elements);
        TestTableModelListener listener = createTestTableModelListener();
        tableModel.addTableModelListener(listener);
        // When
        tableModel.removeElement(1);
        // Then
        assertThat(tableModel.getElements().size(), is(equalTo(1)));
        assertThat(tableModel.getElements().get(0), is(equalTo(element)));
        assertThat(tableModel.getElement(0), is(equalTo(element)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isRowRemoved(1), is(equalTo(true)));
    }

    @Test
    public void shouldClearElements() {
        // Given
        List<Object> elements = new ArrayList<>();
        elements.add(new Object());
        AbstractMultipleOptionsBaseTableModel<Object> tableModel = new MultipleOptionsBaseTableModelImpl(elements);
        TestTableModelListener listener = createTestTableModelListener();
        tableModel.addTableModelListener(listener);
        // When
        tableModel.clear();
        // Then
        assertThat(tableModel.getElements(), is(empty()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isDataChanged(), is(equalTo(true)));
    }

    private static class MultipleOptionsBaseTableModelImpl extends AbstractMultipleOptionsBaseTableModel<Object> {

        private static final long serialVersionUID = -7940238455706609659L;

        private List<Object> elements;

        public MultipleOptionsBaseTableModelImpl() {
            elements = new ArrayList<>();
        }

        public MultipleOptionsBaseTableModelImpl(List<Object> elements) {
            this.elements = elements;
        }

        @Override
        public List<Object> getElements() {
            return elements;
        }

        @Override
        public int getRowCount() {
            return 0;
        }

        @Override
        public int getColumnCount() {
            return 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return null;
        }

    }
}
