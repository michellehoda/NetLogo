package org.nlogo.deltatick.dnd;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class MaxLengthTextDocument extends PlainDocument {
	
	private int maxChars;
	
	public void insertString(int offs, String str, AttributeSet aSet) throws BadLocationException{
		
		if (str != null && (getLength() + str.length() < maxChars)){
			super.insertString(offs, str, aSet);
		}		
	}
	
	public int getMaxChars(){
		return maxChars;
	}
	
	public void setMaxChars(int maxC){
		maxChars = maxC;
	}
}
