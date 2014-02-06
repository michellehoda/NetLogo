package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.Variation;

//import javax.swing;
//import java.awt.*;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/16/13
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitBlockNew
    extends CodeBlock
{
    //JTextField textName;
    ArrayList<String> varList;
    String breedNameTMP = new String(); // This may not be valid if the user changes the text field in breed block
    BreedBlock myBreedBlock = null;
    String traitName;
    String traitOffsetVarName;
    int traitOffsetVarValue;
    String varColor;
    JLabel breedName = new JLabel();
    String visualizeCode;
    String visualizeProcedure;
    String visualizeGoCode;
    String visualizeGoProcedure;

    //transient Trait trait;
    transient TraitState traitState;
    //transient Frame parentFrame;

    HashMap<String, String> varPercentage;
    HashMap<String, Integer> varNum = new HashMap<String, Integer>();
    HashMap<String, Variation> variationHashMap = new HashMap<String, Variation>();

    public TraitBlockNew (BreedBlock breedBlock, TraitState traitState, HashMap<String, Variation> variationHashMap, HashMap<String, String> variationValues ) {
        super (traitState.getNameTrait(), ColorSchemer.getColor(4));
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                CodeBlock.traitBlockFlavor,
                CodeBlock.codeBlockFlavor};

        this.traitState = new TraitState(traitState);
        this.traitName = this.traitState.getNameTrait();
        this.traitOffsetVarName = this.traitState.getOffsetName();
        this.traitOffsetVarValue = this.traitState.getOffsetValue();
        this.variationHashMap.clear();
        this.variationHashMap.putAll(variationHashMap);
        this.varColor = traitState.getColor();
        this.visualizeCode = traitState.getVisualizeCode();
        this.visualizeProcedure = traitState.getVisualizeProcedure();
        this.visualizeGoCode = traitState.getVisualizeGoCode();
        this.visualizeGoProcedure = traitState.getVisualizeGoProcedure();

        myBreedBlock = breedBlock;

        java.util.List<Component> componentList = new ArrayList<Component>();
        breedName.setText(" of " + breedBlock.plural());
        java.awt.Font font = breedName.getFont();
        breedName.setFont(new java.awt.Font("Arial", font.getStyle(), 12));

        componentList.add(breedName);

        //int y = 0;
        for (Component c : componentList) {
          label.add(c);
          //y += c.getPreferredSize().getHeight();
        }
        //label.setPreferredSize(new Dimension(100, y + 11));
        this.setPreferredSize(getPreferredSize());
        this.setMaximumSize(getPreferredSize());
        this.revalidate();
    }

    public String getTraitName() {
        return traitName;
    }

    public String getTraitOffsetVarName() { return traitOffsetVarName; }

    public int getTraitOffsetVarValue() { return traitOffsetVarValue; }

    public String unPackAsCode() {
        if (myParent == null) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }

    public void numberAgents() {
        int i = 0;
        int accumulatedTotal = 0;
        int totalAgents = 0;
        int numberOfVariation;
        String tmp;

        tmp = ((BreedBlock) myParent).number.getText().toString();
        totalAgents = Integer.parseInt(tmp);

            for (Map.Entry<String, String> entry : varPercentage.entrySet()) {
                String variationType = entry.getKey();
                String numberType = entry.getValue();

                int k = Integer.parseInt(entry.getValue());

                if (i == (varPercentage.size() - 1)) {
                    numberOfVariation = (totalAgents - accumulatedTotal);
                }
                else {
                    numberOfVariation = (int) ( ( (float) k/100.0) * (float) totalAgents);
                }
                varNum.put(variationType, numberOfVariation);
                accumulatedTotal += numberOfVariation;
                i++;
            }
    }

    public HashMap<String, Integer> getVarNum() {
        return varNum;
    }


    public BreedBlock getMyParent() {
        return ((BreedBlock) myParent);
    }


    public String unPackAsCommand() {
        String passBack = "";

        return passBack;
    }

    public String unPackAsProcedure() {
        String passBack = "";
        return passBack;
    }

    public ArrayList<String> getVariations() {
        return varList;
    }

    public HashMap<String, Variation> getVariationHashMap() {
        return variationHashMap;
    }

    public String getBreedName() {
        // 20130807 Not sure why the line below was commented
        // return ((BreedBlock) myParent).plural(); // Commented May 28, 2013 OOJH
        return breedNameTMP; // Commented 20130807
        // return myBreedBlock.plural();
    }

    // This method is irrelevant if myBreedBlock is used to get breed name
    public void setBreedName(String breedName) {
        breedNameTMP = new String(breedName);
        this.breedName.setText(" of " + breedName);
        validate();
    }

    public int getPreferredWidth() {
        if (getParent() instanceof BuildPanel) {
            return DEFAULT_CODEBLOCK_WIDTH;
        }
        else if (getParent() instanceof BehaviorBlock) {
            //System.out.println("TraitBlockNew.getPreferredWidth()");
            return 50;
        }
        return DEFAULT_CODEBLOCK_WIDTH;
    }

    public java.awt.Dimension getMinimumSize() {
        return new java.awt.Dimension(getPreferredWidth(), 20);
    }
    public void hideRemoveButton() {
        removeButton.setVisible(false);
        revalidate();
    }

    private void removeBreedLabel() {
        label.remove(breedName);
    }

    private void setSmallSize() {
        //this.setSize(50, 20);
        this.setPreferredSize(new Dimension(50, 20));
        //label.setBorder(BorderFactory.createLineBorder(Color.black));
        //this.label.setMinimumSize(new Dimension(30, 20));
        //this.label.setMaximumSize(new Dimension(30,20));
        revalidate();
        repaint();
    }



    public String getMutateCode() {
        return traitState.getMutateCode();
    }

    public String getVarColor() {
        return varColor;
    }

    public String getVisualizeCode() {
        return visualizeCode;
    }

    public String getVisualizeProcedure() {
        String passBack = "";
        passBack += "to " + visualizeCode + "\n";
        passBack += visualizeProcedure;
        passBack += "\nend\n";
        return passBack;
    }

    public String getVisualizeGoCode() {
        return visualizeGoCode;
    }

    public String getVisualizeGoProcedure() {
        String passBack = "";
        passBack += "to " + visualizeGoCode + "\n";
        passBack += visualizeGoProcedure;
        passBack += "\nend\n";
        return passBack;
    }



}
