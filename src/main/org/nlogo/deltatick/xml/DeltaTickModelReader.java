package org.nlogo.deltatick.xml;

//import com.sun.tools.javac.tree.Pretty;
import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.*;
import org.nlogo.deltatick.dnd.AgentInput;
import org.nlogo.deltatick.dnd.BehaviorDropTarget;
import org.nlogo.deltatick.dnd.PlantedCodeBlockDragSource;
import org.nlogo.deltatick.dnd.PrettyInput;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.swing.*;
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
import java.awt.datatransfer.Transferable;
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
                    if (behaviorChildNodes.item(m).getNodeName() == "agentInput") {
                        String inputName = behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent();
                        // Set corresponding agent input's value
                        ((BehaviorBlock) o).getAgentInput(inputName).setText(defaultValue);
                    }
                    if (behaviorChildNodes.item(m).getNodeName() == "percentInput") {
                        String inputName = behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent();
                        String defaultValue = behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent();
                        // Set corresponding percent input's value
                        ((BehaviorBlock) o).getPercentInput(inputName).setText(defaultValue);
                    }
                    if (behaviorChildNodes.item(m).getNodeName() == "energyInput") {
                        ((BehaviorBlock) o).addInputEnergy(behaviorChildNodes.item(m).getAttributes().getNamedItem("name").getTextContent(),
                                behaviorChildNodes.item(m).getAttributes().getNamedItem("default").getTextContent());
                    }
                    if (behaviorChildNodes.item(m).getNodeName() == "behaviorInput") {
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
                    // Get the traitblock and put it here
                    TraitBlockNew traitBlock = deltaTickTab.getBuildPanel().getMyTrait(traitName);
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
                    ((BehaviorBlock) o).setTrait(t);
                    ((BehaviorBlock) o).getMyBreedBlock().addBlock(t);

                }


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
                String maxAge = new String();
                String maxEnergy = new String();
                String traitName = new String("");
                ArrayList<String> selectedLabels = new ArrayList<String>();
                HashMap<String, TraitState> selectedTraitStateMap = new HashMap<String, TraitState>();

                //BreedBlock bBlock = deltaTickTab.makeBreedBlock(plural, number);
                //CodeBlock bBlock = (BreedBlock) deltaTickTab.makeBreedBlock(plural, number);
                CodeBlock bBlock = deltaTickTab.makeBreedBlock(plural, number);

                NodeList breedBlockChildNodes = breedBlockNode.getChildNodes();

                // Process Ownvar childnodes for age & energy
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    if (breedBlockChildNodes.item(j).getNodeName() == "ownVar") {
                        Node ownVar = breedBlockChildNodes.item(j);
                        if (ownVar.getAttributes().getNamedItem("name").getTextContent().equals("age")) {
                            maxAge = ownVar.getAttributes().getNamedItem("maxReporter").getTextContent();
                            ((BreedBlock) bBlock).setMaxAge(maxAge);
                        }
                        if (ownVar.getAttributes().getNamedItem("name").getTextContent().equals("energy")) {
                            maxEnergy = ownVar.getAttributes().getNamedItem("maxReporter").getTextContent();
                            ((BreedBlock) bBlock).setMaxEnergy(maxEnergy);
                        }
                    }
                }
                // Process selectedLabels
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    // Check if any labels/traitlabels had been selected
                    if (breedBlockChildNodes.item(j).getNodeName() == "selectedLabel") {
                        Node labelNode = breedBlockChildNodes.item(j);
                        String labelName = labelNode.getAttributes().getNamedItem("name").getTextContent();
                        selectedLabels.add(labelName);
                    }
                }

                // Process TRAIT nodes. These MUST be processed before any behavior blocks are processed
                // Behavior blocks assume that all trait blocks have been created
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    //TraitChildNodes for trait of breeds
                    if (breedBlockChildNodes.item(j).getNodeName() == "trait") {
                        Node trait = breedBlockChildNodes.item(j);
                        traitName = new String(trait.getAttributes().getNamedItem("name").getTextContent()); //traitname
                        NodeList variationNodes = trait.getChildNodes();
                        HashMap<String, String> selectedVariationsPercent = new HashMap<String, String>();
                        HashMap<String, Variation> selectedVariationsHashMap = new HashMap<String, Variation>();


                        for (Trait newTrait : deltaTickTab.getBuildPanel().getBgInfo().getTraits()) {
                            if (traitName.equalsIgnoreCase(newTrait.getNameTrait())) {
                                for (int k = 0; k < variationNodes.getLength(); k++) {
                                    if (variationNodes.item(k).getNodeName() == "variation") {
                                        Node variationNode = variationNodes.item(k);
                                        String varName = variationNode.getAttributes().getNamedItem("name").getTextContent();
                                        String varValue = variationNode.getAttributes().getNamedItem("value").getTextContent();
                                        String percentage = variationNode.getAttributes().getNamedItem("percent").getTextContent();
                                        int percent = Integer.parseInt(percentage);
                                        Variation variation = new Variation(traitName, varName, varValue, percent);
                                        selectedVariationsHashMap.put(varName, variation);
                                        selectedVariationsPercent.put(varName, percentage);
                                    }
                                }
                                TraitState traitState = new TraitState(newTrait, selectedVariationsPercent);
                                traitState.getVariationHashMap().clear();
                                traitState.getVariationHashMap().putAll(selectedVariationsHashMap);
                                selectedTraitStateMap.put(traitName, traitState);
                            }
                        }

//                        // Udate species inspector panel
//                        SpeciesInspectorPanel speciesInspectorPanel = deltaTickTab.getSpeciesInspectorPanel(((BreedBlock) bBlock));
//                        speciesInspectorPanel.getTraitPreview().setSelectedTraitsMap(selectedTraitStateMap);
//                        speciesInspectorPanel.getTraitPreview().setSelectedTrait(traitName);
//                        speciesInspectorPanel.updateTraitDisplay();

//                        // Create the traitblock
//                        for (TraitState traitState : selectedTraitStateMap.values()) {
//                            deltaTickTab.makeTraitBlock(((BreedBlock) bBlock), traitState);
//                        }
                    }
                }

                // Now create the traitblocks. These MUST be made prior to processing behavior blocks/nodes
                for (TraitState traitState : selectedTraitStateMap.values()) {
                    deltaTickTab.makeTraitBlock(((BreedBlock) bBlock), traitState);
                }

                // Now process behavior block nodes. All trait nodes MUST be processed and traitBlocks MADE prior to this.
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    //behaviorBlocks as childNodes for a breedBlock
                    if (breedBlockChildNodes.item(j).getNodeName() == "behaviorBlock") {
                        makeAttachBehaviorBlock((BreedBlock) bBlock, bBlock, breedBlockChildNodes.item(j));
                    }
                }

                // Now process condition block nodes
                for (int j = 0; j < breedBlockChildNodes.getLength(); j++) {
                    //ConditionBlocks as childNodes for a breedBlock
                    if (breedBlockChildNodes.item(j).getNodeName() == "conditionBlock") {
                        String condition = breedBlockChildNodes.item(j).getAttributes().getNamedItem("name").getTextContent();
                        for (ConditionBlock conditionBlock : deltaTickTab.getLibraryHolder().getConditionBlocksList()) {
                            if (conditionBlock.getName().equalsIgnoreCase(condition)) {
                                Object o = conditionBlock.getTransferData(CodeBlock.codeBlockFlavor);
                                bBlock.addBlock((ConditionBlock) o);
                                NodeList childNodesCondition = breedBlockChildNodes.item(j).getChildNodes();
                                for (int n = 0; n < childNodesCondition.getLength(); n++) {
                                    if (childNodesCondition.item(n).getNodeName() == "input") {
                                        //TODO add code here
                                    }
                                    if (childNodesCondition.item(n).getNodeName() == "behaviorBlock") {
                                        makeAttachBehaviorBlock((BreedBlock) bBlock, conditionBlock, childNodesCondition.item(n));
                                        deltaTickTab.addCondition((ConditionBlock) o);
                                        //make behaviorBlock and attach to conditionBlock

                                    }
                                }
                            }
                        }
                    }
                }
                // Processing of breedchild nodes of this breedblock done
                // Set labels/traitLabels for that BreedBlock
                ((BreedBlock) bBlock).setTraitLabels(selectedLabels);

                // Udate species inspector panel
                SpeciesInspectorPanel speciesInspectorPanel = deltaTickTab.getSpeciesInspectorPanel(((BreedBlock) bBlock));
                speciesInspectorPanel.getTraitPreview().setSelectedTraitsMap(selectedTraitStateMap);
                speciesInspectorPanel.getTraitPreview().setSelectedTrait(traitName);
                speciesInspectorPanel.updateTraitDisplay();
                speciesInspectorPanel.getTraitPreview().updateCheckBoxes(selectedTraitStateMap);
                // Set the checkboxes in speciesInspectorPanel.labelPanel to true
                speciesInspectorPanel.getTraitPreview().getLabelPanel().setSelectedLabels(selectedLabels);
            }

            // Now process Plot Blocks
            NodeList plotBlockNodeList = model.getElementsByTagName("plotBlock");
            for (int i = 0; i < plotBlockNodeList.getLength(); i++) {
                Node plotBlockNode = plotBlockNodeList.item(i);
                String plotName = plotBlockNode.getAttributes().getNamedItem("name").getTextContent();
                boolean isHisto = plotBlockNode.getAttributes().getNamedItem("isHisto").getTextContent().equalsIgnoreCase("true");

                // Make the plot block
                PlotBlock plotBlock = deltaTickTab.makePlotBlock(isHisto);
                plotBlock.setPlotName(plotName);

                // Iterate over Quantity Blocks
                NodeList plotChildNodes = plotBlockNode.getChildNodes();
                for (int j = 0; j < plotChildNodes.getLength(); j++) {
                    if (plotChildNodes.item(j).getNodeName() == "quantityBlock") {
                        Node quantityBlockNode = plotChildNodes.item(j);
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
                            if (quantityBlockChildNodes.item(k).getNodeName() == "input") {
                                Node inputNode = quantityBlockChildNodes.item(k);
                                // Read the input name and value from XML
                                String inputName = inputNode.getAttributes().getNamedItem("name").getTextContent();
                                String inputValue = inputNode.getAttributes().getNamedItem("default").getTextContent();

                                // Set the corresponding value in the quantity block
                                quantityBlock.getInput(inputName).setText(inputValue);
                            }

                        }

                    }
                }
            }
            //// Plot block processing complete

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
                        variationElement.setAttribute("value", variation.value);
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
                        Element conditionBlock = doc.createElement("conditionBlock");
                        breedBlockElement.appendChild(conditionBlock);
                        conditionBlock.setAttribute("name", codeBlock.getName());
                        //mutate attribute

                        // Process behavior block (child of condition block)
                        for (CodeBlock cBlock : ((ConditionBlock) codeBlock).getMyBlocks()) {
                            Element behaviorBlock = makeElementFromBehaviorBlock(doc, (BehaviorBlock) cBlock);
                            conditionBlock.appendChild(behaviorBlock);
                        }
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
                // Append to root element (<model>)
                rootElement.appendChild(plotBlockElement);

                // Iterate over quantity blocks
                for (QuantityBlock quantityBlock : plotBlock.getMyBlocks()) {
                    // Create element
                    Element quantityBlockElement = doc.createElement("quantityBlock");
                    // Set Attributes
                    quantityBlockElement.setAttribute("name", quantityBlock.getName());
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
            behaviorBlock.setAttribute("traitName", traitName);
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
