package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 9/20/13
 * Time: 4:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class DiveInDropTarget
        extends DropTarget {

    DiveInBlock diveInBlock;

    public DiveInDropTarget(DiveInBlock dBlock) {
        super(dBlock);
        this.diveInBlock = dBlock;
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (o instanceof BehaviorBlock) {
                if (diveInBlock.getMyBlocks().size() == 1) {
                    String message = "Oops! You can perform only one action on clicking!";
                    JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    addCodeBlock((BehaviorBlock) o);
                    return true;
                }
            }
        }
        return false;
    }


}
