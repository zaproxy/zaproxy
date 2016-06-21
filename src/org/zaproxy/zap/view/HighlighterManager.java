package org.zaproxy.zap.view;

import java.awt.Color;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.zaproxy.zap.extension.search.SearchMatch;

/*
 * Handles Highlights
 * Keeps them in a ordered list. Allows access via get/set.
 * Can inform interested partys when the list of Highlights changes.
 */
public class HighlighterManager extends Observable {
	static HighlighterManager hilighterManager = null;
	
	public static HighlighterManager getInstance() {
		if ( hilighterManager == null) {
			hilighterManager = new HighlighterManager();
		}
		return hilighterManager;
	}
	
	private LinkedList<HighlightSearchEntry> highlights;
	
	private List<SoftObserver> observers;
	
	public HighlighterManager() {
		highlights = new LinkedList<>();
		observers =  new ArrayList<>();
	}
	
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
	public LinkedList<HighlightSearchEntry> getHighlights() {
		return (LinkedList<HighlightSearchEntry>) highlights.clone();
	}

	@Override
	public synchronized void addObserver(Observer obs) {
		if (obs == null) {
			throw new NullPointerException();
		}

		boolean observerContained = false;
		for (Iterator<SoftObserver> it = observers.iterator(); it.hasNext();) {
			Observer other = it.next().get();
			if (other == null) {
				it.remove();
			} else if (obs.equals(other)) {
				observerContained = true;
				break;
			}
		}
		if (!observerContained) {
			observers.add(new SoftObserver(obs));
		}
	}

	@Override
	public synchronized void deleteObserver(Observer obs) {
		for (Iterator<SoftObserver> it = observers.iterator(); it.hasNext();) {
			Observer other = it.next().get();
			if (other == null) {
				it.remove();
			} else if (obs.equals(other)) {
				it.remove();
				break;
			}
		}
	}

	private synchronized void deleteSoftObserver(SoftObserver obs) {
		observers.remove(obs);
	}

	@Override
	public void notifyObservers(Object arg) {
		Observer[] arrLocal;

		synchronized (this) {
			if (!hasChanged()) {
				return;
			}
			arrLocal = new Observer[observers.size()];
			arrLocal = observers.toArray(arrLocal);
			clearChanged();
		}

		for (int i = arrLocal.length - 1; i >= 0; i--) {
			arrLocal[i].update(this, arg);
		}
	}

	@Override
	public synchronized void deleteObservers() {
		observers.clear();
	}

	@Override
	public synchronized int countObservers() {
		return observers.size();
	}

	private class SoftObserver extends SoftReference<Observer> implements Observer {

		public SoftObserver(Observer referent) {
			super(referent);
		}

		@Override
		public void update(Observable o, Object arg) {
			Observer observer = get();
			if (observer != null) {
				observer.update(o, arg);
			} else {
				deleteSoftObserver(this);
			}
		}
	}
}
