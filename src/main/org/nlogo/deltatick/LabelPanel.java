package org.nlogo.deltatick;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/7/13
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class LabelPanel extends JPanel {
    //JLabel label;
    //JCheckBox checkBox;
    ArrayList<String> ownVarNames = new ArrayList<String>();
    ArrayList<String> traitNames = new ArrayList<String>();
    HashMap<String, JCheckBox> allCheckBoxes = new HashMap<String, JCheckBox>();

    public static final int LABELPANEL_WIDTH = 350;
    public static final int LABELPANEL_HEIGHT = 100;


    public LabelPanel(ArrayList<String> ownVarNames) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //this.setPreferredSize(new Dimension(LABELPANEL_WIDTH, LABELPANEL_HEIGHT));
        this.ownVarNames.addAll(ownVarNames);
        for (String name : ownVarNames) {
            traitNames.add(name);
        }
//        this.traitNames.add("age");
//        this.traitNames.add("energy");
        initiComponents(traitNames);
        this.revalidate();
    }

    public void initiComponents(ArrayList<String> list) {
        for (String nam : list) {
            JCheckBox box = new JCheckBox(nam);
            allCheckBoxes.put(nam, box);
            this.add(box);
        }
        this.revalidate();
    }

//    public void addTraitCheckBox(String trait) {
//        JCheckBox box = new JCheckBox(trait);
//        this.add(box);
//        this.revalidate();
//    }

    public void updateData(Set<String> tNames) {

        for (JCheckBox box : allCheckBoxes.values()) {    //remove visually
            this.remove(box);
        }
        allCheckBoxes.clear();
        traitNames.clear();

        for (String name : ownVarNames) {
            traitNames.add(name);
        }
//            traitNames.add("age");
//            traitNames.add("energy");
        for (String str : tNames) {
            traitNames.add(str);
        }

        initiComponents(traitNames);


        this.revalidate();

    }

//    public HashMap<String, JCheckBox> getCheckBoxes() {
//        return allCheckBoxes;
//    }

    public ArrayList<String> getSelectedLabels() {
        ArrayList<String> selectedLabels = new ArrayList<String>();
        for (Map.Entry<String, JCheckBox> entry : allCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedLabels.add(entry.getKey());
            }
        }
        return selectedLabels;
    }

    // Sets the checkboxes corresponding to arraylist selectedLabels to true
    // Assumes the checkboxes have already been populated
    public void setSelectedLabels(ArrayList<String> selectedLabels) {
        for (String labelName : selectedLabels) {
            if (traitNames.contains(labelName)) {
                allCheckBoxes.get(labelName).setSelected(true);
            }
        }
    }


//    public void updateCheckBox(String traitName, HashMap<String, String> varPercent) {
//        // Check if trait has been added
//        if (traitNames.contains(traitName) == false) {
//
//            // Trait not previously present
//            // Create corresponding charts
//            JCheckBox box = new JCheckBox(traitName);
//            traitNames.add(traitName);
//
//
//            this.validate();
//
//        }
//
//        // Check is trait is to be removed
//        if (varPercent.size() == 0) {
//            // Remove corresponding panel
//            this.remove(chartsPanelMap.get(traitName));
//            this.validate();
//
//            chartsPanelMap.remove(traitName);
//        }
//
//    }

//    public void updateCheckBoxes (){
//        JCheckBox box = new JCheckBox()
//    }
}
