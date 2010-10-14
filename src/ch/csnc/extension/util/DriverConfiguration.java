/*
 * Copyright (C) 2010, Compass Security AG
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/copyleft/
 * 
 */


package ch.csnc.extension.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class DriverConfiguration extends Observable {
	final static String filename = "xml/drivers.xml";

	private Vector<String> names;
	private Vector<String> paths;
	private Vector<Integer> slots;

	public DriverConfiguration() {
		names = new Vector<String>();
		paths = new Vector<String>();
		slots = new Vector<Integer>();

		try {
			Document doc = new SAXBuilder().build(filename);
			Element root = doc.getRootElement();
			for (Object o : root.getChildren("driver")) {
				Element nameElement = ((Element) o).getChild("name");
				names.add(nameElement.getValue());

				Element pathElement = ((Element) o).getChild("path");
				paths.add(pathElement.getValue());

				Element slotElement = ((Element) o).getChild("slot");
				slots.add(Integer.parseInt(slotElement.getValue()));
			}

		} catch (JDOMException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error accessing key store: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error accessing key store: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error slot id is not a number: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public void write() {
		Document doc = new Document();
		Element root = new Element("driverConfiguration");
		doc.addContent(root);

		for (int i = 0; i < names.size(); i++) {

			Element driver = new Element("driver");
			root.addContent(driver);

			Element name = new Element("name");
			driver.addContent(name);
			name.addContent(names.get(i));

			Element path = new Element("path");
			driver.addContent(path);
			path.addContent(paths.get(i));

			Element slot = new Element("slot");
			driver.addContent(slot);
			slot.addContent(slots.get(i).toString());
		}

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			XMLOutputter out = new XMLOutputter();
			out.output(doc, fileOutputStream);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error accessing key store: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error accessing key store: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public Vector<String> getNames() {
		return names;
	}

	public void setNames(Vector<String> names) {
		this.names = names;
	}

	public Vector<String> getPaths() {
		return paths;
	}

	public void setPaths(Vector<String> paths) {
		this.paths = paths;
	}

	public Vector<Integer> getSlots() {
		return slots;
	}

	public void setSlots(Vector<Integer> slots) {
		this.slots = slots;
	}

}
