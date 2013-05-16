package org.nlogo.deltatick.dnd;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

public class MaxLengthNoSpaceDocument extends MaxLengthTextDocument {
	
	public void insertString(int offs, String str, AttributeSet attr) throws BadLocationException {
		String newstr = str.replaceAll(" ", ""); 
		super.insertString(offs, newstr, attr);
		}
			    
	public void replace(int offs, int len, String str, AttributeSet attr) throws BadLocationException {
		String newstr = str.replaceAll(" ", "");  
		super.replace(offs, len, newstr, attr);
		}
}
