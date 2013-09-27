package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.JCharNumberFieldFilter;
import org.nlogo.window.MonitorWidget;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 9/25/13
 * Time: 12:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class MonitorBlock
        extends CodeBlock
        implements MouseMotionListener,
        MouseListener {

    MonitorBlock monitorBlock = this; // for deleteAction
    JTextField monitorNameField;
    MonitorWidget monitorWidget;
    //org.nlogo.plot.Plot netLogoPlot;
    JCharNumberFieldFilter textFilter;

    JPanel quantityblockLabelPanel;

    public MonitorBlock() {
        super("monitor", ColorSchemer.getColor(3));
        //makePlotLabel();
        //add(label);
        addQuantityblockPanel();

        setBorder(org.nlogo.swing.Utils.createWidgetBorder());

        addMouseMotionListener(this);
        addMouseListener(this);

        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                plotBlockFlavor
        };
    }


    public void addQuantityblockPanel() {
        // Set up the panel
        quantityblockLabelPanel = new JPanel();
        quantityblockLabelPanel.setPreferredSize(new Dimension(this.getWidth(), 50));
        quantityblockLabelPanel.setBackground(getBackground());
        quantityblockLabelPanel.setAlignmentX(CENTER_ALIGNMENT);
        quantityblockLabelPanel.setAlignmentY(CENTER_ALIGNMENT);
        quantityblockLabelPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        // Set up the label
        JLabel traitblockLabel = new JLabel();
        traitblockLabel.setText("Add orange block here");
        traitblockLabel.setFont(new Font("Arial", 0, 11));
        traitblockLabel.setBackground(getBackground());
        traitblockLabel.setForeground(Color.BLACK);
        traitblockLabel.setAlignmentX(CENTER_ALIGNMENT);
        traitblockLabel.setAlignmentY(CENTER_ALIGNMENT);

        // Add label and update
        quantityblockLabelPanel.add(traitblockLabel);
        quantityblockLabelPanel.setVisible(true);
        quantityblockLabelPanel.validate();
        add(quantityblockLabelPanel);
        validate();
    }

    public void removeQuantityblockPanel() {
        remove(quantityblockLabelPanel);
        validate();
    }

    public java.util.List<QuantityBlock> getMyBlocks() {
        java.util.List<QuantityBlock> blocks = new ArrayList<QuantityBlock>();

        for (CodeBlock block : myBlocks) {
            if (block instanceof QuantityBlock) {
                blocks.add((QuantityBlock) block);
            }
        }
        return blocks;
    }

    public String unPackAsCode() {
        String passBack = "";
        for (QuantityBlock quantBlock : getMyBlocks()) {
            passBack += "  " + quantBlock.unPackAsCommand();
        }
        return passBack;
    }

    public void mouseEnter(MouseEvent evt) {
    }

    public void mouseExit(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseMoved(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
    }

    int beforeDragX;
    int beforeDragY;

    int beforeDragXLoc;
    int beforeDragYLoc;

    public void mousePressed(MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        beforeDragX = point.x;
        beforeDragY = point.y;
        beforeDragXLoc = getLocation().x;
        beforeDragYLoc = getLocation().y;
    }

    public void mouseDragged(MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        this.setLocation(
                point.x - beforeDragX + beforeDragXLoc,
                point.y - beforeDragY + beforeDragYLoc);
    }

}
