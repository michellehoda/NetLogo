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
                codeBlock.enableInputs();

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

            NodeList breedBlocks = model.getElementsByTagName("breedBlock");
            for (int i = 0; i < breedBlocks.getLength(); i++) {
                Node breedBlock = breedBlocks.item(i);
                String plural = breedBlock.getAttributes().getNamedItem("plural").getTextContent();
                String number = breedBlock.getAttributes().getNamedItem("number").getTextContent();
                String maxAge = new String();
                String maxEnergy = new String();
                //BreedBlock bBlock = deltaTickTab.makeBreedBlock(plural, number);
                //CodeBlock bBlock = (BreedBlock) deltaTickTab.makeBreedBlock(plural, number);
                CodeBlock bBlock = deltaTickTab.makeBreedBlock(plural, number);

                NodeList breedBlockChildNodes = breedBlock.getChildNodes();
                //Ownvar childnodes for age & energy
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

                //TraitChildNodes for trait of breeds
                    if (breedBlockChildNodes.item(j).getNodeName() == "trait") {
                        Node trait = breedBlockChildNodes.item(j);
                        String traitName = new String(trait.getAttributes().getNamedItem("name").getTextContent()); //traitname
                        NodeList variationNodes = trait.getChildNodes();
                        HashMap<String, String> selectedVariationsPercent = new HashMap<String, String>();
                        HashMap<String, Variation> selectedVariationsHashMap = new HashMap<String, Variation>();
                        HashMap<String, TraitState> selectedTraitStateMap = new HashMap<String, TraitState>();

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
                        // Udate species inspector panel
                        SpeciesInspectorPanel speciesInspectorPanel = deltaTickTab.getSpeciesInspectorPanel(((BreedBlock) bBlock));
                        speciesInspectorPanel.getTraitPreview().setSelectedTraitsMap(selectedTraitStateMap);
                        speciesInspectorPanel.getTraitPreview().setSelectedTrait(traitName);
                        speciesInspectorPanel.updateTraitDisplay();

                        // Create the traitblock
                        for (TraitState traitState : selectedTraitStateMap.values()) {
                            deltaTickTab.makeTraitBlock(((BreedBlock) bBlock), traitState);
                        }
                    }

                    //behaviorBlocks as childNodes for a breedBlock
                    if (breedBlockChildNodes.item(j).getNodeName() == "behaviorBlock") {
                        makeAttachBehaviorBlock((BreedBlock) bBlock, bBlock, breedBlockChildNodes.item(j));
                    }

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
            }
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
                Element breedBlock = doc.createElement("breedBlock");
                rootElement.appendChild(breedBlock);
                breedBlock.setAttribute("singular", bBlock.singular());
                breedBlock.setAttribute("plural", bBlock.plural());
                breedBlock.setAttribute("number", bBlock.getNumber());

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
                    breedBlock.appendChild(traitElement);
                }

                for (CodeBlock codeBlock : bBlock.getMyBlocks()) {
                    // Process behavior block (child of breed block)
                    if (codeBlock instanceof BehaviorBlock) {
                        Element behaviorBlock = makeElementFromBehaviorBlock(doc, (BehaviorBlock) codeBlock);
                        breedBlock.appendChild(behaviorBlock);
                    }

                    if (codeBlock instanceof ConditionBlock) {
                        Element conditionBlock = doc.createElement("conditionBlock");
                        breedBlock.appendChild(conditionBlock);
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




            //// Convert DOM to XML ////
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
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
        String isMutate = (((BehaviorBlock) block).getIsMutate())? "true" : "false";
        behaviorBlock.setAttribute("mutate", isMutate);
        // isTrait
        String isTrait = (((BehaviorBlock) block).getIsTrait()) ? "true" : "false";
        behaviorBlock.setAttribute("isTrait", isTrait);
        if (((BehaviorBlock) block).getIsTrait()) {
            String traitName = ((BehaviorBlock) block).getTrait();
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
