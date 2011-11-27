package org.parosproxy.paros.extension.option;

import java.awt.Color;
import java.util.LinkedList;

import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.extension.highlighter.HighlightSearchEntry;
import org.zaproxy.zap.extension.highlighter.HighlighterManager;
import org.zaproxy.zap.extension.search.SearchMatch;

public class OptionsHighlight extends AbstractParam {

	public static final String HIGHLIGHT_TOKEN = "highlight.entry.token";
	public static final String HIGHLIGHT_TYPE = "highlight.entry.type";
	public static final String HIGHLIGHT_COLOR = "highlight.entry.color";
	public static final String HIGHLIGHT_ACTIVE = "highlight.entry.active";
	
	private LinkedList<HighlightSearchEntry> highlights = null; 
	private int readMaxItem = 0;
	
	@Override
	protected void parse() {
		String token = "";
		String type = "";
		String color = "";
		String active = "";
		
		Integer n = 0;
		
		highlights = new LinkedList<HighlightSearchEntry>();
		
		while(true) {
			token = getConfig().getString(HIGHLIGHT_TOKEN + n);
			type = getConfig().getString(HIGHLIGHT_TYPE + n);
			color = getConfig().getString(HIGHLIGHT_COLOR + n);
			active = getConfig().getString(HIGHLIGHT_ACTIVE + n);
			
			if (token == null || token.length() == 0) {
				break;
			}
			
			HighlightSearchEntry h = new HighlightSearchEntry(
					token, 
					SearchMatch.Location.valueOf(type), 
					new Color(Integer.parseInt(color)), 
					Boolean.parseBoolean(active));
			
			highlights.add(h);
			
			n++;
		}
		
		readMaxItem = n;
	}
	
	public LinkedList<HighlightSearchEntry> getHighlights() {
		if (highlights == null) {
			parse();
		}
		return highlights;
	}
	
	public void save(LinkedList<HighlightSearchEntry> highlights) {
		Integer n = 0;
		for(HighlightSearchEntry entry: highlights) {
			String token = entry.getToken();
			
			if (token.length() == 0) {
				continue;
			}
			String type = entry.getType().toString();
			String color = Integer.toString(entry.getColor().getRGB());
			String active = entry.isActive().toString();
			
			getConfig().setProperty(HIGHLIGHT_TOKEN + n, token);
			getConfig().setProperty(HIGHLIGHT_TYPE + n, type);
			getConfig().setProperty(HIGHLIGHT_COLOR + n, color);
			getConfig().setProperty(HIGHLIGHT_ACTIVE + n, active);
			
			n++;
		}
		
		while (n < readMaxItem) {
			getConfig().setProperty(HIGHLIGHT_TOKEN + n, "");
			getConfig().setProperty(HIGHLIGHT_TYPE + n, "");
			getConfig().setProperty(HIGHLIGHT_COLOR + n, "");
			getConfig().setProperty(HIGHLIGHT_ACTIVE + n, "");
			n++;
		}
	}

}
