package org.nlogo.deltatick.xml;

import java.awt.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.*;

import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.*;
import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 2, 2010
 * Time: 6:52:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class LibraryReader {

    Frame frame;
    //FileDialog class displays a dialog window from which the user can select a file. -A. (sept 13)
    FileDialog fileLoader;
    DeltaTickTab deltaTickTab;
    public String fileName;

    //java.<String> openLibraries = new ArrayList<String>();
    HashSet<String> openLibraries = new HashSet<String>();

    CodeBlock block;

        // Aditi: Apr 16, 2013
    // new LibraryReader() is created in 2 cases:
    // 1. Explicit: When user clicks "Load Library" button and selects a library file. Argument libraryFileName is null
    // 2. Implicit: When user opens a model (via "Open Model" button). The model file specifies the library filename
    //              This filename is passed as an argument to the constructor. This argument MUST NOT be ignored.
    //              Further, if libraryFileName is specified (i.e. NOT null), then FileDialog MUST NOT be opened because
    //              the library file name has alredy been specified.
    public LibraryReader(Frame frame, DeltaTickTab deltaTickTab) {
        ////fileName = new String();
    	this.deltaTickTab = deltaTickTab;
        this.frame = frame;

//        // clear out any existing blocks
//       // this.deltaTickTab.clearLibrary(); // TODo commented out on feb 22, 2012- will need to re-think this, might have to bring it back
//        File file = null;
//        if (libraryFileName == null) {
//            // User has clicked "Load Library" button.
//            // Explicit library open.
//
//            while (fileName.indexOf(".xml") < 0 && !fileName.equals("nullnull")) {
//                fileLoader = new FileDialog(frame);
//                fileLoader.setVisible(true);
//                file = new File(fileLoader.getDirectory() + fileLoader.getFile());
//                fileName = new String (fileLoader.getDirectory() + fileLoader.getFile());
//
//                if (fileName.indexOf(".xml") < 0 && !fileName.equals("nullnull")){
//                    JOptionPane.showMessageDialog(deltaTickTab, "Oops! Please select a .xml file", "OOPS!", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//
//            if(fileName.equals("nullnull")){
//                return;
//            }
//        }
//        else {
//            // Library is opened implicity because user clicked "Open Model"
//            // libraryFileName specifies the full path
//            fileName = new String(libraryFileName);
//            // Now open the library file
//            file = new File(libraryFileName);
//        }
//
//        String thisLibraryName = readLibraryName(file);
//        if (!openLibraries.contains(thisLibraryName)) {
//            readLibraryAndPopulate(file);
//            openLibraries.add(thisLibraryName);
//        }
//        else {
//            System.out.println("Library already open");
//        }
    }

    public void openLibrary(String libraryFileName) {
        fileName = new String();

        // clear out any existing blocks
        // this.deltaTickTab.clearLibrary(); // TODo commented out on feb 22, 2012- will need to re-think this, might have to bring it back
        File file = null;
        if (libraryFileName == null) {
            // User has clicked "Load Library" button.
            // Explicit library open.

            while (fileName.indexOf(".xml") < 0 && !fileName.equals("nullnull")) {
                fileLoader = new FileDialog(this.frame);
                fileLoader.setVisible(true);
                file = new File(fileLoader.getDirectory() + fileLoader.getFile());
                fileName = new String (fileLoader.getDirectory() + fileLoader.getFile());

                if (fileName.indexOf(".xml") < 0 && !fileName.equals("nullnull")){
                    JOptionPane.showMessageDialog(deltaTickTab, "Oops! Please select a .xml file", "OOPS!", JOptionPane.ERROR_MESSAGE);
                }
            }

            if(fileName.equals("nullnull")){
                return ;
            }
        }
        else {
            // Library is opened implicity because user clicked "Open Model"
            // libraryFileName specifies the full path
            fileName = new String(libraryFileName);
            // Now open the library file
            file = new File(libraryFileName);
        }

//        // Check if this library is already open
        String thisLibraryName = readLibraryName(file);
//        if (!openLibraries.contains(thisLibraryName)) {
//            If the library is not open then open it
            readLibraryAndPopulate(file);
            openLibraries.add(thisLibraryName);
            return ;
//        }
//        else {
//            // Already open. Display error message.
//            String message = new String("Oops! This library is already open!");
//            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
//            return ;
//        }

    }

    public String readLibraryName(File libraryFile) {
        String libraryName = new String();
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document library = builder.parse(libraryFile);

            libraryName = library.getElementsByTagName("library").item(0).getAttributes().getNamedItem("name").getTextContent();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return libraryName;
    }

    private void readLibraryAndPopulate(File libraryFile) {
        //DocumentBuilder converts XML file into Document -A. (sept 13)
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document library = builder.parse(libraryFile);

            //needs to be in order as provided as parameters in populate() -A. (sept 13)
            deltaTickTab.getBuildPanel().getBgInfo().populate(
                    library.getElementsByTagName("breed"),
                    library.getElementsByTagName("trait"),
                    library.getElementsByTagName("global"),
                    library.getElementsByTagName("envt"),
                    library.getElementsByTagName("setup"),
                    library.getElementsByTagName("go"),
                    library.getElementsByTagName("library"),
                    library.getElementsByTagName("draw"),
                    library.getElementsByTagName("erase"),
                    library.getElementsByTagName("behavior"),
                    library.getElementsByTagName("diveIn"),
                    library.getElementsByTagName("interface")

            );

            // Initialize layout constants
            deltaTickTab.initializeLayoutConstantsFromXML(library.getElementsByTagName("interfaceLayout"));

            NodeList miscProcedures = library.getElementsByTagName("miscProcedure");
            if (miscProcedures.getLength() > 0) {
                deltaTickTab.getBuildPanel().getBgInfo().addMiscProcedures(miscProcedures);
            }

            NodeList behaviors = library.getElementsByTagName("behavior");

            for (int i = 0; i < behaviors.getLength(); i++) {
                Node behavior = behaviors.item(i);
                boolean b = false;
                block = new BehaviorBlock(behavior.getAttributes().getNamedItem("name").getTextContent(),
                        behavior.getAttributes().getNamedItem("traits").getTextContent());
                if (behavior.getAttributes().getNamedItem("mutate") != null) {
                    b = behavior.getAttributes().getNamedItem("mutate").getTextContent().equalsIgnoreCase("true");
                    if (b) {
                        ((BehaviorBlock) block).setIsMutate(true);
                    }
                }
                if (behavior.getAttributes().getNamedItem("inputReporter") != null) {
                    // Get reporter from bg info and add it to behavior block
                    String inputReporter = behavior.getAttributes().getNamedItem("inputReporter").getTextContent();
                    ((BehaviorBlock) block).setInputReporter(inputReporter);
                }
                seekAndAttachInfo(behavior);
                deltaTickTab.getLibraryHolder().addToBehaviorBlocksList((BehaviorBlock) block);
                block.hideInputs();
            }

            // make the conditions
            NodeList conditions = library.getElementsByTagName("condition");
            for (int i = 0; i < conditions.getLength(); i++) {
                Node condition = conditions.item(i);
                block = new ConditionBlock(condition.getAttributes().getNamedItem("name").getTextContent(),
                        condition.getAttributes().getNamedItem("traits").getTextContent());

                seekAndAttachInfo(condition);
                deltaTickTab.getLibraryHolder().addToConditionBlocksList((ConditionBlock) block);
            }

            NodeList traits = library.getElementsByTagName("trait");
            for (int i = 0; i < traits.getLength(); i++) {
                Node trait = traits.item(i);
                seekAndAttachInfo( trait );
            }

            //makes patch blocks appear in library panel -A. (sept 13)
            NodeList patches = library.getElementsByTagName("patch");
            for (int i = 0; i < patches.getLength(); i++) {
                Node patch = patches.item(i);
                block = new PatchBlock(patch.getAttributes().getNamedItem("name").getTextContent());
                seekAndAttachInfo(patch);
            }

            // make the quantities
            NodeList quantities = library.getElementsByTagName("quantity");
            for (int i = 0; i < quantities.getLength(); i++) {
                Node quantity = quantities.item(i);

                boolean histo = false;
                boolean isTrait = false;
                String bars = "0";
                String trait = " ";
                boolean isRunResult = false;
                boolean needsTrait = false;

                String xLabel = new String(quantity.getAttributes().getNamedItem("xlabel").getTextContent());
                String yLabel = new String(quantity.getAttributes().getNamedItem("ylabel").getTextContent());

                if (quantity.getAttributes().getNamedItem("histo").getTextContent().contains("true")) {
                    histo = true;
                    bars = quantity.getAttributes().getNamedItem("bars").getTextContent();
                }
                if (quantity.getAttributes().getNamedItem("runresult") != null) {
                    isRunResult = quantity.getAttributes().getNamedItem("runresult").getTextContent().equalsIgnoreCase("true");

                }
                if (quantity.getAttributes().getNamedItem("istrait").getTextContent().contains("true")) {
                    isTrait = true;
                }


                block = new QuantityBlock(quantity.getAttributes().getNamedItem("name").getTextContent(), histo, bars, trait, xLabel, yLabel, isTrait);
                seekAndAttachInfo(quantity);
                ((QuantityBlock) block).addColorButton();
                ((QuantityBlock) block).setRunResult(isRunResult);
                deltaTickTab.getLibraryHolder().addToQuantityBlocksMap((QuantityBlock) block);
            }

            NodeList breeds = library.getElementsByTagName("breed");
            NodeList globals = library.getElementsByTagName("global");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String reIntroduceLtGt(String input) {
        String output = "";

        output = input.replace("&lt;", "<");
        output = output.replace("&gt;", ">");

        return output;
    }

    // ask salil how this works - A.
    // add patch for this to show up in code
    public void seekAndAttachInfo(Node infoNode) {
        NodeList behaviorInfo = infoNode.getChildNodes();
        for (int j = 0; j < behaviorInfo.getLength(); j++) {
            if (behaviorInfo.item(j).getNodeName() == "commands") {
                //NodeList childNodes = behaviorInfo.item(j).getChildNodes();
                block.setCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));

            } else if (behaviorInfo.item(j).getNodeName() == "test") {
                block.setCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));
            } else if (behaviorInfo.item(j).getNodeName() == "reporter") {
                block.setCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));
            } else if (behaviorInfo.item(j).getNodeName() == "commandsPatch") {
                block.setCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));
            } else if (behaviorInfo.item(j).getNodeName() == "input") {
                //addInput takes 2 parameters: String inputName & default value
                block.addInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
            }
            else if (behaviorInfo.item(j).getNodeName() == "if-condition") {
                block.setIfCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));

            }

            //TODO: Figure out how setCode is fine for ENEGRYINPUT or should I switch to addInput
            else if (behaviorInfo.item(j).getNodeName() == "energyInput") {
                //block.setCode( behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
                block.addInputEnergy( behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());

            }

            else if (behaviorInfo.item(j).getNodeName() == "behaviorInput") {
                String name = behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent();
                String defaultValue = behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent();
                String tooltip = behaviorInfo.item(j).getAttributes().getNamedItem("tooltip").getTextContent();
                block.addBehaviorInput(name, defaultValue, tooltip);
            }
            else if (behaviorInfo.item(j).getNodeName() == "distanceInput") {
                block.addDistanceInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
            }
            else if (behaviorInfo.item(j).getNodeName() == "agentInput") {
                String name = behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent();
                String defaultValue = behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent();
                String tooltip = behaviorInfo.item(j).getAttributes().getNamedItem("tooltip").getTextContent();
                block.addAgentInput(name, defaultValue, tooltip);
            }
            else if (behaviorInfo.item(j).getNodeName() == "percentInput") {
                block.addPercentInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
            }
            else if (behaviorInfo.item(j).getNodeName() == "tooltip") {
                block.setToolTipText("<html><font size=\"3.5\">" + behaviorInfo.item(j).getTextContent() + "</font></html>");
            }
        }

        block.disableInputs();
        deltaTickTab.getLibraryHolder().addBlock( block );
        deltaTickTab.addDragSource(block);

        // the line above is what makes the blocks drag-able (Feb 14, 2012)
    }

    public String getFileName() {
        return fileName;
    }
}
