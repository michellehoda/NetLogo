package org.nlogo.deltatick.dnd;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

public class JNumberFieldFilter extends MaxLengthNoSpaceDocument{

	private static String allowedChars = "0123456789";
   
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException{
    	String newStr = str;
    	
    	if(allowedChars.indexOf(newStr.charAt(0)) < 0){
    		newStr = newStr.replaceAll(newStr.substring(0, 1), "");
    	}	
		
    	super.insertString(offset, newStr, attr);
    }
    
    public void replace(int offs, int len, String str, AttributeSet attr) throws BadLocationException {
    	String newStr = str;
    	
    	if(allowedChars.indexOf(newStr.charAt(0)) < 0){
    		newStr = newStr.replaceAll(newStr.substring(0, 1), "");
    	}
    
    	super.replace(offs, len, newStr, attr);
	}
}