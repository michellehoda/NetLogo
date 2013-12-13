package org.nlogo.deltatick;

import org.nlogo.deltatick.dialogs.ColorButton;
import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.deltatick.dnd.QuantityDropTarget;
import org.nlogo.deltatick.xml.Variation;
import org.nlogo.window.MonitorWidget;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public strictfp class QuantityBlock
        extends CodeBlock {

    //transient JPanel penColorButton;
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
    boolean isTrait = false;
    JLabel image;
    ImageIcon histoImageIcon;
    ImageIcon lineImageIcon;

    TraitBlockNew tBlock;
    JPanel traitblockLabelPanel = null;



    public QuantityBlock(String name, boolean histo, String bars, String trait, String xLabel, String yLabel, boolean isTrait) {
        super(name, ColorSchemer.getColor(2));
        this.histo = histo;
        this.bars = bars;
        this.trait = trait;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.isTrait = isTrait;
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                quantityBlockFlavor
        };

        colorButton = new ColorButton(parent, this);  //commented out for interviewing Gabriel (March 9, 2013)
        colorButton.setBackground(color);
        makeQuantityBlockLabel();
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
            name.setText("count by");
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

    public void removeInput() {
        for (PrettyInput input : inputs.values()) {
            input.setVisible(false);
            //this.remove(input);
        }
    }

    public void setTrait(TraitBlockNew tBlock) {
        this.tBlock = tBlock;
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
        if ((parent instanceof PlotBlock) && ((PlotBlock) parent).isHisto == false) {
            passBack += "to-report " + getName();
            if (inputs.size() > 0) {
                passBack += " [ ";
                for (String input : inputs.keySet()) {
                    passBack += input + " ";
                }
                passBack += "]";
            }
            passBack += "\n " + code + "\nend\n";
        }

        if (parent instanceof MonitorBlock) {
            passBack += "to-report " + getName();
            if (inputs.size() > 0) {
                passBack += " [ ";
                for (String input : inputs.keySet()) {
                    passBack += input + " ";
                }
                passBack += "]";
            }
            passBack += "\nreport " + code + "\nend\n";
        }

        return passBack;
    }


    public String unPackAsCommand() {
        String passBack = "";
        String population = "";
        String variable = "";
        Container parent = getParent();

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

                if (isTrait && (tBlock != null)) {
                    variable = tBlock.getMyTraitName();
                    population = tBlock.getBreedName();
                }
                passBack += "\thistogram [ " + variable + " ] of " + population ;
                //passBack += "histogram [ " + tBlock.getName() + " ] of " + tBlock.getBreedName();
                passBack += "\n";
            }

            else {
                // Not Histogram -- plot line
                variable = "";
                // Plot command
                if (isRunResult()) {
                    passBack += "\tplot (runresult task [" + getName() + " ";
                }
                else {
                    passBack += "plot " + getName() + " ";
                }
                // Generate plot task parameters
                if (isTrait && (tBlock != null)) {
                    variable = tBlock.getMyTraitName();
                    population = tBlock.getBreedName();
                }
                else {
                    for (JTextField input : inputs.values()) {
                        if (input.getName().equalsIgnoreCase("variable")) {
                            variable = input.getText();
                        }
                        if (input.getName().equalsIgnoreCase("breed-type")) {
                            population = input.getText();
                        }
                    }
                }
                // If runresult, enclose variable (trait) in double quotes to pass as a string
                variable = isRunResult() ? "\"" + variable + "\"" : variable;
                passBack += population + " " + variable;
                passBack += isRunResult() ? "])" : "";
//                if (isRunResult()) {
//                    passBack += "\tplot (runresult task [" + getName() + " ";
//                    for (JTextField input : inputs.values()) {
//                        if (input.getName().equalsIgnoreCase("variable")) {
//                            variable += "\"" + input.getText() + "\"" + " ";
//                        }
//                        else {
//                            variable += input.getText() + " ";
//                        }
//                    }
//                    passBack += variable + "])";
//                }
//                else {
//                    passBack += "plot " + getName() + " ";
//                    for (JTextField input : inputs.values()) {
//                        variable += input.getText() + " ";
//                    }
//                    passBack += variable;
//                }
            }

            passBack += "\n";
        }
        penUpdateCode = passBack;
        return passBack;
    }

    public String getMonitorCode() {
        String code = new String();
        if (getHisto() == false) {
            code += getName() + " ";
            if (inputs.size() > 0) {
                for (JTextField input : inputs.values()) {
                    code += input.getText() + " ";
                }
            }
        }
        else if (getHisto() == true) {
            //insert code
        }

        return code;
    }

    public Map<String, PrettyInput> getInputs() {
        return inputs;
    }

    public void setLabelImage() {
        try {
            image = new JLabel();

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

    public void addTraitblockPanel() {

        if (isTrait == true) {
            removeInput();
            // Set up the panel
            traitblockLabelPanel = new JPanel();
            traitblockLabelPanel.setPreferredSize(new Dimension(this.getWidth(), 40));
            traitblockLabelPanel.setBackground(getBackground());
            traitblockLabelPanel.setAlignmentX(CENTER_ALIGNMENT);
            traitblockLabelPanel.setAlignmentY(CENTER_ALIGNMENT);
            traitblockLabelPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

            // Set up the label
            JLabel traitblockLabel = new JLabel();
            traitblockLabel.setText("Add trait block here");
            traitblockLabel.setFont(new Font("Arial", 1, 11));
            traitblockLabel.setBackground(getBackground());
            traitblockLabel.setForeground(Color.WHITE);
            traitblockLabel.setAlignmentX(CENTER_ALIGNMENT);
            traitblockLabel.setAlignmentY(CENTER_ALIGNMENT);

            // Add label and update
            traitblockLabelPanel.add(traitblockLabel);
            traitblockLabelPanel.setVisible(true);
            traitblockLabelPanel.validate();
            add(traitblockLabelPanel);
            validate();
        }
    }

    public void removeTraitblockPanel() {
        if (traitblockLabelPanel != null) {
            remove(traitblockLabelPanel);
        }
        validate();
    }

    public void addColorButton() {
        label.add(colorButton);
        colorButton.setVisible(false);
        validate();
    }

    public void showColorButton() {
        colorButton.setVisible(true);
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
        if (isTrait && (tBlock != null)) {
            passBack += "-" + tBlock.getMyTraitName();
            passBack += "-" + tBlock.getBreedName();
        }
        else {
            for (JTextField input : inputs.values()) {
                passBack += "-" + input.getText();
            }
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
            retLabel = "Variations of " + tBlock.getTraitName();
            //inputs.get("trait").getText();
        }
        return retLabel;
    }
    public String getYLabel() {
        String retLabel = yLabel;
        String breedName = null;
        for (Map.Entry<String, PrettyInput> entry : inputs.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("breed-type")) {
                breedName = entry.getValue().getText();
            }
        }
        if (histo && (tBlock != null)) {
            retLabel += " " + tBlock.getBreedName();
        }
        else if (isTrait && (tBlock != null)) {
            retLabel += " " + tBlock.getMyTraitName();
        }
        else if (breedName != null) {
            retLabel += " " + breedName;
        }
        return retLabel;
    }

    public String getTrait() {
        return tBlock.getTraitName();
    }

    public String getTraitBreed() {
        return tBlock.getBreedName();
    }

    public HashMap<String, Variation> getHistoVariation() {
        return tBlock.getVariationHashMap();
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
    public boolean getIsTrait() {
        return isTrait;
    }

}
