package org.zaproxy.zap.view;

/**
 * The Class ComboBoxValue is as a container to ease the display (in ComboBoxes) of Objects with a label
 * different from the {@link #toString()}.
 * 
 * @param <E> the element type
 */
public class ComboBoxItem<E> {

	/** The value. */
	private E value;

	/** The label. */
	private String label;

	/**
	 * Instantiates a new combo box value.
	 * 
	 * @param value the value
	 * @param label the label
	 */
	public ComboBoxItem(E value, String label) {
		super();
		this.value = value;
		this.label = label;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public E getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value the new value
	 */
	public void setValue(E value) {
		this.value = value;
	}

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * 
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
