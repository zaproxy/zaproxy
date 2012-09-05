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
package org.zaproxy.zap.extension.websocket.fuzz;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.fuzz.FuzzResult;
import org.zaproxy.zap.extension.fuzz.FuzzerContentPanel;
import org.zaproxy.zap.extension.search.SearchResult;
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessagesView;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessagesViewModel;

/**
 * The fuzzing tab with its WebSocket messsages view differs slightly from the
 * messages view in the WebSockets tab, as there are 2 more columns here.
 * <p>
 * Moreover it is not backed by the database but a {@link List}. You have to add
 * messages yourself via
 * {@link WebSocketFuzzMessagesView#addFuzzResult(FuzzResult)}.
 */
public class WebSocketFuzzMessagesView extends WebSocketMessagesView implements FuzzerContentPanel {
    private static final Logger logger = Logger.getLogger(WebSocketFuzzMessagesView.class);
    
    public static final String TABLE_NAME = "fuzz.websocket.table";

    public WebSocketFuzzMessagesView(WebSocketMessagesViewModel model, TableWebSocket table) {
    	super(model);
    }

	@Override
	protected String getViewComponentName() {
		return TABLE_NAME;
	}
    
    @Override
    public void setColumnWidths() {
    	super.setColumnWidths();

        // state
        setColumnWidth(6, 75, 100, 80);
        
        // fuzz part (do not set max & preferred size => stretches to maximum)
        setColumnWidth(7, 50, -1, -1);
    }

    @Override
	protected MouseListener getMouseListener() {
    	final JTable view = this.view;
    	
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
    
    @Override
    public void addFuzzResult(FuzzResult fuzzResult) {
        addFuzzResultInEdt((WebSocketFuzzResult)fuzzResult);
    }
    
    private void addFuzzResultInEdt(final WebSocketFuzzResult fuzzResult) {
        if (EventQueue.isDispatchThread()) {
            addFuzzResultToView(fuzzResult);
            return;
        }
        
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addFuzzResultToView(fuzzResult);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addFuzzResultToView(WebSocketFuzzResult fuzzResult) {
    	if (fuzzResult.getState().equals(FuzzResult.State.ERROR)) {
    		((WebSocketFuzzMessagesViewModel) model).addErroneousWebSocketMessage(fuzzResult.getMessage());
    	}
    }
    
    @Override
    public void clear() {
//    	((WebSocketFuzzMessagesViewModel) model).clear();
    }
    
    @Override
    public JComponent getComponent() {
        return getViewComponent();
    }
    
    public List<SearchResult> searchResults(Pattern pattern, boolean inverse) {
        return Collections.emptyList();
    }
}
