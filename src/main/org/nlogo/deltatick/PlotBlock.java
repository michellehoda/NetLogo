package org.nlogo.deltatick;

// need to have some kind of pens

import org.nlogo.deltatick.dnd.JCharNumberFieldFilter;
import org.nlogo.deltatick.dnd.PrettyInput;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.List;

public strictfp class PlotBlock
        extends CodeBlock
        implements MouseMotionListener,
        MouseListener {

    PlotBlock plotBlock = this; // for deleteAction
    JTextField plotNameField;
    org.nlogo.plot.Plot netLogoPlot;
    boolean isHisto = false;
    JCharNumberFieldFilter textFilter;

    JPanel quantityblockLabelPanel;

//    // UNUSED
//    public PlotBlock() {
//        super("plot", ColorSchemer.getColor(3));
//        setBorder(org.nlogo.swing.Utils.createWidgetBorder());
//        this.isHisto = false;
////        textFilter = new JCharNumberFieldFilter();
////        textFilter.setMaxChars(10);
////        plotNameField.setDocument(textFilter);
//
//        addMouseMotionListener(this);
//        addMouseListener(this);
//
//        flavors = new DataFlavor[]{
//                DataFlavor.stringFlavor,
//                plotBlockFlavor
//        };
//    }


    //constructor is for histograms if argument is true- not sure -A. (jan 15, 2013)
    public PlotBlock(boolean histo) {
        super("plot", ColorSchemer.getColor(3));
        this.isHisto = histo;

        makePlotLabel();
        add(label);
        addQuantityblockPanel();

        //label.add(removeButton);

//        if (this.isHisto == true) {
//            label.add(new JLabel("Histogram of "));
//        }
//        else {
//            label.add(new JLabel("Graph of "));
//        }
//        label.add(plotNameField);

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
        //quantityblockLabelPanel.setLayout(new BoxLayout(quantityblockLabelPanel, BoxLayout.Y_AXIS));
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


    //called in DeltaTickTab to populate plots -A. (sept 26)
    public void setNetLogoPlot(org.nlogo.plot.Plot netLogoPlot) {
        this.netLogoPlot = netLogoPlot;
    }

    // called in DeltaTickTab to get plots -A. (sept 26)
    public org.nlogo.plot.Plot getNetLogoPlot() {
        return netLogoPlot;
    }

    public void removePen(String penName) {
        netLogoPlot.removePen(penName);
    }

    /*
    public java.awt.Dimension getMinimumSize() {
        return new java.awt.Dimension( 250 , 200 );
    }

    public Dimension getPreferredSize() {
        return new java.awt.Dimension( 250 , 275 );
    }                  */

    public void addBlock (CodeBlock block) {
        System.out.println(myBlocks.size());
        if (isHisto &&
            (myBlocks.size() > 0)) {
            String message = "You can only use one block here";
            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            myBlocks.add(block);
            this.add(block);
            block.enableInputs();

            block.showRemoveButton();
            this.add(Box.createRigidArea(new Dimension(this.getWidth(), 4)));
            block.setMyParent(this);
            block.doLayout();
            block.validate();
            block.repaint();

            doLayout();
            validate();
            repaint();

            this.getParent().doLayout();
            this.getParent().validate();
            this.getParent().repaint();
        }
    }

    public List<QuantityBlock> getMyBlocks() {
        List<QuantityBlock> blocks = new ArrayList<QuantityBlock>();

        for (CodeBlock block : myBlocks) {
            if (block instanceof QuantityBlock) {
                blocks.add((QuantityBlock) block);
            }
        }

        return blocks;
    }

    @Override
    public void makeLabel() {
        showRemoveButton();
    }

    public void makePlotLabel() {
        if (this.isHisto == true) {
            label.add(new JLabel("Bar Graph of "));
        }
        else {
            label.add(new JLabel("Line Graph of "));
        }
        textFilter = new JCharNumberFieldFilter();
        textFilter.setMaxChars(10);
        plotNameField = new PrettyInput(this);
        plotNameField.setDocument(textFilter);

        label.add(plotNameField);
        label.setBackground(getBackground());
        validate();
    }


    public String getPlotName() {
        return plotNameField.getText();
    }

    public String unPackAsCode() {
        String passBack = "";
        passBack += "  set-current-plot \"" + getPlotName() + "\"\n";

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

    public void setPlotName(String name) {
        plotNameField.setText(name);
    }

    public boolean isHistogram() {
        return isHisto;
    }

    public void getPlotPen () {
        netLogoPlot.createPlotPen(plotNameField.getText(), false);
    }

    public JTextField getPlotNameField() {
        return plotNameField;
    }
}
