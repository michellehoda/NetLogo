package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.Envt;
import org.nlogo.deltatick.xml.ModelBackgroundInfo;
import org.nlogo.deltatick.xml.ModelBackgroundInfo2;
import org.nlogo.window.GUIWorkspace;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 8, 2010
 * Time: 5:13:59 PM
 * To change this template use File | Settings | File Templates.
 */

// Transferable defines the interface for classes that can be used to provide data for a transfer operation -A. (sept 8)
public class BuildPanel
        extends JPanel
        implements Transferable {

    GUIWorkspace workspace;

    // Linked list is a class. In addition to implementing the List interface, the LinkedList class provides
    // uniformly named methods to get, remove and insert an element at the beginning and end of the list. -a.

    List<BreedBlock> myBreeds = new LinkedList<BreedBlock>();
    List<TraitBlockNew> myTraitsNew = new LinkedList<TraitBlockNew>();
    List<PlotBlock> myPlots = new LinkedList<PlotBlock>();
    List<HistogramBlock> myHisto = new LinkedList<HistogramBlock>();
    List<MonitorBlock> myMonitors = new LinkedList<MonitorBlock>();
    List<EnvtBlock> myEnvts = new LinkedList<EnvtBlock>();
    List<DiveInBlock> myDiveIns = new LinkedList<DiveInBlock>();
    ModelBackgroundInfo bgInfo = new ModelBackgroundInfo();
    JLabel label;

    static int plotNumber = 0;


    //DataFlavor"[]" is an array - A. (sept 8)
    DataFlavor[] flavors = new DataFlavor[]{
            DataFlavor.stringFlavor
    };

    public BuildPanel(GUIWorkspace workspace) {
        super();
        this.workspace = workspace;
        setBackground(java.awt.Color.white);
        setLayout(null);
        validate();
    }

    public Object getTransferData(DataFlavor dataFlavor)
            throws UnsupportedFlavorException {
        if (isDataFlavorSupported(dataFlavor)) {
            return unPackAsCode();
        }
        return null;
    }

    public String unPackAsCode() {
        String passBack = "";

        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.declareBreed();
        }
        passBack += "\n";
        //TODO Allow students to give this name (Sept 20, 2013)
        for (DiveInBlock block : myDiveIns) {
            passBack += "breed [predators predator] \n";
        }

        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.breedVars();
            // traitBlock declared as breed variable here -A. (Aug 8, 2012)
            HashSet<String> allTraits = new HashSet<String>(); // exclusive list of myTraits & myUsedBehInputs to add in breeds-own
                                                                        //-A. (Aug 10, 2012)
            if ( myTraitsNew.size() > 0 ) {
                for ( TraitBlockNew traitBlock : myTraitsNew ) {
                    if ( traitBlock.getMyParent().plural().equals(breedBlock.plural()) ) {      // TODO check if works March 8, 2013
                        allTraits.add(traitBlock.getName());
                    }
                }
            }
            for ( String string : allTraits ) {
                passBack += string + "\n";
            }
            passBack += "]\n";
        }

        passBack += "\n";
        for (EnvtBlock envtBlock : myEnvts) {
            passBack += envtBlock.OwnVars();
        }

        passBack += "\n";

        passBack += bgInfo.declareGlobals();

        passBack += "\n";

        //TraitBlock's setup code doesn't come from here at all. It comes from breeds -A. (Aug 8, 2012)
        passBack += bgInfo.setupBlock(myBreeds, myTraitsNew, myEnvts, myPlots, myDiveIns);
        passBack += "\n";

        // begin function to go
        passBack += "to go\n";
        // 20130819 Check maxNumber for each breed, stop of maxNumber is reached
        for (BreedBlock breedBlock : myBreeds) {
            passBack += "if count " + breedBlock.plural() + " > " + breedBlock.getMaxNumber() + " [\n"
                        + "\tuser-message(word \"There can only be at most " + breedBlock.getMaxNumber()
                        + " " + breedBlock.plural() + " !\")"
                        + " stop" + "\n]\n";
        }
        passBack += bgInfo.updateBlock(myBreeds, myEnvts);

        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.unPackAsCode();
        }
        for (EnvtBlock envtBlock : myEnvts) {
            passBack += envtBlock.unPackAsCode();
        }
        for (DiveInBlock block : myDiveIns) {
            passBack += block.unPackAsCode();
        }


        if (myPlots.size() > 0) {
            passBack += "do-plotting\n";
        }
        passBack += "tick\n";

        passBack += "end\n";
        passBack += "\n";

        for (DiveInBlock block : myDiveIns) {

            passBack += "to-report student-in\n";
            passBack += block.getDiveInCode();
            passBack += "\nend\n";
        }

        //new function: to draw - Aditi (jan 17, 2013)
        passBack += "to draw\n";
        passBack += bgInfo.drawCode() + "\n";
        passBack += "end\n";

        // remaining procedures
        passBack += unPackProcedures();
        passBack += "\n";

        if (myPlots.size() > 0) {
            passBack += "\n\n";
            passBack += "to do-plotting\n";
            for (PlotBlock plot : myPlots) {
                passBack += plot.unPackAsCode();
            }
            passBack += "end\n";
        }

        return passBack;
    }

