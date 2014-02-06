package org.nlogo.deltatick.xml;

//import com.sun.tools.javac.tree.Pretty;
import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.*;
import org.nlogo.deltatick.dnd.BehaviorDropTarget;
import org.nlogo.deltatick.dnd.PlantedCodeBlockDragSource;
import org.nlogo.deltatick.dnd.PrettyInput;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/31/13
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeltaTickModelReader {
    Frame myFrame;
    DeltaTickTab deltaTickTab;
    FileDialog fileLoader;
    String fileName;

    public DeltaTickModelReader(Frame frame, DeltaTickTab deltaTickTab) {
        this.deltaTickTab = deltaTickTab;
        this.myFrame = frame;
        //fileLoader = new FileDialog(frame);
        //fileLoader.setVisible(true);
        //File file = new File(fileLoader.getDirectory() + fileLoader.getFile());
        //fileName = new String (fileLoader.getDirectory() + fileLoader.getFile());


    }

    // myBreedBlock is the associated breed block for this behavior block
    // codeBlock is the block that physically contains the behavior block -- can be breed block or condition block
    public void makeAttachBehaviorBlock(BreedBlock myBreedBlock, CodeBlock codeBlock, Node node)
                throws IOException, UnsupportedFlavorException {
        String behavior = node.getAttributes().getNamedItem("name").getTextContent();
        for (BehaviorBlock behaviorBlock : deltaTickTab.getLibraryHolder().getBehaviorBlocksList()) {
            if (behaviorBlock.getName().equalsIgnoreCase(behavior)) {
                Object o = DeepCopyStream.deepClone(behaviorBlock.getTransferData(CodeBlock.codeBlockFlavor));

                // addCodeBlock stuff from DropTarget for codeBlock (physical container)
                codeBlock.addBlock((BehaviorBlock) o);
                new PlantedCodeBlockDragSource((BehaviorBlock) o);
                codeBlock.doLayout();
                codeBlock.validate();
                codeBlock.repaint();
                ((BehaviorBlock) o).enableInputs();
                ((BehaviorBlock) o).showInputs();

                //TODO: Get ismutate code from BreedDropTarget
                if (((BehaviorBlock) o).getIsMutate() == true) {
                    //((BreedBlock) myBreedBlock).setReproduceUsed(true);
                    myBreedBlock.setReproduceUsed(true);
                }
                new BehaviorDropTarget((BehaviorBlock) o);
                // Each behavior block MUST know its breed block
                ((BehaviorBlock) o).setMyBreedBlock(myBreedBlock);

                NodeList behaviorChildNodes = node.getChildNodes();
                for (int m = 0; m < behaviorChildNodes.getLength(); m++) {
                    if (behaviorChildNodes.item(m).getNodeName().equalsIgnoreCase("agentInput")) {
                        String inputName = behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent();
                        // Set corresponding agent input's value
                        ((BehaviorBlock) o).getAgentInput(inputName).setText(defaultValue);
                    }
                    if (behaviorChildNodes.item(m).getNodeName().equalsIgnoreCase("percentInput")) {
                        String inputName = behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent();
                        // Set corresponding percent input's value
                        ((BehaviorBlock) o).getPercentInput(inputName).setText(defaultValue);
                    }
                    if (behaviorChildNodes.item(m).getNodeName().equalsIgnoreCase("energyInput")) {
                        ((BehaviorBlock) o).addInputEnergy(behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent(),
                                behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent());
                    }
                    if (behaviorChildNodes.item(m).getNodeName().equalsIgnoreCase("behaviorInput")) {
                       String inputName = behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent();
                        // Set corresponding behavior input's value
                        ((BehaviorBlock) o).getBehaviorInput(inputName).setText(defaultValue);
                    }
                }

                // Check if this behavior block has a trait associated with it
                String isTrait = node.getAttributes().getNamedItem("isTrait").getTextContent();
                if (isTrait.equalsIgnoreCase("true")) {
                    // There is a trait associated with this behavior block
                    String traitName = node.getAttributes().getNamedItem("traitName").getTextContent();
                    String traitBreedName = node.getAttributes().getNamedItem("traitBreedName").getTextContent();
                    // Get the traitblock and put it here
                    TraitBlockNew traitBlock = deltaTickTab.getBuildPanel().getMyTrait(traitName, traitBreedName);
                    // Make a copy of the trait block
                    TraitBlockNew t = (TraitBlockNew) DeepCopyStream.deepClone(traitBlock.getTransferData(CodeBlock.codeBlockFlavor));
                    // Attach the trait block to this behavior block
                    // Perform ncessary add/validate functions (see DropTarget::addCodeBlock() )
                    ((BehaviorBlock) o).addBlock(t);
                    new PlantedCodeBlockDragSource(t);
                    ((BehaviorBlock) o).doLayout();
                    ((BehaviorBlock) o).validate();
                    ((BehaviorBlock) o).repaint();
                    ((BehaviorBlock) o).enableInputs();
                    // Other stuff related to the newly created traitblock
                    // see BehaviorDropTarget
                    t.setMyParent(((BehaviorBlock) o).getMyBreedBlock());
                    t.hideRemoveButton();
                    ((BehaviorBlock) o).removeBehaviorInput();
                    ((BehaviorBlock) o).setTrait(t.getTraitName(), t.getTraitOffsetVarName());
                    ((BehaviorBlock) o).getMyBreedBlock().addBlock(t);

                }


                ((BehaviorBlock) o).validate();
                ((BehaviorBlock) o).repaint();
            }
        }

    }

    // myBreedBlock is the associated breed block for this condition block
    // codeBlock is the block that physically contains the condition block -- can be breed block or condition block
    public void makeAttachConditionBlock(BreedBlock myBreedBlock, CodeBlock codeBlock, Node node)
            throws IOException, UnsupportedFlavorException {
        // Name of condition block
        String condition = node.getAttributes().getNamedItem("name").getTextContent();
        for (ConditionBlock conditionBlock : deltaTickTab.getLibraryHolder().getConditionBlocksList()) {
            if (conditionBlock.getName().equalsIgnoreCase(condition)) {
                // Found matching condition block
                Object o = DeepCopyStream.deepClone(conditionBlock.getTransferData(CodeBlock.codeBlockFlavor));
                // addCodeBlock stuff from DropTarget for codeBlock (physical container)
                codeBlock.addBlock((ConditionBlock) o);
                new PlantedCodeBlockDragSource((ConditionBlock) o);
                codeBlock.doLayout();
                codeBlock.validate();
                codeBlock.repaint();
                ((ConditionBlock) o).enableInputs();
                ((ConditionBlock) o).showInputs();

                // Make a drop target
                deltaTickTab.addCondition((ConditionBlock) o);

                // Process the inputs of the condition block
                NodeList conditionChildNodes = node.getChildNodes();
                for (int n = 0; n < conditionChildNodes.getLength(); n++) {
                    if (conditionChildNodes.item(n).getNodeName().equalsIgnoreCase("input")) {
                        String inputName = conditionChildNodes.item(n).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = conditionChildNodes.item(n).getAttributes().getNamedItem("default").getTextContent();
                        ((ConditionBlock) o).getInput(inputName).setText(defaultValue);
                    }
                    else if (conditionChildNodes.item(n).getNodeName().equalsIgnoreCase("agentInput")) {
                        String inputName = conditionChildNodes.item(n).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = conditionChildNodes.item(n).getAttributes().getNamedItem("default").getTextContent();
                        ((ConditionBlock) o).getAgentInput(inputName).setText(defaultValue);
                    }
                    // PROCESS BEHAVIOR BLOCKS AFTER TRAITS ARE PROCESSED FOR THE CONDITION BLOCK
                }

                // Check if this codnition block has a trait associated with it
                String isTrait = node.getAttributes().getNamedItem("isTrait").getTextContent();
                if (isTrait.equalsIgnoreCase("true")) {
                    // There is a trait associated with this condition block
                    String traitName = node.getAttributes().getNamedItem("traitName").getTextContent();
                    String traitBreedName = node.getAttributes().getNamedItem("traitBreedName").getTextContent();
                    // Get the traitblock and put it here
                    TraitBlockNew traitBlock = deltaTickTab.getBuildPanel().getMyTrait(traitName, traitBreedName);
                    // Make a copy of the trait block
                    TraitBlockNew t = (TraitBlockNew) DeepCopyStream.deepClone(traitBlock.getTransferData(CodeBlock.codeBlockFlavor));
                    // Attach the trait block to this condition block
                    // Perform ncessary add/validate functions (see DropTarget::addCodeBlock() )
                    ((ConditionBlock) o).addBlock(t);
                    new PlantedCodeBlockDragSource(t);
                    ((ConditionBlock) o).doLayout();
                    ((ConditionBlock) o).validate();
                    ((ConditionBlock) o).repaint();
                    ((ConditionBlock) o).enableInputs();
                    // Other stuff related to the newly created traitblock
                    // see ConditionDropTarget
                    t.hideRemoveButton();
                    ((ConditionBlock) o).setTrait(traitName);
                    ((ConditionBlock) o).removeBehaviorInput();
                }

                // Now process child blocks
                for (int n = 0; n < conditionChildNodes.getLength(); n++) {
                    if (conditionChildNodes.item(n).getNodeName().equalsIgnoreCase("behaviorBlock")) {
                        makeAttachBehaviorBlock(myBreedBlock, (ConditionBlock) o, conditionChildNodes.item(n));
                    }
                    else if (conditionChildNodes.item(n).getNodeName().equalsIgnoreCase("conditionBlock")) {
                        makeAttachConditionBlock(myBreedBlock, (ConditionBlock) o, conditionChildNodes.item(n));
                    }
                }
                    // Processed the condition block. No need to further iterate through the loop.
                break;
            }
        }
    }

    // myPlotBlock is the associated plot block for this quantity block
    public void makeAttachQuantityBlock(PlotBlock plotBlock, Node quantityBlockNode)
            throws IOException, UnsupportedFlavorException {

        // Read name of the quantity block
        String quantityBlockName = quantityBlockNode.getAttributes().getNamedItem("name").getTextContent();
        QuantityBlock qBlockFromLib = deltaTickTab.getLibraryHolder().getQuantityBlocksMap().get(quantityBlockName);
        // Create copy of the quantity block
        Object o = DeepCopyStream.deepClone(qBlockFromLib.getTransferData(CodeBlock.quantityBlockFlavor));
        QuantityBlock quantityBlock = (QuantityBlock) o;

        // "Drop" this new quantity block in the plot block
        // From DropTarget
        plotBlock.addBlock(quantityBlock);
        new PlantedCodeBlockDragSource(quantityBlock);
        plotBlock.removeQuantityblockPanel();
        plotBlock.doLayout();
        plotBlock.validate();
        plotBlock.repaint();
        plotBlock.enableInputs();

        // Now iterate over inputs of quantity blocks and set the values accordingly
        NodeList quantityBlockChildNodes = quantityBlockNode.getChildNodes();
        for (int k = 0; k < quantityBlockChildNodes.getLength(); k++) {
            if (quantityBlockChildNodes.item(k).getNodeName().equalsIgnoreCase("input")) {
                Node inputNode = quantityBlockChildNodes.item(k);
                // Read the input name and value from XML
                String inputName = inputNode.getAttributes().getNamedItem("name").getTextContent();
                String inputValue = inputNode.getAttributes().getNamedItem("default").getTextContent();

                // Set the corresponding value in the quantity block
                quantityBlock.getInput(inputName).setText(inputValue);
            }
        }

        // Check if this behavior block has a trait associated with it
        String isTrait = quantityBlockNode.getAttributes().getNamedItem("isTrait").getTextContent();
        if (isTrait.equalsIgnoreCase("true")) {
            // There is a trait associated with this behavior block
            String traitName = quantityBlockNode.getAttributes().getNamedItem("traitName").getTextContent();
            String traitBreedName = quantityBlockNode.getAttributes().getNamedItem("traitBreedName").getTextContent();
            // Get the traitblock and put it here
            TraitBlockNew traitBlock = deltaTickTab.getBuildPanel().getMyTrait(traitName, traitBreedName);
            // Make a copy of the trait block
            TraitBlockNew t = (TraitBlockNew) DeepCopyStream.deepClone(traitBlock.getTransferData(CodeBlock.codeBlockFlavor));
            // Attach the trait block to this behavior block
            // Perform ncessary add/validate functions (see DropTarget::addCodeBlock() )
            ((QuantityBlock) o).addBlock(t);
            new PlantedCodeBlockDragSource(t);
            ((QuantityBlock) o).doLayout();
            ((QuantityBlock) o).validate();
            ((QuantityBlock) o).repaint();
            ((QuantityBlock) o).enableInputs();
            // Other stuff related to the newly created traitblock
            // see QuantityDropTarget
            t.setMyParent((QuantityBlock) o);
            t.hideRemoveButton();
            ((QuantityBlock) o).setTrait(t);
            ((QuantityBlock) o).removeInput();
            ((QuantityBlock) o).removeTraitblockPanel();
        }

        // Refresh block
        ((QuantityBlock) o).validate();
        ((QuantityBlock) o).repaint();
    }

    public void makeAttachBehaviorBlock(DiveInBlock diveInBlock, Node node)
            throws IOException, UnsupportedFlavorException {
        String behavior = node.getAttributes().getNamedItem("name").getTextContent();
        for (BehaviorBlock behaviorBlock : deltaTickTab.getLibraryHolder().getBehaviorBlocksList()) {
            if (behaviorBlock.getName().equalsIgnoreCase(behavior)) {
                Object o = DeepCopyStream.deepClone(behaviorBlock.getTransferData(CodeBlock.codeBlockFlavor));

                // addCodeBlock stuff from DropTarget for codeBlock (physical container)
                diveInBlock.addBlock((BehaviorBlock) o);
                new PlantedCodeBlockDragSource((BehaviorBlock) o);
                diveInBlock.doLayout();
                diveInBlock.validate();
                diveInBlock.repaint();
                ((BehaviorBlock) o).enableInputs();
                ((BehaviorBlock) o).showInputs();

                NodeList behaviorChildNodes = node.getChildNodes();
                for (int m = 0; m < behaviorChildNodes.getLength(); m++) {
                    if (behaviorChildNodes.item(m).getNodeName().equalsIgnoreCase("agentInput")) {
                        String inputName = behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent();
                        // Set corresponding agent input's value
                        ((BehaviorBlock) o).getAgentInput(inputName).setText(defaultValue);
                    }
                    if (behaviorChildNodes.item(m).getNodeName().equalsIgnoreCase("percentInput")) {
                        String inputName = behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent();
                        // Set corresponding percent input's value
                        ((BehaviorBlock) o).getPercentInput(inputName).setText(defaultValue);
                    }
                    if (behaviorChildNodes.item(m).getNodeName().equalsIgnoreCase("energyInput")) {
                        ((BehaviorBlock) o).addInputEnergy(behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent(),
                                behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent());
                    }
                    if (behaviorChildNodes.item(m).getNodeName().equalsIgnoreCase("behaviorInput")) {
                        String inputName = behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent();
                        // Set corresponding behavior input's value
                        ((BehaviorBlock) o).getBehaviorInput(inputName).setText(defaultValue);
                    }
                }

                // There cannot be a trait associated with this behaviro block
                // because it is within a divein blocks

                ((BehaviorBlock) o).validate();
                ((BehaviorBlock) o).repaint();
            }
        }
    }

    // Opens a model file
    public void openModel(File modelFile) {
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document model = builder.parse(modelFile);

            // Open the library
            NodeList usedLibraries = model.getElementsByTagName("usedLibrary");
            for (int i = 0; i < usedLibraries.getLength(); i++) {
                Node usedLibrary = usedLibraries.item(i);
                String path = new String(usedLibrary.getAttributes().getNamedItem("path").getTextContent());
                deltaTickTab.openLibrary(path);
            }

            NodeList breedBlockNodes = model.getElementsByTagName("breedBlock");
            for (int i = 0; i < breedBlockNodes.getLength(); i++) {
                Node breedBlockNode = breedBlockNodes.item(i);
                String plural = breedBlockNode.getAttributes().getNamedItem("plural").getTextContent();
                String number = breedBlockNode.getAttributes().getNamedItem("number").getTextContent();
                String shape = breedBlockNode.getAttributes().getNamedItem("shape").getTextContent();
                String color = breedBlockNode.getAttributes().getNamedItem("color").getTextContent();
                String xcor = breedBlockNode.getAttributes().getNamedItem("xcor").getTextContent();
                String ycor = breedBlockNode.getAttributes().getNamedItem("ycor").getTextContent();
                String maxAge = new String();
                String maxEnergy = new String();
                String traitName = new String("");
                ArrayList<String> selectedLabels = new ArrayList<String>();
                HashMap<String, TraitState> selectedTraitStateMap = new HashMap<String, TraitState>();

                //BreedBlock bBlock = deltaTickTab.makeBreedBlock(plural, number);
                //CodeBlock bBlock = (BreedBlock) deltaTickTab.makeBreedBlock(plural, number);
                CodeBlock bBlock = deltaTickTab.makeBreedBlock(plural, number);
                // Set the X,Y location
                bBlock.setLocation(Integer.parseInt(xcor), Integer.parseInt(ycor));
                bBlock.repaint();
                // Create the species editor
                SpeciesEditorPanel speciesEditorPanel = deltaTickTab.createSpeciesEditorPanel();
                speciesEditorPanel.getMyFrame().setTitle("Species Editor: " + plural);
                // Set breed name
                speciesEditorPanel.setSelectedBreed(plural);
                // Set setup number
                speciesEditorPanel.setSetupNumber(number);
                // Set shape
                speciesEditorPanel.setBreedShape(shape);
                // Set color
                speciesEditorPanel.setBreedColor((int) Long.parseLong(color, 16));
                // Other setup
                speciesEditorPanel.setMakeNewBreedBlock(false);
                speciesEditorPanel.setMyBreedBlock((BreedBlock) bBlock);
                speciesEditorPanel.removeBreedNameComboBox();
                // Add this species editor panel to deltaticktab's hashmap
                deltaTickTab.putSpeciesEditorPanelInHashMap(plural, speciesEditorPanel);
                // Update breedblock
                ((BreedBlock) bBlock).setBreedShape(shape);
                ((BreedBlock) bBlock).setInspectSpeciesButtonShapeColor(speciesEditorPanel.getMyBreedVectorShape(), speciesEditorPanel.getMyBreedColor());
                ((BreedBlock) bBlock).setColorName(speciesEditorPanel.getMyBreedColorName());
                ((BreedBlock) bBlock).setColorRGB(speciesEditorPanel.getMyBreedColorRGB());
                ((BreedBlock) bBlock).setSetupNumber(number);

                // BreedBlock now created, enable TrackSpeciesButton
                deltaTickTab.getAddTrackSpecies().setEnabled(true);

                NodeList breedBlockChildNodes = breedBlockNode.getChildNodes();

                // Process Ownvar childnodes for age & energy
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    if (breedBlockChildNodes.item(j).getNodeName().equalsIgnoreCase("ownVar")) {
                        Node ownVar = breedBlockChildNodes.item(j);
                        if (ownVar.getAttributes().getNamedItem("name").getTextContent().equalsIgnoreCase("age")) {
                            maxAge = ownVar.getAttributes().getNamedItem("maxReporter").getTextContent();
                            ((BreedBlock) bBlock).setMaxAge(maxAge);
                        }
                        if (ownVar.getAttributes().getNamedItem("name").getTextContent().equalsIgnoreCase("energy")) {
                            maxEnergy = ownVar.getAttributes().getNamedItem("maxReporter").getTextContent();
                            ((BreedBlock) bBlock).setMaxEnergy(maxEnergy);
                        }
                    }
                }
                // Process selectedLabels
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    // Check if any labels/traitlabels had been selected
                    if (breedBlockChildNodes.item(j).getNodeName().equalsIgnoreCase("selectedLabel")) {
                        Node labelNode = breedBlockChildNodes.item(j);
                        String labelName = labelNode.getAttributes().getNamedItem("name").getTextContent();
                        selectedLabels.add(labelName);
                    }
                }

                // Process TRAIT nodes. These MUST be processed before any behavior blocks are processed
                // Behavior blocks assume that all trait blocks have been created
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    //TraitChildNodes for trait of breeds
                    if (breedBlockChildNodes.item(j).getNodeName().equalsIgnoreCase("trait")) {
                        Node trait = breedBlockChildNodes.item(j);
                        traitName = new String(trait.getAttributes().getNamedItem("name").getTextContent()); //traitname
                        NodeList variationNodes = trait.getChildNodes();
                        HashMap<String, String> selectedVariationsPercent = new HashMap<String, String>();
                        HashMap<String, Variation> selectedVariationsHashMap = new HashMap<String, Variation>();


                        for (Trait libTrait : deltaTickTab.getBuildPanel().getBgInfo().getTraits()) {
                            if (traitName.equalsIgnoreCase(libTrait.getNameTrait())) {
                                for (int k = 0; k < variationNodes.getLength(); k++) {
                                    if (variationNodes.item(k).getNodeName().equalsIgnoreCase("variation")) {
                                        Node variationNode = variationNodes.item(k);
                                        String varName = variationNode.getAttributes().getNamedItem("name").getTextContent();
                                        String varValue = new String(libTrait.getVariationHashMap().get(varName).value);
                                        String varColor = new String(libTrait.getVariationHashMap().get(varName).color);
                                        String percentage = variationNode.getAttributes().getNamedItem("percent").getTextContent();
                                        int percent = Integer.parseInt(percentage);
                                        Variation variation = new Variation(traitName, varName, varValue, varColor, percent);
                                        selectedVariationsHashMap.put(varName, variation);
                                        selectedVariationsPercent.put(varName, percentage);
                                    }
                                }
                                TraitState traitState = new TraitState(libTrait, selectedVariationsPercent);
                                traitState.getVariationHashMap().clear();
                                traitState.getVariationHashMap().putAll(selectedVariationsHashMap);
                                selectedTraitStateMap.put(traitName, traitState);
                            }
                        }
                    }
                }

                // Now create the traitblocks. These MUST be made prior to processing behavior blocks/nodes
                for (TraitState traitState : selectedTraitStateMap.values()) {
                    deltaTickTab.makeTraitBlock(((BreedBlock) bBlock), traitState);
                }

                // Now process behavior block nodes. All trait nodes MUST be processed and traitBlocks MADE prior to this.
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    //behaviorBlocks as childNodes for a breedBlock
                    if (breedBlockChildNodes.item(j).getNodeName().equalsIgnoreCase("behaviorBlock")) {
                        makeAttachBehaviorBlock((BreedBlock) bBlock, bBlock, breedBlockChildNodes.item(j));
                    }
                    else if (breedBlockChildNodes.item(j).getNodeName().equalsIgnoreCase("conditionBlock")) {
                        makeAttachConditionBlock((BreedBlock) bBlock, bBlock, breedBlockChildNodes.item(j));
                    }
                }

                // Processing of breedchild nodes of this breedblock done
                // Set labels/traitLabels for that BreedBlock
                ((BreedBlock) bBlock).setTraitLabels(selectedLabels);

                // Udate species inspector panel
                //SpeciesEditorPanel speciesEditorPanel = deltaTickTab.getSpeciesEditorPanel(((BreedBlock) bBlock).plural());
                speciesEditorPanel.getTraitPreview().setSelectedTraitsMap(selectedTraitStateMap);
                speciesEditorPanel.getTraitPreview().saveOrigSelectedTraitsMap();
                speciesEditorPanel.getTraitPreview().setSelectedTrait(traitName);
                speciesEditorPanel.updateTraitDisplay();
                speciesEditorPanel.getTraitPreview().updateCheckBoxes(selectedTraitStateMap);
                // Set the checkboxes in speciesInspectorPanel.labelPanel to true
                speciesEditorPanel.getTraitPreview().getLabelPanel().setSelectedLabels(selectedLabels);
            }

            // Now process Plot Blocks
            NodeList plotBlockNodeList = model.getElementsByTagName("plotBlock");
            for (int i = 0; i < plotBlockNodeList.getLength(); i++) {
                Node plotBlockNode = plotBlockNodeList.item(i);
                String plotName = plotBlockNode.getAttributes().getNamedItem("name").getTextContent();
                boolean isHisto = plotBlockNode.getAttributes().getNamedItem("isHisto").getTextContent().equalsIgnoreCase("true");
                String xcor = plotBlockNode.getAttributes().getNamedItem("xcor").getTextContent();
                String ycor = plotBlockNode.getAttributes().getNamedItem("ycor").getTextContent();

                // Make the plot block
                PlotBlock plotBlock = deltaTickTab.makePlotBlock(isHisto);
                plotBlock.setPlotName(plotName);
                // Set the X,Y location
                plotBlock.setLocation(Integer.parseInt(xcor), Integer.parseInt(ycor));

                // Iterate over Quantity Blocks
                NodeList plotChildNodes = plotBlockNode.getChildNodes();
                for (int j = 0; j < plotChildNodes.getLength(); j++) {
                    if (plotChildNodes.item(j).getNodeName().equalsIgnoreCase("quantityBlock")) {
                        Node quantityBlockNode = plotChildNodes.item(j);
                        makeAttachQuantityBlock(plotBlock, quantityBlockNode);
                    }
                }
            }
            //// Plot block processing complete

            // Process DiveIn blocks
            NodeList diveInBlockNodes = model.getElementsByTagName("diveInBlock");
            for (int i = 0; i < diveInBlockNodes.getLength(); i++) {
                Node diveInBlockNode = diveInBlockNodes.item(i);
                // Create the DiveIn Block
                DiveInBlock diveInBlock = deltaTickTab.makeDiveInBlock();
                // Process child behavior blocks
                NodeList diveInChildNodes = diveInBlockNode.getChildNodes();
                for (int j = 0; j < diveInChildNodes.getLength(); j++) {
                    if (diveInChildNodes.item(j).getNodeName().equalsIgnoreCase("behaviorBlock")) {
                        makeAttachBehaviorBlock(diveInBlock, diveInChildNodes.item(j));
                    }
                }
            }
            // DiveIn blocks processing complete

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveModel(File modelFile) {
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            // Create the root element
            Element rootElement = doc.createElement("model");
            doc.appendChild(rootElement);

            // Create usedLibrary element
            Element usedLibrary = doc.createElement("usedLibrary");
            rootElement.appendChild(usedLibrary);
            // Create attributes
            usedLibrary.setAttribute("path", deltaTickTab.getLibraryReader().getFileName());

            // Create breedBlock element
            for (BreedBlock bBlock : deltaTickTab.getBuildPanel().getMyBreeds()) {
                Element breedBlockElement = doc.createElement("breedBlock");
                rootElement.appendChild(breedBlockElement);
                breedBlockElement.setAttribute("singular", bBlock.singular());
                breedBlockElement.setAttribute("plural", bBlock.plural());
                breedBlockElement.setAttribute("number", bBlock.getNumber());
                breedBlockElement.setAttribute("shape", bBlock.getBreedShape());
                breedBlockElement.setAttribute("color", Integer.toHexString(bBlock.getColorRGB()));
                breedBlockElement.setAttribute("xcor", Integer.toString(bBlock.getX()));
                breedBlockElement.setAttribute("ycor", Integer.toString(bBlock.getY()));

                // Save labels (traitLabels)
                for (String traitLabel : bBlock.getTraitLabels()) {
                    Element labelElement = doc.createElement("selectedLabel");
                    labelElement.setAttribute("name", traitLabel);
                    // Add label element to breedblock
                    breedBlockElement.appendChild(labelElement);
                }

                // Save traits associated with this breed/breedblock
                for (TraitBlockNew traitBlockNew : bBlock.getMyTraitBlocks()) {
                    Element traitElement = doc.createElement("trait");
                    traitElement.setAttribute("name", traitBlockNew.getTraitName());
                    for (Variation variation : traitBlockNew.getVariationHashMap().values()) {
                        Element variationElement = doc.createElement("variation");
                        variationElement.setAttribute("name", variation.name);
                        variationElement.setAttribute("percent", Integer.toString(variation.percent));
                        // Add variationElement to traitElement
                        traitElement.appendChild(variationElement);
                    }
                    // Add the trait to BreedBlock
                    breedBlockElement.appendChild(traitElement);
                }

                for (CodeBlock codeBlock : bBlock.getMyBlocks()) {
                    // Process behavior block (child of breed block)
                    if (codeBlock instanceof BehaviorBlock) {
                        Element behaviorBlock = makeElementFromBehaviorBlock(doc, (BehaviorBlock) codeBlock);
                        breedBlockElement.appendChild(behaviorBlock);
                    }

                    if (codeBlock instanceof ConditionBlock) {
                        Element conditionBlock = makeElementFromConditionBlock(doc, (ConditionBlock) codeBlock);
                        breedBlockElement.appendChild(conditionBlock);
                    }
                }
            }
            // Processing of breed blocks done

            // Now process and save plot blocks
            for (PlotBlock plotBlock : deltaTickTab.getBuildPanel().getMyPlots()) {
                // Create the plot block element
                Element plotBlockElement = doc.createElement("plotBlock");
                // Set its attributes
                plotBlockElement.setAttribute("name", plotBlock.getPlotName());
                plotBlockElement.setAttribute("isHisto", plotBlock.isHistogram()?"true":"false");
                plotBlockElement.setAttribute("xcor", Integer.toString(plotBlock.getX()));
                plotBlockElement.setAttribute("ycor", Integer.toString(plotBlock.getY()));
                // Append to root element (<model>)
                rootElement.appendChild(plotBlockElement);

                // Iterate over quantity blocks
                for (QuantityBlock quantityBlock : plotBlock.getMyBlocks()) {
                    // Create element
                    Element quantityBlockElement = doc.createElement("quantityBlock");
                    // Set Attributes
                    quantityBlockElement.setAttribute("name", quantityBlock.getName());
                    // If this block has a trait associated, save that as well
                    String isTrait = quantityBlock.getIsTrait() ? "true" : "false";
                    quantityBlockElement.setAttribute("isTrait", isTrait);
                    if (quantityBlock.getIsTrait()) {
                        String traitName = quantityBlock.getTrait();
                        String traitBreedName = quantityBlock.getTraitBreed();
                        quantityBlockElement.setAttribute("traitName", traitName);
                        quantityBlockElement.setAttribute("traitBreedName", traitBreedName);
                    }
                    // Append to plotblock
                    plotBlockElement.appendChild(quantityBlockElement);

                    // Process inputs of the quantity block
                    for (Map.Entry<String, PrettyInput> entry : quantityBlock.getInputs().entrySet()) {
                        Element inputElement = doc.createElement("input");
                        inputElement.setAttribute("name", entry.getKey());
                        inputElement.setAttribute("default", entry.getValue().getText());
                        quantityBlockElement.appendChild(inputElement);
                    }
                }
            }
            // Processing of plot blocks done

            // Process DiveIn Blocks
            for (DiveInBlock diveInBlock : deltaTickTab.getBuildPanel().getMyDiveIns()) {
                Element diveInBlockElement = doc.createElement("diveInBlock");
                rootElement.appendChild(diveInBlockElement);
                // DiveIn has no attributes

                for (CodeBlock codeBlock : diveInBlock.getMyBlocks()) {
                    // Process behavior block (child of breed block)
                    if (codeBlock instanceof BehaviorBlock) {
                        Element behaviorBlock = makeElementFromBehaviorBlock(doc, (BehaviorBlock) codeBlock);
                        diveInBlockElement.appendChild(behaviorBlock);
                    }
                }
            }
            // Processing of DiveIn blocks done

            //// Convert DOM to XML ////
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            // Produce indented output XML
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = null;

            // Append extension .xml if necessary
            String modelFileName = modelFile.getAbsolutePath();
            String ext = null;
            String s = modelFile.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            if (ext == null) {
                modelFileName = modelFileName + ".xml";
            }

            // Create the stream result
            result = new StreamResult(new File(modelFileName));

            // Set indentation
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // transform the DOM to XML
            transformer.transform(source, result);

            //System.out.println("Model file " + modelFile.getName() + " saved");

        } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  } catch (TransformerException tfe) {
		tfe.printStackTrace();
	  }
    }

    public Element makeElementFromConditionBlock(Document doc, ConditionBlock block) {
        Element conditionBlock = doc.createElement("conditionBlock");
        conditionBlock.setAttribute("name", block.getName());
        // Traits
        String isTrait = (block.getIsTrait()) ? "true" : "false";
        conditionBlock.setAttribute("isTrait", isTrait);
        if (block.getIsTrait()) {
            String traitName = block.getTraitName();
            String traitBreedName = block.getMyBreedBlock().plural();
            conditionBlock.setAttribute("traitName", traitName);
            conditionBlock.setAttribute("traitBreedName", traitBreedName);
        }
        // Process PrettyInputs (INPUTS)
        for (Map.Entry entry : block.getInputs().entrySet()) {
            Element inputElement = doc.createElement("input");
            inputElement.setAttribute("name", (String) entry.getKey());
            inputElement.setAttribute("default", ((PrettyInput) entry.getValue()).getText() );
            conditionBlock.appendChild(inputElement);
        }
        // Process PrettyInputs (BEHAVIOR INPUTS)
        for (Map.Entry entry : block.getBehaviorInputs().entrySet()) {
            Element inputElement = doc.createElement("behaviorInput");
            inputElement.setAttribute("name", (String) entry.getKey());
            inputElement.setAttribute("default", ((PrettyInput) entry.getValue()).getText() );
            conditionBlock.appendChild(inputElement);
        }
        // Process PrettyInputs (AGENT INPUTS)
        for (Map.Entry entry : block.getAgentInputs().entrySet()) {
            Element inputElement = doc.createElement("agentInput");
            inputElement.setAttribute("name", (String) entry.getKey());
            inputElement.setAttribute("default", ((PrettyInput) entry.getValue()).getText() );
            conditionBlock.appendChild(inputElement);
        }
        //mutate attribute

        // Process behavior block (child of condition block)
        for (CodeBlock cBlock : block.getMyBlocks()) {
            if (cBlock instanceof BehaviorBlock) {
                Element childBehaviorBlock = makeElementFromBehaviorBlock(doc, (BehaviorBlock) cBlock);
                conditionBlock.appendChild(childBehaviorBlock);
            }
            else if (cBlock instanceof ConditionBlock) {
                Element childCOnditionBlock = makeElementFromConditionBlock(doc, (ConditionBlock) cBlock);
                conditionBlock.appendChild(childCOnditionBlock);
            }
        }
        return conditionBlock;
    }
    public Element makeElementFromBehaviorBlock(Document doc, BehaviorBlock block) {

        Element behaviorBlock = doc.createElement("behaviorBlock");
        //breedBlock.appendChild(behaviorBlock);
        behaviorBlock.setAttribute("name", block.getName());
        //mutate attribute
        String isMutate = (block.getIsMutate())? "true" : "false";
        behaviorBlock.setAttribute("mutate", isMutate);
        // isTrait
        String isTrait = (block.getIsTrait()) ? "true" : "false";
        behaviorBlock.setAttribute("isTrait", isTrait);
        if (block.getIsTrait()) {
            String traitName = block.getTrait();
            String traitBreedName = block.getMyBreedBlock().plural();
            behaviorBlock.setAttribute("traitName", traitName);
            behaviorBlock.setAttribute("traitBreedName", traitBreedName);
        }
        // Uses Input reporter
        String usesInputReporter = (block.usesInputReporter())? "true" : "false";
        behaviorBlock.setAttribute("usesInputReporter", usesInputReporter);
        if (block.usesInputReporter()) {
            String inputReporter = block.getInputReporter();
            behaviorBlock.setAttribute("inputReporter", inputReporter);
        }

        // Process PrettyInputs (INPUTS)
        for (Map.Entry entry : block.getInputs().entrySet()) {
            Element inputElement = doc.createElement("input");
            inputElement.setAttribute("name", (String) entry.getKey());
            inputElement.setAttribute("default", ((PrettyInput) entry.getValue()).getText() );
            behaviorBlock.appendChild(inputElement);
        }
        // Process PrettyInputs (BEHAVIOR INPUTS)
        for (Map.Entry entry : block.getBehaviorInputs().entrySet()) {
            Element inputElement = doc.createElement("behaviorInput");
            inputElement.setAttribute("name", (String) entry.getKey());
            inputElement.setAttribute("default", ((PrettyInput) entry.getValue()).getText() );
            behaviorBlock.appendChild(inputElement);
        }
        // Process PrettyInputs (AGENT INPUTS)
        for (Map.Entry entry : block.getAgentInputs().entrySet()) {
            Element inputElement = doc.createElement("agentInput");
            inputElement.setAttribute("name", (String) entry.getKey());
            inputElement.setAttribute("default", ((PrettyInput) entry.getValue()).getText() );
            behaviorBlock.appendChild(inputElement);
        }
        // Process PrettyInputs (PERCENT INPUTS)
        for (Map.Entry entry : block.getPercentInputs().entrySet()) {
            Element inputElement = doc.createElement("percentInput");
            inputElement.setAttribute("name", (String) entry.getKey());
            inputElement.setAttribute("default", ((PrettyInput) entry.getValue()).getText() );
            behaviorBlock.appendChild(inputElement);
        }


        return behaviorBlock;
    }

}
