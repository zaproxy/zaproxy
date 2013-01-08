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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.websocket.WebSocketException;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.utils.TableColumnManager;

/**
 * Wraps a {@link JTable} that is used to display WebSocket
 * messages.
 */
public class WebSocketMessagesView implements Runnable {

	public static final String PANEL_NAME = "websocket.table";

	private static final Logger logger = Logger.getLogger(WebSocketMessagesView.class);

	protected JTable view;
	protected WebSocketMessagesViewModel model;

	private HttpPanel requestPanel;
	private HttpPanel responsePanel;
    private Vector<WebSocketMessageDTO> displayQueue;
    
    private Thread thread = null;
	
	public WebSocketMessagesView(WebSocketMessagesViewModel model) {
		this.model = model;
		
		displayQueue = new Vector<>();
	}
	
	/**
	 * Lazy initializes the view component.
	 * 
	 * @return messages view
	 */
	public JTable getViewComponent() {
		if (view == null) {			
			view = new JTable();
			view.setName(getViewComponentName());
			view.setModel(model);
			view.setColumnSelectionAllowed(false);
			view.setCellSelectionEnabled(false);
			view.setRowSelectionAllowed(true);
			view.setAutoCreateRowSorter(false);
			
			// prevents columns to loose their width when switching models
			view.setAutoCreateColumnsFromModel(false);

			setColumnWidths();

			view.setFont(new Font("Dialog", Font.PLAIN, 12));
			view.setDoubleBuffered(true);
			view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			view.addMouseListener(getMouseListener());
			
			view.getSelectionModel().addListSelectionListener(getListSelectionListener());
			
			// allows to show/hide columns
			new TableColumnManager(view);
			
			view.revalidate();
		}
		return view;
	}

	protected String getViewComponentName() {
		return PANEL_NAME;
	}

	protected MouseListener getMouseListener() {
		return new MouseAdapter() { 
		    @Override
		    public void mousePressed(MouseEvent e) {
	
				if (SwingUtilities.isRightMouseButton(e)) {
	
					// Select table item
				    int row = view.rowAtPoint( e.getPoint() );
				    if (row < 0 || !view.getSelectionModel().isSelectedIndex(row)) {
				    	view.getSelectionModel().clearSelection();
				    	if (row >= 0) {
				    		view.getSelectionModel().setSelectionInterval(row, row);
				    	}
				    }
					
					View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
		        }
		    }
		};
	}
	
	protected ListSelectionListener getListSelectionListener() {
		return new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// only display messages when there are no more selection changes.
				if (!e.getValueIsAdjusting()) {
					int rowIndex = view.getSelectedRow();
				    if (rowIndex < 0) {
				    	// selection got filtered away
				        return;
				    }
				    
				    WebSocketMessagesViewModel model = (WebSocketMessagesViewModel) view.getModel();
				    
				    // as we use a JTable here, that can be sorted, we have to
				    // transform the row index to the appropriate model row
                    int modelRow = view.convertRowIndexToModel(rowIndex);
					final WebSocketMessageDTO message = model.getDTO(modelRow);
                    readAndDisplay(message);
				}
			}
		};
	}

	protected void setColumnWidths() {
		// channel + consecutive number
		setColumnWidth(0, 50, 100, 70);

		// direction
		setColumnWidth(1, 25, 100, 25);
		
		// timestamp
		setColumnWidth(2, 160, 200, 160);
		
		// opcode
		setColumnWidth(3, 70, 120, 75);
		
		// payload length
		setColumnWidth(4, 45, 100, 45);
		
		// payload (do not set max & preferred size => stretches to maximum)
		setColumnWidth(5, 100, -1, -1);
	}
	
	/**
	 * Helper method for setting the column widths of this view.
	 * 
	 * @param index
	 * @param min
	 * @param max
	 * @param preferred
	 */
	protected void setColumnWidth(int index, int min, int max, int preferred) {
		TableColumn column = view.getColumnModel().getColumn(index);
		
		if (min != -1) {
			column.setMinWidth(min);
		}
		
		if (max != -1) {
			column.setMaxWidth(max);
		}
		
		if (preferred != -1) {
			column.setPreferredWidth(preferred);
		}
	}
    
    @Override
    public void run() {
        WebSocketMessageDTO message = null;
        int count = 0;
        
        do {
            synchronized(displayQueue) {
                count = displayQueue.size();
                if (count == 0) {
                    break;
                }
                
                message = displayQueue.get(0);
                displayQueue.remove(0);
            }
            
            try {
                final WebSocketMessageDTO msg = message;
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        if (msg.isOutgoing.booleanValue()) {
                            requestPanel.setMessage(msg);
                            responsePanel.clearView(false);
                            requestPanel.setTabFocus();
                        } else {
                            requestPanel.clearView(true);
                            responsePanel.setMessage(msg, true);
                            responsePanel.setTabFocus();
                        }
                    }
                });
                
            } catch (Exception e) {
                // ZAP: Added logging.
                logger.error(e.getMessage(), e);
            }
            
            // wait some time to allow another selection event to be triggered
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                // safely ignore exception
            }
        } while (true);
    }
    
    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;
    }
    
    private void readAndDisplay(final WebSocketMessageDTO message) {
    	if (message == null) {
    		return;
    	}
    	
        synchronized(displayQueue) {
            if (displayQueue.size() > 0) {
                displayQueue.clear();
            }

            message.tempUserObj = WebSocketPanel.connectedChannelIds.contains(message.channel.id);
            displayQueue.add(message);
        }
        
        if (thread != null && thread.isAlive()) {
            return;
        }
        
        thread = new Thread(this);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }

	public void revalidate() {
		view.revalidate();
	}
	
	public void selectAndShowItem(WebSocketMessageDTO message) throws WebSocketException {
		Integer modelRowIndex = model.getModelRowIndexOf(message);
		
		if (modelRowIndex == null) {
			throw new WebSocketException("Element not found");
		}

		int viewRowIndex = view.convertRowIndexToView(modelRowIndex);
		view.setRowSelectionInterval(viewRowIndex, viewRowIndex);
		
		int rowHeight = view.getRowHeight();
		Rectangle r = new Rectangle(0, rowHeight * viewRowIndex, 10, rowHeight);
		view.scrollRectToVisible(r);
	}

	public void pause() {
		getViewComponent().setEnabled(false);
	}

	public void resume() {
		getViewComponent().setEnabled(true);
		getViewComponent().revalidate();		
	}
}
