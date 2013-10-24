package org.nlogo.deltatick.xml;

import org.parboiled.support.Var;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/20/12
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */

public class Trait {
    String traitName = new String();
    String visualizeCode = new String();
    String visualizeProcedure = new String();
    String setupReporter = new String();
    String color = new String();
    HashMap<String, Variation> variationHashMap = new HashMap<String, Variation>();
    String message = new String();
    String mutateCode = new String();


    public Trait() {

    }

    // Copy Constructor
    // Copy all members
    public Trait(Trait trait) {
        traitName = new String(trait.traitName);
        visualizeCode = new String(trait.visualizeCode);
        visualizeProcedure = new String(trait.visualizeProcedure);
        color = new String(trait.color);
        setupReporter = new String(trait.setupReporter);
        message = new String(trait.message);
        variationHashMap.clear();
        for (Map.Entry <String, Variation> entry : trait.variationHashMap.entrySet()) {
            this.variationHashMap.put(entry.getKey(), new Variation(entry.getValue()));
        }
        // variationHashMap = new HashMap<String, Variation>(trait.variationHashMap);
        mutateCode = new String(trait.mutateCode);
    }

    public Trait(Node traitNode) {
        traitName = traitNode.getAttributes().getNamedItem("name").getTextContent();

        NodeList traitNodes = traitNode.getChildNodes();
        for (int i = 0; i < traitNodes.getLength(); i++) {
            if (traitNodes.item(i).getNodeName() == "setupReporter") {
                setupReporter = traitNodes.item(i).getTextContent();
            }
            if (traitNodes.item(i).getNodeName() == "variation") {
                Node variationNode = traitNodes.item(i);
                String name = variationNode.getAttributes().getNamedItem("name").getTextContent();
                String value = variationNode.getAttributes().getNamedItem("value").getTextContent();
                String color = variationNode.getAttributes().getNamedItem("color").getTextContent();
                String setupNumber = variationNode.getAttributes().getNamedItem("setupNumber").getTextContent();
                Variation variation = new Variation(traitName, name, value, color, Integer.parseInt(setupNumber));
                variationHashMap.put(name, variation);
            }
            if (traitNodes.item(i).getNodeName() == "visualizeCode") {
                visualizeCode = traitNodes.item(i).getTextContent();
            }
            if (traitNodes.item(i).getNodeName() == "visualizeProcedure") {
                visualizeProcedure = traitNodes.item(i).getTextContent();
            }
            if (traitNodes.item(i).getNodeName() == "message") {
                message = traitNodes.item(i).getTextContent();
            }
            /*if (traitNodes.item(i).getNodeName() == "mutateCode") {
                mutateCode = traitNodes.item(i).getTextContent();
            }*/
        }
    }

    public String getNameTrait() {
        return traitName;
    }

    public String getVisualizeCode() {
        return visualizeCode;
    }

    public String getVisualizeProcedure() {
        return visualizeProcedure;
    }

    public String getColor() {
        return color;
    }

    public String getSetupReporter() {
        return setupReporter;
    }

    public HashMap<String, Variation> getVariationHashMap() {
        return variationHashMap;
    }

    public HashMap<String, String> getVariationsValuesList() {
        HashMap<String, String> variationsValuesList = new HashMap<String, String>();
        for (Variation variation : variationHashMap.values()) {
            variationsValuesList.put(variation.name, variation.value);
        }
        return variationsValuesList;
    }

    public HashMap<String, String> getVariationsNumbersList() {
        HashMap<String, String> variationsPercentList = new HashMap<String, String>();
        for (Variation variation : variationHashMap.values()) {
            String percent = Integer.toString(variation.percent);
            variationsPercentList.put(variation.name, percent);
        }
        return variationsPercentList;
    }

    public HashMap<String, String> getValuesPercentList() {
        HashMap<String, String> valuesPercentList = new HashMap<String, String>();
        for (Variation variation : variationHashMap.values()) {
            String percent = Integer.toString(variation.percent);
            valuesPercentList.put(variation.value, percent);
        }
        return valuesPercentList;
    }

    public ArrayList<String> getVariationsList() {
        ArrayList<String> variationsList = new ArrayList<String>(variationHashMap.keySet());
        return variationsList;
    }

    public String getMessage() {
        return message;
    }

    public String getMutateCode() {
        return mutateCode;
    }
}
