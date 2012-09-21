package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Got original code snippet from http://tutiez.com/how-make-jcombobox-drop-down-width-as-wide-as-needed.html
 *
 * @param <E> the type of the elements of this combo box
 */
public class WiderDropdownJComboBox<E> extends JComboBox<E> {
	private static final long serialVersionUID = -5796470611592964798L;
	
	private boolean layingOut = false;
    private int widestLengh = 0;
    private boolean wide = false;

    public WiderDropdownJComboBox(ComboBoxModel<E> channelComboBoxModel, boolean isWide) {
        super(channelComboBoxModel);
        setWide(isWide);
        
        channelComboBoxModel.addListDataListener(new ListDataListener() {
			
			@Override
			public void intervalRemoved(ListDataEvent evt) {
		        widestLengh = getWidestItemWidth();
			}
			
			@Override
			public void intervalAdded(ListDataEvent evt) {
		        widestLengh = getWidestItemWidth();
			}
			
			@Override
			public void contentsChanged(ListDataEvent evt) {
		        widestLengh = getWidestItemWidth();
			}
		});
    }

    public boolean isWide() {
        return wide;
    }

    public void setWide(boolean wide) {
        this.wide = wide;
        widestLengh = getWidestItemWidth();
    }

    @Override
    public Dimension getSize() {
        Dimension dim = super.getSize();
        if (!layingOut && isWide()) {
            dim.width = Math.max(widestLengh, dim.width);
        }
        return dim;
    }

    public int getWidestItemWidth() {
        int numOfItems = getItemCount();
        Font font = getFont();
        FontMetrics metrics = getFontMetrics(font);
        
        int widest = 0;
        for (int i = 0; i < numOfItems; i++) {
            Object item = getItemAt(i);
            int lineWidth = metrics.stringWidth(item.toString());
            widest = Math.max(widest, lineWidth);
        }

        // icon + scrollbar
        return widest + 20 + 20;
    }

    @Override
    public void doLayout() {
        try {
            layingOut = true;
            super.doLayout();
        } finally {
            layingOut = false;
        }
    }
}
