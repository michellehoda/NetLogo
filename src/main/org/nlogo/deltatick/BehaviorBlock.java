package org.nlogo.deltatick;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.nlogo.deltatick.dnd.JCharNumberFieldFilter;
import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

// strictfp: When applied to a class, all calculations inside the class use strict floating-point math.-a.

public strictfp class BehaviorBlock
        extends CodeBlock {

    boolean isTrait;
    boolean isMutate;
    TraitBlockNew tBlockNew = null; // TODO need this to have trait Block work as an input in code (March, 25, 2013)
    //CodeBlock container = null;
    BreedBlock myBreedBlock = null;
    private JToolTip toolTip;

    // Set of acceptable traits
    Set<String> applicableTraits;// = new HashSet<String>();
    JPanel traitblockLabelPanel = null;


    public BehaviorBlock(String name, String aTraits) {
        super(name, ColorSchemer.getColor(0).brighter());
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                CodeBlock.behaviorBlockFlavor,
                CodeBlock.codeBlockFlavor,
        };

        if (!aTraits.isEmpty()) {
            applicableTraits = new HashSet<String>(Arrays.asList(aTraits.split(",")));
        }
    }


    //codeBlockFlavor in Condition Block, Beh Block is what makes it a valid block for Breed   -A.

    @Override
    public void processCodePlaceholders() {
        // Replace all %breed% with breed name
        if (myBreedBlock != null) {
            processedCode = code.replaceAll(Matcher.quoteReplacement("%breed%"), myBreedBlock.plural());
        }
        else {
            processedCode = code.replaceAll(Matcher.quoteReplacement("%breed%"), "");
        }
    }

    public String unPackAsCode() {
        if (myParent == null) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }

    //TODO: Box bracket appears when it need not (March 9)
    //extracting the argument to be passed after name of procedure (March 2)
    public String unPackAsProcedure() {

        processCodePlaceholders();

        String passBack = "";
        passBack += "to " + getName() + " ";

        if ((agentInputs.size() > 0) || (inputs.size() > 0) || (behaviorInputs.size() > 0 || percentInputs.size() > 0) || (isTrait == true) ) {
            passBack += "[ ";
        }

        // May 27, 2013 hardcoded fix for reproduce block
        // Must be cleaned up later
        if (isMutate) {
            passBack += "breedname ";
        }

        if (inputs.size() > 0) {
            for (String input : inputs.keySet()) {
                passBack += input + " ";
            }
        }
        if (agentInputs.size() > 0) {
            for (String s : agentInputs.keySet()) {
                passBack += s + " ";
            }
        }
        if (behaviorInputs.size() > 0) {
            for (String s : behaviorInputs.keySet()) {
                passBack += s + " ";
            }
        }
        if (percentInputs.size() > 0) {
            for (String s : percentInputs.keySet()) {
                passBack += s + " ";
            }
        }
        //when traitname appears as an input, it clashes with the trait defined as a breed-variable so using behaviorinput
        //instead (March 29, 2013)
//          if (isTrait == true) {
//              passBack += tBlockNew.getTraitName() + " ";
//          }
        if ((agentInputs.size()  > 0) || (inputs.size() > 0) || (behaviorInputs.size() > 0 || (percentInputs.size() > 0)) || (isTrait == true) ) {
            passBack += " ]";
        }

        if ( ifCode != null ) {
            passBack += "\n" + ifCode + "[\n" + processedCode + "\n" + "]";
        }
// May 15, 2013. The following original code is commented out to try the hacky way of implementing the 'reproduce' behavior
// The commented code reads the behavior from XML
// Eventually, the hacky code below must somehow be integrated in the XML
//          else {
//              passBack += "\n" + code + "\n";
//              if (isMutate == true) {
//                  if (myBreedBlock != null) {
//                      if (myBreedBlock.getMyTraitBlocks().size() > 0) {
//                          passBack += "mutate";
//                      }
//                      passBack += "\n\t\t]"; //corresponds to hatch
//                      passBack += "\n\t]\n"; // corresponds to if-chance block
//
//                  }
//              }
//          }

        // Checking carryingCapacitySlider
        if (isMutate) {
            // This is a reproduce behavior block
            //String carryingCapacitySliderName = myBreedBlock.plural() + "-carrying-capacity";
            passBack += "\n";
            passBack += "\tif count breedname  < runresult (word \"max-number-of-\" breedname) [\n";
            //passBack += "\tif count " + myBreedBlock.plural() + " < " + carryingCapacitySliderName +" [\n";
// Commented May 27, 2013 for OOJH Activity1
//            passBack += "\t\thatch 1 [ " + "set age 0 fd 1 rt random-float 360\n";
//            // mutate here
//            if (myBreedBlock.getMyTraitBlocks().size() > 0) {
//                passBack += "\t\tmutate\n";
//            }
//            // Closing brackets for code

            passBack += processedCode + "\n";
            passBack += "\t\t] ]";
            passBack += "\t]\n";
        }
        else {
            // Regular behavior block
            passBack += "\n" + processedCode + "\n";

        }
        // End - Hacky code


        if (energyInputs.size() > 0) {
            for (JTextField inputName : energyInputs.values()) {
                passBack += "set energy energy " + inputName.getText() + "\n";
            }
        }


        passBack += "end\n\n";

        return passBack;
    }

    // extracting name of behavior into "to go" -A. (sept 24)
    public String unPackAsCommand() {
        String passBack = "";

        passBack += " " + getName() + " ";
        // May 27, 2013 Hardcoded fix for reproduce block
        if (isMutate) {
            passBack += getMyBreedBlock().plural() + " ";
        }
        // Fix done
        for (JTextField input : inputs.values()) {
            passBack += input.getText() + " ";
        }

        for (JTextField agentInput : agentInputs.values()) {
            passBack += agentInput.getText() + " ";
        }
        if (isTrait) {
            passBack += tBlockNew.getTraitName();
        }
        else {
            for (JTextField behaviorInput : behaviorInputs.values()) {
                passBack += behaviorInput.getText() + " ";
            }
        }
        for (JTextField percentInput : percentInputs.values()) {
            passBack += percentInput.getText() + " ";
        }

        passBack += "\n";

        return passBack;
    }

    public void die() {
        super.die();
        if (isMutate) {
            isMutate = false;
            myBreedBlock.setReproduceUsed(false);
        }
    }

    public void updateBehaviorInput() {
        Container parent = getParent();
        if (parent instanceof TraitBlock) {
        	JCharNumberFieldFilter textFilter = new JCharNumberFieldFilter();
            HashMap<String, Variation> hashMap = ((TraitBlock) parent).variationHashMap;
            String selectedVariationName = ((TraitBlock) parent).getDropdownList().getSelectedItem().toString();
            String trait = ((TraitBlock) parent).getName();
            Variation tmp = hashMap.get(selectedVariationName);

            String value = tmp.value;
            for ( String s : behaviorInputs.keySet()) {
                if (s.equals(trait)) {
                	textFilter.setMaxChars(10);
                    JTextField textField = behaviorInputs.get(s);
                    textField.setDocument(textFilter);
                    textField.setText(value);
                }
            }
        }
    }

    //remove behaviorInput from block if a TraitBlock has been added -(March 25, 2013)
    //assumption that there's only one behaviorInput per block
    public void removeBehaviorInput() {
        for ( Map.Entry<String, PrettyInput> map : behaviorInputs.entrySet()) {
            String s = map.getKey();
            PrettyInput j = map.getValue();
            //remove(j); // TODO: prefer that it is removed entirely because it can't be used alone again (March 25, 2013)
            j.setVisible(false);
            revalidate();
            repaint();
            //behaviorInputs.remove(s);  // need the behavior input to generate code esp when trait blocks are used (March 29,2013)
        }
    }

    // will work only for one behaviorInput per block -A. (Aug 10, 2012)
    public String getBehaviorInputName() {
        String behaviorInputName = new String();
        for ( String s : behaviorInputs.keySet()) {
            behaviorInputName = s;
            }
        return behaviorInputName;
    }

    public String getAgentInputName() {
        String agentInputName = new String();
        for ( String s : agentInputs.keySet()) {
            agentInputName = s;
            }
        return agentInputName;
    }

    public String getPercentInputName() {
        String percentInputName = new String();
        for ( String s : percentInputs.keySet()) {
            percentInputName = s;
            }
        return percentInputName;
    }

    public boolean getIsTrait() {
        return isTrait;
    }

