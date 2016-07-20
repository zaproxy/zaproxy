/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright the ZAP Development team
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
 * 
 * Please note that this file was originally released under the 
 * GNU General Public License  as published by the Free Software Foundation; 
 * either version 2 of the License, or (at your option) any later version
 * by Compass Security AG
 * 
 * As of October 2014 Compass Security AG granted the OWASP ZAP Project 
 * permission to redistribute this code under the Apache License, Version 2.0 
 */

package ch.csnc.extension.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class DriverConfiguration extends Observable {
	private File file = null;

	private Vector<String> names;
	private Vector<String> paths;
	private Vector<Integer> slots;
	private Vector<Integer> slotListIndexes;

	private final Logger logger = Logger.getLogger(this.getClass());

	public DriverConfiguration(File file) {
		this.file = file;
		names = new Vector<String>();
		paths = new Vector<String>();
		slots = new Vector<Integer>();
		slotListIndexes = new Vector<Integer>();

		try {
			final Document doc = new SAXBuilder().build(file);
			final Element root = doc.getRootElement();
			for (final Object o : root.getChildren("driver")) {
				final Element nameElement = ((Element) o).getChild("name");
				names.add(nameElement.getValue());

				final Element pathElement = ((Element) o).getChild("path");
				paths.add(pathElement.getValue());

				final Element slotElement = ((Element) o).getChild("slot");
				slots.add(getIntValue(slotElement));

				final Element slotListIndex = ((Element) o).getChild("slotListIndex");
				slotListIndexes.add(getIntValue(slotListIndex));
			}

		} catch (final JDOMException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error accessing key store: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error(e.getMessage(), e);
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error accessing key store: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error(e.getMessage(), e);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error slot or slot list index is not a number: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Gets an integer from the given element.
	 * <p>
	 * If the given element is {@code null} or does not have an integer, zero is returned.
	 *
	 * @param element the element with the integer value
	 * @return an integer
	 */
	private int getIntValue(Element element) {
		if (element != null) {
			try {
				return Integer.parseInt(element.getValue());
			} catch (NumberFormatException e) {
				logger.error("Failed to extract an integer from: " + element.getValue());
			}
		}
		return 0;
	}

	public void write() {
		final Document doc = new Document();
		final Element root = new Element("driverConfiguration");
		doc.addContent(root);

		for (int i = 0; i < names.size(); i++) {

			final Element driver = new Element("driver");
			root.addContent(driver);

			final Element name = new Element("name");
			driver.addContent(name);
			name.addContent(names.get(i));

			final Element path = new Element("path");
			driver.addContent(path);
			path.addContent(paths.get(i));

			final Element slot = new Element("slot");
			driver.addContent(slot);
			slot.addContent(slots.get(i).toString());

			final Element slotListIndex = new Element("slotListIndex");
			driver.addContent(slotListIndex);
			slotListIndex.addContent(slotListIndexes.get(i).toString());
		}

		try {
			final OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
			final XMLOutputter out = new XMLOutputter();
			out.output(doc, fileOutputStream);
			fileOutputStream.close();
		} catch (final FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error accessing key store: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error(e.getMessage(), e);
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(null, new String[] {
					"Error accessing key store: ", e.toString() }, "Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error(e.getMessage(), e);
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

	public Vector<Integer> getSlotIndexes() {
		return slotListIndexes;
	}

	public void setSlotListIndexes(Vector<Integer> slotListIndexes) {
		this.slotListIndexes = slotListIndexes;
	}

}
