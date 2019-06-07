/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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

import org.zaproxy.zap.utils.Orderable;

/**
 * An {@code AbstractMultipleOptionsBaseTableModel} that allows its elements to be ordered.
 *
 * <p>Allows to move the elements to top, up and down one position and to bottom. The order of the
 * elements starts with number one.
 *
 * <p><strong>Note:</strong> No validations are done prior moving the elements, which might or not
 * lead to {@code Exception}s. The move methods should be called only when the move is known to be
 * valid.
 *
 * @param <E> the type of elements of this table model
 * @see Orderable
 * @since 2.4.0
 */
public abstract class AbstractMultipleOrderedOptionsBaseTableModel<E extends Orderable>
        extends AbstractMultipleOptionsBaseTableModel<E> {

    private static final long serialVersionUID = 8139923613612787185L;

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to update the order of the elements after the removed element.
     */
    @Override
    public void removeElement(int row) {
        boolean updateOrders = row != getElements().size() - 1;

        getElements().remove(row);
        fireTableRowsDeleted(row, row);

        if (updateOrders) {
            updateOrdersAndFireNotification(row);
        }
    }

    /**
     * Moves the element at the given {@code row} to the top of the elements, thus occupying the
     * first position.
     *
     * <p>The order of all elements is updated and all listeners notified.
     *
     * @param row the row of the element that should be moved to the top
     * @see #updateOrdersAndFireNotification(int)
     */
    public void moveTop(int row) {
        E entry = getElement(row);
        getElements().add(0, entry);
        getElements().remove(row + 1);

        updateOrdersAndFireNotification(0);
    }

    /**
     * Moves the element at the given {@code row} one position up, effectively switching position
     * with the previous element.
     *
     * <p>The order of both elements is updated accordingly and all listeners notified.
     *
     * @param row the row of the element that should be moved one position up
     */
    public void moveUp(int row) {
        E entry = getElement(row);

        int firstRow = row - 1;
        getElements().add(firstRow, entry);
        getElements().remove(row + 1);

        entry.setOrder(row);
        getElements().get(row).setOrder(row + 1);

        fireTableRowsUpdated(firstRow, row);
    }

    /**
     * Moves the element at the given {@code row} one position down, effectively switching position
     * with the following element.
     *
     * <p>The order of both elements is updated accordingly and all listeners notified.
     *
     * @param row the row of the element that should be moved one position down
     */
    public void moveDown(int row) {
        moveUp(row + 1);
    }

    /**
     * Moves the element at the given {@code row} to the bottom of the elements, thus occupying the
     * last position.
     *
     * <p>The order of all elements is updated and all listeners notified.
     *
     * @param row the row of the element that should be moved to the bottom
     * @see #updateOrdersAndFireNotification(int)
     */
    public void moveBottom(int row) {
        E entry = getElement(row);
        getElements().remove(row);
        getElements().add(entry);

        updateOrdersAndFireNotification(row);
    }

    /**
     * Updates the order of all the elements, whose row index is equal or greater to {@code
     * startingRow}, and notifies all listeners of the changes.
     *
     * @param startingRow the row index at which the update of the order should start
     * @see #fireTableDataChanged()
     */
    protected void updateOrdersAndFireNotification(int startingRow) {
        for (int i = startingRow; i < getElements().size(); i++) {
            getElement(i).setOrder(i + 1);
        }

        fireTableDataChanged();
    }
}
