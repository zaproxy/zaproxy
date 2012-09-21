package org.zaproxy.zap.view;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Observable;

import org.zaproxy.zap.extension.search.SearchMatch;

/*
 * Handles Highlights
 * Keeps them in a ordered list. Allows access via get/set.
 * Can inform interested partys when the list of Highlights changes.
 */
public class HighlighterManager extends Observable {
	static HighlighterManager hilighterManager = null;
	
	static public HighlighterManager getInstance() {
		if ( hilighterManager == null) {
			hilighterManager = new HighlighterManager();
		}
		return hilighterManager;
	}
	
	private LinkedList<HighlightSearchEntry> highlights;
	
	public HighlighterManager() {
		highlights = new LinkedList<>();
	}
	
	public void reinitHighlights(LinkedList<HighlightSearchEntry> list) {
		this.highlights = (LinkedList<HighlightSearchEntry>) list.clone();
		
		setChanged();
		notifyObservers(null);
	}
	
	public void addHighlightEntry(String token, SearchMatch.Location type, boolean isActive) {
		HighlightSearchEntry entry = new HighlightSearchEntry(token, type, Color.red, isActive); 
		addHighlightEntry(entry);
	}
	
	public void addHighlightEntry(HighlightSearchEntry entry) {
		highlights.add(entry);
		
		setChanged();
		notifyObservers(entry);
	}
	
	public void removeHighlightEntry(int id) {
		highlights.remove(id);
		
		// null means the observers call getHighlights() to rebuild all highlights
		setChanged();
		notifyObservers(null);
	}
	
	// TODO: sux
	public LinkedList<HighlightSearchEntry> getHighlights() {
		return (LinkedList<HighlightSearchEntry>) highlights.clone();
	}
}
