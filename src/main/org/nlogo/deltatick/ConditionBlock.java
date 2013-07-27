package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.window.Widget;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;

public strictfp class ConditionBlock
        extends CodeBlock {

    JPanel rectPanel = new JPanel();
    Boolean removedRectPanel = false;
    public boolean addedRectPanel = false; //!< If true, rectPanel will appear/disappear as block is moved over breedblock

    public ConditionBlock(String name) {
        super(name, ColorSchemer.getColor(1));
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                CodeBlock.conditionBlockFlavor,
                CodeBlock.codeBlockFlavor,
                CodeBlock.patchBlockFlavor,
                CodeBlock.envtBlockFlavor
        };
    }
    //codeBlockFlavor is what makes Condition Blocks valid for Breed Block -a. (Sept 6)

    public void setMyParent(CodeBlock block) {
        myParent = block;
    }

    // dragged from the library, or used to write full procedures.
    public String unPackAsCode() {
        if (myParent != null) {
            return unPackAsCommand();
        }
        return unPackAsProcedure();
    }


    // this shows up under "to go" (Feb 15, 2012)
    public String unPackAsCommand() {
        String passBack = "";

        passBack += "if " + getName() + " ";
        for (PrettyInput input : inputs.values()) {
            passBack += input.getText() + " ";
        }
        for (PrettyInput agentInput : agentInputs.values()) {
            passBack += agentInput.getText() + " ";
        }
        passBack += "[\n";
        for (CodeBlock block : myBlocks) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";

        return passBack;
    }

    ;

    //this shows up as a separate procedure (Feb 15, 2012)
    public String unPackAsProcedure() {
        String passBack = "";

        passBack += "to-report " + getName() + " ";

        if (inputs.size() > 0 || agentInputs.size() > 0) {
            passBack += "[ ";

            for (String input : inputs.keySet()) {
                passBack += input + " ";
            }
            for (String agentInput : agentInputs.keySet()) {
                passBack += agentInput + " ";
            }
            passBack += "]";
        }

        passBack += "\n";

        passBack += code + "\n";
        //passBack += "    [ report true ]\n";
        //passBack += "  report false\n";

        passBack += "end\n\n";

        return passBack;
    }

    public void addBlock(CodeBlock block) {
        //super.addBlock(block);

        myBlocks.add(block);
        this.add(block);
        block.enableInputs();

        block.showRemoveButton();
        this.add(Box.createRigidArea(new Dimension(this.getWidth(), 4)));
        if (removedRectPanel == false) {     //checking if rectPanel needs to be removed
            remove(rectPanel);
            removedRectPanel = true;
        }
        block.setMyParent(this);
        block.doLayout();

        block.validate();
        block.repaint();
        if (block instanceof BehaviorBlock) {
            ((BehaviorBlock) block).updateBehaviorInput();
        }
        if (block instanceof BehaviorBlock) {
            String tmp = ((BehaviorBlock) block).getBehaviorInputName();
            addBehaviorInputToList(tmp);
        }
        else if (block instanceof ConditionBlock) {
            String tmp = ((ConditionBlock) block).getBehaviorInputName();
            addBehaviorInputToList(tmp);
            String s = ((ConditionBlock) block).getAgentInputName();
            addAgentInputToList(s);
            ((ConditionBlock) block).addRect();

        }

        doLayout();
        validate();
        repaint();

        this.getParent().doLayout();
        this.getParent().validate();
        this.getParent().repaint();
    }

    // this code is the label you see on the condition block
    public void makeLabel() {
        JLabel name = new JLabel("if " + getName());
        java.awt.Font font = name.getFont();
        name.setFont(new java.awt.Font("Arial", font.getStyle(), 12));
        ////label.add(removeButton);
        ////removeButton.setVisible(false);
        label.setBackground(getBackground());
        label.add(name);
    }

    public void addRect() {
        rectPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        rectPanel.setPreferredSize(new Dimension(this.getWidth(), 40));
        rectPanel.setBackground(getBackground());
        JLabel label = new JLabel();
        label.setText("Add blocks here");
        rectPanel.add(label);
        add(rectPanel);
        validate();
    }
    public void showRectPanel() {
        if (removedRectPanel) {
            //rectPanel.setVisible(true);
            this.add(rectPanel);
            this.validate();
            this.repaint();
            this.getMyBreedBlock().validate();
        }
    }
    public void hideRectPanel() {
        if (removedRectPanel) {
            //rectPanel.setVisible(false);
            this.remove(rectPanel);
            this.validate();
            this.repaint();
            this.getMyBreedBlock().validate();
        }
    }
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

    public java.util.List<CodeBlock> getMyBlocks() {
        return myBlocks;
    }
}
