package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.VariationDropDown;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
    JTextField textName;
    ArrayList<String> varList;
    //LinkedList<Variation> variationList = new LinkedList<Variation>();
    String breedNameTMP = new String(); // This may not be valid if the user changes the text field in breed block
    BreedBlock myBreedBlock = null;
    String traitName;
    String varColor;
    JLabel name = new JLabel();

    transient Trait trait;
    transient TraitState traitState;
    transient Frame parentFrame;
    //Variation variation;
    //JList TraitsList;
    HashMap<String, String> varPercentage;
    //HashMap<String, String> traitNumVar = new HashMap<String, String>();
    HashMap<String, Integer> varNum = new HashMap<String, Integer>();

    HashMap<String, Variation> variationHashMap = new HashMap<String, Variation>();
    //HashMap<String, String> variationNamesValues = new HashMap<String, String>();

    public TraitBlockNew (BreedBlock breedBlock, TraitState traitState, HashMap<String, Variation> variationHashMap, HashMap<String, String> variationValues ) {
        super (traitState.getNameTrait(), ColorSchemer.getColor(4));
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                CodeBlock.traitBlockFlavor,
                CodeBlock.codeBlockFlavor};
        //this.breedName = breedBlock.plural();
        this.traitState = new TraitState(traitState);
        this.traitName = this.traitState.getNameTrait();
        //this.variationHashMap = variationHashMap;
        this.variationHashMap.clear();
        this.variationHashMap.putAll(variationHashMap);
        this.varColor = traitState.getColor();
        //this.variationNamesValues = variationValues;

        // Set my breed block
        myBreedBlock = breedBlock;

        java.util.List<Component> componentList = new ArrayList<Component>();
        name.setText(" of " + breedBlock.plural());
        componentList.add(name);

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

    public String getMyTraitName() {
        String passback = "";
        passback += traitName + "\n ";
        return passback;
    }

    public BreedBlock getMyParent() {
        return ((BreedBlock) myParent);
    }


    public String unPackAsCommand() {
        String passBack = "";
        //String value = variationNamesValues.get(variation);

        //passBack += "if " + this.getMyParent().plural() + "-" + this.getTraitName() + " = " + value + " [\n";
//        for (CodeBlock block : myBlocks) {
//            passBack += block.unPackAsCode();
//        }
        //passBack += "] \n";
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
        name.setText(" of " + breedName);
        validate();
    }

    public void lookBetter() {
        hideRemoveButton();
        removeBreedLabel();
        setSmallSize();
    }

    public void hideRemoveButton() {
        removeButton.setVisible(false);
    }

    private void removeBreedLabel() {
        label.remove(name);
    }

    private void setSmallSize() {
        this.setSize(73, 14);
    }



    public String getMutateCode() {
        return traitState.getMutateCode();
    }

    public String getVarColor() {
        return varColor;
    }

}
