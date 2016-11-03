package org.knime.preferences.prefCreator.gui;

import java.awt.Font;

import javax.swing.JTextArea;

/**
 * This class represents a JTextArea which automatically breaks lines.
 * @author Stefan Wohlfart
 * version 1.0
 *
 */
@SuppressWarnings("serial")
public class CustomWrappedLabel extends JTextArea {
	
	/**
	 * Constructor which sets all important settings.
	 * @param text - a text which should be displayed by the CustomWrappedLabel
	 */
    public CustomWrappedLabel(String text) {
        super(text);
        setBackground(null);
        setEditable(false);
        setBorder(null);
        setLineWrap(true);
        setWrapStyleWord(true);
        setFocusable(false);
        Font font = getFont();  
        setFont(font.deriveFont(Font.BOLD));
    }
}