/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.multiFuzz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.ArrayUtils;

class CharTypes {
	static boolean isLetter(char c) {
		return (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z'));
	}

	static boolean isSmall(char c) {
		return (('a' <= c && c <= 'z') || c == '_');
	}

	static boolean isLarge(char c) {
		return ('A' <= c && c <= 'Z');
	}

	static boolean isDigit(char c) {
		return ('0' <= c && c <= '9');
	}

	static boolean isCharSet(char c) {
		return (c == '\\' || c == '[' || c == '.');
	}

	static boolean isRepeat(char c) {
		return (c == '*' || c == '+' || c == '?');
	}

	static boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f');
	}

	static boolean isNewline(char c) {
		return (c == '\r' || c == '\n' || c == '\f');
	}
}

class ParseTree {
	int label = -1;
	ArrayList<Character> value = new ArrayList<Character>();
	ArrayList<ParseTree> children = new ArrayList<ParseTree>();

	public ParseTree(int label) {
		this.label = label;
	}

	public int getLabel() {
		return label;
	}

	public ArrayList<Character> getValue() {
		return value;
	}

	public void addValue(char c) {
		this.value.add(c);
	}

	public void addValues(ArrayList<Character> values) {
		this.value.addAll(values);
	}

	public void setValue(ArrayList<Character> value) {
		this.value = value;
	}

	public ArrayList<ParseTree> getChildren() {
		return children;
	}

	public void addChild(ParseTree child) {
		if (child.getLabel() != -1) {
			this.children.add(child);
		}
	}

	public void prettyPrint(String tab) {
		System.out.print(label);
		for (Character c : value) {
			System.out.print(" " + c);
		}
		System.out.println();
		for (ParseTree p : children) {
			System.out.print(tab + "----");
			p.prettyPrint(tab + "|   ");
		}

	}
}

class Parser {
	final int INVALID = -1;
	// ParseTokens
	final int START = 0;
	final int UNION = 1;
	final int CONCAT = 2;
	final int QUEST = 3;
	final int STAR = 4;
	final int PLUS = 5;
	final int SET = 6;
	final int LIT = 7;
	// LexTokens
	final int NEGLECT = 8;
	final int REPEAT = 9;
	final int BRACKET = 10;

