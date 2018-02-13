package org.zaproxy.zap.view;

import java.awt.Color;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.zaproxy.zap.extension.search.SearchMatch;

/*
 * Handles Highlights
 * Keeps them in a ordered list. Allows access via get/set.
 * Can inform interested partys when the list of Highlights changes.
 */
public class HighlighterManager {
	static HighlighterManager hilighterManager = null;
	
	public static HighlighterManager getInstance() {
		if ( hilighterManager == null) {
			hilighterManager = new HighlighterManager();
		}
		return hilighterManager;
	}
	
	private LinkedList<HighlightSearchEntry> highlights;
	
	private List<SoftReference<HighlighterManagerListener>> eventListeners;
	
	public HighlighterManager() {
		highlights = new LinkedList<>();
		eventListeners = Collections.synchronizedList(new ArrayList<>()); 
	}
	
	@SuppressWarnings("unchecked")
	public void reinitHighlights(LinkedList<HighlightSearchEntry> list) {
		this.highlights = (LinkedList<HighlightSearchEntry>) list.clone();
		
		fireHighlighterChanged(new HighlighterManagerEvent(this, HighlighterManagerEvent.Type.HIGHLIGHTS_SET));
	}
	
	public void addHighlightEntry(String token, SearchMatch.Location type, boolean isActive) {
		HighlightSearchEntry entry = new HighlightSearchEntry(token, type, Color.red, isActive); 
		addHighlightEntry(entry);
	}
	
	public void addHighlightEntry(HighlightSearchEntry entry) {
		highlights.add(entry);
		
		fireHighlighterChanged(new HighlighterManagerEvent(this, HighlighterManagerEvent.Type.HIGHLIGHT_ADDED, entry));
	}
	
	public void removeHighlightEntry(int id) {
		HighlightSearchEntry entry = highlights.remove(id);

		if (entry != null) {
			fireHighlighterChanged(new HighlighterManagerEvent(this, HighlighterManagerEvent.Type.HIGHLIGHT_REMOVED, entry));
		}
	}
	
	// TODO: sux
	@SuppressWarnings("unchecked")
	public LinkedList<HighlightSearchEntry> getHighlights() {
		return (LinkedList<HighlightSearchEntry>) highlights.clone();
	}

	public void addHighlighterManagerListener(HighlighterManagerListener listener) {
		eventListeners.add(new SoftReference<>(listener));
	}

	public void removeHighlighterManagerListener(HighlighterManagerListener listener) {
		synchronized (eventListeners) {
			for (Iterator<SoftReference<HighlighterManagerListener>> it = eventListeners.iterator(); it.hasNext();) {
				HighlighterManagerListener currentListener = it.next().get();
				if (currentListener == null) {
					it.remove();
				} else if (listener.equals(currentListener)) {
					it.remove();
					break;
				}
			}
		}
	}

	private void fireHighlighterChanged(HighlighterManagerEvent e) {
		synchronized (eventListeners) {
			for (Iterator<SoftReference<HighlighterManagerListener>> it = eventListeners.iterator(); it.hasNext();) {
				HighlighterManagerListener listener = it.next().get();
				if (listener == null) {
					it.remove();
				} else {
					listener.highlighterChanged(e);
				}
			}
		}
	}

	@FunctionalInterface
	public static interface HighlighterManagerListener extends EventListener {

		void highlighterChanged(HighlighterManagerEvent e);
	}

	public static final class HighlighterManagerEvent extends EventObject {

		private static final long serialVersionUID = 1L;

		public enum Type {
			HIGHLIGHTS_SET,
			HIGHLIGHT_ADDED,
			HIGHLIGHT_REMOVED
		}

		private Type type;
		private HighlightSearchEntry highlight;

		private HighlighterManagerEvent(HighlighterManager source, Type type) {
			this(source, type, null);
		}

		private HighlighterManagerEvent(HighlighterManager source, Type type, HighlightSearchEntry highlight) {
			super(source);
			this.type = type;
			this.highlight = highlight;
		}

		public Type getType() {
			return type;
		}

		public HighlightSearchEntry getHighlight() {
			return highlight;
		}
	}
}
