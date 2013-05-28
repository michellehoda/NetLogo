package org.nlogo.deltatick;

import org.nlogo.deltatick.dialogs.ColorButton;
import org.nlogo.deltatick.dnd.PrettyInput;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;
//import org.nlogo.deltatick.xml.Breed;

public strictfp class QuantityBlock
        extends CodeBlock {

    transient JPanel penColorButton;
    transient JFrame parent;
    Color penColor = Color.black;
    boolean histo = false;
    String bars = "0";
    String trait = " ";
    String population;
    String variable;

    // The pen name is stored in this string when the tab is switched May 12, 2013
    String savedPenName = new String("");

    String penSetUpCode;
    String penUpdateCode;
    String penColorString;
    ColorButton colorButton;

    String xLabel;
    String yLabel;

    boolean isRunResult = false;

    JLabel image;
    //Image histoImage;
    ImageIcon histoImageIcon;
    //Image lineImage;
    ImageIcon lineImageIcon;



    public QuantityBlock(String name, boolean histo, String bars, String trait, String xLabel, String yLabel) {
        super(name, ColorSchemer.getColor(2));
        this.histo = histo;
        this.bars = bars;
        this.trait = trait;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                quantityBlockFlavor
        };

        colorButton = new ColorButton(parent, this);  //commented out for interviewing Gabriel (March 9, 2013)
        colorButton.setBackground(color);
        //label.add(colorButton);
        makeQuantityBlockLabel();
        //updateLabelImage();
        validate();
    }

    @Override
    public void makeLabel() {
    }

    public void makeQuantityBlockLabel() {
        label.setBackground(getBackground());

        setLabelImage();

        JLabel name = new JLabel(getName());
        if (histo) {
            name.setText("count");
        }
        java.awt.Font font = name.getFont();
        name.setFont(new java.awt.Font("Arial", font.getStyle(), 12));

        label.add(name);
        label.validate();
        validate();
    }

    @Override
    public void addInput(String inputName, String defaultValue) {
        PrettyInput input = new PrettyInput(this);
        input.setName(inputName);
        input.setText(defaultValue);

        //inputs is a linked hashmap <String, JTextField> (march 2)
        inputs.put(inputName, input);

        if (this.histo) {
            if (inputName.equalsIgnoreCase("trait")) {
                JLabel byLabel = new JLabel();
                byLabel.setText("by");
                java.awt.Font font = byLabel.getFont();
                byLabel.setFont(new java.awt.Font("Arial", font.getStyle(), 12));
                label.add(byLabel);
            }
        }

        label.add(input);

    }

    // Trying to remove pen from parent plotblock when a quantity block is removed (aditi Apr 10, 2013)
    @Override
    public void die() {
        super.die();
        //Container parent = getParent();
        if (myParent instanceof PlotBlock) {
            if (((PlotBlock) myParent).getNetLogoPlot() != null) {
            // Remove pen
            ((PlotBlock) myParent).removePen(getPenName());
            }
        }
        myParent.doLayout();
        myParent.validate();
        myParent.repaint();
    }

    public String unPackAsCode() {
        if (myParent == null) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }


    public String unPackAsProcedure() {

        String passBack = "";
        Container parent = getParent();
        if (parent instanceof PlotBlock) {
        passBack += "to-report " + getName();
        if (inputs.size() > 0) {
            passBack += " [ ";
            for (String input : inputs.keySet()) {
                passBack += input + " ";
            }
            passBack += "]";
        }
        passBack += "\n";
        passBack += "report " + code;
        passBack += "\n";
        passBack += "end";
        passBack += "\n";
        passBack += "\n";
        }

        if (parent instanceof HistogramBlock) {
            passBack = "" ;
        }

        return passBack;
    }


    public String unPackAsCommand() {
        String passBack = "";
        Container parent = getParent();

        /* not being used because HistogramBlock is not used any more -Aditi (Jan 15, 2013)
        if (parent instanceof HistogramBlock) {
            passBack += "set-plot-pen-mode 1 \n";

            for (Map.Entry<String, JTextField> entry : inputs.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("breed-type")) {
                    population = entry.getValue().getText().toString();
                }
                if (entry.getKey().equalsIgnoreCase("trait")) {
                    variable = entry.getValue().getText().toString();
                }
            }
            //passBack += "set-histogram-num-bars " + bars + "\n";
            //passBack += "set-plot-x-range 0 max " + getName() + " ";
            //passBack += "plotxy" + x + y + "\n";
            passBack += "histogram [ " + variable + " ] of " + population ;
            passBack += "\n";
        }
        */

        if (parent instanceof PlotBlock) {
            //passBack += "  set-current-plot-pen \"" + this.getName() + "\" \n"; // commented 20130319
            passBack += "\tset-current-plot-pen \"" + this.getPenName() + "\" \n";
            if (colorButton.gotColor() == true) {
                passBack += "\tset-plot-pen-color " + colorButton.getSelectedColorName() + "\n";
            }

            if (((PlotBlock) parent).isHisto == true) {
                passBack += "\tset-plot-pen-mode 1 \n";
                for (Map.Entry<String, PrettyInput> entry : inputs.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("breed-type")) {
                        population = entry.getValue().getText().toString();
                    }
                    if (entry.getKey().equalsIgnoreCase("trait")) {
                        variable = entry.getValue().getText().toString();
                    }
                }
                passBack += "\thistogram [ " + variable + " ] of " + population ;
                passBack += "\n";
            }

            else {
                // Not Histogram -- plot line
                if (isRunResult()) {
                    passBack += "\tplot (runresult task [" + getName() + " ";
                    for (JTextField input : inputs.values()) {
                        if (input.getName().equalsIgnoreCase("variable")) {
                            passBack += "\"" + input.getText() + "\"" + " ";
                        }
                        else {
                            passBack += input.getText() + " ";
                        }
                    }
                    passBack += "])";
                }
                else {
                    passBack += "plot " + getName() + " ";
                    for (JTextField input : inputs.values()) {
                        passBack += input.getText() + " ";
                    }
                }
            }

            passBack += "\n";
        }
        penUpdateCode = passBack;
        return passBack;
    }


    public Map<String, PrettyInput> getInputs() {
        return inputs;
    }

    public void setLabelImage() {
        try {
            image = new JLabel();
              //trying new stuff here to make this work
            image.setTransferHandler(new TransferHandler("button"));
            image.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent evt) {
                    JComponent comp = (JComponent) evt.getSource();
                    TransferHandler th = comp.getTransferHandler();
                    th.exportAsDrag(comp, evt, TransferHandler.COPY);
                }
            });

            histoImageIcon = new ImageIcon(ImageIO.read(getClass().getResource("/images/deltatick/bar-graph.png")));

            lineImageIcon = new ImageIcon(ImageIO.read(getClass().getResource("/images/deltatick/line-graph.png")));
            image.setTransferHandler(new TransferHandler("image"));

            if (histo == true) {
                image.setIcon(histoImageIcon);
                label.add(image);
            }
            else if (histo == false) {
                image.setIcon(lineImageIcon);
                label.add(image);
            }
        }
        catch (IOException ex) {
            System.err.println("Could not set label image for quantity block " + getName());
             }
    }

    public void updateLabelImage() {
        try {
            histoImageIcon = new ImageIcon(ImageIO.read(getClass().getResource("/images/deltatick/bar-graph.png")));
        if (histo == true) {
            image.setIcon(histoImageIcon);
            image.revalidate();
            }
        }
        catch (IOException ex) {
        }
        validate();
    }

    public void addColorButton() {
        label.add(colorButton);
        validate();
    }

    public void setRunResult(boolean runResult) {
        isRunResult = runResult;
    }

    public boolean isRunResult() {
        return isRunResult;
    }

    public void setButtonColor( Color color ) {
        colorButton.setBackground(color);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);
    }

    public String getPenName() {
        String passBack = new String();
        passBack += getName();
        for (JTextField input : inputs.values()) {
            passBack += "-" + input.getText();
        }
        return passBack;
    }

    public void setSavedPenName() {
        savedPenName = new String(getPenName());
    }
    public String getSavedPenName() {
        return savedPenName;
    }

    public String getXLabel() {
        String retLabel = xLabel;
        if (histo) {
            retLabel = inputs.get("trait").getText();
        }
        return retLabel;
    }
    public String getYLabel() {
        return yLabel;
    }

    public void mouseReleased(java.awt.event.MouseEvent event) {
    }

    public void mouseEntered(java.awt.event.MouseEvent event) {
    }

    public void mouseExited(java.awt.event.MouseEvent event) {
    }

    public void mousePressed(java.awt.event.MouseEvent event) {
    }

        /*

        public void mouseClicked(java.awt.event.MouseEvent event) {
            penColor = JColorChooser.showDialog(null, "Pick a pen color...", java.awt.Color.BLACK);
            //if( penColor == null ) { penColor = java.awt.Color.BLACK; }
            this.setBackground(penColor);
            this.setForeground(penColor);
            this.setVisible(true);
            //System.out.println(penColor);
            //System.out.println(penColor.getRGB());
        }

    }

    private final javax.swing.Action colorAction =
            new javax.swing.AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JColorChooser.showDialog(null, "Pick a pen color...", java.awt.Color.BLACK);

                    //System.out.println(penColor);
                    //System.out.println(penColor.getRGB());
                    //penColorButton.setBackground( penColor );
                }
            };

    public Color getPenColor() {
        //System.out.println(penColor);
        return penColor;
    }
    */
    public String getPenUpdateCode() {
        return penUpdateCode;
    }

    public boolean getHisto() {
        return histo;
    }

}
