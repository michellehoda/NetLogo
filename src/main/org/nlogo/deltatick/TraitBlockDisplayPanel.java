package org.nlogo.deltatick;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Created with IntelliJ IDEA.
 * User: salilw
 * Date: 10/27/13
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitBlockDisplayPanel extends JPanel {
    private JLabel traitLabel;
    // Set the width and height here
    private final int DEFAULT_WIDTH = 50;
    private final int DEFAULT_HEIGHT = 30;
    private final Color DEFAULT_COLOR = ColorSchemer.getColor(4);

    public TraitBlockDisplayPanel(String name) {
        // Set the layout for the panel
        this.setLayout(new BorderLayout(0, 0));

        // Customize the label to look like a trait block
        traitLabel = new JLabel(name, JLabel.CENTER);
        traitLabel.setBackground(this.DEFAULT_COLOR);
        traitLabel.setVerticalAlignment(SwingConstants.CENTER);
        traitLabel.setHorizontalAlignment(SwingConstants.CENTER);
        traitLabel.setFont(new java.awt.Font("Arial", Font.PLAIN, 11));
        traitLabel.setPreferredSize(new Dimension(this.DEFAULT_WIDTH, this.DEFAULT_HEIGHT));

        // Add the label and customize the panel to look like a trait block
        this.add(traitLabel, BorderLayout.CENTER);
        this.setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        this.setBackground(this.DEFAULT_COLOR);
        this.setPreferredSize(new Dimension(this.DEFAULT_WIDTH, this.DEFAULT_HEIGHT));
    }
    // Update trait name
    public void setTraitName(String name) {
        traitLabel.setText(name);
        revalidate();
    }

}
