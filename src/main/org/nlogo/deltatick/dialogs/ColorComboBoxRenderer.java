package org.nlogo.deltatick.dialogs;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: salilw
 * Date: 10/14/13
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorComboBoxRenderer extends JLabel
        implements ListCellRenderer {
    Color chosenColor = Color.GRAY;

    public ColorComboBoxRenderer() {
        // Set opaque
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        // Don't use index, obtain selected index from value
        // see: http://docs.oracle.com/javase/tutorial/uiswing/examples/components/CustomComboBoxDemoProject/src/components/CustomComboBoxDemo.java

        if (value instanceof Color) {
            chosenColor = (Color) value;
        }
        return this;
    }

    public void paint(Graphics g) {
        setBackground(chosenColor);
        super.paint(g);
    }
}
