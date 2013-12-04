package org.nlogo.deltatick.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import scala.actors.threadpool.Arrays;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 9/20/13
 * Time: 9:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class DiveIn {
    String reporterCode;
    String setupCode;
    String applicableBehaviorBlocks;

    public DiveIn (Node diveInNode) {
        applicableBehaviorBlocks = diveInNode.getAttributes().getNamedItem("validBlocks").getTextContent();
        NodeList codeNodes = diveInNode.getChildNodes();

        for (int i = 0; i < codeNodes.getLength(); i++) {
            if (codeNodes.item(i).getNodeName().equalsIgnoreCase("setupCode")) {
                setupCode = codeNodes.item(i).getTextContent();
            }
            if (codeNodes.item(i).getNodeName().equalsIgnoreCase("reporterCode")) {
                reporterCode = codeNodes.item(i).getTextContent();
            }
        }
    }

    public String getName() {
        return "When clicked?";
    }

    public String getReporterCode() {
        return reporterCode;
    }

    public String getSetupCode() {
        return setupCode;
    }

    public String getApplicableBehaviorBlocks() {
        return applicableBehaviorBlocks;
    }
}
