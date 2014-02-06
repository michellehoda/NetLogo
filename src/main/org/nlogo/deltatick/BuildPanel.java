package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.*;
import org.nlogo.window.GUIWorkspace;

import javax.swing.*;
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
        passBack += "breed [sense-cones sense-cone]\n";
        passBack += "breed [speed-icons speed-icon]\n";

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
//        for (EnvtBlock envtBlock : myEnvts) {
//            passBack += envtBlock.OwnVars();
//        }

        passBack += bgInfo.declareDrawingEnvt();

        passBack += "\n";

        passBack += bgInfo.declareGlobals();

        passBack += "\n";

        //TraitBlock's setup code doesn't come from here at all. It comes from breeds -A. (Aug 8, 2012)
        passBack += bgInfo.setupBlock(myBreeds, myTraitsNew, myEnvts, myPlots, myDiveIns);
        passBack += "\n";

        // begin function to go
        passBack += "to go\n";
        // 20131116 - The following code is no longer needed since we're not using maxNumber anymore.
        // Kept it here in case we decide to return to it.
//        // 20130819 Check maxNumber for each breed, stop of maxNumber is reached
//        for (BreedBlock breedBlock : myBreeds) {
//            passBack += "if count " + breedBlock.plural() + " > " + breedBlock.getMaxNumber() + " [\n"
//                        + "\tuser-message(word \"There can only be at most " + breedBlock.getMaxNumber()
//                        + " " + breedBlock.plural() + " !\")"
//                        + " stop" + "\n]\n";
//        }
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

        passBack += bgInfo.getGoCode();

        if (myPlots.size() > 0) {
            passBack += "do-plotting\n";
        }
        passBack += "tick\n";

        passBack += "end\n";
        passBack += "\n";

        for (DiveInBlock block : myDiveIns) {

            passBack += "to-report student-in\n";
            passBack += block.getDiveInCode();
            passBack += "\nend\n\n";
        }

        for (MonitorBlock block : myMonitors) {
            for (QuantityBlock qBlock : block.getMyBlocks()) {   // will be just one here -Aditi (Sept 30, 2013)
                if (qBlock.histo == true) {
                    int i = qBlock.getHistoVariation().size();
                    for (Variation var : qBlock.getHistoVariation().values()) {
                        String breed = qBlock.getTraitBreed();
                        String trait = qBlock.getTrait();
                        String variation = var.value;
                        passBack += "to-report " + qBlock.getName() + i + " [" + breed + variation + "]\n";
                        passBack += "report count " + breed + " with [" + trait + " = " + variation + "]";
                        passBack += "\nend\n";
                        i = i - 1;
                    }
                }
            }
        }

        //new function: to draw - Aditi (jan 17, 2013)
        passBack += "to draw-barrier\n";
        passBack += bgInfo.drawCode() + "\n";
        passBack += "end\n";

        passBack += "to erase-barrier\n";
        passBack += bgInfo.eraseCode() + "\n";
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

        block.doLayout();
        block.validate();
        block.repaint();
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
    public List<DiveInBlock> getMyDiveIns() {
        return myDiveIns;
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

    public TraitBlockNew getMyTrait(String traitName, String breedName) {
        for (TraitBlockNew traitBlockNew : myTraitsNew) {
            if (traitBlockNew.getTraitName().equalsIgnoreCase(traitName) &&
                    traitBlockNew.getBreedName().equalsIgnoreCase(breedName)) {
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
        HashMap<String, TraitBlockNew> visualizeProcedures = new HashMap<String, TraitBlockNew>();
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
                for (QuantityBlock qBlock : mBlock.getMyBlocks()) {   //if it's a histo, code coming from above (Aditi, Sept 30, 2013)
                    if (qBlock.getHisto() == false) {
                        procedureCollection.putAll(mBlock.children());
                    }
                }
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

        // Populate visualizeProcedures
        for (BreedBlock breedBlock : myBreeds) {
            for (TraitBlockNew traitBlock : breedBlock.getMyTraitBlocks()) {
                if (!visualizeProcedures.containsKey(traitBlock.getTraitName())) {
                    visualizeProcedures.put(traitBlock.getTraitName(), traitBlock);
                }
            }
        }

        for (String name : procedureCollection.keySet()) {
            passBack += procedureCollection.get(name).unPackAsProcedure();
        }
        // Unpack visualize procedure even if the trait is now present
        // This prevents the NetLogo code error "nothing named set-sense-cone is defined"
        for (Trait trait : getBgInfo().getTraits()) {
            if (! traitExists(trait.getNameTrait())) {
                passBack += getBgInfo().unPackBlankVisualizeProcedure(trait.getNameTrait());
                passBack += getBgInfo().unPackBlankVisualizeGoProcedure(trait.getNameTrait());
            }
        }

        // Unpack misc supporting procedures
        passBack += getBgInfo().unPackMiscProcedures(myTraitsNew.size() > 0);
        passBack += "\n";

        // Unpack visualize procedures
        passBack += "\n";
        for (TraitBlockNew tBlock : visualizeProcedures.values()) {
            passBack += "\n" + tBlock.getVisualizeProcedure();
            passBack += "\n" + tBlock.getVisualizeGoProcedure() + "\n";
        }


        // Mutate procedure
        boolean needMutateProcedureCode = false;
        boolean needMutateProcedureHeader = false;
        for (BreedBlock bBlock : myBreeds) {
            if (bBlock.getReproduceUsed()) {
                needMutateProcedureHeader = true;
                if (bgInfo.getEnableMutationSlider() &&
                        bBlock.getMyTraitBlocks().size() > 0) {
                    needMutateProcedureCode = true; // Commented May 27, 2013 for OOJH Activity1
                }
            }
        }
        if (needMutateProcedureHeader) {
            passBack += "to mutate\n";
            if (needMutateProcedureCode) {
                passBack += generateMutateCode();
            }
            passBack += " end\n\n";
        }

        return passBack;
    }

    public String generateMutateCode() {
        String passBack = "";
        for (BreedBlock breedBlock : myBreeds)  {
            if (breedBlock.getReproduceUsed()) {

//                passBack += "\tif breed = " + breedBlock.plural() + " [\n";
                // Foreach trait of that breed
                for (TraitBlockNew traitBlock: breedBlock.getMyTraitBlocks()) {
                    String traitName = traitBlock.getTraitName();
                    String mutateProcedureName = "mutate-breed-" + traitName;
                    String mutateBreed = breedBlock.plural();
                    String mutateProbabilty = breedBlock.plural() + "-" + traitName + "-mutation";
                    String mutateParameters = mutateBreed + " " + mutateProbabilty;
                    passBack += "\t" + mutateProcedureName + " " + mutateParameters + "\n";
//                    passBack += "\t\tif random-float 100 < " + breedBlock.plural() + "-" + traitBlock.getTraitName() + "-mutation [ ";
//                    passBack += "ifelse random-float 100 < 50\n";
//                    passBack += "\t\t[set " + traitName + " (" + traitName + " + .5 )]\n";
//                    passBack += "\t\t[set " + traitName + " (" + traitName + " - .5 )]\n";
//                    passBack += "\tif " + traitName + " < 0 [set " + traitName + " 0]\n";
//                    passBack += "\tif " + traitName + " > 10 [set " + traitName + " 10]\n";
//                    passBack += "\t\t]\n";
                }
//                passBack += "\t]\n"; // corresponds to if condition
            }
        }
        return  passBack;
    }

    //I think this is where you clear the window to remove everything-a.
    public void clear() {
        myBreeds.clear();
        myPlots.clear();
        myHisto.clear();
        myMonitors.clear();
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

    public boolean breedExists(String name) {
        boolean retVal = false;
        for ( BreedBlock breedBlock : myBreeds ) {
            if (breedBlock.plural().equalsIgnoreCase(name)) {
                retVal = true;
            }
        }
        return retVal;
    }

    public boolean traitExists(String name) {
        boolean exists = false;
        for (TraitBlockNew traitBlockNew : myTraitsNew) {
            if (traitBlockNew.getTraitName().equalsIgnoreCase(name)) {
                exists = true;
                break;
            }
        }
        return exists;
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
