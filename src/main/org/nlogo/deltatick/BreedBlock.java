package org.nlogo.deltatick;

import org.nlogo.api.Shape;
import org.nlogo.deltatick.dialogs.ShapeSelector;
import org.nlogo.deltatick.dnd.*;
import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.OwnVar;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;
import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.nlogo.shape.VectorShape;
import org.nlogo.shape.editor.ImportDialog;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import sun.jvm.hotspot.code.CodeBlob;

// BreedBlock contains code for how whatever happens in BreedBlock is converted into NetLogo code -A. (aug 25)

public strictfp class BreedBlock
        extends CodeBlock
        implements java.awt.event.ActionListener,
        ImportDialog.ShapeParser,
        MouseMotionListener,
        MouseListener {

    // "transient" means the variable's value need not persist when the object is stored  -a.
    String breedShape = "default";
    transient Breed breed;
    transient VectorShape shape = new VectorShape();
    transient Frame parentFrame;
    transient ShapeSelector selector;
    transient JButton breedShapeButton;
    public transient JButton inspectSpeciesButton;
    transient PrettyInput number;
    //transient PrettyInput plural;
    transient PrettierInput plural;
    // HashMap<String, Variation> breedVariationHashMap = new HashMap<String, Variation>(); // assuming single trait -A. (Aug 8, 2012)
    HashSet<String> myUsedBehaviorInputs = new HashSet<String>();
    List<String> myUsedAgentInputs = new ArrayList<String>();
    List<String>myUsedPercentInputs = new ArrayList<String>();

    // This list contains all defined trait(blocks) for this breed(block)
    List<TraitBlockNew>myTraitBlocks = new ArrayList<TraitBlockNew>(); // to have setupTrait code once trait is defined in SpeciesInspector (March 26, 2013)
    // This is a list of all behavior blocks for this breedblock
    List<BehaviorBlock> myBehaviorBlocks = new ArrayList<BehaviorBlock>();
    // This is a list of all condition blocks for this breedblock
    List<ConditionBlock> myConditionBlocks = new ArrayList<ConditionBlock>();

    String setupNumber;
    String maxNumber;
    String maxAge;
    String maxEnergy;
    String colorName = new String("gray");
    int colorRGB;
    //ShapeSelector myShapeSelector;
    int id;
    transient String trait;
    JTextField traitLabel; //Apparently has no function, never used
    transient String variation;
    HashSet<String> myUsedTraits = new HashSet<String>(); // Perhaps not necessary (May 18, 2013)
    boolean hasSpeciesInspector;
    ArrayList<String> traitLabels = new ArrayList<String>();

    //JPanel rectPanel;
    RectPanel rectPanel;
    boolean removedRectPanel = false;
    public boolean addedRectPanel = false; //!< If true, rectPanel will appear/disappear as block is moved over breedblock
    boolean reproduceUsed = false;
    boolean carryingCapacitySliderEnabled = false;
    int curIconIndex;
    Color curColor;
    JNumberFieldFilter numberDocument;
    JCharNumberNoSpaceFieldFilter pluralDocument;

    //dummy constructor - Aditi (Jan 27, 2013)
   public BreedBlock() {

   }
    // constructor for breedBlock without trait & variation
    public BreedBlock(Breed breed, String plural, Frame frame) {
        super(plural, ColorSchemer.getColor(3));
        this.parentFrame = frame;
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setLocation(0, 0);
        this.setForeground(color);
        this.breed = breed;
        this.maxAge = breed.getOwnVarMaxReporter("age");
        this.maxEnergy = breed.getOwnVarMaxReporter("energy");
        this.maxNumber = breed.getMaxQuant();
        number.setText(breed.getStartQuant());
        String word = number.getText();
        curIconIndex = 0;
        curColor = Color.GRAY;
        numberDocument = new JNumberFieldFilter();
        pluralDocument = new JCharNumberNoSpaceFieldFilter();
        //numberDocument.setMaxChars(5);
        //pluralDocument.setMaxChars(8);
        //number.setDocument(numberDocument);
        //this.plural.setDocument(pluralDocument);
        number.setText(breed.getStartQuant().toString());
        this.plural.setText(getName());

        //myShapeSelector = new ShapeSelector( parentFrame , allShapes() , this );
        setBorder(org.nlogo.swing.Utils.createWidgetBorder());

        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                codeBlockFlavor,
                breedBlockFlavor,
                //patchBlockFlavor
        };

        // Add listener to plural()
        this.plural.getDocument().addDocumentListener( new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                updateMyTraitBlocks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                updateMyTraitBlocks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                updateMyTraitBlocks();
            }

            public void updateMyTraitBlocks() {
                for (TraitBlockNew traitBlockNew : myTraitBlocks) {
                    traitBlockNew.setBreedName(plural());
                }
            }
        });
    }

    // This method should be called from SpeciesPanelOkayListener whenever a trait is added/removed/modified
    public void updateMyBehaviorBlocks() {
        List<CodeBlock> removeTheseBlocks = new ArrayList<CodeBlock>();
        List<BehaviorBlock> allMyBehaviorBlocks = new ArrayList<BehaviorBlock>();
        List<ConditionBlock> allMyConditionBlocks = new ArrayList<ConditionBlock>();

        // First add all the behavior blocks that are directly in this breed block
        allMyBehaviorBlocks.addAll(getMyBehaviorBlocks());
        // Then iterate over condition blocks and get their behavior blocks
        for (ConditionBlock conditionBlock : getMyConditionBlocks()) {
            allMyBehaviorBlocks.addAll(conditionBlock.getAllMyBehaviorBlocks());
        }

        // There may be traits applied to condition blocks
        // These blocks will need to be removed if that trait is removed
        allMyConditionBlocks.addAll(getMyConditionBlocks());
        for (ConditionBlock conditionBlock : getMyConditionBlocks()) {
            allMyConditionBlocks.addAll(conditionBlock.getAllMyConditionBlocks());
        }

        // After traits are defined *after* applicable behavior blocks are added
        // Then update the behavior block to accept the corresponding trait blocks
        for (BehaviorBlock behaviorBlock : allMyBehaviorBlocks) {
            for (TraitBlockNew traitBlock : getMyTraitBlocks()) {
                // Check if this behavior block already has a trait associated with it
                if (!behaviorBlock.getIsTrait() &&
                    behaviorBlock.isTraitApplicable(traitBlock.getTraitName()) &&
                    !behaviorBlock.getIsWaitingForTrait()) {
                    // This trait applies to this behavior block, but hasn't been applied to the block yet,
                    // and this block isn't already waiting for the trait (with traitpanel)
                    // Remove behavior input and add the traitpanel
                    behaviorBlock.removeBehaviorInput();
                    // Update 20140201
                    // behaviorBlock.addTraitblockPanel();
                    behaviorBlock.setTrait(traitBlock.getTraitName(), traitBlock.getTraitOffsetVarName());
                }
            }
            // If traits are removed but there are behavior blocks that depend of the (removed) traits,
            // these blocks must be removed. Add them to a remove list -- they cannot be removed in this loop.
            // Directly removing them in this loop results in concurrent modification exception
            for (String traitName : behaviorBlock.getApplicableTraits()) {
                if (!this.hasTrait(traitName) &&
                    (behaviorBlock.getIsTrait() ||
                     behaviorBlock.getIsWaitingForTrait())) {
                    removeTheseBlocks.add(behaviorBlock);
                }
            }
        }
        // Check if condition blocks need to be removed
        for (ConditionBlock conditionBlock : allMyConditionBlocks) {
            // If traits are removed but there are behavior blocks that depend of the (removed) traits,
            // these blocks must be removed. Add them to a remove list -- they cannot be removed in this loop.
            // Directly removing them in this loop results in concurrent modification exception
            for (String traitName : conditionBlock.getApplicableTraits()) {
                if (!this.hasTrait(traitName) &&
                        (conditionBlock.getIsTrait())) { // ||
                                //conditionBlock.getIsWaitingForTrait())) {
                    removeTheseBlocks.add(conditionBlock);
                }
            }

        }
        // Remove any blocks that need to be removed
        for (CodeBlock codeBlock : removeTheseBlocks) {
            if (codeBlock != null)
                codeBlock.die();
        }
        validate();
        doLayout();
        repaint();
    }

    public void addBlock(CodeBlock block) {
        if (block instanceof TraitBlockNew) {
            // This is called when a traitblock is dropped in a behavior block
            addTraitBlock(block);
        }
        else {
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
//        if (block instanceof TraitBlock) {
//            addTraitBlock(block);
//        }
            if (block instanceof BehaviorBlock) {
                // Add to list of behavior blocks
                myBehaviorBlocks.add((BehaviorBlock)block);
                String tmp = ((BehaviorBlock) block).getBehaviorInputName();
                addBehaviorInputToList(tmp);
                String s = ((BehaviorBlock) block).getAgentInputName();
                addAgentInputToList(s);
                String p = ((BehaviorBlock) block).getPercentInputName();
                addPercentInputToList(p);
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

    public void addTraitBlock(CodeBlock block) {
        if (((TraitBlockNew) block).getBreedName().equalsIgnoreCase(this.plural()) == false) {// if traitBlock is put in a breedBlock that it's not defined for

            String message = new String(((TraitBlockNew) block).getTraitName() + " is not a trait of " + this.getName());
            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            myUsedTraits.add(((TraitBlockNew) block).getTraitName());
            myBlocks.add(block);
        }
    }

    //TODO: Figure out how breed declaration always shows up first in code
    public String declareBreed() {
        return "breed [ " + plural() + " " + singular() + " ]\n";
    }

    public String declareBreedXML() {
        return "<breedBlock singular=\"" + singular() + "\" plural=\"" + plural() + "\" number=\"" + number.getText() + "\"></breedBlock>\n";
    }

    //this is where breeds-own variables show up in NetLogo code -A. (aug 25)
    public String breedVars() {
        String code = "";
        if (breed.getOwnVars().size() > 0) {
            code += plural() + "-own [\n";
            for (OwnVar var : breed.getOwnVars()) {
                code += "  " + var.name + "\n";
            }
            code += "\n";
        }
        return code;
    }

    // code to setup in NetLogo code window. This method is called in MBgInfo -A.
    public String setup() {
        String code = "";
        if (breed.needsSetupBlock()) {
            code += "create-" + plural() + " " + getSetupNumber() + " [\n";
            if (breed.getSetupCommands() != null) {
                code += breed.getSetupCommands();
            }
            for (OwnVar var : breed.getOwnVars()) {
                if (var.setupReporter != null) {
                    code += "set " + var.name + " " + var.setupReporter + "\n";
                }
            }
            code += "set color " + getColorName() + '\n';

            code += setupTrait();
            code += "]\n";
            //code += setupTraitLabels();
            code += setupTraitVisualization();
            code += setupBreedShape();
            //code += "ask patches [set pcolor white]\n";
            int i;
        }
        return code;
    }

    public String setupTrait() {
        String code = "";
        ArrayList<String> setTraits = new ArrayList<String>();  // to make sure setupTrait is called only once

        for (TraitBlockNew block : myTraitBlocks) {
            if (block instanceof TraitBlockNew) {
                String traitName =  block.getTraitName();
                if (setTraits.contains(traitName) != true) {
                    code += "let all-" + plural() + "-" + traitName + " shuffle sort " + plural() + " \n";
                    setTraits.add(traitName);

                    int i = 0;
                    int startValue = 0;
                    int endValue = 0;

                    Iterator it = block.variationHashMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry) it.next();
                        Variation variation = (Variation) entry.getValue();

                        int k =  (int) Math.round(((variation.percent/100.0)) * Double.parseDouble(number.getText()));

                        if (it.hasNext()) {
                            endValue = startValue + k;
                        }
                        else {
                            endValue = (Integer.parseInt(number.getText()));
                        }

                        code += "let " + traitName + i + " sublist all-" + plural() + "-" + traitName +
                                " " + startValue + " " + endValue + "\n";

                        code += "foreach " + traitName + i + " [ ask ? [ set " + traitName + " " + variation.value + " \n";
                        code += "set color " + colorName + variation.color;
                        if (traitName.equalsIgnoreCase("body-size")) {
                            code += "set size body-size\n";
                        }
                        code += " ]] \n";

                        i++;

                        startValue = endValue;
                    }


                }
            }// if
        }
        return code;
    }

    public String setupTraitLabels() {
        String code = "";
        if (numTraits() > 0) {
            code += "\task " + plural() + " [";
            code += "if (" + plural() + "-label = \"none\")\n\t" +
                    "[set label \"\" ]  \n";
            code += setupTraitVisualization() + " ]\n";
        }

        return code;
    }

    public String setupTraitVisualization() {
        String code = "";
        if (numTraits() > 0) {
            code += "\task " + plural() + "[ ";
            for (TraitBlockNew tBlock : myTraitBlocks) {
                code += "if " + tBlock.getBreedName() + "-label = \"" +
                        tBlock.getName() + " visual\" [" ;
                code += tBlock.getVisualizeCode() + " " + tBlock.getVisualizeGoCode() + " ]\n";
                code += "if " + tBlock.getBreedName() + "-label = \"" +
                        tBlock.getName() + "\" [ " + tBlock.getVisualizeCode() + " set label runresult " + plural() + "-label set label-color black]\n";
                code += "if (" + plural() + "-label = \"none\")\n\t" +
                    "[set label \"\" ]  \n";
                code += " \n";
            }
            code += "\n]";
        }

        return code;
    }

    // UNUSED
    private String setTraitLabelCode() {
        String code = "";
        // Generate code if there is atleast one label
        if (traitLabels.size() > 0) {
            code += "set label (word ";
            code += traitLabels.get(0);
            for (int i = 1; i < traitLabels.size(); i++) {
                code += " \"-\" ";
                code += traitLabels.get(i);
            }
            code += ")\n";
        }
        return code;
    }

    // moves Update Code from XML file to procedures tab - A. (feb 14., 2012)
    public String update() {
        String code = "";
        if (breed.needsUpdateBlock()) {
            // This enables changing labels even when the model is running
            // code += setupTraitVisualization();
            code += "ask " + plural() + " [\n";
            if (breed.getUpdateCommands() != null) {
                code += breed.getUpdateCommands();
            }
            for (OwnVar var : breed.getOwnVars()) {
                if (var.updateReporter != null) {
                    code += "set " + var.name + " " + var.updateReporter + "\n";
                }
            }
            for (TraitBlockNew tBlock : myTraitBlocks) {    // setting size of turtles if "body-size" is a trait (April 11, 2013)
                if (tBlock.getTraitName().equalsIgnoreCase("body-size")) {
                    code += "set size body-size\n";
                }
            }
            code += "\n";

            // Update labels
            for (TraitBlockNew tBlock : myTraitBlocks) {
                code += "\task " + plural() + "[ ";
                code += "if " + tBlock.getBreedName() + "-label = \"" +
                        tBlock.getName() + " visual\" [" ;
                code += "set label \"\" " + tBlock.getVisualizeGoCode() + "]\n";
                code += "if " + tBlock.getBreedName() + "-label = \"" +
                        tBlock.getName() + "\" [ if any? my-out-links [ask my-out-links [set hidden? true]] set label runresult " + plural() + "-label set label-color black]\n";
                code += "if (" + plural() + "-label = \"none\")\n\t" +
                    "[set label \"\" ]  \n";
                code += " \n";
                code += "\n]";
            }

                //code += tBlock.getVisualizeGoCode();   // revert to this if above update visualize code doesn't wrk
            //}

//           // Need this code so labels update for offspring if their trait mutates from their parents' (May 8, 2013)
//            if (traitLabels.size() >= 1) {
//                code += "set label (word ";
//                for (int i = 0; i < traitLabels.size(); i++) {
//                    code += traitLabels.get(i);
//                    if (traitLabels.size() > 1) {
////               if (i++ == traitLabels.lastIndexOf(traitLabels.get(i)) == false) { // if this is not the last item
//                        code += "\"-\"";
//                        i--;
//                    }
//                }
//                code += " ) \n";
//            }

            code += "]\n";
        }
        return code;
    }

    // very smart! singular is just prefixed plural -A.
    public String singular() {
        return "one-of-" + plural.getText();
    }


    // get text students have entered in prettyInput, plural (march 1)
    public String plural() {
        return plural.getText();
    }

    public void setPlural(String name) {
        this.plural.setText(name);
        this.rectPanel.setBreedName(name);
    }

    public void setNumber(String number) {
        this.number.setText(number);
    }

    public void setMaxAge(String age) {
        maxAge = age;
    }
    public String getMaxAge() {
        return maxAge;
    }

    public void setMaxEnergy(String energy) {
        maxEnergy = energy;
    }
    public String getMaxEnergy() {
        return maxEnergy;
    }
    public void setMaxNumber(String number) {
        maxNumber = number;
    }
    public String getMaxNumber() {
        return maxNumber;
    }
    public void setSetupNumber(String number) {
        setupNumber = number;
        this.number.setText(setupNumber);
    }
    public String getSetupNumber() {
        //return number.getText();
        return setupNumber;
    }

    public void setColorName(String color) {
        colorName = color;
    }
    public int getColorRGB() {
        return this.colorRGB;
    }
    public void setColorRGB(int colorRGB) {
        this.colorRGB = colorRGB;
    }
    public void addToTraitLabels(String trait) {
        traitLabels.add(trait);
    }

    public ArrayList<String> getTraitLabels() {
        return traitLabels;
    }
    public void setTraitLabels(ArrayList<String> selectedLabels) {
        traitLabels.clear();
        traitLabels.addAll(selectedLabels);
    }

    public ArrayList<String> getOwnVarNames() {
        return breed.getOwnVarsName();
    }

    public Object getTransferData(DataFlavor dataFlavor)
            throws UnsupportedFlavorException {
        if (isDataFlavorSupported(dataFlavor)) {
            if (dataFlavor.equals(breedBlockFlavor)) {
                return this;
            }
            if (dataFlavor.equals(envtBlockFlavor)) {
                return this;
            }
            if (dataFlavor.equals(DataFlavor.stringFlavor)) {
                return unPackAsCode();
            }
            if (dataFlavor.equals(traitBlockFlavor)) {
                return unPackAsCode();
            }

        } else {
            return "Flavor Not Supported";
        }
        return null;
    }


    public String unPackAsCode() {
        String passBack = "";

        passBack += "ask " + plural() + " [\n";
        for (CodeBlock block : myBlocks) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";

        return passBack;
    }

    public void makeLabel() {
        JPanel label = new JPanel();
        // TODO: This is a hard coded hack for now. Fix it.
//        label.add(removeButton);
        this.showRemoveButton();

        label.add(new JLabel("Ask"));

        number = new PrettyInput(this); // number of turtles of a breed starting with -a.
        number.setText("100");
        // Setup number cannot be edited. Must be updated from Species Editor Panel
        number.setEditable(false);
        number.setBorder(new EmptyBorder(0, 0, 0, 0));
        number.setFont(new java.awt.Font("Arial", 0, 11));
        label.add(number);

        //plural = new PrettyInput(this);
        plural = new PrettierInput(this);
        plural.setText(getName());
        plural.setEditable(false);
        plural.setBorder(new EmptyBorder(0, 0, 0, 0));
        plural.setFont(new java.awt.Font("Arial", 0, 11));

        label.add(plural);

        //label.add(makeBreedShapeButton());
        inspectSpeciesButton = new InspectSpeciesButton(this);
        label.add(inspectSpeciesButton);

        // makeRect();
        rectPanel = new RectPanel(getName(), getBackground());

        add(label);
        add(rectPanel);

        label.setBackground(getBackground());
        //label.setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        label.validate();
    }

//    public void makeRect() {
//        rectPanel = new JPanel();
//        rectPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
//        rectPanel.setPreferredSize(new Dimension(this.getWidth(), 40));
//        rectPanel.setBackground(getBackground());
//        JLabel label = new JLabel();
//        label.setBackground(getBackground());
//        label.setText("What do you want " + plural() + " to do?");
//        rectPanel.add(label);
//        rectPanel.validate();
//    }

    public void showRectPanel() {
        if (removedRectPanel) {
        //rectPanel.setVisible(true);
        this.add(rectPanel);
        this.validate();
        this.repaint();
        }
    }

    public void hideRectPanel() {
        if (removedRectPanel) {
        //rectPanel.setVisible(false);
        this.remove(rectPanel);
        this.validate();
        this.repaint();
        }
    }

    public void setRemovedRectPanel(boolean flag) {
        removedRectPanel = flag;
    }

    public String[] getTraitTypes() {
            String[] traitTypes = new String[breed.getTraitsArrayList().size()];
            int i = 0;
            for (Trait trait : breed.getTraitsArrayList()) {
                traitTypes[i] = trait.getNameTrait();
                i++;
            }
            return traitTypes;
        }

    public ArrayList<Trait> getTraits() {
        return breed.getTraitsArrayList();
    }

    public String[] getVariationTypes(String traitName) {
        String [] variations = null;
        for (Trait trait : breed.getTraitsArrayList()) {
            if (trait.getNameTrait().equals(traitName)) {
                variations = new String[trait.getVariationsList().size()];
                trait.getVariationsList().toArray(variations);
            }
        }
        return variations;
    }

    private final javax.swing.Action pickBreedShape =
            new javax.swing.AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }
            };

    public JButton makeBreedShapeButton() {
        breedShapeButton = new JButton(new ShapeIcon(org.nlogo.shape.VectorShape.getDefaultShape()));
        breedShapeButton.setActionCommand(this.getName());
        breedShapeButton.addActionListener(this);
        breedShapeButton.setSize(30, 30);
        breedShapeButton.setBackground(color);
        breedShapeButton.setToolTipText("Change shape");
        return breedShapeButton;
    }


    // when clicks on shape selection -a.
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        ShapeSelector myShapeSelector = new ShapeSelector(parentFrame, allShapes(), this, curIconIndex, curColor);
        myShapeSelector.setVisible(true);
        if (myShapeSelector.getChosenValue() >= 0) {
            ShapeIcon shapeIcon = new ShapeIcon(myShapeSelector.getShape());
            shapeIcon.setColor(myShapeSelector.getSelectedColor());
            breedShapeButton.setIcon(shapeIcon);
            breedShape = myShapeSelector.getChosenShape();
            curColor = myShapeSelector.getSelectedColor();
            curIconIndex = myShapeSelector.getChosenValue();
        }
    }

    // getting shapes from NL -a.
    String[] allShapes() {
        String[] defaultShapes =
                org.nlogo.util.Utils.getResourceAsStringArray
                        ("/system/defaultShapes.txt");  //default NetLogo shapes shapes (Sept 19, 2013)
                        //("/system/deltatickShapes.txt");      //DeltaTick shapes with animate objects (Sept 19, 2013)
        String[] libraryShapes =
                org.nlogo.util.Utils.getResourceAsStringArray
                        ("/system/libraryShapes.txt");
        String[] mergedShapes =
                new String[defaultShapes.length + 1 + libraryShapes.length];
        System.arraycopy(defaultShapes, 0,
                mergedShapes, 0,
                defaultShapes.length);
        mergedShapes[defaultShapes.length] = "";
        System.arraycopy(libraryShapes, 0,
                mergedShapes, defaultShapes.length + 1,
                libraryShapes.length);
        return defaultShapes; // NOTE right now just doing default
    }

    public java.util.List<Shape> parseShapes(String[] shapes, String version) {
        return org.nlogo.shape.VectorShape.parseShapes(shapes, version);
    }

    public void setBreedShape(String shape) {
        breedShape = new String(shape);
    }

    public String setupBreedShape() {
        if (breedShape != null) {
            return "set-default-shape " + plural() + " \"" + breedShape + "\"\n";
        }
        return "";
    }

    public class InspectSpeciesButton extends JButton {
        BreedBlock myParent;

        public InspectSpeciesButton(BreedBlock bBlock) {
            this.myParent = bBlock;
            setPreferredSize(new Dimension(30, 30));
            try {
            Image img = ImageIO.read(getClass().getResource("/images/magnify.gif"));
            setIcon(new ImageIcon(img));
            }
            catch (IOException ex) {
             }
            setForeground(java.awt.Color.gray);
            setBackground(color);
            //setBorderPainted(true);
            setMargin(new java.awt.Insets(1, 1, 1, 1));
            setToolTipText("Edit species");
        }
    }


    public Breed myBreed() {
        return breed;
    }

    public boolean hasTrait(String traitName) {
        for (TraitBlockNew block : myTraitBlocks) {
            if (block.getTraitName().equalsIgnoreCase(traitName)) {
                return true;
            }
        }
        return false;
    }
    public int numTraits() {
        return myTraitBlocks.size();
    }

    public RemoveButton getRemoveButton() {
        return removeButton;
    }

