package org.nlogo.deltatick.xml;

import org.nlogo.api.Property;
import org.nlogo.deltatick.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 8, 2010
 * Time: 4:55:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelBackgroundInfo {
    ArrayList<Breed> breeds = new ArrayList<Breed>(); //list of breeds available in XML
    ArrayList<Global> globals = new ArrayList<Global>();
    ArrayList<Envt> envts = new ArrayList<Envt>();
    ArrayList<Trait> traits = new ArrayList<Trait>();
    ArrayList<DiveIn> diveIns = new ArrayList<DiveIn>();
    ArrayList<MiscProcedure> miscProcedures = new ArrayList<MiscProcedure>();
    String setup;
    String go;

    // TODO: Aditi: June 25, 2013. There need to be multiple libraries
    String library;
    String version;
    String draw;
    String erase;
    boolean isThereDraw = false; //does the XML have a drawing procedure
    boolean enableMutationSlider = false;
    boolean distributionButtonEnabled = false;

    String maxNumberSpecies;
    boolean activateStepIn;

    public ModelBackgroundInfo() {
    }

    public void clear() {
        breeds.clear();
        globals.clear();
        envts.clear();
        setup = null;
        go = null;
        library = null;
        version = null;
        draw = null;
        traits.clear();
        enableMutationSlider = false;
    }

    public void populate(NodeList breedNodes, NodeList traitNodes, NodeList globalNodes, NodeList envtNodes, NodeList setup, NodeList go, NodeList library,
                         NodeList draw, NodeList erase, NodeList behavior, NodeList diveInNodes, NodeList interfaceNodes) throws Exception {
        try {
            if (setup.getLength() > 0) {
                this.setup = setup.item(0).getTextContent();
            }
            if (go.getLength() > 0) {
                this.go = go.item(0).getTextContent();
            }

            if (draw.getLength() > 0) {
                this.draw = draw.item(0).getTextContent();
            }
            if (erase.getLength() > 0) {
                this.erase = erase.item(0).getTextContent();
            }

            for (int i = 0; i < traitNodes.getLength(); i++) {
                Node traitNode = traitNodes.item(i);
                traits.add(new Trait(traitNode));
            }

            // populating class variable, breeds with the given breedNodes nodelist -A. (oct 17)
            for (int i = 0; i < breedNodes.getLength(); i++) {
                Node breedNode = breedNodes.item(i);
                breeds.add(new Breed(breedNode));
            }

            for (Breed breed : breeds) {
                breed.setTraitsArrayList(traits);
            }

            for (int i = 0; i < envtNodes.getLength(); i++) {
                Node envtNode = envtNodes.item(i);
                boolean addToList = true;
                Envt newEnvt = new Envt(envtNode);
                for (Envt envt : envts) {
                    if (envt.nameEnvt.equalsIgnoreCase(newEnvt.nameEnvt)) {
                        addToList = true;
                    }
                }
                if (addToList) {
                    envts.add(new Envt(envtNode));
                }
            }

            for (int i = 0; i < globalNodes.getLength(); i++) {
                Node globalNode = globalNodes.item(i);
                boolean addToList = true;
                Global newGlobal = new Global(globalNode);
                for (Global global : globals) {
                    if (global.name.equalsIgnoreCase(newGlobal.name)) {
                        addToList = false;
                    }
                }
                if (addToList) {
                    globals.add(newGlobal);
                }
            }

            for (int i = 0; i < diveInNodes.getLength(); i++) {
                Node diveInNode = diveInNodes.item(i);
                diveIns.add(new DiveIn(diveInNode));
            }

            for (int i = 0; i < interfaceNodes.getLength(); i++) {
                Node interfaceNode = interfaceNodes.item(i);
                NodeList iNodes = interfaceNode.getChildNodes();
                for (int j = 0; j < iNodes.getLength(); j++) {
                    Node iNode = iNodes.item(j);
                    if (iNode.getNodeName().equalsIgnoreCase("numberSpecies")) {
                        maxNumberSpecies = iNode.getTextContent();
                    }
                    if (iNode.getNodeName().equalsIgnoreCase("stepIn")) {
                        String tempActivateStepIn = new String(iNode.getTextContent());
                        activateStepIn = Boolean.parseBoolean(tempActivateStepIn);
                    }
                    if (iNode.getNodeName().equalsIgnoreCase("drawFeature")) {
                        String temp = new String(iNode.getTextContent());
                        isThereDraw = Boolean.parseBoolean(temp);
                    }                                            //
                    if (iNode.getNodeName().equalsIgnoreCase("mutationSlider")) {
                        enableMutationSlider = Boolean.parseBoolean(iNode.getTextContent());
                    }
                    if (iNode.getNodeName().equalsIgnoreCase("distributionButton")) {
                        distributionButtonEnabled = Boolean.parseBoolean(iNode.getTextContent());
                    }
                }
            }

            this.library = library.item(0).getAttributes().getNamedItem("name").getTextContent();
            this.version = library.item(0).getAttributes().getNamedItem("version").getTextContent();
        } catch (Exception e) {
            throw new Exception("Malformed XML file!" + e);
        }
    }

    public void addMiscProcedures(NodeList miscProcedureNodes) {
        for (int i = 0; i < miscProcedureNodes.getLength(); i++) {
            miscProcedures.add(new MiscProcedure(miscProcedureNodes.item(i)));
        }
    }

    // Where this stuff is being translated into NetLogo code in the code window -A.

    //these methods are activated and implemented when library is loaded itself -A. (sept 13)
    public String declareGlobals() {
        String code = "";
        if (globals.size() > 0) {
            code += "globals [\n";
            for (Global global : globals) {
                code += "  " + global.name + "\n";
            }
            code += "]\n";
        }
        return code;
    }

    public String declareDrawingEnvt() {
        String code = "";
        code += "patches-own [painted?]\n";
        return code;
    }

    //will have to insert setup code for patches here as well -A. (sept 13)
    public String setupBlock(List<BreedBlock> usedBreeds, List<TraitBlockNew> usedTraits, List<EnvtBlock> usedEnvts, List<PlotBlock> myPlots, List<DiveInBlock> usedDiveIns) {
        String code = "to setup\n";
        code += "  clear-all\n";
        //code += "ask patches [set pcolor white]\n";
        if (setup != null) {
            code += setup + "\n";
        }

        if (draw != null) {
            code += "ask patches [set painted? false]\n";
        }
        //this shows up when BreedBlock is dragged into BuildPanel -A. (sept 13)
        for (BreedBlock breedBlock : usedBreeds) {
            //code += breedBlock.setBreedShape();
            code += breedBlock.setup();
        }

        for (DiveInBlock dBlock : usedDiveIns ) {
            code += dBlock.setup();
        }

        for (Global global : globals) {
            code += global.setup();
        }
        for (EnvtBlock envtBlock : usedEnvts) {
            //code += "to set";
            code += envtBlock.setup();
        }
        //insert setup code for patches here
        code += "  reset-ticks\n";
        if (myPlots.size() > 0) {
            //code += "  do-plotting\n";
        }
        code += "end\n";

        return code;
    }

    public String updateBlock(List<BreedBlock> usedBreeds, List<EnvtBlock> usedEnvts) {
        String code = "";
//        if (go != null) {
//            code += go;
//        }

        for (BreedBlock breedBlock : usedBreeds) {
            code += breedBlock.update();
        }
        for (Global global : globals) {
            code += global.update();
        }
        for (EnvtBlock envtBlock : usedEnvts) {
            code += envtBlock.update();
        }

        return code;
    }

    public String getGoCode() {
        if (go != null) {
            return go;
        }
        else {
            return  "";
        }
    }

    public String drawCode() {
        String code = "";
        if (draw != null) {
            code += draw;
        }
        return code;
    }

    public String eraseCode() {
        String code = "";
        if (erase != null) {
            code += erase;
        }
        return code;
    }

    // return only names of breeds -A. (Oct 17)
    public String[] getBreedNames() {
        // breedTypes is an array of size n of breeds -A. (oct 5)
        String[] breedTypes = new String[breeds.size()];
        int i = 0;
        for (Breed breed : breeds) {
            breedTypes[i] = breed.plural;
            i++;
        }
        //System.out.println(breedTypes);
        return breedTypes;
    }

    public ArrayList<String []> getBreedSetupNumbers() {
        ArrayList<String []> breedSetupNumbers = new ArrayList<String []>();
        for (Breed breed : breeds) {
            breedSetupNumbers.add(breed.getSetupNumbers());
        }
        return breedSetupNumbers;
    }

    // If I give you a breed name, give me other information about that breed -A. (oct 17)
    public Breed getBreed(String name) {
        for (Breed breed : breeds) {
            if (breed.plural().equalsIgnoreCase(name)) {
                return breed;
            }
        }
        return null;
    }

    //get entire ArrayList -A. (oct 17)
    public ArrayList<Breed> getBreeds() {
        return breeds;
    }



    public String[] getEnvtTypes() {
        String[] envtTypes = new String[envts.size()];
        int i = 0;
        for (Envt envt : envts) {
            envtTypes[i] = envt.nameEnvt;
            i++;
        }
        return envtTypes;

    }

    public ArrayList<Envt> getEnvts() {
        return envts;
    }

    public ArrayList<DiveIn> getDiveIns() {
        return diveIns;
    }

    public String[] getTraitNames() {
        String[] traitTypes = new String[traits.size()];
        int i = 0;
        for (Trait trait : traits) {
            traitTypes[i] = trait.getNameTrait();
            i++;
        }
        return traitTypes;
    }

    public ArrayList<Trait> getTraits() {
        return traits;
    }

    public String[] getVariationTypes(String traitName) {
        String [] variations = null;
        for (Trait trait : traits) {
            if (trait.getNameTrait().equals(traitName)) {
                variations = new String[trait.getVariationsList().size()];
                trait.getVariationsList().toArray(variations);
            }
        }
        return variations;
    }

    class Global {
        String name;
        String setupReporter;
        String updateReporter;

        public Global(Node globalNode) {
            name = globalNode.getAttributes().getNamedItem("name").getTextContent();

            NodeList info = globalNode.getChildNodes();
            for (int i = 0; i < info.getLength(); i++) {
                if (info.item(i).getNodeName().equalsIgnoreCase("setupReporter")) {
                    setupReporter = info.item(i).getTextContent();
                }

                if (info.item(i).getNodeName().equalsIgnoreCase("updateReporter")) {
                    updateReporter = info.item(i).getTextContent();
                }
            }
        }

        public String setup() {
            String code = "";
            code += "set " + name + " " + setupReporter + "\n";
            return code;
        }

        public String update() {
            String code = "";
            code += "set " + name + " " + updateReporter + "\n";
            return code;
        }
    }

    public String getLibrary() {
        return library;
    }

    public int getMaxNumberSpeciesAllowed() {
        return Integer.parseInt(maxNumberSpecies);
    }

    public boolean getActivateStepIn() {
        return activateStepIn;
    }

    public boolean getIsDraw() {
        return isThereDraw;
    }

    public boolean getEnableMutationSlider() {
        return enableMutationSlider;
    }

    public boolean isDistributionButtonEnabled() {
        return distributionButtonEnabled;
    }

    public String unPackMiscProcedures(boolean isTraitDefined) {
        String passBack = "";
        for (MiscProcedure miscProcedure : miscProcedures) {
            passBack += miscProcedure.unPackAsProcedure(isTraitDefined);
        }
        return passBack;
    }
    public String unPackVisualizeProcedures() {
        String passBack = "";
        for (Trait trait : traits) {
            passBack += unPackVisualizeProcedure(trait.getNameTrait());
        }
        return passBack;
    }
    public String unPackVisualizeGoProcedures() {
        String passBack = "";
        for (Trait trait : traits) {
            passBack += unPackVisualizeGoProcedure(trait.getNameTrait());
        }
        return passBack;
    }
    public String unPackVisualizeProcedure(String traitName) {
        String passBack = "";
        for (Trait trait : traits) {
            if (trait.getNameTrait().equalsIgnoreCase(traitName)) {
                passBack += "to " + trait.getVisualizeCode() + "\n";
                passBack += trait.getVisualizeProcedure();
                passBack += "\nend\n";
            }
        }
        return passBack;
    }
    public String unPackVisualizeGoProcedure(String traitName) {
        String passBack = "";
        for (Trait trait : traits) {
            if (trait.getNameTrait().equalsIgnoreCase(traitName)) {
                passBack += "to " + trait.getVisualizeGoCode() + "\n";
                passBack += trait.getVisualizeGoProcedure();
                passBack += "\nend\n";
            }
        }
        return passBack;
    }
    public String unPackBlankVisualizeProcedure(String traitName) {
        String passBack = "";
        for (Trait trait : traits) {
            if (trait.getNameTrait().equalsIgnoreCase(traitName)) {
                passBack += "to " + trait.getVisualizeCode() + "\n";
                passBack += "\nend\n";
            }
        }
        return passBack;
    }
    public String unPackBlankVisualizeGoProcedure(String traitName) {
        String passBack = "";
        for (Trait trait : traits) {
            if (trait.getNameTrait().equalsIgnoreCase(traitName)) {
                passBack += "to " + trait.getVisualizeGoCode() + "\n";
                passBack += "\nend\n";
            }
        }
        return passBack;
    }
    private class MiscProcedure {
        String name;
        boolean isReporter;
        String paramters;
        boolean needsTraitDefined;
        String procedureCode;

        MiscProcedure(Node procedureNode) {
            this.name = new String (procedureNode.getAttributes().getNamedItem("name").getTextContent());
            this.isReporter = procedureNode.getAttributes().getNamedItem("isReporter").getTextContent().equalsIgnoreCase("true");
            String aParamters = procedureNode.getAttributes().getNamedItem("parameters").getTextContent();
            this.paramters = new String (aParamters.replace(',', ' '));
            this.needsTraitDefined = procedureNode.getAttributes().getNamedItem("needsTraitDefined").getTextContent().equalsIgnoreCase("true");
            NodeList childNodes = procedureNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("procedureCode")) {
                    this.procedureCode = new String(childNodes.item(i).getTextContent());
                }
            }
        }
        public boolean isNeedsTraitDefined() {
            return needsTraitDefined;
        }
        public String getName() {
            return name;
        }
        public String getProcedureCode() {
            return procedureCode;
        }
        public String unPackAsProcedure(boolean isTraitDefined) {
            String passBack = "";
            // Check type
            if (isReporter) {
                passBack += "to-report ";
            }
            else {
                passBack += "to ";
            }
            // Procedure name and parameters
            passBack += name;
            if (!paramters.equalsIgnoreCase("")) {
                passBack += " [ " + paramters + " ]\n";
            }
            // Procedure code
            if (isTraitDefined) {
                passBack += "\t" + procedureCode;
            }

            passBack += "\nend\n";
            return passBack;
        }
    }
}
