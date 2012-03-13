package org.zaproxy.zap.extension.httppanel.view.text;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextArea;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.view.text.menus.SyntaxMenu;
import org.zaproxy.zap.extension.httppanel.view.text.menus.ViewMenu;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.view.HighlightSearchEntry;
import org.zaproxy.zap.view.HighlighterManager;

/* ZAP Text Area
 * Which enhanced functionality. Used to display HTTP Message request / response, or parts of it.
 */
public abstract class HttpPanelTextArea extends RSyntaxTextArea implements Observer {

	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(HttpPanelTextArea.class);
	
	public static final String PLAIN_SYNTAX_LABEL = Constant.messages.getString("http.panel.text.syntax.plain");
	
	private HttpMessage httpMessage;
	private Vector<SyntaxStyle> syntaxStyles;
	
	private static SyntaxMenu syntaxMenu = null;
	private static ViewMenu viewMenu = null;
	private static TextAreaMenuItem cutAction = null;
	private static TextAreaMenuItem copyAction = null;
	private static TextAreaMenuItem pasteAction = null;
	private static TextAreaMenuItem deleteAction = null;
	private static TextAreaMenuItem undoAction = null;
	private static TextAreaMenuItem redoAction = null;
	private static TextAreaMenuItem selectAllAction = null;
	
	static {
		//Hack to set the language that is used by ZAP.
		RTextArea.setLocaleI18n(Constant.getLocale());
	}
	
	public HttpPanelTextArea(HttpMessage httpMessage) {
		((RSyntaxDocument)getDocument()).setTokenMakerFactory(getTokenMakerFactory());
		setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
		
		syntaxStyles = new Vector<SyntaxStyle>();
		addSyntaxStyle(PLAIN_SYNTAX_LABEL, SyntaxConstants.SYNTAX_STYLE_NONE);
		
		if (syntaxMenu == null) {
			initActions();
		}
		
		setPopupMenu(null);
		
		this.httpMessage = httpMessage;
		
		setHyperlinksEnabled(false);

		setAntiAliasingEnabled(false);

		setLineWrap(true);
		
		setHighlightCurrentLine(false);
		setFadeCurrentLineHighlight(false);

		setWhitespaceVisible(false);
		setEOLMarkersVisible(false);
		
		setMarkOccurrences(false);

		setBracketMatchingEnabled(false);
		setAnimateBracketMatching(false);
		
		setAutoIndentEnabled(false);
		setCloseCurlyBraces(false);
		setCloseMarkupTags(false);
		setClearWhitespaceLinesEnabled(false);
		
		initHighlighter();
	}
	
	@Override
	protected JPopupMenu createPopupMenu() {
		return null;
	}
	
	private void initHighlighter() {
		HighlighterManager highlighter = HighlighterManager.getInstance();
		
		highlighter.addObserver(this);
		
		if (httpMessage != null) {
			highlightAll();
		}
	}
	
	// Highlight all search strings from HighlightManager
	private void highlightAll() {
		HighlighterManager highlighter = HighlighterManager.getInstance();
		
		LinkedList<HighlightSearchEntry> highlights = highlighter.getHighlights();
		for (HighlightSearchEntry entry: highlights) {
			highlightEntryParser(entry);
		}
	}
	
	// Parse the TextArea data and search the HighlightEntry strings
	// Highlight all found strings
	private void highlightEntryParser(HighlightSearchEntry entry) {
		String text;
		int lastPos = 0;
		
		text = this.getText();
		
		Highlighter hilite = this.getHighlighter();
		HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(entry.getColor());
		
		while ( (lastPos = text.indexOf(entry.getToken(), lastPos)) > -1) {
			try {
				hilite.addHighlight(lastPos, lastPos + entry.getToken().length(), painter);
				lastPos += entry.getToken().length();
			} catch (BadLocationException e) {
				log.warn("Could not highlight entry", e);
			}
		}
	}
	
	
	// Return selected text
	public abstract SearchMatch getTextSelection();
	
	@Override
	// Apply highlights after a setText()
	public void setText(String s) {
		super.setText(s);
		highlightAll();
	}

	// highlight a specific SearchMatch in the editor
	public abstract void highlight(SearchMatch sm);
	