public String newSaveAsXML() {
    String passBack = "";
    passBack += "<?xml version=\"1.0\" encoding=\"us-ascii\"?>\n" +
            "<!DOCTYPE model SYSTEM \"DeltaTickModelFormat.dtd\">\n";

    passBack += "<model>\n";

    // declare breeds
    for (BreedBlock breedBlock : myBreeds) {
        passBack += breedBlock.declareBreedXML();

    }

    passBack += "\n</model>";
    return passBack;
    }

   //Michelle's code
    public String saveAsXML() {
        String passBack = "<";

        // declare breeds
        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.declareBreed();
        }



        passBack += "\n";
        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.breedVars();
        }

        for (TraitBlockNew traitBlock : myTraitsNew) {
            passBack += traitBlock.getTraitName();
        }

        passBack += "\n";

        passBack += bgInfo.declareGlobals();
        passBack += "\n";

        passBack += bgInfo.setupBlock(myBreeds, myTraitsNew, myEnvts, myPlots, myDiveIns);
        passBack += "\n";

        passBack += "to go\n";
        passBack += bgInfo.updateBlock(myBreeds, myEnvts);

        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.unPackAsCode();
        }
        passBack += "tick\n";
        if (myPlots.size() > 0) {
            passBack += "do-plotting\n";
        }
        passBack += "end\n";
        passBack += "\n";

        passBack += "to draw\n";
        passBack += bgInfo.drawCode() + "\n";
        passBack += "end\n";

        passBack += unPackProcedures();
        passBack += "\n";

        if (myPlots.size() > 0) {
            passBack += "\n\n";
            passBack += "to do-plotting\n";
            for (PlotBlock plot : myPlots) {
                passBack += plot.unPackAsCode();
            }
            passBack += "end\n";
        }

        return passBack;
    }


//*
//isDataFlavorSupported
//public boolean isDataFlavorSupported(DataFlavor flavor)
    //  Returns whether the requested flavor is supported by this Transferable.
    //Specified by:
    //  isDataFlavorSupported in interface Transferable
    //Parameters:
    //  flavor - the requested flavor for the data
    //Returns:
    //   true if flavor is equal to DataFlavor.stringFlavor or DataFlavor.plainTextFlavor; false if flavor is not one of the above flavors
    //Throws:
    //  NullPointerException - if flavor is null
