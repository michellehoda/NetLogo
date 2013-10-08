package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.CodeBlock;
import org.nlogo.deltatick.MonitorBlock;
import org.nlogo.deltatick.PlotBlock;
import org.nlogo.deltatick.QuantityBlock;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 9/25/13
 * Time: 1:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class MonitorDropTarget
        extends DropTarget {

    public MonitorDropTarget(MonitorBlock block) {
        super(block);
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.quantityBlockFlavor);

        if (o instanceof QuantityBlock) {
            ((MonitorBlock) block).removeQuantityblockPanel();
                addCodeBlock((QuantityBlock) o);
            if (((QuantityBlock) o).getNeedsTrait() == true) {
                ((QuantityBlock) o).addTraitblockPanel();
                ((QuantityBlock) o).removeInput();
            }
                new QuantityDropTarget((QuantityBlock) o);
                ((QuantityBlock) o).validate();
                ((QuantityBlock) o).repaint();
                block.validate();
                return true;
        }
        //don't need to distinguish whether histoblock or plotblock here -Aditi (Sept, 25, 2013)
        /*if (o instanceof QuantityBlock) {
            if (((QuantityBlock) o).getHisto() == false) {
                ((PlotBlock) block).removeQuantityblockPanel();
                addCodeBlock((QuantityBlock) o);
                ((QuantityBlock) o).validate();
                ((QuantityBlock) o).repaint();
                block.validate();
                return true;
            }
            else {
                //String message = new String(((QuantityBlock) o).getName() + " is a block for histograms, not line graphs.");
                String message = new String("Oops! You dragged a block for bar graphs. You can only add line graphs here.");
                JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }*/
        else {
            String message = new String("Oops! You can only add orange blocks here!");
            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

    }
}