	final Character[] DIGITS = ArrayUtils.toObject("0123456789".toCharArray());
	final Character[] LOWER = ArrayUtils.toObject("abcdefghijklmnopqrstuvwxyz_"
			.toCharArray());
	final Character[] UPPER = ArrayUtils.toObject("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray());
	final Character[] WHITE = ArrayUtils.toObject(" \t\r\n\f".toCharArray());

	public ParseTree parse(String s) throws Exception {
		return parse(s, new ParseTree(START));

	}

	public ParseTree parse(String s, ParseTree t) throws Exception {
		char c = s.charAt(0);
		ParseTree lh_child = t;
		ParseTree rh_child;
		String remainder = s.substring(1);
		switch (lexClass(c)) {
		case LIT:
			rh_child = new ParseTree(LIT);
			rh_child.addValue(c);
			t = new ParseTree(CONCAT);
			t.addChild(lh_child);
			t.addChild(rh_child);
			break;
		case SET:
			rh_child = new ParseTree(SET);
			if (c == '.') {
				rh_child.addValues(getSet('w'));
			} else if (c == '\\') {
				rh_child.addValues(getSet(s.charAt(1)));
				remainder = s.substring(2);
			} else {
				int b = s.indexOf(']');
				rh_child.addValues(parseSet(s.substring(1, b)));
				remainder = s.substring(b + 1);
			}
			t = new ParseTree(CONCAT);
			t.addChild(lh_child);
			t.addChild(rh_child);
			break;
		case REPEAT:
			ArrayList<ParseTree> ch = t.getChildren();
			ParseTree newCh = new ParseTree(getFunc(c));
			newCh.addChild(ch.get(ch.size() - 1));
			ch.remove(ch.size() - 1);
			ch.add(newCh);
			break;
		case BRACKET:
			int b_count = 1;
			int index = 1;
			while (b_count > 0) {
				if (s.charAt(index) == '(') {
					b_count++;
				} else if (s.charAt(index) == ')') {
					b_count--;
				}
				index++;
			}
			rh_child = parse(s.substring(1, index - 1), new ParseTree(START));
			remainder = s.substring(index);
			t = new ParseTree(CONCAT);
			t.addChild(lh_child);
			t.addChild(rh_child);
			break;
		case UNION:
			t = new ParseTree(UNION);
			t.addChild(lh_child);
			rh_child = parse(s.substring(1), new ParseTree(START));
			t.addChild(rh_child);
			remainder = "";
			break;
		default:
			break;
		}
		if (remainder.equals("")) {
			return t;
		} else {
			return parse(remainder, t);
		}
	}

	private int getFunc(char c) {
		switch (c) {
		case '*':
			return STAR;
		case '?':
			return QUEST;
		case '+':
			return PLUS;
		default:
			break;
		}
		return -1;
	}

	private ArrayList<Character> parseSet(String s) {
		ArrayList<Character> vals = new ArrayList<Character>();
		int index = 0;
		while (s.length() > index) {
			char c = s.charAt(index);
			if (c == '\\') {
				vals.addAll(getSet(s.charAt(index + 1)));
				index += 2;
			} else if (c == '-') {
				char end = s.charAt(index + 1);
				for (char i = (char) (s.charAt(index - 1) + 1); i <= end; i++) {
					vals.add(i);
				}
				index += 2;
			} else if (CharTypes.isDigit(c) || CharTypes.isLetter(c)) {
				vals.add(c);
				index++;
			} else {
				s = s.substring(0, index) + s.substring(index + 1, s.length());
			}
		}
		return vals;
	}

	private ArrayList<Character> getSet(char c) {

		ArrayList<Character> ret = new ArrayList<Character>();
		switch (c) {
		case 'd':
			ret.addAll(Arrays.asList(DIGITS));
			break;
		case 's':
			ret.addAll(Arrays.asList(WHITE));
			break;
		case 'w':
			ret.addAll(Arrays.asList(LOWER));
			ret.addAll(Arrays.asList(UPPER));
			ret.addAll(Arrays.asList(DIGITS));
			break;
		default:
			break;
		}
		return ret;
	}

	private int lexClass(char c) {
		if (CharTypes.isLetter(c) || CharTypes.isDigit(c)) {
			return LIT;
		} else if (CharTypes.isWhitespace(c) || CharTypes.isNewline(c)) {
			return NEGLECT;
		} else if (CharTypes.isCharSet(c)) {
			return SET;
		} else if (CharTypes.isRepeat(c)) {
			return REPEAT;
		} else if (c == '|') {
			return UNION;
		} else if (c == '(') {
			return BRACKET;
		} else {
			return INVALID;
		}
	}
}

class State {
	ArrayList<State> empty = new ArrayList<State>();
	HashMap<Character, HashSet<State>> out = new HashMap<Character, HashSet<State>>();

	public void addTransition(State goal, Character t) {
		if (t == null) {
			empty.add(goal);
			return;
		}
		if (!out.keySet().contains(t)) {
			out.put(t, new HashSet<State>());
		}
		out.get(t).add(goal);
	}
}

class NFA {
	ArrayList<State> states = new ArrayList<State>();
	int end = states.size();
	HashMap<String, HashSet<State>> travers = new HashMap<String, HashSet<State>>();

	void reset() {
		travers = new HashMap<String, HashSet<State>>();
		travers.put("", new HashSet<State>());
		travers.get("").add(states.get(0));
		travers.get("").addAll(states.get(0).empty);
	}

