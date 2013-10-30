package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.window.Widget;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;
import java.util.List;

public strictfp class ConditionBlock
        extends CodeBlock {

    JPanel rectPanel = new JPanel();
    Boolean removedRectPanel = false;
    public boolean addedRectPanel = false; //!< If true, rectPanel will appear/disappear as block is moved over breedblock
    Set<String> applicableTraits = new HashSet<String>();
    boolean isTrait; // When trait block is dropped in this condition block, isTrait is set
    String traitName;
    List<BehaviorBlock> myBehaviorBlocks = new ArrayList<BehaviorBlock>();
    List<ConditionBlock> myConditionBlocks = new ArrayList<ConditionBlock>();
    TraitBlockDisplayPanel traitBlockDisplayPanel;

    public ConditionBlock(String name, String aTraits) {
        super(name, ColorSchemer.getColor(1));
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                CodeBlock.conditionBlockFlavor,
                CodeBlock.codeBlockFlavor,
                CodeBlock.patchBlockFlavor,
                CodeBlock.envtBlockFlavor
        };
        if (!aTraits.isEmpty()) {
            applicableTraits = new HashSet<String>(Arrays.asList(aTraits.split(",")));
        }
        traitBlockDisplayPanel= new TraitBlockDisplayPanel("");
        traitBlockDisplayPanel.setVisible(false);
        label.add(traitBlockDisplayPanel);

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
        if (isTrait) {
            passBack += traitName;
        }
        else {
            for (PrettyInput behaviorInput : behaviorInputs.values()) {
                passBack += behaviorInput.getText() + " ";
            }
        }
//        for (CodeBlock codeBlock : myBlocks) {
//            if (codeBlock instanceof TraitBlockNew) {
//                passBack += ((TraitBlockNew) codeBlock).getTraitName();
//            }
//        }
        passBack += " [\n";
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
            for (String behaviorInput : behaviorInputs.keySet()) {
                passBack += behaviorInput + " ";
            }


//            for (CodeBlock cBlock : myBlocks) {
//                if (cBlock instanceof TraitBlockNew) {
//                    passBack += ((TraitBlockNew) cBlock).getTraitName();
//                }
//            }
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
        if (!(block instanceof TraitBlockNew)) {
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
        }
        if (block instanceof BehaviorBlock) {
            // Add to list of behavior blocks
            myBehaviorBlocks.add((BehaviorBlock)block);
            ((BehaviorBlock) block).updateBehaviorInput();
            String tmp = ((BehaviorBlock) block).getBehaviorInputName();
            addBehaviorInputToList(tmp);
        }
        else if (block instanceof ConditionBlock) {
            // Add to list of condition blocks
            myConditionBlocks.add((ConditionBlock)block);
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

    public void removeBlock(CodeBlock block) {
        super.removeBlock(block);
        if (block instanceof BehaviorBlock) {
            myBehaviorBlocks.remove(block);
        }
        else if (block instanceof ConditionBlock) {
            myConditionBlocks.remove(block);
        }
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

    public void setTrait(String traitName) {
        this.traitName = new String(traitName);
        isTrait = true;
        // Display the traitBlockDisplayPanel
        traitBlockDisplayPanel.setTraitName(traitName);
        traitBlockDisplayPanel.setPreferredSize(new Dimension(40, 30));
        traitBlockDisplayPanel.setVisible(true);
    }
    public boolean getIsTrait() {
        return isTrait;
    }

    public void removeBehaviorInput() {
        for ( Map.Entry<String, PrettyInput> map : behaviorInputs.entrySet()) {
            String s = map.getKey();
            PrettyInput j = map.getValue();
            remove(j);
            //remove(j); // TODO: prefer that it is removed entirely because it can't be used alone again (March 25, 2013)
            j.setVisible(false);
            revalidate();
            repaint();
            //behaviorInputs.remove(s);  // need the behavior input to generate code esp when trait blocks are used (March 29,2013)
        }

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

    public List<ConditionBlock> getMyConditionBlocks() {
        return myConditionBlocks;
    }
    public List<BehaviorBlock> getMyBehaviorBlocks() {
        return myBehaviorBlocks;
    }
    public List<ConditionBlock> getAllMyConditionBlocks() {
        List<ConditionBlock> allMyConditionBlocks = new ArrayList<ConditionBlock>();
        allMyConditionBlocks.addAll(myConditionBlocks);
        for (ConditionBlock conditionBlock : myConditionBlocks) {
            allMyConditionBlocks.addAll(conditionBlock.getAllMyConditionBlocks());
        }
        return allMyConditionBlocks;
    }
    public List<BehaviorBlock> getAllMyBehaviorBlocks() {
        List<BehaviorBlock> allMyBehaviorBlocks = new ArrayList<BehaviorBlock>();
        allMyBehaviorBlocks.addAll(myBehaviorBlocks);
        for (ConditionBlock conditionBlock : myConditionBlocks) {
            allMyBehaviorBlocks.addAll(conditionBlock.getAllMyBehaviorBlocks());
        }
        return allMyBehaviorBlocks;
    }

    public Set<String> getApplicableTraits() {
        return applicableTraits;
    }

    public boolean isTraitApplicable(String traitName) {
        return applicableTraits.contains(traitName);
    }
}
