package cz.zcu.kiv.multiclouddesktop.data;

/**
 * cz.zcu.kiv.multiclouddesktop.data/ListDisplayType.java			<br /><br />
 *
 * List of possible display types of the items in the data list.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum ListDisplayType {

	ICONS("Icons"),
	LINES("Lines");

	/**
	 * Convert string to enumeration ignoring case.
	 * @param text String to be converted.
	 * @return Enumeration returned.
	 */
	public static ListDisplayType fromString(String text) {
		if (text != null) {
			/* try to match the supplied text with associated strings */
			for (ListDisplayType type: ListDisplayType.values()) {
				if (text.equalsIgnoreCase(type.text)) {
					return type;
				}
			}
		}
		/* if the text doesn't match any option, return lines as default */
		return LINES;
	}

	/** Text of the enumeration. */
	private String text;

	/**
	 * Ctor with parameter.
	 * @param text Text of the enumeration.
	 */
	ListDisplayType(String text) {
		this.text = text;
	}

	/**
	 * Returns the text of the enumeration.
	 * @return Text of the enumeration.
	 */
	public String getText() {
		return text;
	}
}