	protected void highlight(int start, int end) {
		Highlighter hilite = this.getHighlighter();
		HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
		
		try {
			// DOBIN
			removeAllHighlights();
			hilite.addHighlight(start, end, painter);
			this.setCaretPosition(start);
		} catch (BadLocationException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void removeAllHighlights() {
		Highlighter hilite = this.getHighlighter();
		hilite.removeAllHighlights();
	}

	@Override
	// HighlighterManager called us
	// there is either
	// - a new highlight
	// - something other (added several, deleted, ...).
	public void update(Observable arg0, Object arg1) {
		if (arg1 == null) {
			// Re-highlight everything
			removeAllHighlights();
			highlightAll();
		} else {
			// Add specific highlight
			HighlightSearchEntry token = (HighlightSearchEntry) arg1;
			highlightEntryParser(token);
		}
		this.invalidate();
	}
	
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;
	}
	
	public HttpMessage getHttpMessage() {
		return httpMessage;
	}
	
	public Vector<SyntaxStyle> getSyntaxStyles() {
		return syntaxStyles;
	}
	
	protected void addSyntaxStyle(String label, String styleKey) {
		syntaxStyles.add(new SyntaxStyle(label, styleKey));
	}
	
	protected abstract CustomTokenMakerFactory getTokenMakerFactory();
	
	private static synchronized void initActions() {
		if (syntaxMenu == null) {
			syntaxMenu = new SyntaxMenu();
			viewMenu = new ViewMenu();
			
			undoAction = new TextAreaMenuItem(RTextArea.UNDO_ACTION, true, false);
			redoAction = new TextAreaMenuItem(RTextArea.REDO_ACTION, false, true);

			cutAction = new TextAreaMenuItem(RTextArea.CUT_ACTION, false, false);
			copyAction = new TextAreaMenuItem(RTextArea.COPY_ACTION, false, false);
			pasteAction = new TextAreaMenuItem(RTextArea.PASTE_ACTION, false, false);
			deleteAction = new TextAreaMenuItem(RTextArea.DELETE_ACTION, false, true);
			
			selectAllAction = new TextAreaMenuItem(RTextArea.SELECT_ALL_ACTION, false, true);
			
			View.getSingleton().getPopupMenu().addMenu(syntaxMenu);
			View.getSingleton().getPopupMenu().addMenu(viewMenu);
			
			View.getSingleton().getPopupMenu().addMenu(undoAction);
			View.getSingleton().getPopupMenu().addMenu(redoAction);
			
			View.getSingleton().getPopupMenu().addMenu(cutAction);
			View.getSingleton().getPopupMenu().addMenu(copyAction);
			View.getSingleton().getPopupMenu().addMenu(pasteAction);
			View.getSingleton().getPopupMenu().addMenu(deleteAction);
			
			View.getSingleton().getPopupMenu().addMenu(selectAllAction);
		}
	}
	
	public static class SyntaxStyle {
		private String label;
		private String styleKey;
		
		public SyntaxStyle(String label, String styleKey) {
			this.label = label;
			this.styleKey = styleKey;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getStyleKey() {
			return styleKey;
		}
	}
	
	protected static class CustomTokenMakerFactory extends AbstractTokenMakerFactory {
		
		@Override
		protected Map<String, String> createTokenMakerKeyToClassNameMap() {
			HashMap<String, String> map = new HashMap<String, String>();

			String pkg = "org.fife.ui.rsyntaxtextarea.modes.";
			map.put(SYNTAX_STYLE_NONE, pkg + "PlainTextTokenMaker");
			
			return map;
		}
	}
	
	private static class TextAreaMenuItem extends ExtensionPopupMenuItem {
		
		private static final long serialVersionUID = -8369459846515841057L;
		
		private int actionId;
		private boolean precedeWithSeparator;
		private boolean succeedWithSeparator;
		
		public TextAreaMenuItem(int actionId, boolean precedeWithSeparator, boolean succeedWithSeparator) throws IllegalArgumentException {
			this.actionId = actionId;
			this.precedeWithSeparator = precedeWithSeparator;
			this.succeedWithSeparator = succeedWithSeparator;
			Action action = RTextArea.getAction(actionId);
			if(action == null) {
				throw new IllegalArgumentException("Action not found with id: " + actionId);
			}
			setAction(action);
		}
		
		public boolean isEnableForComponent(Component invoker) {
			if (invoker instanceof HttpPanelTextArea) {
				HttpPanelTextArea httpPanelTextArea = (HttpPanelTextArea)invoker;
				
				switch(actionId) {
					case RTextArea.CUT_ACTION:
						if (!httpPanelTextArea.isEditable()) {
							this.setEnabled(false);
						}
					break;
					case RTextArea.DELETE_ACTION:
					case RTextArea.PASTE_ACTION:
						this.setEnabled(httpPanelTextArea.isEditable());
					break;
				}
				
				return true;
			}
			return false;
		}
		
		@Override
		public boolean precedeWithSeparator() {
			return precedeWithSeparator;
		}
		
		@Override
		public boolean succeedWithSeparator() {
			return succeedWithSeparator;
		}
	}
}