// -a.

    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        for (int i = 0; i < flavors.length; i++) {
            if (dataFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    // all array being returned -A. (sept 8)
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public ModelBackgroundInfo getBgInfo() {
        return bgInfo;
    }


    // find method in DeltaTicktab to add actors -a.
    public void addBreed(BreedBlock block) {
        myBreeds.add(block);
        block.setBounds(0,
                0,
                block.getPreferredSize().width,
                block.getPreferredSize().height);

        add(block);
        block.doLayout();
        block.validate();
        block.repaint();
    }


    // do we want variation to show up inside a breed block or to act like a condition block? - (feb 4)
    //public void addTrait(TraitBlock block) {
    public void addTrait(TraitBlockNew block) {
        block.setBounds(0,
                        0,
                        block.getPreferredSize().width,
                        block.getPreferredSize().height);

        //block.colorButton.setEnabled(false);
        block.doLayout();
        block.validate();
        block.repaint();
        //myTraits.add(block);
        myTraitsNew.add(block);
    }

    public void addOperator(OperatorBlock oBlock) {
        // do I need a list of OperatorBlocks myOperators.add(oBlock);
    }

    public void addMonitor(MonitorBlock mBlock) {
        //mBlock.setPlotName("plot " + plotNumber);
        myMonitors.add(mBlock);
        mBlock.setBounds(200,
                0,
                mBlock.getPreferredSize().width,
                mBlock.getPreferredSize().height);
        add(mBlock);
        mBlock.doLayout();
        mBlock.validate();
        mBlock.repaint();
        this.validate();
    }


    public void addPlot(PlotBlock block) {
        plotNumber++;
        myPlots.add(block);
        //int plotNumber = myPlots.size() + myHisto.size();
        block.setPlotName("plot " + plotNumber);
        block.setBounds(200,
                0,
                block.getPreferredSize().width,
                block.getPreferredSize().height);
        //block.getPlotPen();
        add(block);
        block.doLayout();
        block.validate();
        block.repaint();
        this.validate();
    }

    public void addDiveIn (DiveInBlock block) {
        block.setBounds(200,
                0,
                block.getPreferredSize().width,
                block.getPreferredSize().height);
        myDiveIns.add(block);
        add(block);
        block.doLayout();
        block.validate();
        block.repaint();
        this.validate();
    }


    //make linked list for envt? -A. (sept 8)
    public void addEnvt(EnvtBlock block) {
        myEnvts.add(block);
         block.setBounds(400,
                0,
                block.getPreferredSize().width,
                block.getPreferredSize().height);
        add(block);
        block.doLayout();
        block.validate();
        block.repaint();


    }



    // Collection<typeObject> object that groups multiple elements into a single unit -A. (sept 8)
    public Collection<BreedBlock> getMyBreeds() {
        return myBreeds;
    }

    public List<PlotBlock> getMyPlots() {
        return myPlots;
    }

    public boolean plotExists(String name) {
        boolean check = false;
        for (PlotBlock plotBlock : this.getMyPlots()) {
            if (plotBlock.getPlotName().equalsIgnoreCase(name)) {
                check = true;
            }
        }
        return check;
    }

    public boolean monitorExists(String name) {
        boolean check = false;
        return check;
    }



    public List<HistogramBlock> getMyHisto() {
        return myHisto;
    }

    public List<MonitorBlock> getMyMonitors() {
        return myMonitors;
    }

    public TraitBlockNew getMyTrait(String traitName) {
        for (TraitBlockNew traitBlockNew : myTraitsNew) {
            if (traitBlockNew.getTraitName().equalsIgnoreCase(traitName)) {
                return traitBlockNew;
            }
        }
        return null;
    }
    public List<TraitBlockNew> getMyTraits() {
        return myTraitsNew;
    }


    public Collection<EnvtBlock> getMyEnvts() {
        return myEnvts;
    }

    //HashMap is a pair of key mapped to values. eg. (key)name1: (value)SSN1 -A. (sept 8)
    public String unPackProcedures() {
        HashMap<String, CodeBlock> procedureCollection = new HashMap<String, CodeBlock>();
        String passBack = "";

        for (BreedBlock breedBlock : myBreeds) {
            if (breedBlock.children() != null) {
                procedureCollection.putAll(breedBlock.children());
            }
        }

        for (PlotBlock plotBlock : myPlots) {
            if (plotBlock.children() != null) {
                procedureCollection.putAll(plotBlock.children());
            }
        }

        for (HistogramBlock histoBlock : myHisto) {
            if (histoBlock.children() != null) {
                procedureCollection.putAll(histoBlock.children());
            }
        }

        for (MonitorBlock mBlock : myMonitors) {
            if (mBlock.children() != null) {
                procedureCollection.putAll(mBlock.children());
            }
        }

        for (DiveInBlock diveInBlock : myDiveIns) {
            if (diveInBlock.children() != null) {
                procedureCollection.putAll(diveInBlock.children());
            }
        }

        for (EnvtBlock envtBlock : myEnvts) {
            if (envtBlock.children() != null) {
                procedureCollection.putAll(envtBlock.children());
            }
        }

        for (String name : procedureCollection.keySet()) {
            passBack += procedureCollection.get(name).unPackAsProcedure();
        }

        // Mutate procedure
        boolean needMutateCode = false;
        for (BreedBlock bBlock : myBreeds) {
            if (bBlock.getReproduceUsed() && bBlock.getMyTraitBlocks().size() > 0) {
                //needMutateCode = true; // Commented May 27, 2013 for OOJH Activity1
            }
        }

        if (needMutateCode) {
            passBack += "to mutate\n";
            // Check breed
            for (BreedBlock breedBlock : myBreeds)  {
                if (breedBlock.getReproduceUsed()) {

                passBack += "\t\tif breed = " + breedBlock.plural() + " [\n";
                // Foreach trait of that breed
                for (TraitBlockNew traitBlock: breedBlock.getMyTraitBlocks()) {
                    String traitName = traitBlock.getTraitName();
                    passBack += "if random-float 100 < " + breedBlock.plural() + "-" + traitBlock.getTraitName() + "-mutation [";
                    passBack += "ifelse random-float 100 < 50 [";
                    passBack += "\nset " + traitName + " (" + traitName + " + .5 )]\n";
                    passBack += "\n[set " + traitName + " (" + traitName + " - .5 )]\n";
                    passBack += "\n]";

//                    passBack += "[set " + traitName + " (" + traitName + " - random-float " + breedBlock.plural() + "-" +
//                                                        traitBlock.getTraitName() + "-mutation)]\n";
//                    passBack += "[set " + traitName + " (" + traitName + " + random-float " + breedBlock.plural() + "-" +
//                                                        traitBlock.getTraitName() + "-mutation)]\n";
                }
                passBack += "\t\t]\n"; // corresponds to if condition
                }
            }
            passBack += " end\n\n";

        }

        return passBack;
    }

    //I think this is where you clear the window to remove everything-a.
    public void clear() {
        myBreeds.clear();
        myPlots.clear();
        myHisto.clear();
        myMonitors.clear();
        //myTraits.clear();
        myTraitsNew.clear();
        myEnvts.clear();
        removeAll();
        doLayout();
        //validate();
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        for (Component c : getComponents()) {
            c.setBounds(c.getBounds());

        }
    }

    public int breedCount() {
        return myBreeds.size();
    }

    public String[] getbreedNames() {
        String [] names = new String [myBreeds.size()];
        int i = 0;
        for ( BreedBlock breedBlock : myBreeds ) {
            names[i] = breedBlock.plural();
            i++;
        }

        return names;
    }

    public String[] getTraitNames() {
        String[] names = new String [myTraitsNew.size()];
        int i = 0;
        for ( TraitBlockNew traitBlock : myTraitsNew ) {
            names[i] = traitBlock.getTraitName();
            i++;
        }
        return names;
    }

    public int plotCount() {
        if (myPlots != null) {
            return myPlots.size();
        }
        return 0;
    }

    public int histoCount() {
        if (myHisto != null) {
            return myHisto.size();
        }
        return 0;
    }


    // breeds available in XML -A. (oct 5)
    public ArrayList<Breed> availBreeds() {
        return bgInfo.getBreeds();
    }

    public ArrayList<Envt> availEnvts() {
        return bgInfo.getEnvts();
    }

    public void removePlot(PlotBlock plotBlock) {
        myPlots.remove(plotBlock);
        remove(plotBlock);
    }

    public void removeHisto(HistogramBlock histoBlock) {
        myHisto.remove(histoBlock);
        remove(histoBlock);
    }

    public void removeBreed(BreedBlock breedBlock) {
        myBreeds.remove(breedBlock);
        remove(breedBlock);
    }

    public void removeEnvt(EnvtBlock envtBlock) {
        myEnvts.remove(envtBlock);
        remove(envtBlock);
    }

    public void removeTrait(TraitBlockNew traitBlock) {
        myTraitsNew.remove(traitBlock);
        remove(traitBlock);
    }

    public void removeDiveIn (DiveInBlock dBlock) {
        myDiveIns.remove(dBlock);
        remove(dBlock);
    }

    public String library() {
        return bgInfo.getLibrary();
    }

    public ArrayList<String> getVariations () {
        ArrayList<String> tmp = new ArrayList<String>();
        for ( TraitBlockNew tBlock : myTraitsNew ) {
            tmp = tBlock.varList;
        }
        return tmp;
    }

    public void addRect(String text) {
        label = new JLabel();
        label.setText(text);
        label.setBackground(Color.GRAY);
        label.setBounds(20, 40, 350, 30);
//        label.setBounds(20,
//                30,
//                this.getWidth() - 40,
//                40);
        add(label);
        validate();


    }

    public void removeRect() {
        this.remove(label);
    }
}