	public NFA(ParseTree t) {
		NFA f;
		NFA s;
		State b;
		State e;
		switch (t.getLabel()) {
		case 0: // START
			b = new State();
			states = new ArrayList<State>();
			states.add(b);
			end = 0;
			break;
		case 1: // UNION
			f = new NFA(t.getChildren().get(0));
			s = new NFA(t.getChildren().get(1));
			b = new State();
			e = new State();
			b.empty.add(f.states.get(0));
			b.empty.add(s.states.get(0));
			f.states.get(f.end).empty.add(e);
			s.states.get(s.end).empty.add(e);
			states.add(b);
			states.addAll(f.states);
			states.addAll(s.states);
			states.add(e);
			this.end = states.size() - 1;
			break;
		case 2: // CONCAT
			f = new NFA(t.getChildren().get(0));
			s = new NFA(t.getChildren().get(1));
			states.addAll(f.states);
			State rem = s.states.get(0);
			State rep = states.get(f.end);
			rep.out.putAll(rem.out);
			rep.empty.addAll(rem.empty);

			for (State p : s.states) {
				if (p.empty.contains(rem)) {
					p.empty.remove(rem);
					p.empty.add(rep);
				}
				for (HashSet target : p.out.values()) {
					if (target.contains(rem)) {
						target.remove(rem);
						target.add(rep);
					}
				}
			}
			s.states.remove(rem);
			states.addAll(s.states);
			end = states.size() - 1;
			break;
		case 3: // QUEST
			f = new NFA(t.getChildren().get(0));
			states.addAll(f.states);
			end = states.size() - 1;
			states.get(0).addTransition(states.get(end), null);
			break;
		case 4: // STAR
			f = new NFA(t.getChildren().get(0));
			states.addAll(f.states);
			end = states.size() - 1;
			states.get(end).addTransition(states.get(0), null);
			states.get(0).addTransition(states.get(end), null);
			break;
		case 5: // PLUS
			f = new NFA(t.getChildren().get(0));
			s = new NFA(t.getChildren().get(0));
			states.addAll(f.states);
			states.get(f.end).out.putAll(s.states.get(0).out);
			states.get(f.end).empty.addAll(s.states.get(0).empty);
			s.states.remove(0);
			states.addAll(s.states);
			end = states.size() - 1;
			states.get(f.end).addTransition(states.get(end), null);
			states.get(end).addTransition(states.get(f.end), null);
			break;
		case 6: // CHARCLASS
			b = new State();
			e = new State();
			for (Character c : t.getValue()) {
				b.addTransition(e, c);
			}
			states = new ArrayList<State>();
			states.add(b);
			states.add(e);
			end = 1;
			break;
		case 7: // LIT
			b = new State();
			e = new State();
			b.addTransition(e, t.getValue().get(0));
			states = new ArrayList<State>();
			states.add(b);
			states.add(e);
			end = 1;
		default:
			break;
		}
	}

	public HashSet<String> traversStep(int cutoff) {
		int limit = cutoff;
		HashSet<String> valid = new HashSet<String>();
		HashMap<String, HashSet<State>> newTravers = new HashMap<String, HashSet<State>>();
		for (String hist : travers.keySet()) {
			for (State s : travers.get(hist)) {
				for (Character c : s.out.keySet()) {
					String key = hist + c;
					HashSet<State> possOld = new HashSet<State>();
					HashSet<State> poss = s.out.get(c);
					while (!possOld.containsAll(poss)) {
						possOld.addAll(poss);
						for (State p : possOld) {
							poss.addAll(p.empty);
						}
					}
					if (newTravers.containsKey(key)) {
						newTravers.get(key).addAll(poss);
					} else {
						newTravers.put(key, poss);
					}
					if (poss.contains(states.get(end))) {
						valid.add(key);
						if (limit-- <= 0) {
							this.travers = newTravers;
							return valid;
						}
					}
				}
			}
		}
		this.travers = newTravers;
		return valid;
	}

	public ArrayList<String> travers(int depth, int cutoff) {
		ArrayList<String> results = new ArrayList<String>();
		reset();
		int limit = cutoff;
		for (int i = 0; i < depth; i++) {
			HashSet<String> stage = traversStep(limit);
			results.addAll(stage);
			limit -= stage.size();
			if (limit <= 0) {
				return results;
			}
		}
		return results;
	}
}

public class RegExStringGenerator {
	public ArrayList<String> regexExpansion(String s, int depth, int cutoff) {
		Parser p = new Parser();
		ParseTree t;
		try {
			t = p.parse(s);
			NFA d = new NFA(t);
			return d.travers(depth, cutoff);
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
}