//    public void addTraittoBreed(TraitBlock traitBlock) { // not used -A. (Aug 10, 2012)
//        traitBlock.showColorButton();
//        traitBlock.doLayout();
//        traitBlock.validate();
//        traitBlock.repaint();
//    }

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


    public void mousePressed(java.awt.event.MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        beforeDragX = point.x;
        beforeDragY = point.y;
        beforeDragXLoc = getLocation().x;
        beforeDragYLoc = getLocation().y;
    }


    public void mouseDragged(java.awt.event.MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        this.setLocation(
                point.x - beforeDragX + beforeDragXLoc,
                point.y - beforeDragY + beforeDragYLoc);
    }

    public void repaint() {
        if (parentFrame != null) {
            parentFrame.repaint();
        }
        super.repaint();
    }

// not used any more
// parent.removeTraitBlock() Not needed because traitblock does not directly go inside breedblock (like it used to) april 3, 2013
//    public void removeTraitBlock(TraitBlockNew traitBlock) {
//        remove(traitBlock);
////        breedVariationHashMap.clear(); // Added March 2, 2013
//    }

    public boolean getHasSpeciesInspector () {
        return hasSpeciesInspector;
    }

    public void setHasSpeciesInspector(boolean value) {
        hasSpeciesInspector = value;
    }

    public HashSet<String> getMyUsedTraits() {
        return myUsedTraits;
    }

    public void addTraitBlocktoList(TraitBlockNew block) {
        myTraitBlocks.add(block);
    }
    public void removeAllTraitBlocks() {
        myTraitBlocks.clear();
    }

    public List<TraitBlockNew> getMyTraitBlocks() {
        return myTraitBlocks;
    }
    public TraitBlockNew getMyTraitBlock(String traitName) {
        for (TraitBlockNew t : myTraitBlocks) {
            if (t.getTraitName().equalsIgnoreCase(traitName)) {
                return t;
            }
        }
        return null;
    }
    public List<BehaviorBlock> getMyBehaviorBlocks() {
        return myBehaviorBlocks;
    }

    public List<ConditionBlock> getMyConditionBlocks() {
        return myConditionBlocks;
    }

    public void setReproduceUsed (boolean value) {
        reproduceUsed = value;
    }

    public boolean getReproduceUsed () {
        return reproduceUsed;
    }

    public boolean isCarryingCapacitySliderEnabled() {
        return carryingCapacitySliderEnabled;
    }

    public String getNumber() {
        return number.getText();
    }

    public String getBreedShape() {
        return breedShape;
    }

    public String getColorName() {
        return colorName;
    }

    public List<CodeBlock> getMyBlocks() {
        return myBlocks;
    }

    public void setInspectSpeciesButtonShapeColor(VectorShape shape, Color color) {
        ShapeIcon icon = new ShapeIcon(shape);
        icon.setColor(color);
        inspectSpeciesButton.setIcon(icon);
    }

    public class RectPanel extends JPanel {
        private String breedName;
        private Color color;
        private JLabel label;

        public RectPanel(String breedName, Color color) {
            this.breedName = breedName;
            this.color = color;
            label = new JLabel();

            setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            setPreferredSize(new Dimension(this.getWidth(), 40));
            setBackground(this.color);
            this.makeLabel();
            add(this.label);
            validate();

        }
        private void makeLabel() {
            this.label.setText("What do you want " + this.breedName + " to do?");
            this.label.setBackground(this.color);
        }
        public void setBreedName(String name) {
            this.breedName = name;
            makeLabel();
        }
    }
}