//    public void setIsTrait(boolean value) {
//        isTrait = value;
//    }

    public void setTrait(TraitBlockNew traitBlockNew) {
        tBlockNew = traitBlockNew;
        isTrait = true;
    }

    public String getTrait() {
        String retVal = "";
        if (isTrait) {
            retVal = new String(tBlockNew.getTraitName());
        }
        return retVal;
    }

    public void addTraitblockPanel() {
        // Set up the panel
        traitblockLabelPanel = new JPanel();
        traitblockLabelPanel.setPreferredSize(new Dimension(this.getWidth(), 40));
        traitblockLabelPanel.setBackground(getBackground());
        traitblockLabelPanel.setAlignmentX(CENTER_ALIGNMENT);
        traitblockLabelPanel.setAlignmentY(CENTER_ALIGNMENT);
        traitblockLabelPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        // Set up the label
        JLabel traitblockLabel = new JLabel();
        traitblockLabel.setText("Add yellow block here");
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

    public void removeTraitblockPanel() {
        if (traitblockLabelPanel != null) {
            remove(traitblockLabelPanel);
        }
        validate();
    }

    // check if I'm a reproduce block -(March 25, 2013)
    public void setIsMutate(boolean value) {
        isMutate = value;
    }

    public boolean getIsMutate() {
        return isMutate;
    }

    //container to access the BreedBlock in which I'm dropped - March 26, 2013
    public void setMyBreedBlock(BreedBlock breedBlock) {
        myBreedBlock = breedBlock;
    }

    public BreedBlock getMyBreedBlock() {
        return myBreedBlock;
    }

    public JPanel getLabel() {
        return label;
    }

    public Set<String> getApplicableTraits() {
        return applicableTraits;
    }

    public boolean isTraitApplicable(String traitName) {
        return applicableTraits.contains(traitName);
    }

    public JToolTip createToolTip() {
        toolTip = super.createToolTip();
        toolTip.setBackground(Color.white);
        toolTip.setForeground(Color.black);
        return toolTip;
    }


}
